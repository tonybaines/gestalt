package com.github.tonybaines.gestalt;


import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public interface TestConfig {
    @NotNull()
    Integer getIntValue();

    String getStringValue();

    double getDoubleValue();

    @NotNull
    long getLongValue();

    Boolean isBooleanValue();

    Handed getHandedness();

    SubConfigLevel1 getSubConfig();

    List<String> getStrings();

    List<Thing> getThings();

    Integer getNonExistent();

    Integer getDeclaredAsAnIntegerButIsAString();

    String getSomethingDefinedTwice();

    @Default.String("default-value")
    String getNonExistentStringWithDefault();

    @Default.Integer(42)
    Integer getNonExistentIntegerWithDefault();

    @Default.Boolean(false)
    Boolean getNonExistentBooleanWithDefault();

    @Default.Double(42.5)
    Double getNonExistentDoubleWithDefault();

    /**
     * Enums need to have defaults defined
     * as String constants such that they
     * evaluate successfully from
     * <pre>MyEnum.valueOf(string)</pre>
     */
    @Default.Enum("right")
    Handed getNonExistentEnumWithDefault();

    @Default.Enum("sideways")
    Handed getDefaultedValueWithBadDefinition();

    String getPropertyDefinedOnlyInGroovyConfig();

    String getPropertyDefinedAllConfigSources();

    @Size(min = 1, max = 2)
    @Default.String("foo")
    String getStringValueWhoseDefaultBreaksValidation();

    @Max(10)
    @Min(1)
    Integer getIntegerThatIsTooLarge();

    public static interface SubConfigLevel1 {
        Integer getIntValue();

        @AssertTrue
        @Default.Boolean(false)
        boolean getBooleanValueWhoseValueBreaksValidation();

        @Pattern(regexp = "f.*")
        @Default.String("fin")
        String getValueWhichIsDefinedToBreakValidationButHasADefault();
    }

    public static interface Thing {
        String getId();

        String getStringValue();
    }
}
