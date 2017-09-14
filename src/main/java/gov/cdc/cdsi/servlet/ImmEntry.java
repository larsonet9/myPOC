/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.servlet;

import gov.cdc.cdsi.db.DBGetter;
import gov.cdc.cdsi.db.NameValuePair;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author eric
 */
@WebServlet(name="ImmEntry", urlPatterns={"/immEntry"})
public class ImmEntry extends HttpServlet {

 
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * This is the entry point and is callable via a web browser.
   * e.g. http://localhost:8080/CDSi/immEntry
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Figure out what to do.
      String selVG = request.getParameter("selVG");

      loadPage(request, response);
  }

  private void loadPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();

    out.println("<form action=\"performCDS\" method\"POST\">");
      writePatient(out);
      ServletUtil.writeVaccineGroup(out, "0");
      ServletUtil.writeOutputOptions(out);
      writeMedHistories(out);
      writeImms(out);
      out.println("Assessment Date:&nbsp;<input type=\"text\" name=\"assessmentDate\" value=\"\"></input><br>");
      out.println("<input type=\"submit\" name=\"cdsi\"  value=\"Perform CDS\" /> &nbsp;");
      out.println("<input type=\"submit\" name=\"reset\" value=\"New Scenario\" />");
    out.println("</form>");
  }

  private void writePatient(PrintWriter out) { writePatient(out, "", "F"); }
  private void writePatient(PrintWriter out, String dob, String gender)
  {
    out.println("Patient DOB:&nbsp;<input type=\"text\" name=\"dob\" value=\""+dob+"\"></input>");

    List<NameValuePair> nvList = new ArrayList();
    nvList.add(new NameValuePair("F", "Female"));
    nvList.add(new NameValuePair("M", "Male"));
    nvList.add(new NameValuePair("U", "Unknown"));


    out.println("<br>Patient Gender:&nbsp;");
    out.println("<select name=\"gender\">");
      ServletUtil.comboWriter(out, nvList, gender);
    out.println("</select>");
  }

  private void writeMedHistories(PrintWriter out) 
  {
    try {
      List<NameValuePair> mhxList = DBGetter.getPatientObservations();

      out.println("<br><br><table><tr><th>&nbsp;</th><th>Medical History</th><tr>");
      for(int i = 1; i <= 5; i++)
      {
        writeMedHistory(out, mhxList, i);
      }
      out.println("</table>");
    }
    catch(Exception e) {
      out.println(e);
    }
  }
  
  private void writeMedHistory(PrintWriter out, List<NameValuePair> nvList, int num)
  {
    out.println("<tr>");
      out.println("<td>"+num+"</td>");
      out.println("<td><select style=\"width:650px\" name=\"selMHx"+num+"\">");
        out.println("  <option value=\"-\">Select...</option>");
        ServletUtil.comboWriter(out, nvList, "");
      out.println("</select></td>");
    out.println("</tr>");
  }
  
  private void writeImms(PrintWriter out)
  {
    try {
      List<NameValuePair> vList = DBGetter.getProducts();
      out.println("<br><br><table><tr><th>Dose #</th><th>Date</th><th>Vaccine Administered</th></tr>");
      for(int i = 1; i <=7; i++)
      {
        writeImm(out, vList, i);
      }
      out.println("</table>");
    }
    catch(Exception e) {
      out.println(e);
    }
  }

  private void writeImm(PrintWriter out, List<NameValuePair> nvList, int num)
  {
    out.println("<tr>");
      out.println("<td>"+num+"</td>");
      out.println("<td><input type=\"text\" name=\"date"+num+"\"\"></input></td>");

      out.println("<td><select name=\"selVDA"+num+"\">");
        out.println("  <option value=\"-\">Select...</option>");
        ServletUtil.comboWriter(out, nvList, "");
      out.println("</select></td>");
    out.println("</tr>");
  }


}
