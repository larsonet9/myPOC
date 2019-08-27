/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.db;

import gov.cdc.cdsi.engine.CDSiPatientData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author eric
 */
public class DBGetter {

  public static List<NameValuePair> getVaccineGroups() throws Exception
  {
    List<NameValuePair> vgList = new ArrayList();

    String SQL = "SELECT distinct vg.vaccine_group_id, vg.short_name from vaccine_group vg, sd_series sds where vg.vaccine_group_id = sds.vaccine_group_id order by short_name asc";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    while(rs.next())
    {
      vgList.add(new NameValuePair(rs.getString(1), rs.getString(2)));
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return vgList;
  }

  public static List<NameValuePair> getPatientObservations() throws Exception
  {
    List<NameValuePair> nvList = new ArrayList();
    String SQL = "SELECT observation_code, concat('(', observation_code, ') ', description) FROM patient_observation";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    while(rs.next())
    {
      nvList.add(new NameValuePair(rs.getString(1), rs.getString(2)));
    }

    DBHelper.disconnect(null, stmt, rs);
    return nvList;
  }
  
  public static List<NameValuePair> getProducts() throws Exception
  {
    List<NameValuePair> nvList = new ArrayList();

    String SQL =
    "select distinct concat(vm.vaccine_id, ':', vm.manufacturer_id) as pk, " +
    "       concat(vg.short_name, ' -> ', vm.trade_name, ' [', v.cvx, '-', m.mvx, ']') as text " +
    "  from vaccine_makeup vm, " +
    "       vaccine v, " +
    "       manufacturer m, " +
    "       vaccine_group vg " +
    " where v.vaccine_id = vm.vaccine_id " +
    "   and m.manufacturer_id = vm.manufacturer_id " +
    "   and vm.vaccine_group_id = vg.vaccine_group_id order by 2 asc";

    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    while(rs.next())
    {
      nvList.add(new NameValuePair(rs.getString(1), rs.getString(2)));
    }

    DBHelper.disconnect(null, stmt, rs);
    return nvList;
  }

  public static String getAntigenName(int antigenId) throws Exception {
    String str = "Unknown";
    String SQL = "select antigen_name from antigen where antigen_id = " + antigenId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      str = rs.getString(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return str;
  }

  public static String GetVaccineGroupName(int vaccineGroupId) throws Exception {
    String str = "Unknown";
    String SQL = "select short_name from vaccine_group where vaccine_group_id = " + vaccineGroupId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      str = rs.getString(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return str;
  }

  public static String GetDefaultForecastCVX(int vaccineGroupId) throws Exception {
    String str = "Unknown";
    String SQL = "select forecast_cvx from vaccine_group where vaccine_group_id = " + vaccineGroupId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      str = rs.getString(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return str;
  }

  public static int getVaccineId(String cvx) throws Exception {
    int id = 0;
    String SQL = "select vaccine_id from vaccine where cvx = '" + cvx + "'";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      id = rs.getInt(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return id;
  }

  public static String getCVXFromVaccineId(int vaccineId) throws Exception {
    String str = "Unknown";
    String SQL = "select cvx from vaccine where vaccine_id = " + vaccineId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      str = rs.getString(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return str;
  }

    public static String getNameAndCVXFromVaccineId(int vaccineId) throws Exception {
    String str = "Unknown";
    String SQL = "select concat(short_name, ' (', cvx, ')') from vaccine where vaccine_id = " + vaccineId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      str = rs.getString(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return str;
  }

  public static int getManufacturerId(String mvx) throws Exception {
    if(mvx == null || mvx.isEmpty()) return 0;
    
    int id = 0;
    String SQL = "select manufacturer_id from manufacturer where mvx = '" + mvx + "'";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      id = rs.getInt(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return id;
  }

  public static String getMVXFromManufacturerId(int manufacturerId) throws Exception {
    String str = "UNK";
    String SQL = "select mvx from manufacturer where manufacturer_id = " + manufacturerId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      str = rs.getString(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return str;
  }

  public static List<NameValuePair> getVaccineGroupsAssociatedWithVaccine(int vaccineId) throws Exception
  {
    List<NameValuePair> vgList = new ArrayList();

    String SQL = " SELECT distinct vg.vaccine_group_id, vg.short_name " +
                 "   FROM vaccine_group vg, vaccine_makeup vm " +
                 "  WHERE vg.vaccine_group_id = vm.vaccine_group_id " +
                 "    AND vm.vaccine_id = " + vaccineId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    while(rs.next())
    {
      vgList.add(new NameValuePair(rs.getString(1), rs.getString(2)));
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return vgList;
  }

  public static String getProductName(int vaccineId, int manufacturerId) throws Exception {
    String str = "Unknown";
    String SQL = "select distinct trade_name from vaccine_makeup where vaccine_id = " + vaccineId + " and manufacturer_id = " + manufacturerId;
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      str = rs.getString(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return str;
  }

  public static String getVaccineShortName(String cvx) throws Exception {
    String str = "Unknown";
    String SQL = "select short_name from vaccine where cvx = '" + cvx + "'";
    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      str = rs.getString(1);
    }
    
    DBHelper.disconnect(null, stmt, rs);
    return str;
  }

  public static boolean isMultiAntigenShot(CDSiPatientData.AntigenAdministered aa, Date dob) throws Exception {
    int count = 0;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    
    String SQL =  "select count(*) from vaccine_makeup where vaccine_id = " + aa.getVaccineId();
           SQL += " AND '" + df.format(aa.getDateAdministered()) + "' >= IFNULL(DATE_ADD('" + df.format(dob) + "', interval left(association_begin_age, instr(association_begin_age,' ') - 1) YEAR), '1900-01-01') ";
           SQL += " AND '" + df.format(aa.getDateAdministered()) + "' <  IFNULL(DATE_ADD('" + df.format(dob) + "', interval left(association_end_age,   instr(association_end_age,' ')   - 1) YEAR), '2999-12-31') ";

    Statement stmt = DBHelper.dbConn().createStatement();

    ResultSet rs = stmt.executeQuery(SQL);

    if(rs.next())
    {
      count = rs.getInt(1);
    }
    DBHelper.disconnect(null, stmt, rs);
    return (count > 1);
  }

}
