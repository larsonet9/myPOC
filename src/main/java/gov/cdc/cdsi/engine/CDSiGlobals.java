/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.engine;

/**
 *
 * @author eric
 */
public interface CDSiGlobals {
  // Patient Series Status
  public static final String SERIES_NOT_COMPLETE    = "Not Complete";
  public static final String SERIES_COMPLETE        = "Complete";
  public static final String SERIES_IMMUNE          = "Immune";
  public static final String SERIES_CONTRAINDICATED = "Contraindicated";
  public static final String SERIES_AGED_OUT        = "Aged Out";
  public static final String SERIES_NOT_RECOMMENDED = "Not Recommended";

  // Target Dose Status
  public static final String TARGET_DOSE_NOT_SATISFIED = "Not Satisfied";
  public static final String TARGET_DOSE_SATISFIED     = "Satisfied";
  public static final String TARGET_DOSE_SKIPPED       = "Skipped";
  public static final String TARGET_DOSE_SUBSTITUTED   = "Substituted";
  public static final String TARGET_DOSE_UNNECESSARY   = "Unnecessary";

  // Evaluation Status
  public static final String ANTIGEN_ADMINISTERED_EXTRANEOUS   = "Extraneous";
  public static final String ANTIGEN_ADMINISTERED_NOT_VALID    = "Not Valid";
  public static final String ANTIGEN_ADMINISTERED_VALID        = "Valid";
  public static final String ANTIGEN_ADMINISTERED_SUB_STANDARD = "Sub-standard";

  // Component Status
  public static final int    COMPONENT_STATUS_VALID      = 1;
  public static final int    COMPONENT_STATUS_NOT_VALID  = 2;
  public static final int    COMPONENT_STATUS_EXTRANEOUS = 3;

  // Forecast Reasons
  public static final String FORECAST_REASON_TOO_OLD         = "No Doses Needed. Patient has exceeded the maximum age.";
  public static final String FORECAST_REASON_COMPLETE        = "No Doses Needed. Patient series is complete.";
  public static final String FORECAST_REASON_CONTRAINDICATED = "No Doses forecasted. Patient has a contraindication.";
  public static final String FORECAST_REASON_NOT_RECOMMENDED = "No Doses forecasted. Patient's immunization history is sufficient for immunity.";
  public static final String FORECAST_REASON_PAST_SEASON     = "No Doses forecasted. Past Seasonal Recommendation End Date.";
  

}
