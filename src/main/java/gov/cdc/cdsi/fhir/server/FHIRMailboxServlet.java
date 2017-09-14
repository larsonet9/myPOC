///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package gov.cdc.cdsi.fhir.server;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.core.Response;
//import org.hl7.fhir.instance.formats.XmlParser;
//import org.hl7.fhir.instance.model.Bundle;
//import org.hl7.fhir.instance.model.Coding;
//import org.hl7.fhir.instance.model.OperationOutcome;
//import org.hl7.fhir.instance.model.StringType;
//
///**
// *
// * @author Eric
// */
//@WebServlet(name = "FHIRMailboxServlet", urlPatterns = {"/Mailbox"})
//public class FHIRMailboxServlet extends HttpServlet {
//
//  /**
//   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
//   * methods.
//   *
//   * @param request servlet request
//   * @param response servlet response
//   * @throws ServletException if a servlet-specific error occurs
//   * @throws IOException if an I/O error occurs
//   */
//  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
//          throws ServletException, IOException, Exception {
//    Response.ResponseBuilder builder = null;
//    // Validate input format check; instantiate the ResourceOrFeed
//    Bundle resource;
//
//    String producesType = "application/xml+fhir";
//    // Convert XML contents to ResourceOrFeed
//    XmlParser xmlP = new XmlParser();
//    resource = (Bundle)xmlP.parse(request.getInputStream());
//
//    BundleProcessor processor = new BundleProcessor();
//    ResourceContainer resourceContainer = processor.process(resource, request.getRequestURL().toString());
//    builder = responseFeed(producesType, resourceContainer,  request.getServletContext().getContextPath());
//        
//    response.setContentType("application/xml+fhir; charset=UTF-8");
//    try (PrintWriter out = response.getWriter()) {
//       out.print(builder.build().getEntity().toString());
//
//  }
//    
//  }
//
//  /**
//   * @param resourcePath
//   * @param producesType
//   * @param resourceContainer
//   * @return
//   * @throws URISyntaxException
//   * @throws Exception
//   */
//  private Response.ResponseBuilder responseFeed(String producesType, ResourceContainer resourceContainer,
//      String context) throws URISyntaxException, Exception {
//
//    Response.ResponseBuilder builder;
//    ByteArrayOutputStream oResponseMessage;
//    if (resourceContainer != null) {
//      builder = Response.status(resourceContainer.getResponseStatus()).tag(producesType)
//          .type(producesType + "; charset=utf-8");
//
//      if (resourceContainer.getBundle() != null) {
//        // Define URI location
//        String locationPath = context + "/Mailbox";
//
//        URI resourceLocation = new URI(locationPath);
//        builder = builder.contentLocation(resourceLocation);
//
//        if (resourceContainer.getResponseStatus().equals(Response.Status.OK)) {
//
//          String sResponseMessage = "";
//
//          if (producesType.indexOf("xml") >= 0) {
//            // Convert AtomFeed to XML
//            oResponseMessage = new ByteArrayOutputStream();
//            XmlParser xmlP = new XmlParser();
//            xmlP.compose(oResponseMessage, resourceContainer.getBundle(), true);
//            sResponseMessage = oResponseMessage.toString();
//          } else {
//            // Convert AtomFeed to JSON
//          }
//
//          builder = builder.entity(sResponseMessage);
//
//        } else {
//          // Something went wrong
//          String message = "Mailbox error - response status: " + resourceContainer.getResponseStatus();
//          String outcome = this.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
//              Response.Status.INTERNAL_SERVER_ERROR.toString(), message);
//          builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome);
//        }
//
//      } else {
//        // Something went wrong
//        String message = resourceContainer.getMessage();
//        String outcome = this.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
//            Response.Status.INTERNAL_SERVER_ERROR.toString(), message);
//        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome);
//      }
//    } else {
//      // Something went wrong
//      String message = "Mailbox error - no response generated.";
//      String outcome = this.getOperationOutcome(OperationOutcome.IssueSeverity.ERROR,
//          Response.Status.INTERNAL_SERVER_ERROR.toString(), message);
//      builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(outcome);
//    }
//
//    return builder;
//  }
//
//  /**
//   * 
//   * @param severity
//   * @param type
//   * @param details
//   * @param location
//   * @return XML string representation of <code>OperationOutcome</code>
//   */
//  protected String getOperationOutcome(OperationOutcome.IssueSeverity severity) {
//    return getOperationOutcome(severity, null);
//  }
//
//  protected String getOperationOutcome(OperationOutcome.IssueSeverity severity, String type) {
//    return getOperationOutcome(severity, type, null);
//  }
//
//  protected String getOperationOutcome(OperationOutcome.IssueSeverity severity, String type, String details) {
//    return getOperationOutcome(severity, type, details, null);
//  }
//
//  protected String getOperationOutcome(OperationOutcome.IssueSeverity severity, String type, String details,
//      String location) {
//
//    String sOp = "";
//
//    try {
//      OperationOutcome op = new OperationOutcome();
//
//      OperationOutcome.OperationOutcomeIssueComponent issue = new OperationOutcome.OperationOutcomeIssueComponent();
//
//      issue.setSeverity(severity);
//      if (type != null) {
//        Coding coding = new Coding();
//        coding.setCode(type);
//        issue.setType(coding);
//      }
//      if (details != null) {
//        issue.setDetails(details);
//      }
//      if (location != null) {
//        issue.getLocation().add(new StringType(location));
//      }
//
//      op.getIssue().add(issue);
//
//      // Convert the OperationOutcome to XML string
//      ByteArrayOutputStream oOp = new ByteArrayOutputStream();
//      XmlParser xmlC = new XmlParser();
//      xmlC.compose(oOp, op, true);
//      sOp = oOp.toString();
//
//    } catch (Exception e) {
//      // Handle generic exceptions
//      e.printStackTrace();
//    }
//
//    return sOp;
//  }
//
//  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
//  /**
//   * Handles the HTTP <code>GET</code> method.
//   *
//   * @param request servlet request
//   * @param response servlet response
//   * @throws ServletException if a servlet-specific error occurs
//   * @throws IOException if an I/O error occurs
//   */
//  @Override
//  protected void doGet(HttpServletRequest request, HttpServletResponse response)
//          throws ServletException, IOException {
//    try {
//      processRequest(request, response);
//    } catch (Exception ex) {
//      Logger.getLogger(FHIRMailboxServlet.class.getName()).log(Level.SEVERE, null, ex);
//    }
//  }
//
//  /**
//   * Handles the HTTP <code>POST</code> method.
//   *
//   * @param request servlet request
//   * @param response servlet response
//   * @throws ServletException if a servlet-specific error occurs
//   * @throws IOException if an I/O error occurs
//   */
//  @Override
//  protected void doPost(HttpServletRequest request, HttpServletResponse response)
//          throws ServletException, IOException {
//    try {
//      processRequest(request, response);
//    } catch (Exception ex) {
//      Logger.getLogger(FHIRMailboxServlet.class.getName()).log(Level.SEVERE, null, ex);
//    }
//  }
//
//  /**
//   * Returns a short description of the servlet.
//   *
//   * @return a String containing servlet description
//   */
//  @Override
//  public String getServletInfo() {
//    return "Short description";
//  }// </editor-fold>
//
//}
