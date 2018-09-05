/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.supportingdata.load;

/**
 *
 * @author eric
 */
public class SQLInserts {
  public static final String insSeries =
    " insert into sd_series                                                " +
    "  (schedule_id, antigen_id, vaccine_group_id, series_name,            " +
    "   default_series_ind, product_path_ind, series_preference,           " +
    "   max_age_to_start, min_age_to_start, sd_version, upload_date)       " +
    "  values (                                                            " +
    "  ?,                                                                  " + // ScheduleID
    "  (select antigen_id from antigen where sd_name = ?),                 " + // targetDisease
    "  (select vaccine_group_id from vaccine_group where sd_name = ?),     " + // vaccineGroup
    "  ?,                                                                  " + // seriesName
    "  ?,                                                                  " + // defaultSeries
    "  ?,                                                                  " + // productPath
    "  ?,                                                                  " + // seriesPreference
    "  ?,                                                                  " + // maxAgeToStart
    "  ?,                                                                  " + // minAgeToStart
    "  ?,                                                                  " + // sdVersion
    "  now())                                                              ";  // Upload Date

  public static final String insSeriesDose =
    " insert into sd_dose (series_id, dose_number) values(                 " +
    "   ?,                                                                 " + // series_id
    "   ?)                                                                 ";  // doseNumber

  public static final String insDoseAge =
    " insert into sd_age                                                    " +
    "   (dose_id, abs_min_age, min_age, earliest_rec_age,                   " + 
    "    latest_rec_age, max_age, effective_date, cessation_date) values(   " +
    "   ?,                                                                  " + // dose_id
    "   ?,                                                                  " + // absMinAge
    "   ?,                                                                  " + // minAge
    "   ?,                                                                  " + // earliestRecAge
    "   ?,                                                                  " + // latestRecAge
    "   ?,                                                                  " + // maxAge
    "   ?,                                                                  " + // effectiveDate
    "   ?)                                                                  ";  // cessationDate

  public static final String insInterval =
    " insert into sd_interval                                               " +
    "   (dose_id, previous_dose_ind, target_dose_num, abs_min_int, min_int, " +
    "    earliest_rec_int, latest_rec_int, priority_flag, effective_date,   " +
    "    cessation_date) values(                                            " +
    "   ?,                                                                  " + // dose_id
    "   ?,                                                                  " + // fromPrevious
    "   ?,                                                                  " + // fromTargetDose
    "   ?,                                                                  " + // absMinInt
    "   ?,                                                                  " + // minInt
    "   ?,                                                                  " + // earliestRecInt
    "   ?,                                                                  " + // latestRecInt
    "   ?,                                                                  " + // priorityFlag
    "   ?,                                                                  " + // effectiveDate
    "   ?)                                                                  ";  // cessationDate
  
  public static final String insIntervalFMRVaccine = 
    " insert into sd_interval_fmr_vaccine                                   " +
    "   (interval_id, vaccine_id) values (                                  " +
    "   ?,                                                                  " + // interval_id
    "   (select vaccine_id from vaccine where cvx = ?))                     ";  // fromMostRecent\n" +

  public static final String insAllowableInterval =
    " insert into sd_allowable_interval                                     " +
    "   (dose_id, previous_dose_ind, target_dose_num, abs_min_int,          " +
    "    effective_date, cessation_date) values(                            " +
    "   ?,                                                                  " + // dose_id
    "   ?,                                                                  " + // fromPrevious
    "   ?,                                                                  " + // fromTargetDose
    "   ?,                                                                  " + // absMinInt
    "   ?,                                                                  " + // effectiveDate
    "   ?)                                                                  ";  // cessationDate

  public static final String insPrefVaccine =
    " insert into sd_preferable_vaccine                                     " +
    "   (dose_id, vaccine_id, begin_age, end_age, manufacturer_id, volume,  " +
    "   forecast_vaccine_type) values(                                      " +
    "   ?,                                                                  " + // dose_id
    "   (select vaccine_id from vaccine where cvx = ?),                     " + // cvx
    "   ?,                                                                  " + // beginAge
    "   ?,                                                                  " + // endAge
    "   (select manufacturer_id from manufacturer where mvx = ?),           " + // mvx
    "   ?,                                                                  " + // volume
    "   ?)                                                                  ";  // forecastVaccineType

  public static final String insAllowVaccine =
    " insert into sd_allowable_vaccine                                      " +
    "   (dose_id, vaccine_id, begin_age, end_age) values(                   " +
    "   ?,                                                                  " + // dose_id
    "   (select vaccine_id from vaccine where cvx = ?),                     " + // cvx
    "   ?,                                                                  " + // beginAge
    "   ?)                                                                  ";  // endAge

  public static final String insInadvertentVaccine =
    " insert into sd_inadvertent_vaccine                                    " +
    "   (dose_id, vaccine_id) values(                                       " +
    "   ?,                                                                  " + // dose_id
    "   (select vaccine_id from vaccine where cvx = ?))                     ";  // cvx

  public static final String insRecurringDose =
    " insert into sd_recurring_dose (dose_id, recurring_ind) values(        " +
    "   ?,                                                                  " + // dose_id
    "   'Y')                                                                ";  // recurring_ind

  public static String insSeasonal =
    " insert into sd_season_recommendation                                  " +
    "   (dose_id, start_date, end_date) values(                             " +
    "   ?,                                                                  " + // dose_id
    "   ?,                                                                  " + // startDate
    "   ?)                                                                  ";  // endDate

  public static String insGender =
    " insert into sd_gender (series_id, required_gender) values(            " +
    "   ?,                                                                  " + // series_id
    "   ?)                                                                  ";  // requiredGender
  
  public static String insConditionalSkip =
    " insert into sd_conditional_skip (dose_id, set_logic, context) values( " +
    "   ?,                                                                  " + // dose_id
    "   ?,                                                                  " + // set_Logic
    "   ?)                                                                  ";  // context

  public static String insCSSet =
    " insert into sd_cs_set (conditional_skip_id, condition_logic,          " +
    "   description, effective_date, cessation_date) values(                " +
    "   ?,                                                                  " + // conditional_skip_id
    "   ?,                                                                  " + // condition_logic
    "   ?,                                                                  " + // description
    "   ?,                                                                  " + // effective_date
    "   ?)                                                                  ";  // cessation_date

  public static String insCSCondition =
    " insert into sd_cs_condition (cs_set_id, condition_type, start_date,   " +
    "   end_date, begin_age, end_age, min_interval, dose_count, dose_type,  " +
    "   dose_count_logic) values(                                           " +
    "   ?,                                                                  " + // cs_set_id
    "   ?,                                                                  " + // condition_type
    "   ?,                                                                  " + // start_date
    "   ?,                                                                  " + // end_date
    "   ?,                                                                  " + // begin_age
    "   ?,                                                                  " + // end_age
    "   ?,                                                                  " + // min_interval
    "   ?,                                                                  " + // dose_count
    "   ?,                                                                  " + // dose_type
    "   ?)                                                                  ";  // dose_count_logic

  public static String insCSCVaccine =
    " insert into sd_csc_vaccine (cs_condition_id, vaccine_id) values(         " +
    "   ?,                                                                  " + // cs_condition_id
    "   (select vaccine_id from vaccine where cvx = ?))                     ";  // cvx

}
