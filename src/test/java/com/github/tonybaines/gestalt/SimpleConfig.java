package com.github.tonybaines.gestalt;

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

    default String getNotAProperty() {
        return "Not a property";
    }

}
