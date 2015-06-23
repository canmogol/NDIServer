package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.action.FileContentHandler;
import com.fererlab.cache.Cache;
import com.fererlab.dto.*;
import com.fererlab.map.MimeTypeMap;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * acm
 */
public class ContentAction extends BaseAction {

    private MimeTypeMap mimeTypeMap = MimeTypeMap.getInstance();

    public Response favicon(Request request) {
        Param<String, Object> param = new Param<>(
                RequestKeys.URI.getValue(),
                "/_/image/favicon.ico"
        );
        request.getParams().put(RequestKeys.URI.getValue(), param);
        return deliver(request);
    }

    public Response deliver(Request request) {
        String requestURI = request.getParams().get(RequestKeys.URI.getValue()).getValue().toString();
        Map.Entry<byte[], String> entry = Cache.getContentIfCached(requestURI);
        if (entry == null) {
            // request URI is either one of these; xsl, css, js, image, file,
            FileContentHandler fileContentHandler = new FileContentHandler();
            byte[] content = new byte[0];
            try {
                content = fileContentHandler.getContent(fileContentHandler.getContentPath(), requestURI);
            } catch (FileNotFoundException e) {
                return new Response(
                        new ParamMap<>(),
                        request.getSession(),
                        Status.STATUS_NOT_FOUND
                );
            }
            String extension = fileContentHandler.getFileExtension();
            Map<byte[], String> contentAndExtension = new HashMap<>();
            contentAndExtension.put(content, extension);
            entry = contentAndExtension.entrySet().iterator().next();
            Cache.putIfCacheable(requestURI, entry);
        }
        Response response = new Response(
                new ParamMap<>(),
                request.getSession(),
                Status.STATUS_OK,
                entry.getKey()
        );
        response.getHeaders().put(
                ResponseKeys.RESPONSE_TYPE.getValue(),
                new Param<>(
                        ResponseKeys.RESPONSE_TYPE.getValue(),
                        mimeTypeMap.get(entry.getValue())
                )
        );
        return response;
    }

}
