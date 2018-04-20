/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application.web.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import application.web.controllers.BookListController;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "GetContent",
urlPatterns = {"/GetContent"})
public class GetContent extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/pdf; charset=UTF-8");
        OutputStream out = response.getOutputStream();
        try {
            int id = Integer.valueOf(request.getParameter("id"));
            String extension = request.getParameter("extension");
            boolean saveItem = Boolean.parseBoolean(request.getParameter("saveItem"));
            
            BookListController searchController = (BookListController) request.getSession(false).getAttribute("bookListController");

            byte[] content = searchController.getContent(id);
            response.setContentLength(content.length);
            if (!extension.equals("pdf") || saveItem) {
                String filename = request.getParameter("filename");
                String author = request.getParameter("author");
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(author, "UTF-8").replaceAll("\\+", "%20")
                        + " - " + URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20") + "." + extension);
            }
            out.write(content);
        } catch (NumberFormatException ex) {
            Logger.getLogger(GetContent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GetContent.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
