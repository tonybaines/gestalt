package issue10;

import com.github.tonybaines.gestalt.Default;

public interface WebSocketConfig {

    @Default.Integer(604800000 /* one week */)
    int getTimeout();

    @Default.Integer(8192)
    int getMaxTextFrameSize();
}
