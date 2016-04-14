package com.github.tonybaines.gestalt;

import com.github.tonybaines.gestalt.validation.ValidationResult;
import com.google.common.collect.Lists;

import javax.validation.constraints.NotNull;

public interface SimpleConfig {

    @NotNull
    @Default.String("foo")
    String getName();

    @Default.Integer(42)
    Integer getLevel();

    @Default.Boolean(false)
    Boolean isEnabled();
    @Default.String("defaulted")
    String getDefaultOnly();

    default String notAProperty() {
        return "Not a property";
    }

}
