/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.cdc.cdsi.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author eric
 */
public class DBHelper {

  private static Connection con;

  private static Connection getConnection() throws Exception
  {
      try {
        //Oracle - Local Instance
        //Class.forName("oracle.jdbc.driver.OracleDriver");
        //String url = "jdbc:oracle:thin:@localhost:1521:xe";
        //return DriverManager.getConnection(url, "cdsi", "cdsi");
        
        // MySQL - Local Instance
//        Class.forName("com.mysql.jdbc.Driver");
//        String url = "jdbc:mysql://localhost/cdsi?";
//        return DriverManager.getConnection(url, "cdsi", "cdsi");

        // MySQL - Openshift instance - Now works on Local Instance too
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://127.8.3.2/cdsi?";
        return DriverManager.getConnection(url, "adminG8FPQED", "Dtm5dFi918Pv");
      }
      catch(Exception e) {
        System.out.println(e);
        throw new Exception("Unable to connect to the database.");
      }

  }

  public static void disconnect(Connection conn, Statement statement, ResultSet rs)
  {
    try {
      if(rs != null)
        rs.close();
    }
    catch(Exception e)
    {
      // Just sallow any exception here.
    }

    try {
      if(statement != null)
        statement.close();
    }
    catch(Exception e)
    {
      // Just sallow any exception here.
    }

    try {
      if(conn != null)
        conn.close();
    }
    catch(Exception e)
    {
      // Just sallow any exception here.
    }
  }

  public static Connection dbConn() throws Exception
  {
    if(con == null || con.isClosed())
      con = getConnection();

    return con;
  }
}
