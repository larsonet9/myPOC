/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.supportingdata.load;

import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.Age;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.AllowableInterval;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.AllowableVaccine;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.InadvertentVaccine;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.Interval;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.PreferableVaccine;
import gov.cdc.cdsi.supportingdata.mapping.AntigenSupportingData.Series.SeriesDose.SeasonalRecommendation;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 *
 * @author eric
 */
public class SDLoaderUtil {

  protected static boolean isEmpty(Age age) {
    return (age == null || 
           (isEmpty(age.getAbsMinAge()) && isEmpty(age.getMinAge()) && isEmpty(age.getEarliestRecAge()) && isEmpty(age.getLatestRecAge()) && isEmpty(age.getMaxAge())));
  }

  protected static boolean isEmpty(Interval interval) {
    return (interval == null || isEmpty(interval.getFromPrevious()));
  }

  protected static boolean isEmpty(AllowableInterval ainterval) {
    return (ainterval == null || isEmpty(ainterval.getFromPrevious()));
  }

  protected static boolean isEmpty(AllowableVaccine allow) {
    return (allow == null || isEmpty(allow.getCvx()));
  }
  
  protected static boolean isEmpty(PreferableVaccine pref) {
    return (pref == null || isEmpty(pref.getCvx()));
  }

  protected static boolean isEmpty(InadvertentVaccine inadvert) {
    return (inadvert == null || isEmpty(inadvert.getCvx()));
  }

  protected static boolean isEmpty(SeasonalRecommendation seasonalRecommendation) {
    return (seasonalRecommendation == null || isEmpty(seasonalRecommendation.getStartDate()));
  }

  protected static boolean isEmpty(String str) {
    return (str == null || str.isEmpty());
  }

  protected static boolean isRecurringDose(String recurringDose) {
   return (recurringDose != null &&
           (recurringDose.equalsIgnoreCase("Yes") ||
            recurringDose.equalsIgnoreCase("Y")));
  }

  protected static String getFirstChar(String str) {
    String ret = "";
    if(str.length() > 0)
      ret = str.substring(0, 1).toUpperCase();

    return ret;
  }

  protected static String cleanNA(String str) {
    String ret = null;
    if(str != null) {
      if(isNA(str))
        ret = null;
      else
        ret = str;
    }

    return ret;
  }

  protected static String cleanNAorUnbound(String str) {
    String ret = null;
    if(str != null) {
      if(isNAorUnbound(str))
        ret = null;
      else
        ret = str;
    }

    return ret;
  }

  protected static boolean isNA(String str) {
    return (str.equalsIgnoreCase("n/a") || str.equalsIgnoreCase("na") || str.isEmpty());
  }
  
  protected static boolean isNAorUnbound(String str) {
    return isNA(str) || str.equalsIgnoreCase("nolimit") || str.equalsIgnoreCase("currentAge");
  }

  protected static Date getDate(String strDate) throws Exception {
    return new java.sql.Date(new SimpleDateFormat("yyyyMMdd").parse(strDate).getTime());
  }





}
