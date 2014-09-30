package com.fererlab.converter;

import com.fererlab.dto.Param;
import com.fererlab.dto.ParamMap;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * acm 10/16/12
 */
public class ParamListConverter implements Converter {

    @Override
    @SuppressWarnings("unchecked")
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ParamMap<String, Param<String, Object>> paramMap = (ParamMap<String, Param<String, Object>>) source;
        writer.startNode("");
        writer.setValue("");
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return ParamMap.class == type;
    }
}
