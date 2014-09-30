package com.fererlab.ws;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.List;
/**
 * acm
 */
public class LogHandlerResolver implements HandlerResolver {

    @Override
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        List<Handler> hchain = new ArrayList<Handler>();
        hchain.add(new LoggingHandler());
        return hchain;
    }
}
