package com.fererlab.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * acm
 */
public class Collector {

    public long COLLECT_TIMEOUT_MILLIS = 10 * 60 * 1000;

    public List<Object> collect(final long timeoutMillis, Exec... execs) {
        return new ArrayList<Object>();
    }

}
