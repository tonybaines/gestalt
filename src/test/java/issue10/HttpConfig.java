package issue10;

import com.github.tonybaines.gestalt.Default;
public interface HttpConfig {

    @Default.Integer(8080)
    int getPort();

    @Default.Integer(9443)
    int getSecurePort();

    @Default.Boolean(false)
    boolean isBypassVip();

    String getVip();
}
