package application.web.controllers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import application.web.beans.Book;
import application.web.db.Database;
import application.web.enums.OrderType;
import application.web.enums.SearchType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@ManagedBean(eager = true)
@SessionScoped
public class BookListController implements Serializable {

    private ArrayList<Book> currentBookList; // текущий список книг для отображения
    private final ArrayList<Book> oneBook = new ArrayList<Book>(); // структура для добавления книги
    private ArrayList<Integer> pageNumbers = new ArrayList<Integer>(); // кол-во страниц для постраничности
    // критерии поиска
    private SearchType selectedSearchType = SearchType.BOOK_NAME; // хранит выбранный тип поиска, по умолчанию - по названию
    private OrderType selectedOrderType = OrderType.NAME; // хранит выбранный порядок сортировки, по умолчанию - по названию
    private int selectedSubjectId; // выбранный предмет
    private String currentSearchString; // хранит поисковую строку
    private StringBuilder currentSqlNoLimit;// последний выполненный sql без добавления limit
    // для постраничности----
    private boolean pageSelected;
    private int booksCountOnPage = 5;// кол-во отображаемых книг на 1 странице
    private long selectedPageNumber = 1; // выбранный номер страницы в постраничной навигации
    private long totalBooksCount; // общее кол-во книг
    //------- режимы редактирования, добавления
    private boolean editModeView;
    private boolean addingModeView;

    public BookListController() {
        fillBooksAll();
    }

    private void submitValues(long selectedPageNumber, int selectedSubjectId, boolean requestFromPager) {
        this.selectedPageNumber = selectedPageNumber;
        this.selectedSubjectId = selectedSubjectId;
        this.pageSelected = requestFromPager;

    }

