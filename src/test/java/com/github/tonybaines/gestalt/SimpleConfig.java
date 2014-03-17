package com.github.tonybaines.gestalt;

import javax.validation.constraints.NotNull;

public interface SimpleConfig {

    @NotNull
    @Default.String("foo")
    String getName();

    @Default.Integer(42)
    int getLevel();

    @Default.Boolean(false)
    boolean isEnabled();
}
