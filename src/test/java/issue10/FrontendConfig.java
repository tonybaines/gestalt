package issue10;


public interface FrontendConfig {

    HttpConfig getHttp();

    WebSocketConfig getWebSocket();

    StompConfig getStomp();

    TransparentProxyConfig getTransparentProxy();

    SecurityConfig getSecurity();
}
