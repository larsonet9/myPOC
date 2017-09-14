/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.supportingdata.load;

import gov.cdc.cdsi.db.DBHelper;
import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author eric
 */
public class SDUnloader {

  protected static void deleteAllSeriesForAntigen(String antigen) throws Exception {
    Connection conn = DBHelper.dbConn();
    conn.setAutoCommit(false);
    Statement stmt = conn.createStatement();

    // Delete the four lower level tables under Dose concepts
    stmt.executeUpdate(buildSQL(SQLDeletes.delCSCVaccine,  antigen, ""));
    stmt.executeUpdate(buildSQL(SQLDeletes.delCSCondition, antigen, ""));
    stmt.executeUpdate(buildSQL(SQLDeletes.delCSSet,       antigen, ""));
    stmt.executeUpdate(buildSQL(SQLDeletes.delFMRVaccine,  antigen, ""));
    
    // Delete all the tables directly under sd_dose (age, interval, etc...)
    for(String tbl : SQLDeletes.lstDoseRuleTables) {
      stmt.executeUpdate(buildSQL(SQLDeletes.delDoseRules, antigen, tbl));
    }

    // Delete the Doses and finally the series
    stmt.executeUpdate(buildSQL(SQLDeletes.delDose,   antigen, ""));
    stmt.executeUpdate(buildSQL(SQLDeletes.delSeries, antigen, ""));

    // Commit and close
    conn.commit();
    DBHelper.disconnect(conn, stmt, null);
  }

  private static String buildSQL(String sqlStmt, String antigen, String table) {
    CharSequence csOld = "REPLACE~ANTIGEN";
    CharSequence csNew = antigen;
    String       sql   = sqlStmt.replace(csOld, csNew);

    csOld = "REPLACE~TABLE";
    csNew = table;
    return sql.replace(csOld, csNew);
  }
}