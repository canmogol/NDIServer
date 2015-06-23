package com.fererlab.dto;

import com.fererlab.session.Session;

import java.io.IOException;
import java.io.Serializable;

/**
 * acm 10/15/12
 */
public class Response implements Serializable {

    private ParamMap<String, Param<String, Object>> headers;
    private Session session;
    private Status status;
    private String content = null;
    private byte[] contentChar = null;

    public Response(ParamMap<String, Param<String, Object>> headers, Session session, Status status, byte[] contentChar) {
        this(headers, session, status);
        this.setContentChar(contentChar);
    }

    public Response(ParamMap<String, Param<String, Object>> headers, Session session, Status status, String content) {
        this(headers, session, status);
        this.setContent(content);
    }

    public Response(ParamMap<String, Param<String, Object>> headers, Session session, Status status) {
        this.headers = headers;
        this.session = session;
        this.status = status;
        this.setContent(status.getStatus() + ": " + status.getMessage());
    }

    public ParamMap<String, Param<String, Object>> getHeaders() {
        return headers;
    }

    public void setHeaders(ParamMap<String, Param<String, Object>> headers) {
        this.headers = headers;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getContentChar() {
        return contentChar;
    }

    public void setContentChar(byte[] contentChar) {
        this.contentChar = contentChar;
    }

    @Override
    public String toString() {
        return "Response{" +
                "headers=" + headers +
                ", status=" + status +
                '}';
    }

    public byte[] getContent() {
        if (contentChar != null) {
            return getContentChar();
        } else {
            try {
                try {
                    return content.getBytes("UTF-8");
                } catch (IOException e) {
                    return content.getBytes();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new byte[]{};
    }

    /*
    static create response method
     */

    public static Response create(final Request request, String content, Status status) {
        return new Response(
                new ParamMap<String, Param<String, Object>>(),
                request.getSession(),
                status,
                content == null ? "" : content
        );
    }

    public static Response internalServerError(Request request, Exception e) {
        StringBuilder exception = new StringBuilder();
        exception.append("\n<h3>\n");
        exception.append(e.getClass().getName());
        exception.append(": ");
        exception.append(e.getMessage());
        if (e.getCause() != null) {
            Throwable cause = e.getCause();
            while (cause != null) {
                exception.append("<br/>");
                exception.append(cause.getClass().getName());
                exception.append(": ");
                exception.append(cause.getMessage());
                cause = cause.getCause();
            }
        }
        exception.append("\n</h3><h5>\n");
        for (StackTraceElement element : e.getStackTrace()) {
            exception.append(element.getClassName()).append(".").append(element.getMethodName()).append("(").append(element.getFileName()).append(":").append(element.getLineNumber()).append(")").append("\n<br/>\n");
        }
        e.printStackTrace();
        return Response.create(request, "<h1>" + Status.STATUS_INTERNAL_SERVER_ERROR.getMessage() + "</h1>" + exception + "</h5>", Status.STATUS_INTERNAL_SERVER_ERROR);
    }


}
