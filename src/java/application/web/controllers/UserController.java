package application.web.controllers;

import application.web.beans.User;
import application.web.db.Database;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(eager = true)
@SessionScoped
public class UserController implements Serializable {

    private ArrayList<User> userList;
    private ArrayList<String> groupList;

    private User user;
    private String group;
    //------- режимы редактирования, добавления
    private boolean editModeView;
    private boolean addingModeView;

    public UserController() {
        fillLists();
        user = new User();
        group = "";
    }

    private void fillLists() {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        userList = new ArrayList<User>();
        groupList = new ArrayList<String>();

        try {
            conn = Database.getConnection();

            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from library.users order by username");
            while (rs.next()) {
                if (rs.getString("username").equals("admin"))
                    continue;
                User u = new User();
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                userList.add(u);
            }

            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from library.groups order by groupid desc");
            while (rs.next()) {
                groupList.add(rs.getString("groupid"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
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

    public String createUser() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        if (user.getUsername().equals("")) {
            return "books";
        }

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("INSERT INTO library.users set username=?, password=?");
            prepStmt.setString(1, user.getUsername());
            prepStmt.setString(2, sha256(user.getPassword()));
            prepStmt.executeUpdate();

            prepStmt = conn.prepareStatement("INSERT INTO library.users_groups set userid=?, groupid=?");
            prepStmt.setString(1, user.getUsername());
            prepStmt.setString(2, group);
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
        userList.add(user);
        user = new User();
        group = "";
        cancelAddingMode();
        return "books";
    }

    public String changeUser() {
        imitateLoading();

        PreparedStatement prepStmt = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            prepStmt = conn.prepareStatement("update library.users set password=? where username=?");
            prepStmt.setString(1, sha256(user.getPassword()));
            prepStmt.setString(2, user.getUsername());
            prepStmt.executeUpdate();
            prepStmt.close();
            prepStmt = conn.prepareStatement("update library.users_groups set groupid=? where userid=?");
            prepStmt.setString(1, group);
            prepStmt.setString(2, user.getUsername());
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
        fillLists();
        group = "";
        user = new User();
        cancelEditMode();
        return "books";
    }

    private static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
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

    public ArrayList<User> getUserList() {
        return userList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<String> getGroupList() {
        return groupList;
    }

    public void setGroupList(ArrayList<String> groupList) {
        this.groupList = groupList;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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
