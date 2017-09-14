<%-- 
    Document   : index
    Created on : Mar 5, 2013, 7:42:09 AM
    Author     : eric
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
      <h1><a href="immEntry">Manually Enter a Scenario</a></h1>
      <ul><li>Create individual scenarios for a selected vaccine group</li></ul>
      <br>
      <h1><a href="testCase">Execute a Test Case</a></h1>
      <ul>
        <li>Run a specific test case from the CDSi published CDSi Test Cases</li>
        <li>Run all published CDSi Test Cases for a selected vaccine group at once</li>
      </ul>
      <br>
      <h1><a href="loadSupportingData">Load Antigen Supporting Data</a></h1>
      <ul>
        <li>Reload Antigen Supporting Data XML file.</li>
        <li>Please do with understanding that this affects all users.</li>
        <li>It may be helpful to return to published version of XML once finished.</li>
      </ul>
      <h1><a href="loadTestCases">Load Test Cases</a></h1>
      <ul>
        <li>Reload Test Cases using the Test Case Management Spreadsheet.</li>
        <li>Please do with understanding that this affects all users.</li>
        <li>This will wipe out all test cases and completely reload all FINAL test cases.</li>
      </ul>
    </body>
</html>
