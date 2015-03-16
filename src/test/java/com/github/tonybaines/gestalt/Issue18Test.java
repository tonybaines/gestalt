package com.github.tonybaines.gestalt;

import com.google.common.base.Strings;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class Issue18Test {
    public interface AppConfig {
        @Default.String("ooo")
        SimpleType getSimpleType();

    }

    public static class SimpleType {
        private final int v;

        public static SimpleType fromString(String s) {
            return new SimpleType(s.length());
        }

        private SimpleType(int v) {
            this.v = v;
        }

        public int getValue() {
            return v;
        }

        @Override
        public String toString() {
            return Strings.repeat("o", v);
        }
    }


    @Test(expected = ConfigurationException.class)
    public void shouldWork() throws Exception {
        Properties props = new Properties(){{
//            put("simpleType", "----");
        }};
        AppConfig config = Configurations.definedBy(AppConfig.class)
                .fromProperties(props)
                .without(Configurations.Feature.ExceptionOnNullValue)
                .done();

        assertEquals("oooo", config.getSimpleType().toString());
        String xml = Configurations.serialise(config, AppConfig.class).toXml();
        System.out.println(xml);
    }
}
