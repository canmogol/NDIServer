package com.fererlab.ws;


import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * acm
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private long out;

    @Override
    public void close(MessageContext arg0) {
        System.out.println("------------------ closed " + (new Date().getTime() - out) + " msec");
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        System.out.println("------------------ handleFault");
        try {
            SOAPMessage message = context.getMessage();
            System.out.println("------------------ message: " + message);
            SOAPHeader header = message.getSOAPHeader();
            System.out.println("------------------ header: " + header);
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            System.out.println("------------------ envelope: " + envelope);
        } catch (SOAPException e) {
            e.printStackTrace();
        }

        boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {
            System.out.println("------------------ Direction=outbound");
        } else {
            System.out.println("------------------ Direction=inbound");
        }
        if (!outbound) {
            try {
                SOAPMessage msg = context.getMessage();
                dumpSOAPMessage(msg);
                if (context.getMessage().getSOAPBody().getFault() != null) {
                    String detailName;
                    try {
                        detailName = context.getMessage().getSOAPBody().getFault().getDetail().getFirstChild().getLocalName();
                        System.out.println("------------------ detailName=" + detailName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (SOAPException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void dumpSOAPMessage(SOAPMessage msg) {
        if (msg == null) {
            System.out.println("------------------ SOAP Message is NULL !!!");
            return;
        }
        System.out.println("");
        System.out.println("--------------------");
        System.out.println("DUMP OF SOAP MESSAGE");
        System.out.println("--------------------");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
            System.out.println(baos.toString(getMessageEncoding(msg)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--------------------");
        System.out.println("");
    }

    private String getMessageEncoding(SOAPMessage msg) throws SOAPException {
        String encoding = "utf-8";
        if (msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING) != null) {
            encoding = msg.getProperty(SOAPMessage.CHARACTER_SET_ENCODING).toString();
        }
        return encoding;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext arg0) {
        if ((Boolean) arg0.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            out = new Date().getTime();
            System.out.println("------------ OUT handleMessage");
        } else {
            System.out.println("------------ IN handleMessage");
        }
        log(arg0);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<QName> getHeaders() {
        System.out.println("------------------ getHeaders");
        return Collections.EMPTY_SET;
    }

    private void log(SOAPMessageContext messageContext) {
        try {
            SOAPMessage msg = messageContext.getMessage();
            try {
                msg.writeTo(System.out);
                System.out.println("");
            } catch (SOAPException ex) {
                Logger.getLogger(LoggingHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LoggingHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


