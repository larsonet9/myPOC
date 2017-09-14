/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.servlet;

import gov.cdc.cdsi.engine.CDSiEngine;
import gov.cdc.cdsi.engine.CDSiResult;
import gov.cdc.cdsi.engine.CDSiScenario;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Eric
 */
@WebServlet(name = "forecast", urlPatterns = {"/forecast"})
public class forecast extends HttpServlet {

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
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    
        // Save the user entered scenario as a CDSiScenario
    CDSiScenario scenario = saveScenario(request);

    try {
      // perform CDS
      CDSiResult result = CDSiEngine.process(scenario);

      // Write the Results to the screen
      ServletUtil.writeResultToScreen(out, result, scenario, request.getParameter("resultFormat"));
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new IOException("We blew up in the CDSiEngine.<br>" + e);
    }

    out.close();
  }

  private CDSiScenario saveScenario(HttpServletRequest request) throws IOException {
    CDSiScenario scenario = new CDSiScenario();
    try {
      scenario.setDob(request.getParameter("patientDob"));
      scenario.setGender(request.getParameter("patientSex"));
      scenario.setAssessmentDate(request.getParameter("evalDate"));

      for(int i = 1; i <= 50; i++)
      {
        String dateParam = "vaccineDate"+i;
        String cvxParam  = "vaccineCvx"+i;
        String mvxParam  = "vaccineMvx"+i;
        String dateAdmin = request.getParameter(dateParam);
        String cvxAdmin  = request.getParameter(cvxParam);
        String mvxAdmin  = request.getParameter(mvxParam);
        // Only add imms with a date and a vaccine
        if(dateAdmin != null && dateAdmin.length() > 0 &&
           cvxAdmin != null && cvxAdmin.length() > 1)
        {
          scenario.addVaccineDoseAdministered(cvxAdmin, mvxAdmin, dateAdmin, i);
        }
      }
    }
    catch (Exception e) {
      throw new IOException("We blew up saving the scenario.<br>" + e);
    }

    return scenario;
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
