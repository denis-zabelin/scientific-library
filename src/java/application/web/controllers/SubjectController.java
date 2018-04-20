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
import application.web.beans.Subject;
import application.web.db.Database;
import java.sql.PreparedStatement;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.SessionScoped;

@ManagedBean(eager = true)
@ApplicationScoped
public class SubjectController implements Serializable {

    private static ArrayList<Subject> subjectList;
    private Subject subject;
    //------- режимы редактирования, добавления
    private boolean editModeView;
    private boolean addingModeView;

    public SubjectController() {
        fillSubjectList();
        subject = new Subject();
    }

    private void fillSubjectList() {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        subjectList = new ArrayList<Subject>();

        try {
            conn = Database.getConnection();

            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from library.subject order by name");
            while (rs.next()) {
                Subject s = new Subject();
                s.setName(rs.getString("name"));
                s.setId(rs.getLong("id"));
                subjectList.add(s);
            }

        } catch (SQLException ex) {
            Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String addSubject() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("INSERT INTO library.subject set name=?");
            prepStmt.setString(1, subject.getName());
            prepStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        subject = new Subject();
        fillSubjectList();
        cancelAddingMode();
        return "books";
    }

    public String updateSubject() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("update library.subject set name=? where id=?");
            prepStmt.setString(1, subject.getName());
            prepStmt.setLong(2, subject.getId());
            prepStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fillSubjectList();
        subject = new Subject();
        cancelEditMode();
        return "books";
    }

    public String deleteSubject() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("delete from library.subject where id=?");
            prepStmt.setLong(1, subject.getId());
            prepStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        fillSubjectList();
        subject = new Subject();
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

    public ArrayList<Subject> getSubjectList() {
        return subjectList;
    }

    public static Subject getSubjectById(long id) {
        for (Subject s : subjectList) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
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
            Logger.getLogger(SubjectController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
