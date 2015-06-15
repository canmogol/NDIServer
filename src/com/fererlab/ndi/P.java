package com.fererlab.ndi;

import java.lang.annotation.*;

/**
 * acm
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface P {
    String value();
}
