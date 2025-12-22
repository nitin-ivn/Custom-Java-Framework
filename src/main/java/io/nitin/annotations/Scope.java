package io.nitin.annotations;

import io.nitin.enums.ScopeType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {
    public ScopeType value() default ScopeType.SINGLETON;
}
