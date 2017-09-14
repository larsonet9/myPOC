/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.servlet;

import gov.cdc.cdsi.engine.CDSiEngine;
import gov.cdc.cdsi.engine.CDSiPatientSeries;
import gov.cdc.cdsi.engine.CDSiResult;
import gov.cdc.cdsi.engine.CDSiScenario;
import gov.cdc.cdsi.testcase.ResultData;
import gov.cdc.cdsi.testcase.TestCaseData;
import gov.cdc.cdsi.testcase.TestCaseResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author eric
 */
@WebServlet(name="ExecuteTestCase", urlPatterns={"/executeTestCase"})
public class ExecuteTestCase extends HttpServlet {
   
  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    String testCaseId = request.getParameter("testCaseID");
    String cdsiButton = request.getParameter("cdsi");

    try {
      if(cdsiButton != null && cdsiButton.equalsIgnoreCase("Execute Vaccine Group"))
      {
        // Execute the Vaccine Group
        executeVaccineGroup(request.getParameter("selVG"));
        // Retrieve the results
        List<ResultData> rdList = TestCaseResult.getCDSiResults();
        // Print the test case Results
        writeTestCaseResultsToScreen(out, rdList);
      }
      else {
        // Populate the Scenario
        CDSiScenario scenario = TestCaseData.getTestCase(testCaseId);
        
        // Get Guidance
        CDSiResult result = CDSiEngine.process(scenario);
        // Write the Results to the screen
        ServletUtil.writeResultToScreen(out, result, scenario, "Verbose");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new IOException("We blew up in the CDSiEngine.<br>" + e);
    }
    finally {
     out.close();
    }
  }

  private void executeVaccineGroup(String vaccineGroupId) throws Exception {
    // Get the test case ids for the vaccine group
    List<String> testIds = TestCaseData.getAllTestCasesByVaccineGroup(vaccineGroupId);

    // Clear out the Test Case Results table first.
    TestCaseResult.purgeResultsTable();

    for(String id : testIds) {
      // Populate the Scenario
      CDSiScenario scenario = TestCaseData.getTestCase(id);
      // Get Guidance
      CDSiResult result = CDSiEngine.process(scenario);
      //Write the best series to the DB.
      TestCaseResult.writeCDSiResult(result, id, vaccineGroupId );
    }

  }

  private void writeTestCaseResultsToScreen(PrintWriter out, List<ResultData> rdList) throws Exception {
    // Some Basic HTML Stuff
    ServletUtil.setHtmlStyle(out);

    int evalAndForecastPassed = 0;
    int evalPassed = 0;
    int forecastPassed = 0;
    int evalAndForecastFailed = 0;
    int evalFailed = 0;
    int forecastFailed = 0;

    for(ResultData rd : rdList) {
      if(rd.evaluationAndForecastPassed()) evalAndForecastPassed++;
      else evalAndForecastFailed++;

      if(rd.evaluationPassed()) evalPassed++;
      else evalFailed++;

      if(rd.forecastPassed()) forecastPassed++;
      else forecastFailed++;
    }

    // Header
    out.println("<h1>TEST CASE RESULTS:</h1>");
    out.println("<hr><h2>Summary</h2><table><tr><th>&nbsp;</th><th>Test Cases</th><th>Passed</th><th>Failed</th></tr>");
    out.println("<tr><td>Eval and Forecast</td><td>" + rdList.size() + "</td><td>" + evalAndForecastPassed + "</td><td>" + evalAndForecastFailed + "</td></tr>");
    out.println("<tr><td>Evaluation Only</td><td>"   + rdList.size() + "</td><td>" + evalPassed            + "</td><td>" + evalFailed            + "</td></tr>");
    out.println("<tr><td>Forecast Only</td><td>"     + rdList.size() + "</td><td>" + forecastPassed        + "</td><td>" + forecastFailed        + "</td></tr></table>");
    out.println("<hr><h2>Test Case Details</h2>");

    for(ResultData rd : rdList) {
      out.println(rd.toString());
      String href = "executeTestCase?testCaseID=" + rd.getTestId();
      out.println("<a href=\""+href+"\">Detailed Execution</a><br><br>");
    }

    // Close the HTML.
    out.println("</html>");
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
