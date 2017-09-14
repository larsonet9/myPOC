/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.testcase;

import gov.cdc.cdsi.db.DBHelper;
import gov.cdc.cdsi.engine.CDSiPatientData.AntigenAdministered;
import gov.cdc.cdsi.engine.CDSiPatientData.Forecast;
import gov.cdc.cdsi.engine.CDSiPatientSeries;
import gov.cdc.cdsi.engine.CDSiResult;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class TestCaseResult {

  public static void writeCDSiResult(CDSiResult result, String testCaseId, String vaccineGroupId) throws Exception {
    String SQL = "insert into cdsi_test_case_result values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement pstmt = DBHelper.dbConn().prepareStatement(SQL);

    // Preset some values as they may not be set later.
    pstmt.setString(3, null);
    pstmt.setString(4, null);
    pstmt.setString(5, null);
    pstmt.setString(6, null);
    pstmt.setString(7, null);
    pstmt.setString(8, null);
    pstmt.setString(9, null);
    pstmt.setString(10, null);
    pstmt.setString(11, null);
    pstmt.setString(12, null);
    pstmt.setString(13, null);
    pstmt.setString(14, null);
    pstmt.setString(15, null);
    pstmt.setString(16, null);
    pstmt.setString(17, "-");
    pstmt.setString(18, "-");
    pstmt.setDate(19, null);
    pstmt.setDate(20, null);
    pstmt.setDate(21, null);

    CDSiPatientSeries ps = result.getBestPatientSeries(Integer.parseInt(vaccineGroupId));
    pstmt.setString(1,testCaseId);

    // Eval Results. Start at 3
    int evalResults = 3;
    for(AntigenAdministered aa : ps.getPatientData().getAntigenAdministeredList()) {
      pstmt.setString(evalResults++, aa.getEvaluationStatus());
      pstmt.setString(evalResults++, aa.getEvaluationReason());
    }

    // Forecast Results
    Forecast forecast = result.getVaccineGroupForecast(ps.getVaccineGroupId());
    pstmt.setString(2,forecast.getStatus());
    if(forecast.hasForecast()) {
      pstmt.setString(17, "" + forecast.getTargetDoseNumber());
      pstmt.setString(18, "" + (ps.getPatientData().getCountOfValidDoses() + 1));
      pstmt.setDate(19, new java.sql.Date(forecast.getEarliestDate().getTime()));
      pstmt.setDate(20, new java.sql.Date(forecast.getAdjustedRecommendedDate().getTime()));
      if(forecast.getAdjustedPastDueDate() != null)
        pstmt.setDate(21, new java.sql.Date(forecast.getAdjustedPastDueDate().getTime()));
    }
    pstmt.setString(22, forecast.getReason());

    pstmt.executeUpdate();

    DBHelper.disconnect(null, pstmt, null);

  }

  public static void purgeResultsTable() throws Exception {
    String SQL = "truncate table cdsi_test_case_result";
    Statement stmt = DBHelper.dbConn().createStatement();

    stmt.executeUpdate(SQL);
    DBHelper.disconnect(null, stmt, null);
  }

  public static List<ResultData> getCDSiResults() throws Exception
  {
    List<ResultData> rdList = new ArrayList();

    String SQL =
    "  select exp.cdsi_test_id, exp.test_case_name,  " +
    "         exp.forecast_vda_number, exp.earliest_date, exp.recommended_date, exp.past_due_date, exp.series_status, exp.forecast_test_type,  " +
    "         act.forecast_td_number,  act.earliest_date, act.recommended_date, act.past_due_date, act.series_status, act.forecast_reason, " +
    "         (select distinct exp.eval_status_1 from vaccine v, vaccine_makeup vm where v.vaccine_id = vm.vaccine_id and vm.vaccine_group_id = vg.vaccine_group_id and v.cvx = exp.cvx_1) evalstatus1, " +
    "         (select distinct exp.eval_status_2 from vaccine v, vaccine_makeup vm where v.vaccine_id = vm.vaccine_id and vm.vaccine_group_id = vg.vaccine_group_id and v.cvx = exp.cvx_2) evalstatus2, " +
    "         (select distinct exp.eval_status_3 from vaccine v, vaccine_makeup vm where v.vaccine_id = vm.vaccine_id and vm.vaccine_group_id = vg.vaccine_group_id and v.cvx = exp.cvx_3) evalstatus3, " +
    "         (select distinct exp.eval_status_4 from vaccine v, vaccine_makeup vm where v.vaccine_id = vm.vaccine_id and vm.vaccine_group_id = vg.vaccine_group_id and v.cvx = exp.cvx_4) evalstatus4, " +
    "         (select distinct exp.eval_status_5 from vaccine v, vaccine_makeup vm where v.vaccine_id = vm.vaccine_id and vm.vaccine_group_id = vg.vaccine_group_id and v.cvx = exp.cvx_5) evalstatus5, " +
    "         (select distinct exp.eval_status_6 from vaccine v, vaccine_makeup vm where v.vaccine_id = vm.vaccine_id and vm.vaccine_group_id = vg.vaccine_group_id and v.cvx = exp.cvx_6) evalstatus6, " +
    "         (select distinct exp.eval_status_7 from vaccine v, vaccine_makeup vm where v.vaccine_id = vm.vaccine_id and vm.vaccine_group_id = vg.vaccine_group_id and v.cvx = exp.cvx_7) evalstatus7, " +
    "         act.eval_status_1, act.eval_status_2, act.eval_status_3, act.eval_status_4, act.eval_status_5, act.eval_status_6, act.eval_status_7 " +
    "    from cdsi_test_case exp,  " +
    "         cdsi_test_case_result act, " +
    "         vaccine_group vg " +
    "   where exp.cdsi_test_id = act.cdsi_test_id  " +
    "     and vg.test_case_name = exp.vaccine_group " +
    "    order by exp.cdsi_test_id ";

    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      do {
        ResultData rd = new ResultData();
        rd.setTestId(rs.getString(1));
        rd.setTestCaseName(rs.getString(2));
        rd.setExpectedForecast(rs.getString(3), rs.getDate(4), rs.getDate(5), rs.getDate(6), rs.getString(7), rs.getString(8));
        rd.setActualForecast(rs.getString(9), rs.getDate(10), rs.getDate(11), rs.getDate(12), rs.getString(13), rs.getString(14));

        // Actual Evaluation
        for (int i = 15; i < 22; i++){
          if(rs.getString(i) != null)
            rd.addActualEvaluation(rs.getString(i));
        }

        // Expected Evaluation
        for (int i = 22; i < 29; i++){
          if(rs.getString(i) != null)
            rd.addExpectedEvaluation(rs.getString(i));
        }

        rdList.add(rd);
      } while(rs.next());
    }
    DBHelper.disconnect(null, stmt, rs);

    return rdList;
  }
}
