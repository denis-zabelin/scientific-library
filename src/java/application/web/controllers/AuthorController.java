package application.web.controllers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import application.web.beans.Author;
import application.web.db.Database;
import java.sql.PreparedStatement;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.SessionScoped;

@ManagedBean(eager = true)
@ApplicationScoped
public class AuthorController implements Serializable {

    private static ArrayList<Author> authorList;
    private Author author;
    //------- режимы редактирования, добавления
    private boolean editModeView;
    private boolean addingModeView;

    public AuthorController() {
        fillAuthorList();
        author = new Author();
    }

    private void fillAuthorList() {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        authorList = new ArrayList<Author>();

        try {
            conn = Database.getConnection();

            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from library.author order by fio");
            while (rs.next()) {
                Author a = new Author();
                a.setFio(rs.getString("fio"));
                a.setId(rs.getLong("id"));
                authorList.add(a);
            }

        } catch (SQLException ex) {
            Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String addAuthor() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("INSERT INTO library.author set fio=?");
            prepStmt.setString(1, author.getFio());
            prepStmt.execute();

        } catch (SQLException ex) {
            Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fillAuthorList();
        author = new Author();
        cancelAddingMode();
        return "books";
    }

    public String updateAuthor() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("update library.author set fio=? where id=?");
            prepStmt.setString(1, author.getFio());
            prepStmt.setLong(2, author.getId());
            prepStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fillAuthorList();
        author = new Author();
        cancelEditMode();
        return "books";
    }

    public String deleteAuthor() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("delete from library.author where id=?");
            prepStmt.setLong(1, author.getId());
            prepStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fillAuthorList();
        author = new Author();
        cancelEditMode();
        return "books";
    }

    //<editor-fold defaultstate="collapsed" desc="режим редактирования">
    public void showEdit() {
        editModeView = true;
    }

    public void cancelEditMode() {
        author = new Author();
        editModeView = false;
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

    public ArrayList<Author> getAuthorList() {
        return authorList;
    }

    public static Author getAuthorById(long id) {
        for (Author a : authorList) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public boolean isAddingMode() {
        return addingModeView;
    }

    public boolean isEditMode() {
        return editModeView;
    }

    private void imitateLoading() {
        try {
            Thread.sleep(700);// имитация загрузки процесса
        } catch (InterruptedException ex) {
            Logger.getLogger(AuthorController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
