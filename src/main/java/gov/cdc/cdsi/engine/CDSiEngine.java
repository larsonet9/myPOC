/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class CDSiEngine {

  // Chapter 4 of the Logic Specification
  public static CDSiResult process(CDSiScenario scenario) throws Exception
  {
    // 4.1 Gather Necessary Data
    // This step came from the user input on the screen or a external call.

    // 8.2 Organize Immunization History
    CDSiPatientData patientData = PatientData.organizeImmunizationHistory(scenario);

    // 4.3 Create Relevant Patient Series
    List<CDSiPatientSeries> patientSeries = SupportingData.getPatientSeries(scenario.getVaccineGroupId(), patientData, scenario.getAssessmentDate());


    // 4.4 Evaluate and Forecast all Patient Series
    for(CDSiPatientSeries ps : patientSeries)
    {
      CDSiEvaluator.evaluateDosesAdministeredAgainstPatientSeries(ps);
      CDSiForecaster.forecastNextDose(ps);
    }

    // 4.5 For Each Antigen, Select the best Patient Series
    for(Integer antigenId : getUniqueAntigens(patientSeries)) {
      CDSiBestSeriesSelector.selectBestPatientSeries(getSeriesForAntigen(patientSeries, antigenId));
    }

    // 4.6 For Each Vaccine Group, Identify and Evaluate Vaccine Group
    // This better only iterate once for right now, since the data entry is limited to one vaccine group.
    CDSiResult result = new CDSiResult();
    for(Integer vaccineGroupId : getUniqueVaccineGroups(patientSeries))
      result.addVaccineGroupForecast(CDSiIdentifyAndEvaluateVaccineGroup.createVaccineGroupForecast(getAllBestAntigenSeriesForVaccineGroup(patientSeries, vaccineGroupId.intValue())));

    result.setPatientSeries(patientSeries);

    return result;
  }

  ////////////////////
  // Helper Methods //
  ////////////////////

  private static List<Integer> getUniqueAntigens(List<CDSiPatientSeries> patientSeries) {
    List<Integer> antigens = new ArrayList();
    for(CDSiPatientSeries ps : patientSeries) {
      Integer ant = ps.getAntigenId();
      if(!antigens.contains(ant))
      {
        antigens.add(ant);
      }
    }
    return antigens;
  }

  private static List<CDSiPatientSeries> getSeriesForAntigen(List<CDSiPatientSeries> psList, int antigenToScore) {
    List<CDSiPatientSeries> retList = new ArrayList();
    for(CDSiPatientSeries ps : psList) {
      if(ps.getAntigenId() == antigenToScore)
        retList.add(ps);
    }
    return retList;
  }

  private static List<Integer> getUniqueVaccineGroups(List<CDSiPatientSeries> patientSeries) {
    List<Integer> lstVG = new ArrayList();
    for(CDSiPatientSeries ps : patientSeries) {
      Integer ant = ps.getVaccineGroupId();
      if(!lstVG.contains(ant))
      {
        lstVG.add(ant);
      }
    }
    return lstVG;
  }

  private static List<CDSiPatientSeries> getAllBestAntigenSeriesForVaccineGroup(List<CDSiPatientSeries> psList, int vaccineGroupToForecast) {
    List<CDSiPatientSeries> retList = new ArrayList();
    for(CDSiPatientSeries ps : psList) {
      if(ps.getVaccineGroupId() == vaccineGroupToForecast && ps.isBestSeries())
        retList.add(ps);
    }
    return retList;
  }


}
