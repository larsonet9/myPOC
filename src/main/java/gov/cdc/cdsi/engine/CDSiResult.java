/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

import gov.cdc.cdsi.engine.CDSiPatientData.Forecast;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class CDSiResult {
  private List<CDSiPatientSeries> patientSeries;
  private List<Forecast> vaccineGroupForecast = new ArrayList();

  public List<CDSiPatientSeries> getPatientSeries() {
    return patientSeries;
  }
  
  public CDSiPatientSeries getBestPatientSeries(int vaccineGroupId) {
    for(CDSiPatientSeries ps : patientSeries)
    {
      if(ps.getVaccineGroupId() == vaccineGroupId &&
         ps.isBestSeries())
        return ps;
    }
    return null;
  }

  public void setPatientSeries(List<CDSiPatientSeries> patientSeries) {
    this.patientSeries = patientSeries;
  }

  public List<Forecast> getVaccineGroupForecast() {
    return vaccineGroupForecast;
  }

  public Forecast getVaccineGroupForecast(int vaccineGroupId) {
    for(Forecast f : vaccineGroupForecast)
    {
      if(f.getVaccineGroupId() == vaccineGroupId)
        return f;
    }
    return null;
  }

  public void addVaccineGroupForecast(Forecast vaccineGroupForecast) {
    this.vaccineGroupForecast.add(vaccineGroupForecast);
  }
  


}
