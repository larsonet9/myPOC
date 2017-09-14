//package gov.cdc.cdsi.fhir.server;
//
//import gov.cdc.cdsi.db.DBGetter;
//import gov.cdc.cdsi.db.NameValuePair;
//import gov.cdc.cdsi.engine.CDSiEngine;
//import gov.cdc.cdsi.engine.CDSiPatientData;
//import gov.cdc.cdsi.engine.CDSiPatientData.Forecast;
//import gov.cdc.cdsi.engine.CDSiPatientSeries;
//import gov.cdc.cdsi.engine.CDSiResult;
//import gov.cdc.cdsi.engine.CDSiScenario;
//import gov.cdc.cdsi.engine.CDSiScenario.ScenarioImmunization;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//import javax.ws.rs.core.Response;
//import org.hl7.fhir.dstu3.model.Bundle;
//import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
//import org.hl7.fhir.dstu3.model.Immunization;
//import org.hl7.fhir.dstu3.model.Immunization.ImmunizationVaccinationProtocolComponent;
//import org.hl7.fhir.dstu3.model.ImmunizationRecommendation;
//import org.hl7.fhir.dstu3.model.MessageHeader;
//import org.hl7.fhir.dstu3.model.Patient;
//import org.hl7.fhir.dstu3.model.Resource;
//
//public class BundleProcessor
//{
//  
//  public ResourceContainer process(Bundle immunizationFeed, String baseURL)
//  {
//    ResourceContainer forecastResult = new ResourceContainer();
//    
//      //////////////////////////////////////////////////////////////////////////
//      ///
//      /// Convert Incoming request into FHIR POJOs. 
//      /// Header - (1..1) 
//      /// Patient - (1..1)
//      /// Immunization - (0..*) 
//      ///
//      //////////////////////////////////////////////////////////////////////////
//
//    try {
//      MessageHeader messageHeaderRequest = null;
//      BundleEntryComponent patientEntry = null;
//      Patient patientRequest = null;
//      List<Immunization> immunizationRequestList = new ArrayList<>();
//
//      // Extract patient and Imm data to send to CDSi engine
//      int totalEntries = immunizationFeed.getEntry().size();
//
//      // We expect at least three entries - MessageHeader, Patient, Immunization
//      if (totalEntries < 2) {
//        // Not enough information to execute the forecast; build error response
//				forecastResult.setResource(null);
//				forecastResult.setBundle(null);
//        forecastResult.setResponseStatus(Response.Status.PRECONDITION_FAILED);
//        forecastResult.setMessage("Not enough information to execute the forecast. Missing MessageHeader, Patient and/or Immunization resource entries.");
//        return forecastResult;
//      }
//
//      // Iterate through the immunizationFeed Entry list
//      int entryIndex = 0;
//      Resource entryResource;
//      for (BundleEntryComponent entry : immunizationFeed.getEntry()) {
//        entryIndex++;
//        entryResource = entry.getResource();
//
//        if (entryIndex == 1) {
//          // First entry must be MessageHeader
//          if (entryResource != null
//              && entryResource instanceof MessageHeader) {
//            messageHeaderRequest = (MessageHeader) entryResource;
//          } else {
//            // First entry not expected type MessageHeader; build error response
//						forecastResult.setResource(null);
//						forecastResult.setBundle(null);
//            forecastResult.setResponseStatus(Response.Status.PRECONDITION_FAILED);
//            forecastResult.setMessage("First entry not expected type MessageHeader. Missing MessageHeader, Patient and/or Immunization resource entries.");
//            return forecastResult;
//          }
//
//        } else if (entryIndex == 2) {
//          // Second entry must be Patient
//          if (entryResource != null && entryResource instanceof Patient) {
//            patientEntry = entry;
//            patientRequest = (Patient) entryResource;
//          } else {
//            // Second entry not expected type Patient; build error response
//						forecastResult.setResource(null);
//						forecastResult.setBundle(null);
//            forecastResult.setResponseStatus(Response.Status.PRECONDITION_FAILED);
//            forecastResult.setMessage("Second entry not expected type Patient. Missing MessageHeader, Patient and/or Immunization resource entries.");
//            return forecastResult;
//          }
//
//        } else {
//          // Third plus entries must be Immunization
//          if (entryResource != null && entryResource instanceof Immunization) {
//            immunizationRequestList.add((Immunization) entryResource);
//          } else {
//            // Ignore missing or invalid Resource types
//          }
//
//        }
//
//      }
//      
//      //////////////////////////////////////////////////////////////////////////
//      ///
//      /// Incoming request is now into FHIR POJOs. 
//      /// Now convert to CDSi Scenario and execute CDSi logic
//      /// It's probably possible to go straight from Bundle Resource to a 
//      /// CDSi Scenario and thus skipping the first step.
//      ///
//      //////////////////////////////////////////////////////////////////////////
//      
//      CDSiScenario scenario = makeCDSiScenario(patientRequest, immunizationRequestList);
//      CDSiResult result = null;
//      try {
//        // perform CDS
//        result = CDSiEngine.process(scenario);
//      }
//      catch (Exception e) {
//        e.printStackTrace();
//        throw new IOException("We blew up in the CDSiEngine.<br>" + e);
//      }
//      
//      
//      //////////////////////////////////////////////////////////////////////////
//      ///
//      /// CDSi logic applied.  
//      /// Now convert CDSi output (eval/forecast) back into FHIR
//      /// Header - (1..1) 
//      /// Patient - (1..1)
//      /// Immunization - (0..*) 
//      /// ImmunizationRecommendation - (0..*)
//      ///
//      //////////////////////////////////////////////////////////////////////////
//      
//      // FHIR Bundle to hold forecast feed result
//      Bundle forecastBundle = new Bundle();
//      forecastResult.setBundle(forecastBundle);
//      forecastResult.setResponseStatus(Response.Status.OK);
//      Date updatedTime = new Date();
//
//      forecastBundle.setId(getUUID());
//			ResourceMetaComponent forecastBundleMeta = new ResourceMetaComponent();
//			forecastBundleMeta.setVersionId("1");
//			forecastBundleMeta.setLastUpdated(updatedTime);
//			forecastBundle.setMeta(forecastBundleMeta);
//			forecastBundle.setType(BundleType.MESSAGE);
//      // MessageHeader (1) + Patient (1) + Imms Administered + Imm Recommendation
//			forecastBundle.setTotal(1 + 1 + scenario.getVaccineDosesAdministered().size() + 1);
//			forecastBundle.setBase(baseURL);
//     
//      // FHIR MessageHeader response - 1st entry
//      BundleEntryComponent entry1 = new BundleEntryComponent();
//      forecastBundle.getEntry().add(entry1);
//      
//			MessageHeader messageHeaderResponse = new MessageHeader();
//			String messageHeaderId = getUUID();
//			messageHeaderResponse.setId(messageHeaderId);
//
//			ResourceMetaComponent messageHeaderMeta = new ResourceMetaComponent();
//			messageHeaderMeta.setVersionId("1");
//			messageHeaderMeta.setLastUpdated(updatedTime);
//			messageHeaderRequest.setMeta(messageHeaderMeta);
//
//			messageHeaderResponse.setIdentifier(messageHeaderId);
//			messageHeaderResponse.setTimestamp(updatedTime);
//
//			Coding responseEvent = new Coding().setCode("ClinicalDecisionSupportImmunizationForecast");
//			responseEvent.setSystem("http://hl7.org/fhir/message-events");
//			messageHeaderResponse.setEvent(responseEvent);
//
//			MessageHeader.MessageSourceComponent responseSource = new MessageHeader.MessageSourceComponent().setSoftware("CDC CDSi Proof Of Concept System");
//			responseSource.setName("Centers for Disease Control and Prevention");
//			responseSource.setVersion("0.01.00");
//			responseSource.setEndpoint(baseURL);
//			messageHeaderResponse.setSource(responseSource);
//
//			MessageHeader.MessageHeaderResponseComponent responseComponent = new MessageHeader.MessageHeaderResponseComponent();
//			responseComponent.setIdentifier(messageHeaderRequest.getIdentifier());
//			responseComponent.setCode(MessageHeader.ResponseCode.OK);
//			messageHeaderResponse.setResponse(responseComponent);
//
//			entry1.setResource(messageHeaderResponse);
//
//      // FHIR Patient resource - 2nd entry (add Entry from original request)
//      String patientId = patientRequest.getId();
//      forecastBundle.getEntry().add(patientEntry);
//      
//			Reference patientReference = new Reference();
//			patientReference.setReference(patientId);
//			messageHeaderResponse.getData().add(patientReference);
//
//      // FHIR ImmunizationEntry (One Entry per immunization administered)
//      for(ScenarioImmunization simm : scenario.getVaccineDosesAdministered()) {
//        // For Each imm, create an imm entry and all corresponding evaluations (multiple for combos)
//        Immunization imm = new Immunization();
//
//        // Set Date of Forecast
//        imm.setDate(new DateAndTime(simm.getDateAdministered()));
//
//        // Set vaccineType
//        String display = simm.getProduct().equalsIgnoreCase("UNKNOWN") ? DBGetter.getVaccineShortName(simm.getCvx()) : simm.getProduct();
//        imm.setVaccineType(getCodeableConcept(simm.getCvx(), display, "http://hl7.org/fhir/v3/vs/VaccineType"));
//        
//        // Set the Evaluation Status (Usually 1; more if a combo vaccine)
//        for(NameValuePair vgs : DBGetter.getVaccineGroupsAssociatedWithVaccine(simm.getVaccineId())) {
//          CDSiPatientSeries ps = result.getBestPatientSeries(Integer.parseInt(vgs.getId()));
//          if (ps != null) {
//             for(CDSiPatientData.AntigenAdministered aa : ps.getPatientData().getAntigenAdministeredList()) {
//               if(aa.getDoseId() == simm.getDoseId()) {
//                 ImmunizationVaccinationProtocolComponent vaxProtocol = new ImmunizationVaccinationProtocolComponent();
//                 vaxProtocol.setDoseSequence(aa.getSatisfiedTargetDoseNumber());
//                 vaxProtocol.setDescription("ACIP Schedule");
//                 vaxProtocol.setSeries(ps.getSeriesName());
//                 vaxProtocol.setSeriesDoses(ps.getTargetDoses().size());
//                 vaxProtocol.setDoseTarget(getCodeableConcept(DBGetter.GetDefaultForecastCVX(Integer.parseInt(vgs.getId())), vgs.getValue(), "http://hl7.org/fhir/vs/vaccination-protocol-dose-target"));
//                 vaxProtocol.setDoseStatus(getCodeableConcept(aa.getEvaluationStatus(), aa.getEvaluationStatus(), "http://hl7.org/fhir/vs/vaccination-protocol-dose-status"));
//                 vaxProtocol.setDoseStatusReason(getCodeableConcept("reasonCode", aa.getEvaluationReason(), "http://hl7.org/fhir/vs/vaccination-protocol-dose-status-reason"));
//                 imm.getVaccinationProtocol().add(vaxProtocol);
//               }
//             }
//          }
//        }
//
//        BundleEntryComponent immEntry = new BundleEntryComponent();
//        
//        // Set identifier and Meta
//        imm.setId(getUUID());
//				ResourceMetaComponent immEntryMeta = new ResourceMetaComponent();
//			  immEntryMeta.setVersionId("1");
//				immEntryMeta.setLastUpdated(updatedTime);
//				imm.setMeta(immEntryMeta);
//
//        // Set subject - reference to Patient resource
//        patientReference = new Reference();
//        patientReference.setReference(patientId);
//        imm.setSubject(patientReference);
//
//        immEntry.setResource(imm);
//        forecastBundle.getEntry().add(immEntry);
//      }
//      
//      // FHIR ImmunizationRecommendation resource(s) - next entry(s) (create Entries from forecast results)
//      ImmunizationRecommendation ir = new ImmunizationRecommendation();
//      for (Forecast f : result.getVaccineGroupForecast()) {
//        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent irrc =  new ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent();
//        
//        irrc.setDate(updatedTime);
//        
//        irrc.setVaccineType(getCodeableConcept(DBGetter.GetDefaultForecastCVX(f.getVaccineGroupId()), DBGetter.GetVaccineGroupName(f.getVaccineGroupId()), "http://hl7.org/fhir/v3/vs/VaccineType"));
//        irrc.setDoseNumber(f.getTargetDoseNumber());
//        irrc.setForecastStatus(getCodeableConcept(f.getStatus(), f.getStatus(), "http://hl7.org/fhir/vs/immunization-recommendation-status"));
//
//        // Earliest Date
//        if(f.getEarliestDate() != null)
//          irrc.getDateCriterion().add(getDateCriterionComponent("valid", "Earliest Date", f.getEarliestDate()));
//
//        // Recommended Date
//        if(f.getAdjustedRecommendedDate() != null)
//          irrc.getDateCriterion().add(getDateCriterionComponent("due", "Recommended Date", f.getAdjustedRecommendedDate()));
//
//        // Past Due Date
//        if(f.getAdjustedPastDueDate() != null)
//          irrc.getDateCriterion().add(getDateCriterionComponent("overdue", "Past Due Date", f.getAdjustedPastDueDate()));
//
//        // Latest Date
//        if(f.getLatestDate() != null)
//          irrc.getDateCriterion().add(getDateCriterionComponent("latest", "Latest Date", f.getLatestDate()));
//        
//        ir.getRecommendation().add(irrc);
//      }
//        
//      // Set identifier
//      ir.setId(getUUID());
//      ResourceMetaComponent	irMeta = new ResourceMetaComponent();
//      irMeta.setVersionId("1");
//      irMeta.setLastUpdated(updatedTime);
//      ir.setMeta(irMeta);
//
//      // Set subject - reference to Patient resource
//      patientReference = new Reference();
//      patientReference.setReference(patientId);
//      ir.setSubject(patientReference);
//
//      BundleEntryComponent immRecEntry = new BundleEntryComponent();
//      immRecEntry.setResource(ir);
//      forecastBundle.getEntry().add(immRecEntry);
//            
//    } catch (Exception e) {
//      e.printStackTrace();
//      // log.severe(e.getMessage());
//      // Exception caught
//      forecastResult.setBundle(null);
//      forecastResult.setResource(null);
//      forecastResult.setResponseStatus(Response.Status.INTERNAL_SERVER_ERROR);
//      forecastResult.setMessage(e.getMessage());
//    }
//    return forecastResult;
//  }
//  
//  
//  private CDSiScenario makeCDSiScenario(Patient patient, List<Immunization> immunizationRequestList) throws IOException {
//    CDSiScenario scenario = new CDSiScenario();
//    try {
//      // For some reason my dates were jumping 1 month.  Hack to avoid this.  Investigate some other day
//      String year  = "" + patient.getBirthDate().getYear();
//      String month = patient.getBirthDate().getMonth() < 10 ? "0" + patient.getBirthDate().getMonth() : "" + patient.getBirthDate().getMonth();
//      String day   =  patient.getBirthDate().getDay() < 10 ? "0" + patient.getBirthDate().getDay() : "" + patient.getBirthDate().getDay();
//      
//      scenario.setDob(year+month+day);
//      scenario.setGender(patient.getGender().toCode().substring(0, 1).toUpperCase());
//      scenario.setAssessmentDate(Calendar.getInstance().getTime());
//
//      int i = 1;
//      for(Immunization imm : immunizationRequestList)
//      {
//        String cvx = imm.getVaccineType().getCoding().get(0).getCode();
//        year  = "" + imm.getDate().getYear();
//        month = imm.getDate().getMonth() < 10 ? "0" + imm.getDate().getMonth() : "" + imm.getDate().getMonth();
//        day   =  imm.getDate().getDay() < 10 ? "0" + imm.getDate().getDay() : "" + imm.getDate().getDay();
//        scenario.addVaccineDoseAdministered(cvx, null, year+month+day, i++);
//      }
//    }
//    catch (Exception e) {
//      throw new IOException("We blew up saving the scenario.<br>" + e);
//    }
//    
//    return scenario;
//  }
//
//    
//  private String getUUID()
//  {
//    return UUID.randomUUID().toString();
//  }
//
//  private CodeableConcept getCodeableConcept(String code, String display, String system) {
//    CodeableConcept cc = new CodeableConcept();
//    Coding coding = new Coding();
//    coding.setCode(code);
//    coding.setDisplay(display);
//    coding.setSystem(system);
//    cc.getCoding().add(coding);
//    return cc;
//  }
//
//  private ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent getDateCriterionComponent(String code, String display, Date forecastDate) {
//    CodeableConcept dateType = getCodeableConcept(code, display, "http://hl7.org/fhir/immunization-recommendation-date-criterion");
//    DateTimeType dtt = new DateTimeType();
//    dtt.setValue(new DateAndTime(forecastDate));
//    return new ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent(dateType, dtt);
//  }
//
//}
