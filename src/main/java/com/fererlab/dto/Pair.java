package com.fererlab.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import java.util.Map;
import java.util.TreeMap;

/**
 * acm
 */
@XStreamAlias("pair")
@XStreamConverter(PairConverter.class)
public class Pair<Key, Value> {

    private Map<Key, Value> map = new TreeMap<Key, Value>();

    public Pair<Key, Value> add(Key key, Value value) {
        map.put(key, value);
        return this;
    }

    public Map<Key, Value> getMap() {
        return map;
    }

}
