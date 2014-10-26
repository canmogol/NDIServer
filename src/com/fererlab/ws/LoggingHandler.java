package com.fererlab.ws;

import com.fererlab.db.EM;
import com.fererlab.dto.AuditLogModel;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * acm
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private Date outMessageDate;
    private Date inMessageDate;
    private AuditLogModel auditLog;

    @Override
    public void close(MessageContext messageContext) {
        if (outMessageDate != null) {
            log("------ out closed " + (new Date().getTime() - outMessageDate.getTime()) + " msec ");
        } else if (inMessageDate != null) {
            log("------ in closed " + (new Date().getTime() - inMessageDate.getTime()) + " msec");
        }
        outMessageDate = null;
        inMessageDate = null;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        String soapMessage = messageToString(context);
        try {
            log("------ handleFault");
            SOAPHeader header = context.getMessage().getSOAPHeader();
            SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
            log("------ message: " + soapMessage);
            log("------ header: " + header);
            log("------ envelope: " + envelope);
        } catch (Exception e) {
        }
        updateIntegrationPointLog(context, soapMessage);
        return true;
    }


    private String getMessageEncoding(SOAPMessage msg) throws SOAPException {
        String encoding = "utf-8";
        if (msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING) != null) {
            encoding = msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING).toString();
        }
        return encoding;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        String soapMessage = messageToString(context);
        updateIntegrationPointLog(context, soapMessage);
        try {
            if (((Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                InputStream inputStream = new ByteArrayInputStream(soapMessage.getBytes());
                Document document = documentBuilder.parse(inputStream);

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                StreamResult streamResult = new StreamResult(new StringWriter());
                DOMSource domSource = new DOMSource(document);
                transformer.transform(domSource, streamResult);
                soapMessage = streamResult.getWriter().toString();
            }
        } catch (Exception e) {
        }
        log("\n" + soapMessage);
        return true;
    }

    private void updateIntegrationPointLog(SOAPMessageContext context, String soapMessage) {
        try {
            if ((Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
                log("OUT handleMessage");
                outMessageDate = new Date();
                if (auditLog == null) {
                    // our server is client, we will send the SOAP request
                    auditLog = new AuditLogModel();
                    auditLog.setType(AuditLogModel.AuditType.SERVER_REQUEST);
                    auditLog.setClassName(context.get(MessageContext.WSDL_SERVICE).toString());
                    auditLog.setMethodName(context.get(MessageContext.WSDL_INTERFACE).toString());
                    auditLog.setRequest(soapMessage);
                    auditLog.setUsername("localhost");
                    EM.persist(auditLog);
                } else {
                    // our server is server, we are serving a web service here, someone called our service
                    auditLog.setResponse(soapMessage);
                    auditLog.setUpdatedDate(new Date());
                    auditLog = EM.merge(auditLog);
                    auditLog = null;

                }
            } else {
                log("IN handleMessage");
                inMessageDate = new Date();
                if (auditLog == null) {
                    // our server is server, we are serving a web service here, someone called our service
                    auditLog = new AuditLogModel();
                    auditLog.setType(AuditLogModel.AuditType.SERVER_REQUEST);
                    auditLog.setClassName(context.get(MessageContext.WSDL_SERVICE).toString());
                    auditLog.setMethodName(context.get(MessageContext.WSDL_INTERFACE).toString());
                    auditLog.setRequest(soapMessage);
                    auditLog.setUsername("localhost");
                    EM.persist(auditLog);
                } else {
                    // our server is client, we got the response, save it and set it to null
                    auditLog.setResponse(soapMessage);
                    auditLog.setUpdatedDate(new Date());
                    auditLog = EM.merge(auditLog);
                    auditLog = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void log(String log) {
        System.out.println("--------------LoggingHandler[" + this.hashCode() + "][" + Thread.currentThread().getId() + "]: " + log);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<QName> getHeaders() {
        log("------ getHeaders");
        return Collections.EMPTY_SET;
    }

    private String messageToString(SOAPMessageContext messageContext) {
        try {
            SOAPMessage msg = messageContext.getMessage();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            msg.writeTo(out);
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}