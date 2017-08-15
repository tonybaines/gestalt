package com.github.tonybaines.gestalt;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * Use-case:
 *
 * It is useful for certain configuration values to be dynamic at runtime, rather than
 * static for the duration of the program e.g. modifying the size of a DB or thread
 * pool based on metrics such as throughput or machine capacity, or adjusting timeouts
 * based on an estimate of how much work needs to happen.
 */
public class SpikeDynamicConfigValuesTest {

    private static final Properties CONSTANTS = new Properties() {{
        put("someTimeVaryingValue", "0");
    }};

    @NoCache // Caching breaks the dynamic lookup
    static interface SomeConfig {
        long getSomeTimeVaryingValue();
    }

    @Test
    public void returnsAConstantValueByDefault() throws Exception {
        SomeConfig config = Configurations.definedBy(SomeConfig.class)
                .fromProperties(CONSTANTS)
                .done();

        assertThat(config.getSomeTimeVaryingValue(), is(0L));
    }

    @Test
    public void returnsADynamicValueFromAProvider() throws Exception {
        /* This works, but is pretty clunky from the client programmer
         * point-of-view
         */
        ConfigSource dynamicSource = new ConfigSource() {
            private long next = System.currentTimeMillis();
            @Override
            public Object lookup(List<String> path, Method method) {
                // ignore the specific property
                return next++;
            }
        };

        SomeConfig config = Configurations.definedBy(SomeConfig.class)
                .from(dynamicSource) // This has higher priority
                .fromProperties(CONSTANTS)
                .done();

        long firstValue = config.getSomeTimeVaryingValue();
        assertThat(firstValue, is(not(0L)));

        long secondValue = config.getSomeTimeVaryingValue();
        assertThat(secondValue, is(not(firstValue)));
    }

}
