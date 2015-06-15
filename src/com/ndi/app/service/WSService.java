package com.ndi.app.service;

import com.fererlab.ndi.Singleton;
import com.fererlab.ws.LogHandlerResolver;
import com.fererlab.ws.LoggingHandler;
import net.webservicex.CurrencyConvertor;
import net.webservicex.CurrencyConvertorSoap;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * acm
 */
@Singleton
public class WSService {

    private Map<String, ServiceMethod> serviceMethodMap = new HashMap<String, ServiceMethod>();
    private CurrencyConvertorSoap currencyConvertorSoap;

    public Object getWSResponse(String serviceName, String methodName, Object args) throws Exception {
        ServiceMethod serviceMethod = prepareServiceMethod(serviceName, methodName);
        Object service = serviceMethod.getService().invoke(this);
        if (args instanceof List) {
            List argList = (List) args;
            Object[] argArray = argList.toArray();
            return serviceMethod.getMethod().invoke(service, argArray);
        } else {
            return serviceMethod.getMethod().invoke(service, args);
        }
    }

    private ServiceMethod prepareServiceMethod(String serviceName, String methodName) throws Exception {
        String serviceMethodKey = serviceName + "::" + methodName;
        Method foundMethod = null;
        if (!serviceMethodMap.containsKey(serviceMethodKey)) {
            Method serviceMethod = findService(serviceName);
            if (serviceMethod != null) {
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

    public CurrencyConvertorSoap getCurrencyConvertor() {
        if (currencyConvertorSoap == null) {
            Service service = new CurrencyConvertor();
            currencyConvertorSoap = service.getPort(CurrencyConvertorSoap.class);
            BindingProvider bp = ((BindingProvider) currencyConvertorSoap);

            // set logging handlers and connection/request timeouts
            HandlerResolver handlerResolver = new LogHandlerResolver();
            service.setHandlerResolver(handlerResolver);
            Binding binding = bp.getBinding();
            List<Handler> handlerList = binding.getHandlerChain();
            handlerList.add(new LoggingHandler());
            binding.setHandlerChain(handlerList);
        }
        return currencyConvertorSoap;
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
