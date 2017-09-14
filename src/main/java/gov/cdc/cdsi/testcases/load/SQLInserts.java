/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.testcases.load;

/**
 *
 * @author eric
 */
public class SQLInserts {
  public static final String insTestCase =
    " insert into cdsi_test_case                                           " +
    "  (cdsi_test_id, test_case_name, dob, gender, med_history_text,       " +
    "   med_history_code, med_history_code_sys, series_status,             " +
    "   date_admin_1, vac_name_1, cvx_1, mvx_1,                            " + 
    "   eval_status_1, eval_reason_1,                                      " +
    "   date_admin_2, vac_name_2, cvx_2, mvx_2,                            " + 
    "   eval_status_2, eval_reason_2,                                      " +
    "   date_admin_3, vac_name_3, cvx_3, mvx_3,                            " + 
    "   eval_status_3, eval_reason_3,                                      " +
    "   date_admin_4, vac_name_4, cvx_4, mvx_4,                            " + 
    "   eval_status_4, eval_reason_4,                                      " +
    "   date_admin_5, vac_name_5, cvx_5, mvx_5,                            " + 
    "   eval_status_5, eval_reason_5,                                      " +
    "   date_admin_6, vac_name_6, cvx_6, mvx_6,                            " + 
    "   eval_status_6, eval_reason_6,                                      " +
    "   date_admin_7, vac_name_7, cvx_7, mvx_7,                            " + 
    "   eval_status_7, eval_reason_7,                                      " +
    "   forecast_vda_number, earliest_date, recommended_date,              " +
    "   past_due_date, vaccine_group, assessment_date,                     " +
    "   evaluation_test_type, date_added, date_updated,                    " +
    "   forecast_test_type, reason_for_change, changed_in_version)         " +
    "  values (                                                            " +
    "  ?,                                                                  " + // cdsi_test_id
    "  ?,                                                                  " + // test_case_name
    "  ?,                                                                  " + // dob
    "  ?,                                                                  " + // gender
    "  ?,                                                                  " + // med_history_text
    "  ?,                                                                  " + // med_history_code
    "  ?,                                                                  " + // med_history_code_sys
    "  ?,                                                                  " + // series_status
    "  ?,                                                                  " + // date_admin_1
    "  ?,                                                                  " + // vac_name_1
    "  ?,                                                                  " + // cvx_1
    "  ?,                                                                  " + // mvx_1
    "  ?,                                                                  " + // eval_status_1
    "  ?,                                                                  " + // eval_reason_1
    "  ?,                                                                  " + // date_admin_2
    "  ?,                                                                  " + // vac_name_2
    "  ?,                                                                  " + // cvx_2
    "  ?,                                                                  " + // mvx_2
    "  ?,                                                                  " + // eval_status_2
    "  ?,                                                                  " + // eval_reason_2
    "  ?,                                                                  " + // date_admin_3
    "  ?,                                                                  " + // vac_name_3
    "  ?,                                                                  " + // cvx_3
    "  ?,                                                                  " + // mvx_3
    "  ?,                                                                  " + // eval_status_3
    "  ?,                                                                  " + // eval_reason_3
    "  ?,                                                                  " + // date_admin_4
    "  ?,                                                                  " + // vac_name_4
    "  ?,                                                                  " + // cvx_4
    "  ?,                                                                  " + // mvx_4
    "  ?,                                                                  " + // eval_status_4
    "  ?,                                                                  " + // eval_reason_4
    "  ?,                                                                  " + // date_admin_5
    "  ?,                                                                  " + // vac_name_5
    "  ?,                                                                  " + // cvx_5
    "  ?,                                                                  " + // mvx_5
    "  ?,                                                                  " + // eval_status_5
    "  ?,                                                                  " + // eval_reason_5
    "  ?,                                                                  " + // date_admin_6
    "  ?,                                                                  " + // vac_name_6
    "  ?,                                                                  " + // cvx_6
    "  ?,                                                                  " + // mvx_6
    "  ?,                                                                  " + // eval_status_6
    "  ?,                                                                  " + // eval_reason_6
    "  ?,                                                                  " + // date_admin_7
    "  ?,                                                                  " + // vac_name_7
    "  ?,                                                                  " + // cvx_7
    "  ?,                                                                  " + // mvx_7
    "  ?,                                                                  " + // eval_status_7
    "  ?,                                                                  " + // eval_reason_7
    "  ?,                                                                  " + // forecast_vda_number
    "  ?,                                                                  " + // earliest_date
    "  ?,                                                                  " + // recommended_date
    "  ?,                                                                  " + // past_due_date
    "  ?,                                                                  " + // vaccine_group
    "  ?,                                                                  " + // assessment_date
    "  ?,                                                                  " + // evaluation_test_type
    "  ?,                                                                  " + // date_added
    "  ?,                                                                  " + // date_updated
    "  ?,                                                                  " + // forecast_test_type
    "  ?,                                                                  " + // reason_for_change
    "  ?)                                                                  ";  // changed_in_version

}
