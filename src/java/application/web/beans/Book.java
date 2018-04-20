    package application.web.beans;

import application.web.db.Database;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.Part;
import net.playerfinder.jsf.components.rating.UIRating;

public class Book implements Serializable {

    private boolean edit;
    private long id;
    private String name;
    private byte[] content;
    private int pageCount;
    private String extension;
    private Subject subject;
    private Author author;
    private int publishingYear;
    private Publisher publisher;
    private byte[] image;

    // вспомогательные переменные
    private long subjectId;
    private long authorId;
    private long publisherId;

    //рейтинг
    private Double rating = new Double(0);
    private Double currentRate = new Double(0);
    private Double summaryRate = new Double(0);
    private Integer totalRatesAmount = 0;

    private String username;

    // переменные для загрузки blob
    private Part imagePart;
    private Part contentPart;

    public void createRating() {
        Statement stmt = null;
        PreparedStatement prepStmt;
        ResultSet rs = null;
        Connection conn = null;

        username = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName();

        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select rate_value, users_username from library.rate where book_id=" + id);

            while (rs.next()) {
                totalRatesAmount++;
                summaryRate += rs.getDouble("rate_value");
                String user = rs.getString("users_username");
                if (username.equals(user)) {
                    currentRate = rs.getDouble("rate_value");
                }
            }
            if (totalRatesAmount != 0.0) {
                rating = summaryRate / totalRatesAmount;
            }
            prepStmt = conn.prepareStatement("update library.book set rating=?, rates_amount=? where id=?");
            prepStmt.setDouble(1, rating);
            prepStmt.setInt(2, totalRatesAmount);
            prepStmt.setLong(3, id);
            prepStmt.addBatch();
            prepStmt.executeBatch();
        } catch (SQLException ex) {
            Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void rate(AjaxBehaviorEvent actionEvent) {
        PreparedStatement prepStmt = null;
        Connection conn = null;

        Double rate = (Double) ((UIRating) actionEvent.getComponent()).getValue();
        currentRate = rate;
        totalRatesAmount++;
        rating = (summaryRate + currentRate) / (totalRatesAmount);

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("INSERT INTO library.rate set rate_value=?, book_id=?, users_username=?");
            prepStmt.setDouble(1, currentRate);
            prepStmt.setLong(2, id);
            prepStmt.setString(3, username);
            prepStmt.addBatch();
            prepStmt.executeBatch();

            prepStmt = conn.prepareStatement("UPDATE library.book set rating=?, rates_amount=? where id=?");
            prepStmt.setDouble(1, rating);
            prepStmt.setInt(2, totalRatesAmount);
            prepStmt.setLong(3, id);
            prepStmt.addBatch();
            prepStmt.executeBatch();
        } catch (SQLException ex) {
            Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="гетеры+сетеры">
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public int getPublishingYear() {
        return publishingYear;
    }

    public void setPublishingYear(int publishingYear) {
        this.publishingYear = publishingYear;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(long subjectId) {
        this.subjectId = subjectId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(long publisherId) {
        this.publisherId = publisherId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getCurrentRate() {
        return currentRate;
    }

    public void setCurrentRate(Double currentRate) {
        this.currentRate = currentRate;
    }

    public Part getImagePart() {
        return imagePart;
    }

    public void setImagePart(Part imagePart) {
        this.imagePart = imagePart;
    }

    public Part getContentPart() {
        return contentPart;
    }

    public void setContentPart(Part contentPart) {
        this.contentPart = contentPart;
    }

    public Integer getTotalRatesAmount() {
        return totalRatesAmount;
    }

    public void setTotalRatesAmount(Integer totalRatesAmount) {
        this.totalRatesAmount = totalRatesAmount;
    }
    //</editor-fold>
}
