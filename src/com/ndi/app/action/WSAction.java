package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ws.LogHandlerResolver;
import com.fererlab.ws.LoggingHandler;
import net.webservicex.CurrencyConvertor;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.HandlerResolver;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * acm
 */
public class WSAction extends BaseAction {

    private Map<String, ServiceMethod> serviceMethodMap = new HashMap<String, ServiceMethod>();
    private CurrencyConvertor currencyConvertor;

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

    public Response service(Request request) {
        if (request.get("service") != null && request.get("method") != null) {
            try {
                ServiceMethod serviceMethod = prepareServiceMethod(request);
                try {
                    Object service = serviceMethod.getService().invoke(this);
                    Object argObject = getXStreamJSON().fromXML(request.get("args").replace("%22", "\""));
                    Object wsResponse = serviceMethod.getMethod().invoke(service, argObject);
                    return Ok(request).add("data", wsResponse).toResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                    return Error(request, e.getMessage()).toResponse();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return NotFound(request, "no method found").toResponse();
    }

    private ServiceMethod prepareServiceMethod(Request request) throws Exception {
        String serviceMethodKey = request.get("service") + "::" + request.get("method");
        Method foundMethod = null;
        if (!serviceMethodMap.containsKey(serviceMethodKey)) {
            Method serviceMethod = findService(request.get("service"));
            if (serviceMethod != null) {
                String methodName = request.get("method");
                for (Method method : serviceMethod.getReturnType().getDeclaredMethods()) {
                    if (method.getName().equals(methodName)) {
                        foundMethod = method;
                        break;
                    }
                }
                if (foundMethod == null) {
                    throw new Exception("no method found");
                }
            } else {
                throw new Exception("no service found");
            }
            serviceMethodMap.put(serviceMethodKey, new ServiceMethod(serviceMethod, foundMethod));
        }
        return serviceMethodMap.get(serviceMethodKey);
    }

    private Method findService(String service) {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getReturnType().getName().equals(service)) {
                return method;
            }
        }
        return null;
    }

    public CurrencyConvertor geCurrencyConvertor() {
        if (currencyConvertor == null) {
            Service service = new CurrencyConvertor();
            currencyConvertor = service.getPort(CurrencyConvertor.class);
            BindingProvider bp = ((BindingProvider) currencyConvertor);

            // set logging handlers and connection/request timeouts
            HandlerResolver handlerResolver = new LogHandlerResolver();
            service.setHandlerResolver(handlerResolver);
            Binding binding = bp.getBinding();
            List<javax.xml.ws.handler.Handler> handlerList = binding.getHandlerChain();
            handlerList.add(new LoggingHandler());
            binding.setHandlerChain(handlerList);
        }
        return currencyConvertor;
    }

    private class ServiceMethod {
        private final Method service;
        private final Method method;

        public ServiceMethod(Method service, Method method) {
            this.service = service;
            this.method = method;
        }

        private Method getService() {
            return service;
        }

        private Method getMethod() {
            return method;
        }
    }
}
