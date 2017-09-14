/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.servlet;

import gov.cdc.cdsi.db.DBGetter;
import gov.cdc.cdsi.db.NameValuePair;
import gov.cdc.cdsi.engine.CDSiGlobals;
import gov.cdc.cdsi.engine.CDSiPatientData.AntigenAdministered;
import gov.cdc.cdsi.engine.CDSiPatientData.Forecast;
import gov.cdc.cdsi.engine.CDSiPatientSeries;
import gov.cdc.cdsi.engine.CDSiResult;
import gov.cdc.cdsi.engine.CDSiScenario;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class ServletUtil {

  public static void writeVaccineGroup(PrintWriter out) { writeVaccineGroup(out, "0"); }
  public static void writeVaccineGroup(PrintWriter out, String selectedVG)
  {
    try {
      List<NameValuePair> vgList = DBGetter.getVaccineGroups();
      out.println("<br>Vaccine Group:&nbsp;");
      out.println("<select name=\"selVG\">");
      out.println("  <option value=\"0\">Select...</option>");
        comboWriter(out, vgList, selectedVG);
      out.println("</select>");
    }
    catch(Exception e) {
      out.println(e);
    }
  }
  
  public static void writeOutputOptions(PrintWriter out) {
      List<NameValuePair> vgList = new ArrayList();
      vgList.add(new NameValuePair("Verbose", "Verbose"));
      vgList.add(new NameValuePair("Condensed", "Condensed"));
      vgList.add(new NameValuePair("Text", "Text"));
      out.println("<br>Result Format:&nbsp;");
      out.println("<select name=\"resultFormat\">");
        comboWriter(out, vgList, "Verbose");
      out.println("</select>");
    
  }

  public static void comboWriter(PrintWriter out, List<NameValuePair> nvList, String selId)
  {
      for(NameValuePair nv : nvList)
      {
        if(selId.equals(nv.getId()))
          out.println("<option selected value=\""+nv.getId()+"\">"+nv.getValue()+"</option>");
        else
          out.println("<option value=\""+nv.getId()+"\">"+nv.getValue()+"</option>");
      }
  }

  public static void setHtmlStyle(PrintWriter out)
  {
      out.println("<html>");
        out.println("<head>");
          out.println("<style>");
            out.println("table, td, th {border:1px solid black;}");
            out.println("th, td {padding:0px 10px 0px 10px;}");
            out.println("th {background-color:steelBlue;}");
            out.println("tr.best {background-color:lightGreen;}");
            out.println("tr.failed {background-color:lightGreen;}");
            out.println("tr.evalMismatch {background-color:IndianRed;}");
            out.println("td.evalMismatch {background-color:IndianRed;}");
            out.println("td.forecastFailed {background-color:IndianRed;}");
          out.println("</style>");
        out.println("</head>");
  }

  public static void writeResultToScreen(PrintWriter out, CDSiResult result, CDSiScenario scenario, String resultFormat) throws Exception
  {
    switch (resultFormat) {
      case "Verbose":
      case "verbose":
           writeVerboseResultToScreen(out, result, scenario);
           return;
      case "Condensed":
      case "condensed":
           writeCondensedResultToScreen(out, result, scenario);
           return;
      default:
           writeTextResultToScreen(out, result, scenario);
    }
  }
    
  public static void writeVerboseResultToScreen(PrintWriter out, CDSiResult result, CDSiScenario scenario)
  {
    // Some Basic HTML Stuff
    setHtmlStyle(out);

    // Initial Input
    out.println("<h1>INPUT DATA:</h1>" +scenario.toString() + "<hr>");

    // output CDSi antigen series results
    out.println("<h1>RESULTS:</h1>");
    for(CDSiPatientSeries ps : result.getPatientSeries())
      out.println(ps.toString()+ "<hr>");

    // output the Vaccine Group Forecast
    out.println("<h1>VACCINE GROUP FORECAST");
    out.println(result.getVaccineGroupForecast().toString());

        // For testing, just push this to the screen.
    out.println("<br><br><hr><br><h2>Initial Input:</h2><br>" +scenario.toString());

    // Close the HTML.
    out.println("</html>");
  }

  public static void writeCondensedResultToScreen(PrintWriter out, CDSiResult result, CDSiScenario scenario) throws Exception
  {
    // Some Basic HTML Stuff
    setHtmlStyle(out);

    // Initial Input
    out.println("<h1>INPUT DATA:</h1>" +scenario.toString() + "<hr>");

    // output CDSi antigen series results
    out.println("<h1>RESULTS:</h1>");
    out.println("<h3>Immunization History Evaluation</h3>" +
                "  <table border=\"1\"><tr>" +
                "    <th>VG</th>" +
                "    <th>VDA#</th>" +
                "    <th>TD#</th>" +
                "    <th>Date Admin</th>" +
                "    <th>Dose Administered</th>" +
                "    <th>Eval Status</th>" +
                "    <th>Eval Reason</th></tr>");

    for(NameValuePair vgs : DBGetter.getVaccineGroups()) {
      CDSiPatientSeries ps = result.getBestPatientSeries(Integer.parseInt(vgs.getId()));
      if(ps != null) {
        for(AntigenAdministered aa : ps.getPatientData().getAntigenAdministeredList()) {
          out.println("<tr>" +
                        "<td>" + vgs.getValue() + "</td>" +
                        "<td>" + aa.getDoseId() + "</td>" +
                        "<td>" + (aa.getSatisfiedTargetDoseNumber() == 0 ? "-" : aa.getSatisfiedTargetDoseNumber()) + "</td>" + 
                        "<td>" + new SimpleDateFormat("MM/dd/yyyy").format(aa.getDateAdministered()) + "</td>" +
                        "<td>" + aa.getTradeName() + " [" + aa.getCvx() + " - " + aa.getMvx() + "]" +
                        "<td>" + aa.getEvaluationStatus() + "</td>" +
                        "<td>" + aa.getEvaluationReasonsInTableFmt()+ "</td>" +
                      "</tr>");
        }
      }
    }
    out.println("</table>");
    out.println("<br><br>");

    // output the Vaccine Group Forecast
    out.println("<h3>VACCINE GROUP FORECAST</h3>");
      String str = "<table border=\"1\">";
             str += " <tr>";
             str += "   <th>Vaccine Group</th>";
             str += "   <th>TD#</th>";
             str += "   <th>Earliest Date</th>";
             str += "   <th>Recommended Date</th>";
             str += "   <th>Past Due Date</th>";
             str += "   <th>Latest Date</th>";
             str += "   <th>reason</th>";
             str += "   <th>status</th>";
             str += "   <th>Antigens Needed</th>";
             str += "   <th>Recommended Vaccines</th>";
             str += "</tr>";
    out.println(str);
    for(Forecast f : result.getVaccineGroupForecast()) {
      str = "<tr>";
      str += "  <td>" + DBGetter.GetVaccineGroupName(f.getVaccineGroupId()) + "</td>";
      str += "  <td>" + f.getTargetDoseNumber() + "</td>";
      str += "  <td>" + (f.getEarliestDate() == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(f.getEarliestDate())) + "</td>";
      str += "  <td>" + (f.getAdjustedRecommendedDate() == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(f.getAdjustedRecommendedDate())) + "</td>";
      str += "  <td>" + (f.getAdjustedPastDueDate() == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(f.getAdjustedPastDueDate())) + "</td>";
      str += "  <td>" + (f.getLatestDate() == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(f.getLatestDate())) + "</td>";
      str += "  <td>" + f.getReason() + "</td>";
      str += "  <td>" + f.getStatus() + "</td>";
      str += "  <td>" + f.getAntigensNeeded() + "</td>";
      str += "  <td>" + f.writeRecommendedVaccineTypes() + "</td>";
      str += "</tr>";
      
      out.println(str);
    }
      out.println("</table>");

    // Close the HTML.
    out.println("</html>");
  }
  
    public static void writeTextResultToScreen(PrintWriter out, CDSiResult result, CDSiScenario scenario) throws Exception
  {
    out.println("<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">");
    for(Forecast f : result.getVaccineGroupForecast()) {
      String str  = "Forecasting " + DBGetter.GetVaccineGroupName(f.getVaccineGroupId());
//      str += " status "     + (f.getStatus().equals(CDSiGlobals.SERIES_NOT_COMPLETE) ? "due" : f.getStatus().toLowerCase());
      str += " status "     + f.getStatus().toLowerCase();
      if(f.getStatus().equals(CDSiGlobals.SERIES_NOT_COMPLETE)) 
      {
        str += " dose "  + f.getTargetDoseNumber();
        str += " due "   + (f.getAdjustedRecommendedDate() == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(f.getAdjustedRecommendedDate()));
        str += " valid " + (f.getEarliestDate()            == null ? "-" : new SimpleDateFormat("MM/dd/yyyy").format(f.getEarliestDate()));
        str += (f.getAdjustedPastDueDate()     == null ? " overdue 12/31/2999" : " overdue " + new SimpleDateFormat("MM/dd/yyyy").format(f.getAdjustedPastDueDate()));
        str += (f.getLatestDate()              == null ? " finished 12/31/2999" : " finished " + new SimpleDateFormat("MM/dd/yyyy").format(f.getLatestDate()));
      }
      out.println(str);
    }
    
    out.println();
    for(NameValuePair vgs : DBGetter.getVaccineGroups()) {
      CDSiPatientSeries ps = result.getBestPatientSeries(Integer.parseInt(vgs.getId()));
      if(ps != null) {
        for(AntigenAdministered aa : ps.getPatientData().getAntigenAdministeredList()) {
          String str  = "Vaccination #" + aa.getDoseId() + ": ";
                 str += aa.getTradeName() + " [" + aa.getCvx() + " - " + aa.getMvx() + "]";
                 str += " given " + new SimpleDateFormat("MM/dd/yyyy").format(aa.getDateAdministered());
                 str += (aa.getEvaluationStatus().equals(CDSiGlobals.ANTIGEN_ADMINISTERED_NOT_VALID) ? " is an invalid" : " is a " + aa.getEvaluationStatus().toLowerCase()) + " " + vgs.getValue();
                 str += " dose " + (aa.getSatisfiedTargetDoseNumber() == 0 ? aa.getUnsatisfiedTargetDoseNumber() : aa.getSatisfiedTargetDoseNumber()) + ".";
          out.println(str);    
        }
      }
    }
    out.println("</pre>");
  }
}

