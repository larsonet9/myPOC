/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cdc.cdsi.testcases.load;

import gov.cdc.cdsi.db.DBHelper;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Eric
 */
public class TCLoader {
  private static final int cdsi_test_id_XLS_COL = 4;
  private static final int test_case_name_XLS_COL = 5;
  private static final int dob_XLS_COL = 6;
  private static final int gender_XLS_COL = 8;
  private static final int med_history_text_XLS_COL = 9;
  private static final int med_history_code_XLS_COL = 10;
  private static final int med_history_code_sys_XLS_COL = 11;
  private static final int series_status_XLS_COL = 12;
  private static final int date_admin_1_XLS_COL = 14;
  private static final int vac_name_1_XLS_COL = 16;
  private static final int cvx_1_XLS_COL = 17;
  private static final int mvx_1_XLS_COL = 18;
  private static final int eval_status_1_XLS_COL = 22;
  private static final int eval_reason_1_XLS_COL = 24;
  private static final int date_admin_2_XLS_COL = 27;
  private static final int vac_name_2_XLS_COL = 31;
  private static final int cvx_2_XLS_COL = 32;
  private static final int mvx_2_XLS_COL = 33;
  private static final int eval_status_2_XLS_COL = 37;
  private static final int eval_reason_2_XLS_COL = 39;
  private static final int date_admin_3_XLS_COL = 42;
  private static final int vac_name_3_XLS_COL = 47;
  private static final int cvx_3_XLS_COL = 48;
  private static final int mvx_3_XLS_COL = 49;
  private static final int eval_status_3_XLS_COL = 53;
  private static final int eval_reason_3_XLS_COL = 55;
  private static final int date_admin_4_XLS_COL = 58;
  private static final int vac_name_4_XLS_COL = 62;
  private static final int cvx_4_XLS_COL = 63;
  private static final int mvx_4_XLS_COL = 64;
  private static final int eval_status_4_XLS_COL = 68;
  private static final int eval_reason_4_XLS_COL = 70;
  private static final int date_admin_5_XLS_COL = 73;
  private static final int vac_name_5_XLS_COL = 77;
  private static final int cvx_5_XLS_COL = 78;
  private static final int mvx_5_XLS_COL = 79;
  private static final int eval_status_5_XLS_COL = 83;
  private static final int eval_reason_5_XLS_COL = 85;
  private static final int date_admin_6_XLS_COL = 88;
  private static final int vac_name_6_XLS_COL = 92;
  private static final int cvx_6_XLS_COL = 93;
  private static final int mvx_6_XLS_COL = 94;
  private static final int eval_status_6_XLS_COL = 98;
  private static final int eval_reason_6_XLS_COL = 100;
  private static final int date_admin_7_XLS_COL = 103;
  private static final int vac_name_7_XLS_COL = 107;
  private static final int cvx_7_XLS_COL = 108;
  private static final int mvx_7_XLS_COL = 109;
  private static final int eval_status_7_XLS_COL = 113;
  private static final int eval_reason_7_XLS_COL = 115;
  private static final int forecast_vda_number_XLS_COL = 119;
  private static final int earliest_date_XLS_COL = 120;
  private static final int recommended_date_XLS_COL = 125;
  private static final int past_due_date_XLS_COL = 128;
  private static final int vaccine_group_XLS_COL = 133;
  private static final int assessment_date_XLS_COL = 135;
  private static final int evaluation_test_type_XLS_COL = 138;
  private static final int date_added_XLS_COL = 140;
  private static final int date_updated_XLS_COL = 141;
  private static final int forecast_test_type_XLS_COL = 142;
  private static final int reason_for_change_XLS_COL = 144;
  private static final int changed_in_version_XLS_COL = 145;
  
  

  public void loadTestCases(InputStream is) throws Exception {

    Connection conn = DBHelper.dbConn();
    conn.setAutoCommit(true);

    Statement stmt = conn.createStatement();
    stmt.execute("delete from cdsi_test_case");
    stmt.close();
            
    // Prepare the insert statement
    PreparedStatement psTestCase = conn.prepareStatement(SQLInserts.insTestCase);
    

    Workbook wbook = new XSSFWorkbook(is);
    Sheet sheet = wbook.getSheetAt(0);
    for (Row row : sheet) 
    {
      if(row.getCell(0) != null &&
         row.getCell(0).getStringCellValue().equalsIgnoreCase("FINAL"))
      {
        insertTestCase(psTestCase, row);
        System.out.println("Test Case " + row.getCell(4).getStringCellValue() + " loaded.");
      }
    }
    psTestCase.close();

  }

  private void insertTestCase(PreparedStatement ps, Row row) throws SQLException, Exception
  {
    int i = 1;
    ps.setString(i++, row.getCell(cdsi_test_id_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(test_case_name_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(dob_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(gender_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(med_history_text_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(med_history_code_XLS_COL)));
    ps.setString(i++, row.getCell(med_history_code_sys_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(series_status_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(date_admin_1_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(vac_name_1_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(cvx_1_XLS_COL)));
    ps.setString(i++, row.getCell(mvx_1_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_status_1_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_reason_1_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(date_admin_2_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(vac_name_2_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(cvx_2_XLS_COL)));
    ps.setString(i++, row.getCell(mvx_2_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_status_2_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_reason_2_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(date_admin_3_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(vac_name_3_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(cvx_3_XLS_COL)));
    ps.setString(i++, row.getCell(mvx_3_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_status_3_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_reason_3_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(date_admin_4_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(vac_name_4_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(cvx_4_XLS_COL)));
    ps.setString(i++, row.getCell(mvx_4_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_status_4_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_reason_4_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(date_admin_5_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(vac_name_5_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(cvx_5_XLS_COL)));
    ps.setString(i++, row.getCell(mvx_5_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_status_5_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_reason_5_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(date_admin_6_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(vac_name_6_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(cvx_6_XLS_COL)));
    ps.setString(i++, row.getCell(mvx_6_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_status_6_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_reason_6_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(date_admin_7_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(vac_name_7_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(cvx_7_XLS_COL)));
    ps.setString(i++, row.getCell(mvx_7_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_status_7_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(eval_reason_7_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(forecast_vda_number_XLS_COL)));
    ps.setDate(i++, getDate(row.getCell(earliest_date_XLS_COL).getDateCellValue()));
    ps.setDate(i++, getDate(row.getCell(recommended_date_XLS_COL).getDateCellValue()));
    ps.setDate(i++, getDate(row.getCell(past_due_date_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(vaccine_group_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(assessment_date_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(evaluation_test_type_XLS_COL).getStringCellValue());
    ps.setDate(i++, getDate(row.getCell(date_added_XLS_COL).getDateCellValue()));
    ps.setDate(i++, getDate(row.getCell(date_updated_XLS_COL).getDateCellValue()));
    ps.setString(i++, row.getCell(forecast_test_type_XLS_COL).getStringCellValue());
    ps.setString(i++, row.getCell(reason_for_change_XLS_COL).getStringCellValue());
    ps.setString(i++, getCellValue(row.getCell(changed_in_version_XLS_COL)));
    ps.execute();
  }
  
  private Date getDate(java.util.Date utilDate) throws Exception {
    return utilDate != null ? new java.sql.Date(utilDate.getTime()) : null;
  }

  private String getCellValue(Cell cell) {
    try {
      return cell.getStringCellValue();
    }
    catch (IllegalStateException ise) {
      
      return "" + (int)cell.getNumericCellValue();
    }
  }
  
}
