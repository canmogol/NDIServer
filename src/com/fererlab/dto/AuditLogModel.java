package com.fererlab.dto;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * acm
 */
@Entity
@Cacheable(false)
@Table(name = "AUDIT_LOG_MODEL")
public class AuditLogModel implements Serializable, Model {
    private static final Long serialVersionUID = 1L;
    private Long id;
    private Date createDate = new Date();
    private Date updatedDate;
    private String className;
    private String methodName;
    private String request;
    private String response;
    private String username;
    private String groups;
    private String content;
    private String responseContent;
    private String requestUri;
    private String remoteIp;

    @Id
    @SequenceGenerator(name = "AUDIT_LOG_ID_GENERATOR", sequenceName = "SEQ_AUDIT_LOG", allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AUDIT_LOG_ID_GENERATOR")
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "A_CREATED", unique = false, nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "A_UPDATED", unique = false, nullable = true)
    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Column(name = "A_CLASS_NAME", unique = false, nullable = false)
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Column(name = "A_METHOD_NAME", unique = false, nullable = false)
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Column(name = "A_REQUEST", length = 4000, nullable = false)
    public String getRequest() {
        return request;
    }

    @Column(name = "A_REQUEST_URI", length = 4000, nullable = true)
    public String getRequestUri() {
        return requestUri;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    @Column(name = "A_RESPONSE", length = 4000, nullable = true)
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Column(name = "A_RESPONSE_CONTENT", length = 4000, nullable = true)
    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    @Column(name = "A_REMOTE_IP", unique = false, nullable = true)
    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    @Column(name = "A_GROUPS", unique = false, nullable = true)
    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    @Column(name = "A_USERNAME", unique = false, nullable = false)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

}
