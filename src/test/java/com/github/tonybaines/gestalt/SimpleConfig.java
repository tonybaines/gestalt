package com.github.tonybaines.gestalt;

public interface SimpleConfig {

    @Default.String("foo")
    String getName();

    @Default.Integer(42)
    int getLevel();

    @Default.Boolean(false)
    boolean isEnabled();
}
