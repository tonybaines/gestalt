package issue10;

import com.github.tonybaines.gestalt.Default;

public interface TransparentProxyConfig {

    @Default.String("http://localhost:7080")
    String getToUri();

    @Default.Integer(2400000)
    int getTimeout();

    @Default.Integer(2400000)
    int getIdleTimeout();
}
