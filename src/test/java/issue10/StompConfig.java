package issue10;

import com.github.tonybaines.gestalt.Default;

public interface StompConfig {

    @Default.Integer(30000)
    int getMinHeartbeatInterval();
}
