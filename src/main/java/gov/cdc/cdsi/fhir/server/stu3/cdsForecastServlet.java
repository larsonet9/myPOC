/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cdc.cdsi.fhir.server.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.cdc.cdsi.db.DBGetter;
import gov.cdc.cdsi.db.NameValuePair;
import gov.cdc.cdsi.engine.CDSiEngine;
import gov.cdc.cdsi.engine.CDSiPatientData;
import gov.cdc.cdsi.engine.CDSiPatientData.Forecast;
import gov.cdc.cdsi.engine.CDSiPatientSeries;
import gov.cdc.cdsi.engine.CDSiResult;
import gov.cdc.cdsi.engine.CDSiScenario;
import gov.cdc.cdsi.engine.CDSiScenario.ScenarioImmunization;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationVaccinationProtocolComponent;
import org.hl7.fhir.dstu3.model.ImmunizationRecommendation;
import org.hl7.fhir.dstu3.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.TemporalPrecisionEnum;

/**
 *
 * @author Eric
 */
@WebServlet(name = "cds-forecast", urlPatterns = {"/cds-forecast"})
public class cdsForecastServlet extends HttpServlet {

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
          throws ServletException, IOException, Exception {
    // Set context to STU 3
    FhirContext ctx = FhirContext.forDstu3();
    Patient patient = new Patient();
    
    // Map to internal CDSi Scenario
    CDSiScenario scenario = makeCDSiScenario(request, ctx, patient);
    CDSiResult result = null;

    try {
      // perform CDS
      result = CDSiEngine.process(scenario);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new IOException("We blew up in the CDSiEngine.<br>" + e);
    }
    
    // build FHIR response
    Parameters outParam = buildResponse(result, scenario, patient);
   
    // Return Response
    try (PrintWriter out = response.getWriter()) {
      response.setContentType("application/xml+fhir; charset=UTF-8");
      out.print(ctx.newXmlParser().encodeResourceToString(outParam));
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
    try {
      processRequest(request, response);
    } catch (Exception ex) {
      Logger.getLogger(cdsForecastServlet.class.getName()).log(Level.SEVERE, null, ex);
    }
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
    try {
      processRequest(request, response);
    } catch (Exception ex) {
      Logger.getLogger(cdsForecastServlet.class.getName()).log(Level.SEVERE, null, ex);
    }
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

  private CDSiScenario makeCDSiScenario(HttpServletRequest request, FhirContext ctx, Patient patient) throws Exception {
    IParser parser = ctx.newXmlParser();
    Parameters params = parser.parseResource(Parameters.class, request.getReader());
    CDSiScenario scenario = new CDSiScenario();

    int i = 1;
    for(ParametersParameterComponent param : params.getParameter()) {
      if (param.getName().equalsIgnoreCase("Patient"))
      {
        patient = (Patient) param.getResource();
        scenario.setGender(patient.getGender().toCode().substring(0, 1).toUpperCase());
        scenario.setDob(patient.getBirthDate());
      }
      else if (param.getName().equalsIgnoreCase("assessmentDate"))
      {
        DateType dt = (DateType) param.getValue();
        scenario.setAssessmentDate(dt.getValue());

      }
      else if (param.getName().equalsIgnoreCase("Immunization"))
      {
        Immunization imm = (Immunization) param.getResource();
        scenario.addVaccineDoseAdministered(imm.getVaccineCode().getCoding().get(0).getCode(), null, imm.getDate(),i++);
      }

    }
    return scenario;
  }

  private CodeableConcept getCodeableConcept(String code, String display, String system) {
    CodeableConcept cc = new CodeableConcept();
    Coding coding = new Coding();
    coding.setCode(code);
    coding.setDisplay(display);
    coding.setSystem(system);
    cc.getCoding().add(coding);
    return cc;
  }
  
  private ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent getDateCriterionComponent(String code, String display, Date forecastDate) {
    CodeableConcept dateType = getCodeableConcept(code, display, "http://hl7.org/fhir/immunization-recommendation-date-criterion");
    DateTimeType dtt = new DateTimeType();
    dtt.setValue(forecastDate, TemporalPrecisionEnum.DAY);
    return new ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent(dateType, dtt);
  }

  private Parameters buildResponse(CDSiResult result, CDSiScenario scenario, Patient patient) throws Exception {
    Parameters outParam = new Parameters();
    ImmunizationRecommendation ir = new ImmunizationRecommendation();
    ParametersParameterComponent ppc = new ParametersParameterComponent();
    ppc.setName("ImmunizationRecommendation");

    // Set the Patient Reference
    Reference patientReference = new Reference();
    patientReference.setResource(patient);
    ir.setPatient(patientReference);


    // Create Imm References with Evaluation outcomes
    List<Reference> lImms = new ArrayList();
    for(ScenarioImmunization simm : scenario.getVaccineDosesAdministered()) {
        Immunization immFHIR = new Immunization();
        immFHIR.setNotGiven(false);
        immFHIR.setPrimarySource(false);
        immFHIR.setStatus(Immunization.ImmunizationStatus.COMPLETED);
        DateTimeType dtt = new DateTimeType();
        dtt.setValue(simm.getDateAdministered(), TemporalPrecisionEnum.DAY);
        immFHIR.setDate(dtt.getValue());
        String display = simm.getProduct().equalsIgnoreCase("UNKNOWN") ? DBGetter.getVaccineShortName(simm.getCvx()) : simm.getProduct();
        immFHIR.setVaccineCode(getCodeableConcept(simm.getCvx(), display, "http://hl7.org/fhir/v3/vs/VaccineType"));
              
        for(NameValuePair vg : DBGetter.getVaccineGroupsAssociatedWithVaccine(simm.getVaccineId())) {
          CDSiPatientSeries ps = result.getBestPatientSeries(Integer.parseInt(vg.getId()));
          for(CDSiPatientData.AntigenAdministered aa : ps.getPatientData().getAntigenAdministeredList()) {
            if(aa.getDoseId() == simm.getDoseId()) {
              ImmunizationVaccinationProtocolComponent vaxProtocol = new ImmunizationVaccinationProtocolComponent();
              vaxProtocol.setDoseSequence(aa.getSatisfiedTargetDoseNumber());
              vaxProtocol.setDescription("ACIP Schedule");
              vaxProtocol.setSeries(ps.getSeriesName());
              vaxProtocol.setSeriesDoses(ps.getTargetDoses().size());
              vaxProtocol.addTargetDisease(getCodeableConcept(DBGetter.GetDefaultForecastCVX(Integer.parseInt(vg.getId())), vg.getValue(), "http://hl7.org/fhir/vs/vaccination-protocol-dose-target"));
              vaxProtocol.setDoseStatus(getCodeableConcept(aa.getEvaluationStatus(), aa.getEvaluationStatus(), "http://hl7.org/fhir/vs/vaccination-protocol-dose-status"));
              vaxProtocol.setDoseStatusReason(getCodeableConcept("reasonCode", aa.getEvaluationReason(), "http://hl7.org/fhir/vs/vaccination-protocol-dose-status-reason"));
              immFHIR.getVaccinationProtocol().add(vaxProtocol);
            }
          }
        }
        Reference immReference = new Reference();
        immReference.setResource(immFHIR);
        lImms.add(immReference);
    }


    for(Forecast fcastCDSi : result.getVaccineGroupForecast()) {
      ImmunizationRecommendationRecommendationComponent fcastFHIR = new ImmunizationRecommendationRecommendationComponent();
      fcastFHIR.setDate(new Date());
      fcastFHIR.setVaccineCode(getCodeableConcept(DBGetter.GetDefaultForecastCVX(fcastCDSi.getVaccineGroupId()), DBGetter.GetVaccineGroupName(fcastCDSi.getVaccineGroupId()), "http://hl7.org/fhir/v3/vs/VaccineType"));
      fcastFHIR.setDoseNumber(fcastCDSi.getTargetDoseNumber());
      fcastFHIR.setForecastStatus(getCodeableConcept(fcastCDSi.getStatus(), fcastCDSi.getStatus(), "http://hl7.org/fhir/vs/immunization-recommendation-status"));
      
      // Earliest Date
      if(fcastCDSi.getEarliestDate() != null) 
        fcastFHIR.getDateCriterion().add(getDateCriterionComponent("earliest", "Earliest Date", fcastCDSi.getEarliestDate()));
     
      // Recommended Date
      if(fcastCDSi.getAdjustedRecommendedDate() != null)
        fcastFHIR.getDateCriterion().add(getDateCriterionComponent("recommended", "Recommended Date", fcastCDSi.getAdjustedRecommendedDate()));

      // Past Due Date
      if(fcastCDSi.getAdjustedPastDueDate() != null)
        fcastFHIR.getDateCriterion().add(getDateCriterionComponent("overdue", "Past Due Date", fcastCDSi.getAdjustedPastDueDate()));

      // Latest Date
      if(fcastCDSi.getLatestDate() != null)
        fcastFHIR.getDateCriterion().add(getDateCriterionComponent("latest", "Latest Date", fcastCDSi.getLatestDate()));
  
      
      for(Reference fhirImmRef : lImms) {
        if(immSupportsForecast(fcastFHIR.getVaccineCode(), fhirImmRef)) {
          fcastFHIR.addSupportingImmunization(fhirImmRef);
        }
      }
          
      // Add the Recommendation
      ir.getRecommendation().add(fcastFHIR);
    }
    ppc.setResource(ir);
    outParam.addParameter(ppc);
    
    return outParam;
  }

  private boolean immSupportsForecast(CodeableConcept vaccineCode, Reference fhirImmRef) {
    Immunization imm = (Immunization) fhirImmRef.getResource();
    for(ImmunizationVaccinationProtocolComponent evaluation : imm.getVaccinationProtocol()) {
      if(evaluation.getTargetDisease().get(0).getCoding().get(0).getCode().equalsIgnoreCase(vaccineCode.getCoding().get(0).getCode()))
        return true;
    }
    return false;
  }

}
