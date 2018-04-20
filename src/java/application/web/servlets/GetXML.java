/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package application.web.servlets;

import application.web.db.Database;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@WebServlet(name = "GetXML",
        urlPatterns = {"/GetXML"})
public class GetXML extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml; charset=UTF-8");
        OutputStream out = response.getOutputStream();

        Statement stmtRate = null;
        Statement stmtBook = null;
        Statement stmtBookChild = null;
        Connection conn = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            Element rates = doc.createElement("rates");
            doc.appendChild(rates);

            conn = Database.getConnection();
            stmtRate = conn.createStatement();
            ResultSet rsRate = stmtRate.executeQuery("select * from library.rate order by rate_time desc");

            while (rsRate.next()) {
                Element rate = doc.createElement("rate");
                rates.appendChild(rate);
                Attr rateAttr = doc.createAttribute("id");
                rateAttr.setValue(rsRate.getLong("id") + "");
                rate.setAttributeNode(rateAttr);

                Element value = doc.createElement("value");
                rate.appendChild(value);
                value.appendChild(doc.createTextNode(rsRate.getDouble("rate_value") + ""));

                Element time = doc.createElement("time");
                rate.appendChild(time);
                time.appendChild(doc.createTextNode(rsRate.getTimestamp("rate_time") + ""));

                Element username = doc.createElement("username");
                rate.appendChild(username);
                username.appendChild(doc.createTextNode(rsRate.getString("users_username")));

                stmtBook = conn.createStatement();
                ResultSet rsBook = stmtBook.executeQuery("select * from library.book where id=" + rsRate.getLong("book_id"));
                while (rsBook.next()) {
                    Element book = doc.createElement("book");
                    rate.appendChild(book);
                    Attr bookAttr = doc.createAttribute("id");
                    bookAttr.setValue(rsRate.getLong("book_id") + "");
                    book.setAttributeNode(bookAttr);

                    Element bookName = doc.createElement("name");
                    book.appendChild(bookName);
                    bookName.appendChild(doc.createTextNode(rsBook.getString("name")));

                    stmtBookChild = conn.createStatement();
                    ResultSet rsAuthor = stmtBookChild.executeQuery("select * from library.author where id=" + rsBook.getLong("author_id"));
                    while (rsAuthor.next()) {
                        Element author = doc.createElement("author");
                        book.appendChild(author);
                        Attr authorAttr = doc.createAttribute("id");
                        authorAttr.setValue(rsBook.getLong("author_id") + "");
                        author.setAttributeNode(authorAttr);

                        Element fio = doc.createElement("fio");
                        author.appendChild(fio);
                        fio.appendChild(doc.createTextNode(rsAuthor.getString("fio")));
                    }

                    ResultSet rsSubject = stmtBookChild.executeQuery("select * from library.subject where id=" + rsBook.getLong("subject_id"));
                    while (rsSubject.next()) {
                        Element subject = doc.createElement("subject");
                        book.appendChild(subject);
                        Attr subjectAttr = doc.createAttribute("id");
                        subjectAttr.setValue(rsBook.getLong("subject_id") + "");
                        subject.setAttributeNode(subjectAttr);

                        Element name = doc.createElement("name");
                        subject.appendChild(name);
                        name.appendChild(doc.createTextNode(rsSubject.getString("name")));
                    }

                    ResultSet rsPublisher = stmtBookChild.executeQuery("select * from library.publisher where id=" + rsBook.getLong("publisher_id"));
                    while (rsPublisher.next()) {
                        Element publisher = doc.createElement("publisher");
                        book.appendChild(publisher);
                        Attr publisherAttr = doc.createAttribute("id");
                        publisherAttr.setValue(rsBook.getLong("publisher_id") + "");
                        publisher.setAttributeNode(publisherAttr);

                        Element name = doc.createElement("name");
                        publisher.appendChild(name);
                        name.appendChild(doc.createTextNode(rsPublisher.getString("name")));
                    }

                    Element pageCount = doc.createElement("pages");
                    book.appendChild(pageCount);
                    pageCount.appendChild(doc.createTextNode(rsBook.getString("page_count")));

                    Element extension = doc.createElement("extension");
                    book.appendChild(extension);
                    extension.appendChild(doc.createTextNode(rsBook.getString("extension")));

                    Element year = doc.createElement("year");
                    book.appendChild(year);
                    year.appendChild(doc.createTextNode(rsBook.getString("publishing_year")));

                    Element rating = doc.createElement("rating");
                    book.appendChild(rating);
                    rating.appendChild(doc.createTextNode(rsBook.getString("rating")));

                    Element ratesAmount = doc.createElement("rates_amount");
                    book.appendChild(ratesAmount);
                    ratesAmount.appendChild(doc.createTextNode(rsBook.getString("rates_amount")));

                    try {
                        rsAuthor.close();
                        rsPublisher.close();
                        rsSubject.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    rsBook.close();
                } catch (SQLException ex) {
                    Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                rsRate.close();
            } catch (SQLException ex) {
                Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
            }

            TransformerFactory factoryTr = TransformerFactory.newInstance();
            Transformer transformer = factoryTr.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            ByteArrayOutputStream outByte = new ByteArrayOutputStream();
            Result result = new StreamResult(outByte);
            transformer.transform(domSource, result);
            byte[] xmlBytes = outByte.toByteArray();

            response.setContentLength(xmlBytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=rates.xml");
            out.write(xmlBytes);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DOMException ex) {
            Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stmtBook != null) {
                    stmtBook.close();
                }
                if (stmtRate != null) {
                    stmtRate.close();
                }
                if (stmtBookChild != null) {
                    stmtBookChild.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
            }
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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
