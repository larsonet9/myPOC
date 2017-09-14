/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.servlet;

import gov.cdc.cdsi.engine.CDSiEngine;
import gov.cdc.cdsi.engine.CDSiPatientSeries;
import gov.cdc.cdsi.engine.CDSiResult;
import gov.cdc.cdsi.engine.CDSiScenario;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author eric
 */
@WebServlet(name="PerformCDS", urlPatterns={"/performCDS"})
public class PerformCDS extends HttpServlet {

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();

    // User Pressed the Reset Button
    String resetButton = request.getParameter("reset");
    if(resetButton != null) {
      response.sendRedirect("immEntry");
      return;
    }

    // User Changed Vaccine Groups
    String cdsiButton = request.getParameter("cdsi");
    if(cdsiButton == null) {
      String dob    = request.getParameter("dob");
      String gender = request.getParameter("gender");
      String selVG  = request.getParameter("selVG");
      response.sendRedirect("immEntry?dob="+dob+"&gender="+gender+"&selVG="+selVG);
      return;
    }

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
      scenario.setDob(request.getParameter("dob"));
      scenario.setGender(request.getParameter("gender"));
      scenario.setVaccineGroupId(request.getParameter("selVG"));
      scenario.setAssessmentDate(request.getParameter("assessmentDate"));

      for(int i = 1; i <= 5; i++)
      {
        String selMedHx = request.getParameter("selMHx"+i);
        if(selMedHx != null && !selMedHx.equals("-"))
        {
          String[] codeSys = selMedHx.split(":");
          if(codeSys.length == 2)
            scenario.addMedicalHistory(codeSys[0], codeSys[1]);
          else
            scenario.addMedicalHistory(codeSys[0], codeSys[1], codeSys[2]);
        }
      }
      
      for(int i = 1; i <= 7; i++)
      {
        String dateParam = "date"+i;
        String vdaParam  = "selVDA"+i;
        String dateAdmin = request.getParameter(dateParam);
        String vaccAdmin = request.getParameter(vdaParam);
        // Only add imms with a date and a vaccine
        if(dateAdmin != null && dateAdmin.length() > 0 &&
           vaccAdmin != null && vaccAdmin.length() > 1)
        {
          scenario.addVaccineDoseAdministered(vaccAdmin, dateAdmin, i);
        }
      }
    }
    catch (Exception e) {
      throw new IOException("We blew up saving the scenario.<br>" + e);
    }

    return scenario;

  }

}
