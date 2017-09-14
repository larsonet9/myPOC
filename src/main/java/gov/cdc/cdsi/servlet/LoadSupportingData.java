/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.servlet;

import gov.cdc.cdsi.supportingdata.load.SDLoader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author eric
 */
@WebServlet(name="LoadSupportingData", urlPatterns={"/loadSupportingData"})
@MultipartConfig
public class LoadSupportingData extends HttpServlet {
   
  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
      // Figure out what to do.
      String action = request.getParameter("loadButton");

      if(action != null && action.equals("Load Supporting Data"))
        load(request, response);
      else
        initialPageLoad(request, response);

  }

  private void initialPageLoad(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();

    out.println("<html><head></head><body>");
    out.println("<form action=\"loadSupportingData\" method=\"POST\" enctype=\"multipart/form-data\">");
      out.println("Antigen XML File:&nbsp;<input type=\"file\" name=\"xmlFile\"></input><br><br>");
      out.println("Supporting Data Version#:&nbsp;<input type=\"text\" name=\"sdVersion\" value=\"\"></input><br><br>");
      out.println("<input type=\"submit\" name=\"loadButton\"  value=\"Load Supporting Data\" />");
    out.println("</form>");
    out.println("</body></html>");
    out.close();
  }

  private void load(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();

    try {
      Part filepart = request.getPart("xmlFile");
      out.println("<br>ContentType = " + filepart.getContentType());
      out.println("<br>Size = " + filepart.getSize());
      out.println("<br>name = " + filepart.getName());
      out.println("<br>filename = " + extractFileName(filepart));
      SDLoader sdLoader = new SDLoader();
      sdLoader.loadAntigenSupportingData(filepart.getInputStream(),request.getParameter("sdVersion"));
      out.println("<br>SUCCESS!");
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new IOException("We blew up in the CDSiEngine.<br>" + e);
    }
    finally {
     out.close();
    }
  }

  private String extractFileName(Part part) {
    String contentDisp = part.getHeader("content-disposition");
    String[] items = contentDisp.split(";");
    for (String s : items) {
        if (s.trim().startsWith("filename")) {
            return s.substring(s.indexOf("=") + 2, s.length()-1);
        }
    }
    return "";
  }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
