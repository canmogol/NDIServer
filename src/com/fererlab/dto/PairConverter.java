package com.fererlab.dto;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.Map;

/**
 * acm
 */
public class PairConverter extends AbstractCollectionConverter {

    public PairConverter(Mapper mapper) {
        super(mapper);
    }

    public boolean canConvert(Class clazz) {
        return clazz.equals(Pair.class);
    }

    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        Pair map = (Pair) value;
        for (Object key : map.getMap().keySet()) {
            writer.startNode(String.valueOf(key));
            if (map.getMap().get(key) instanceof String) {
                writer.setValue(String.valueOf(map.getMap().get(key)));
            } else {
                context.convertAnother(map.getMap().get(key));
            }
            writer.endNode();
        }
    }

    @SuppressWarnings("unchecked")
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map map = (Map) createCollection(context.getRequiredType());

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            reader.moveDown();
            Object key = readItem(reader, context, map);
            reader.moveUp();

            reader.moveDown();
            Object value = readItem(reader, context, map);
            reader.moveUp();

            map.put(key, value);

            reader.moveUp();
        }
        return map;
    }

}