package issue10;

import com.github.tonybaines.gestalt.Default;

public interface SecurityConfig {

    @Default.String("PASS_THROUGH")
    String getBasicAuthDelegateName();

    @Default.Boolean(false)
    boolean isEnabled();
}
