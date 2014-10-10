package com.fererlab.action;

import com.fererlab.cache.Cache;
import com.fererlab.collect.Collector;
import com.fererlab.collect.Exec;
import com.fererlab.dto.*;
import com.fererlab.map.ContextMap;
import com.fererlab.map.MimeTypeMap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * acm | 1/23/13
 */
public class BaseAction extends ActionResponse implements Action {

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private XStream xStreamJSON = new XStream(new JettisonMappedXmlDriver());
    private XStream xstream = new XStream(new StaxDriver());
    private Collector collector = new Collector();

    public BaseAction() {
        xstream.setMode(XStream.SINGLE_NODE_XPATH_RELATIVE_REFERENCES);
        xStreamJSON.setMode(XStream.SINGLE_NODE_XPATH_RELATIVE_REFERENCES);
        xstream.autodetectAnnotations(true);
        xStreamJSON.autodetectAnnotations(true);
    }

    public List<Object> collect(Exec... execs) {
        return collect(collector.COLLECT_TIMEOUT_MILLIS, execs);
    }

    public List<Object> collect(final long timeoutMillis, Exec... execs) {
        return collector.collect(timeoutMillis, execs);
    }

    public XStream getXStreamJSON() {
        return xStreamJSON;
    }

    public XStream getXStream() {
        return xstream;
    }

    public Response fileContentResponse(Request request, String fileName) {
        Map.Entry<byte[], String> entry = Cache.getContentIfCached(fileName);
        if (entry == null) {
            try {
                Map<byte[], String> contentAndExtension = new HashMap<byte[], String>();
                FileContentHandler fileContentHandler = new FileContentHandler();
                byte[] content = new byte[0];
                content = fileContentHandler.getContent(fileContentHandler.getContentPath(), fileName);
                String extension = fileContentHandler.getFileExtension();
                contentAndExtension.put(content, extension);
                entry = contentAndExtension.entrySet().iterator().next();
                Cache.putIfCacheable(fileName, entry);
            } catch (FileNotFoundException e) {
                return new Response(
                        new ParamMap<String, Param<String, Object>>(),
                        request.getSession(),
                        Status.STATUS_NOT_FOUND,
                        ""
                );
            }
        }
        Response response = new Response(
                new ParamMap<String, Param<String, Object>>(),
                request.getSession(),
                Status.STATUS_OK,
                entry.getKey()
        );
        response.getHeaders().put(
                ResponseKeys.RESPONSE_TYPE.getValue(),
                new Param<String, Object>(
                        ResponseKeys.RESPONSE_TYPE.getValue(),
                        MimeTypeMap.getInstance().get(entry.getValue())
                )
        );
        return response;
    }

    @Override
    public String toContent(Request request, Object... objects) {

        // if RESPONSE_TYPE is defined and is XML return toMXL
        if (request.getHeaders().containsKey(RequestKeys.RESPONSE_TYPE.getValue().toLowerCase())
                && ((String) request.getHeaders().get(RequestKeys.RESPONSE_TYPE.getValue().toLowerCase()).getValue()).equalsIgnoreCase("xml")) {
            request.getHeaders().addParam(new Param<String, Object>(RequestKeys.RESPONSE_TYPE.getValue(), "xml"));
            if (objects.length > 1) {
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>" + toXML(objects) + "</root>";
            } else {
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + toXML(objects);
            }
        }

        // else if RESPONSE_TEMPLATE exists return XML with template
        else if (request.getParams().containsKey(RequestKeys.RESPONSE_TEMPLATE.getValue())) {
            request.getHeaders().addParam(new Param<String, Object>(RequestKeys.RESPONSE_TYPE.getValue(), "xml"));
            StringBuilder responseContent = new StringBuilder();
            responseContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            responseContent.append("<?xml-stylesheet type=\"text/xsl\" href=\"")
                    .append(String.valueOf(request.getParams().getValue(RequestKeys.APPLICATION_URI.getValue())))
                    .append("/_/xsl/")
                    .append(String.valueOf(request.getParams().getValue(RequestKeys.RESPONSE_TEMPLATE.getValue())))
                    .append(".xsl")
                    .append("?")
                    .append(new Random().nextDouble())
                    .append("\"?>");
            if (objects.length > 1) {
                responseContent.append("<root>");
                responseContent.append(toXML(objects));
                responseContent.append("</root>");
            } else {
                responseContent.append(toXML(objects));
            }
            return responseContent.toString();
        }

        // else return JSON
        else {
            request.getHeaders().addParam(new Param<String, Object>(RequestKeys.RESPONSE_TYPE.getValue(), "json"));
            return toJSON(objects);
        }

    }

    public String toJSON(Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            stringBuilder.append(toJSON(object));
            stringBuilder.append(",");
        }
        String json = stringBuilder.toString();
        if (json.endsWith(",")) {
            return json.substring(0, json.length() - 1);
        }
        return json;
    }

    public String toJSON(Object o) {
        return xStreamJSON.toXML(o);
    }

    public String toXML(Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object object : objects) {
            stringBuilder.append(toXML(object));
        }
        return stringBuilder.toString();
    }

    public String toXML(Object o) {
        return xstream.toXML(o).substring("<?xml version=\"1.0\" ?>".length());
    }

    @Override
    public void log(String message) {
        logger.log(Level.INFO, message);
    }

    public <T extends Object> T getObject(Class<T> t) {
        return ContextMap.getInstance().getContext().getObject(t);
    }

    @SuppressWarnings("unchecked")
    public QueryBuilder query(Class<? extends Model> clazz) {
        return new QueryBuilder(clazz);
    }

    private class QueryBuilder<T extends Model> {

        private ModelAction<T> modelAction;
        private ParamMapBuilder<String, Param<String, Object>> paramMapBuilder = new ParamMapBuilder<String, Param<String, Object>>();

        public QueryBuilder(Class<T> clazz) {
            modelAction = new ModelAction<T>(clazz);
        }

        public QueryBuilder add(String field, Object value) {
            return add(field, value, ParamRelation.EQ);
        }

        public QueryBuilder add(String field, Object value, ParamRelation relation) {
            paramMapBuilder.add(field, value, relation);
            return this;
        }

        public List<T> findAll() {
            return modelAction.findAll(paramMapBuilder.getParamMap());
        }
    }
}
