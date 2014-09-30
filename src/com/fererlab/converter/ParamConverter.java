package com.fererlab.converter;

import com.fererlab.dto.Param;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * acm 10/16/12
 */
public class ParamConverter implements Converter {


    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Param param = (Param) source;
        writer.startNode((String) param.getKey());
        if (param.getValue() instanceof String) {
            writer.setValue((String) param.getValue());
        } else {
            writer.setValue("<![CDATA[" + String.valueOf(param.getValue()) + "]]>");
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return Param.class == type;
    }
}
