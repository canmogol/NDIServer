package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ndi.Wire;
import com.ndi.app.service.WSService;

/**
 * acm
 */
public class WSAction extends BaseAction {

    static {
        //for localhost testing only
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                        return true;
                    }
                });
    }

    @Wire
    private WSService wsService;

    public Response service(Request r) {
        // http://localhost:8080/srv/ws/service?service=net.webservicex.CurrencyConvertorSoap&method=conversionRate&args={%22list%22:[{%22net.webservicex.Currency%22:[%22EUR%22,%22USD%22]}]}
        if (r.get("service") != null || r.get("method") != null) {
            try {
                String args = r.get("args").replace("%22", "\"");
                Object argObject = getXStreamJSON().fromXML(args);
                try {
                    Object wsResponse;
                    wsResponse = wsService.getWSResponse(
                            r.get("service"),
                            r.get("method"),
                            argObject
                    );
                    return Ok(r).add("data", wsResponse).toResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                    return Error(r, "could not call method").exception(e).toResponse();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Error(r, "could not prepare service").exception(e).toResponse();
            }
        }
        return NotFound(r, "service, method and args keywords not available").toResponse();
    }

}
