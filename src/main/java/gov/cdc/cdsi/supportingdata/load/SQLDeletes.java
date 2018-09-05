/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.supportingdata.load;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author eric
 */
public class SQLDeletes {


public  static final String delCSCVaccine  = "delete from sd_csc_vaccine where cs_condition_id in (select cs_condition_id from sd_cs_condition where cs_set_id in (select cs_set_id from sd_cs_set where conditional_skip_id in (select conditional_skip_id from sd_conditional_skip where dose_id in (select dose_id from sd_dose where series_id in (select series_id from sd_series where antigen_id = (select antigen_id from antigen where sd_name = 'REPLACE~ANTIGEN'))))))";
public  static final String delCSCondition = "delete from sd_cs_condition where cs_set_id in (select cs_set_id from sd_cs_set where conditional_skip_id in (select conditional_skip_id from sd_conditional_skip where dose_id in (select dose_id from sd_dose where series_id in (select series_id from sd_series where antigen_id = (select antigen_id from antigen where sd_name = 'REPLACE~ANTIGEN')))))";
public  static final String delCSSet       = "delete from sd_cs_set where conditional_skip_id in (select conditional_skip_id from sd_conditional_skip where dose_id in (select dose_id from sd_dose where series_id in (select series_id from sd_series where antigen_id = (select antigen_id from antigen where sd_name = 'REPLACE~ANTIGEN'))))";

public  static final String delFMRVaccine  = "delete from sd_interval_fmr_vaccine where interval_id in (select interval_id from sd_interval where dose_id in (select dose_id from sd_dose where series_id in (select series_id from sd_series where antigen_id = (select antigen_id from antigen where sd_name = 'REPLACE~ANTIGEN'))))";

private static final String delRuleClause = " where dose_id in (select dose_id from sd_dose where series_id in (select series_id from sd_series where antigen_id = (select antigen_id from antigen where sd_name = 'REPLACE~ANTIGEN')))";
public  static final String delDoseRules  = "delete from REPLACE~TABLE " + delRuleClause;

public  static final String delDose     = "delete from sd_dose   where series_id in (select series_id from sd_series where antigen_id = (select antigen_id from antigen where sd_name = 'REPLACE~ANTIGEN'))";
public  static final String delGender   = "delete from sd_gender where series_id in (select series_id from sd_series where antigen_id = (select antigen_id from antigen where sd_name = 'REPLACE~ANTIGEN'))";
public  static final String delSeries   = "delete from sd_series where antigen_id = (select antigen_id from antigen where sd_name = 'REPLACE~ANTIGEN')";

public  static final List<String> lstDoseRuleTables = 
    Arrays.asList("sd_conditional_skip",
                  "sd_recurring_dose",
                  "sd_interval",
                  "sd_allowable_interval",
                  "sd_season_recommendation",
                  "sd_inadvertent_vaccine",
                  "sd_allowable_vaccine",
                  "sd_preferable_vaccine",
                  "sd_age");
}
