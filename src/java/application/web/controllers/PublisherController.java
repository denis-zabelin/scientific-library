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
import application.web.beans.Publisher;
import application.web.db.Database;
import java.sql.PreparedStatement;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.SessionScoped;

@ManagedBean(eager = true)
@ApplicationScoped
public class PublisherController implements Serializable {

    private static ArrayList<Publisher> publisherList;
    private Publisher publisher;
    //------- режимы редактирования, добавления
    private boolean editModeView;
    private boolean addingModeView;

    public PublisherController() {
        fillPublisherList();
        publisher = new Publisher();
    }

    private void fillPublisherList() {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        publisherList = new ArrayList<Publisher>();

        try {
            conn = Database.getConnection();

            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from library.publisher order by name");
            while (rs.next()) {
                Publisher p = new Publisher();
                p.setName(rs.getString("name"));
                p.setId(rs.getLong("id"));
                publisherList.add(p);
            }

        } catch (SQLException ex) {
            Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String addPublisher() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("INSERT INTO library.publisher set name=?");
            prepStmt.setString(1, publisher.getName());
            prepStmt.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        publisher = new Publisher();
        fillPublisherList();
        cancelAddingMode();
        return "books";
    }

    public String updatePublisher() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("update library.publisher set name=? where id=?");
            prepStmt.setString(1, publisher.getName());
            prepStmt.setLong(2, publisher.getId());
            prepStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fillPublisherList();
        publisher = new Publisher();
        cancelEditMode();
        return "books";
    }

    public String deletePublisher() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("delete from library.publisher where id=?");
            prepStmt.setLong(1, publisher.getId());
            prepStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fillPublisherList();
        publisher = new Publisher();
        cancelEditMode();
        return "books";
    }

    //<editor-fold defaultstate="collapsed" desc="режим редактирования">
    public void showEdit() {
        editModeView = true;
    }

    public void cancelEditMode() {
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

    public ArrayList<Publisher> getPublisherList() {
        return publisherList;
    }

    public static Publisher getPublisherById(long id) {
        for (Publisher p : publisherList) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
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
            Logger.getLogger(PublisherController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