    //<editor-fold defaultstate="collapsed" desc="Запросы (select)">
    private void fillBooksBySQL(StringBuilder sql) {

        currentSqlNoLimit = new StringBuilder(sql);

        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();

            if (!pageSelected) {
                rs = stmt.executeQuery(sql.toString());
                rs.last();
                totalBooksCount = rs.getRow();
                fillPageNumbers(totalBooksCount, booksCountOnPage);
            }

            if (totalBooksCount > booksCountOnPage) {
                sql.append(" limit ").append((selectedPageNumber - 1) * booksCountOnPage).append(",").append(booksCountOnPage);
            }

            rs = stmt.executeQuery(sql.toString());

            currentBookList = new ArrayList<Book>();

            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getLong("id"));
                book.setName(rs.getString("name"));
                book.setSubject(SubjectController.getSubjectById(rs.getLong("subject_id")));
                book.setExtension(rs.getString("extension"));
                book.setAuthor(AuthorController.getAuthorById(rs.getLong("author_id")));
                book.setPageCount(rs.getInt("page_count"));
                book.setPublishingYear(rs.getInt("publishing_year"));
                book.setPublisher(PublisherController.getPublisherById(rs.getLong("publisher_id")));
                book.setSubjectId(book.getSubject().getId());
                book.setAuthorId(book.getAuthor().getId());
                book.setPublisherId(book.getPublisher().getId());
                book.createRating();
                currentBookList.add(book);
            }

        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void fillBooksAll() {
        StringBuilder sql = new StringBuilder("select id, name, page_count, extension, subject_id, author_id, publishing_year, publisher_id"
                + " from library.book ");
        appendOrderType(sql);
        fillBooksBySQL(sql);
    }

    public String fillBooksBySubject() {
        imitateLoading();
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedSubjectId = Integer.valueOf(params.get("subject_id"));
        submitValues(1, selectedSubjectId, false);
        StringBuilder sql = new StringBuilder("select id, name, page_count, extension, subject_id, author_id, publishing_year, publisher_id from library.book where subject_id=");
        sql.append(selectedSubjectId).append(" ");
        appendOrderType(sql);
        fillBooksBySQL(sql);
        return "books";
    }

    public String fillBooksBySearch() {
        imitateLoading();
        submitValues(1, -1, false);
        if (currentSearchString.trim().length() == 0) {
            fillBooksAll();
            return "books";
        }
        StringBuilder sql = new StringBuilder("select book.id, book.name, page_count, extension, subject_id, author_id, publishing_year, publisher_id"
                + " from library.book, library.author, library.publisher where author_id=author.id and publisher_id=publisher.id and ");

        switch (selectedSearchType) {
            case AUTHOR:
                sql.append("lower(author.fio) like '%").append(currentSearchString.toLowerCase()).append("%' ");
                break;
            case BOOK_NAME:
                sql.append("lower(book.name) like '%").append(currentSearchString.toLowerCase()).append("%' ");
                break;
            case PUBLISHER:
                sql.append("lower(publisher.name) like '%").append(currentSearchString.toLowerCase()).append("%' ");
                break;
            case PUBLISHING_YEAR:
                sql.append("lower(publishing_year) like '%").append(currentSearchString.toLowerCase()).append("%' ");
                break;
            case EXTENSION:
                sql.append("lower(extension) like '%").append(currentSearchString.toLowerCase()).append("%' ");
        }
        appendOrderType(sql);
        fillBooksBySQL(sql);
        return "books";
    }

    private void appendOrderType(StringBuilder sql) {
        switch (selectedOrderType) {
            case NAME:
                sql.append("order by name");
                break;
            case RATING:
                sql.append("order by rating desc");
        }
    }

    public byte[] getContent(int id) {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        byte[] content = null;
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery("select content from library.book where id=" + id);
            while (rs.next()) {
                content = rs.getBytes("content");
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return content;
    }

    public byte[] getImage(int id) {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        byte[] image = null;

        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery("select image from library.book where id=" + id);
            while (rs.next()) {
                image = rs.getBytes("image");
            }

            if (image == null) {
                rs = stmt.executeQuery("select image from library.default where id=1");
                while (rs.next()) {
                    image = rs.getBytes("image");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return image;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="запросы (update, insert, delete)">
    public String updateBooks() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            for (Book book : currentBookList) {
                if (!book.isEdit()) {
                    continue;
                }
                InputStream imageInputStream = null;
                InputStream contentInputStream = null;
                StringBuilder sqlStringBuilder = new StringBuilder("update library.book set name=?, page_count=?, extension=?, subject_id=?, author_id=?, publishing_year=?, publisher_id=?");
                if (book.getSubjectId() != book.getSubject().getId()) {
                    book.setSubject(SubjectController.getSubjectById(book.getSubjectId()));
                }
                if (book.getAuthorId() != book.getAuthor().getId()) {
                    book.setAuthor(AuthorController.getAuthorById(book.getAuthorId()));
                }
                if (book.getPublisherId() != book.getPublisher().getId()) {
                    book.setPublisher(PublisherController.getPublisherById(book.getPublisherId()));
                }
                if (book.getImagePart() != null) {
                    imageInputStream = book.getImagePart().getInputStream();
                    if (imageInputStream != null) {
                        sqlStringBuilder.append(", image=?");
                    }
                }
                if (book.getContentPart() != null) {
                    contentInputStream = book.getContentPart().getInputStream();
                    if (contentInputStream != null) {
                        sqlStringBuilder.append(", content=?");
                    }
                }
                sqlStringBuilder.append(" where id=?");
                prepStmt = conn.prepareStatement(sqlStringBuilder.toString());
                int i = 0;
                prepStmt.setString(++i, book.getName());
                prepStmt.setInt(++i, book.getPageCount());
                prepStmt.setString(++i, book.getExtension());
                prepStmt.setLong(++i, book.getSubjectId());
                prepStmt.setLong(++i, book.getAuthorId());
                prepStmt.setInt(++i, book.getPublishingYear());
                prepStmt.setLong(++i, book.getPublisherId());
                if (imageInputStream != null) {
                    prepStmt.setBinaryStream(++i, imageInputStream, (int) book.getImagePart().getSize());
                }
                if (contentInputStream != null) {
                    prepStmt.setBinaryStream(++i, contentInputStream, (int) book.getContentPart().getSize());
                }
                prepStmt.setLong(++i, book.getId());
                prepStmt.addBatch();
            }
            if (prepStmt != null) {
                prepStmt.executeBatch();
            }

        } catch (IOException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        cancelEditMode();
        fillBooksBySQL(currentSqlNoLimit);
        return "books";
    }

    public String addBook() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            StringBuilder sqlStringBuilder = new StringBuilder("INSERT INTO library.book set name=?, page_count=?, extension=?, subject_id=?, author_id=?, publishing_year=?, publisher_id=?");
            Book book = oneBook.get(0);

            book.setAuthor(AuthorController.getAuthorById(book.getAuthorId()));
            book.setSubject(SubjectController.getSubjectById(book.getSubjectId()));
            book.setPublisher(PublisherController.getPublisherById(book.getPublisherId()));
            InputStream imageInputStream = null;
            InputStream contentInputStream = null;
            if (book.getImagePart() != null) {
                imageInputStream = book.getImagePart().getInputStream();
                if (imageInputStream != null) {
                    sqlStringBuilder.append(", image=?");
                }
            }
            if (book.getContentPart() != null) {
                contentInputStream = book.getContentPart().getInputStream();
                if (contentInputStream != null) {
                    sqlStringBuilder.append(", content=?");
                }
            }
            prepStmt = conn.prepareStatement(sqlStringBuilder.toString());
            int i = 0;
            prepStmt.setString(++i, book.getName());
            prepStmt.setInt(++i, book.getPageCount());
            prepStmt.setString(++i, book.getExtension());
            prepStmt.setLong(++i, book.getSubjectId());
            prepStmt.setLong(++i, book.getAuthorId());
            prepStmt.setInt(++i, book.getPublishingYear());
            prepStmt.setLong(++i, book.getPublisherId());
            if (imageInputStream != null) {
                prepStmt.setBinaryStream(++i, imageInputStream, (int) book.getImagePart().getSize());
            }
            if (contentInputStream != null) {
                prepStmt.setBinaryStream(++i, contentInputStream, (int) book.getContentPart().getSize());
            }
            prepStmt.executeUpdate();

        } catch (IOException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (currentBookList.size() == booksCountOnPage) {
            currentBookList.remove(booksCountOnPage - 1);
        }
        currentBookList.add(0, oneBook.get(0));
        oneBook.clear();
        totalBooksCount++;
        cancelAddingMode();
        fillBooksBySQL(currentSqlNoLimit);
        return "books";
    }

    public String deleteBooks() {
        imitateLoading();
        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("DELETE FROM library.book where id=?;");

            Iterator<Book> i = currentBookList.iterator();
            int amountOfDeletedBooks = 0;
            while (i.hasNext()) {
                Book book = i.next();
                if (!book.isEdit()) {
                    continue;
                }
                prepStmt.setLong(1, book.getId());
                prepStmt.addBatch();
                i.remove();
                amountOfDeletedBooks++;
            }
            prepStmt.executeBatch();
            totalBooksCount -= amountOfDeletedBooks;
        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        cancelEditMode();
        fillBooksBySQL(currentSqlNoLimit);
        return "books";
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="режим редактирования">
    public void showEdit() {
        editModeView = true;
    }

    public void cancelEditMode() {
        editModeView = false;
        for (Book book : currentBookList) {
            book.setEdit(false);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="режим добавления">
    public void showAdding() {
        addingModeView = true;
    }

    public void cancelAddingMode() {
        addingModeView = false;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Listeners на изменение параметров запроса">
    public void searchStringChanged(ValueChangeEvent e) {
        currentSearchString = e.getNewValue().toString();
    }

    public void searchTypeChanged(ValueChangeEvent e) {
        selectedSearchType = (SearchType) e.getNewValue();
    }

    public void orderTypeChanged(ValueChangeEvent e) {
        selectedOrderType = (OrderType) e.getNewValue();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="постраничность">
    public void changeBooksCountOnPage(ValueChangeEvent e) {
        imitateLoading();
        cancelEditMode();
        pageSelected = false;
        booksCountOnPage = Integer.valueOf(e.getNewValue().toString());
        selectedPageNumber = 1;
        fillBooksBySQL(currentSqlNoLimit);
    }

    public void selectPage() {
        cancelEditMode();
        imitateLoading();
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedPageNumber = Integer.valueOf(params.get("page_number"));
        pageSelected = true;
        fillBooksBySQL(currentSqlNoLimit);
    }

    private void fillPageNumbers(long totalBooksCount, int booksCountOnPage) {

        int pageCount;

        if (totalBooksCount % booksCountOnPage == 0) {
            pageCount = booksCountOnPage > 0 ? (int) (totalBooksCount / booksCountOnPage) : 0;
        } else {
            pageCount = booksCountOnPage > 0 ? (int) (totalBooksCount / booksCountOnPage) + 1 : 0;
        }

        pageNumbers.clear();
        for (int i = 1; i <= pageCount; i++) {
            pageNumbers.add(i);
        }

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="гетеры+сетеры">
    public boolean isAddingMode() {
        return addingModeView;
    }

    public boolean isEditMode() {
        return editModeView;
    }

    public ArrayList<Integer> getPageNumbers() {
        return pageNumbers;
    }

    public void setPageNumbers(ArrayList<Integer> pageNumbers) {
        this.pageNumbers = pageNumbers;
    }

    public String getSearchString() {
        return currentSearchString;
    }

    public void setSearchString(String searchString) {
        this.currentSearchString = searchString;
    }

    public SearchType getSearchType() {
        return selectedSearchType;
    }

    public void setSearchType(SearchType searchType) {
        this.selectedSearchType = searchType;
    }

    public OrderType getOrderType() {
        return selectedOrderType;
    }

    public void setOrderType(OrderType OrderType) {
        this.selectedOrderType = OrderType;
    }

    public ArrayList<Book> getCurrentBookList() {
        return currentBookList;
    }

    public ArrayList<Book> getOneBook() {
        oneBook.clear();
        oneBook.add(new Book());
        return oneBook;
    }

    public void setTotalBooksCount(long booksCount) {
        this.totalBooksCount = booksCount;
    }

    public long getTotalBooksCount() {
        return totalBooksCount;
    }

    public int getSelectedSubjectId() {
        return selectedSubjectId;
    }

    public void setSelectedSubjectId(int selectedSubjectId) {
        this.selectedSubjectId = selectedSubjectId;
    }

    public int getBooksOnPage() {
        return booksCountOnPage;
    }

    public void setBooksOnPage(int booksOnPage) {
        this.booksCountOnPage = booksOnPage;
    }

    public void setSelectedPageNumber(long selectedPageNumber) {
        this.selectedPageNumber = selectedPageNumber;
    }

    public long getSelectedPageNumber() {
        return selectedPageNumber;
    }
    //</editor-fold>

    private void imitateLoading() {
        try {
            Thread.sleep(700);// имитация загрузки процесса
        } catch (InterruptedException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
