package toolbox.TaxonomyBrowser;

import java.net.InetSocketAddress;

/**
 * Attempts to read proxy information from the system.
 * 
 * @author Marvin Froehlich
 */
public class Proxy {
    private final String protocol;
    private final String host;
    private final int port;
    private final java.net.Proxy proxy;

    private Proxy(java.net.Proxy.Type type, String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.proxy = new java.net.Proxy(type, new InetSocketAddress(host, port));
    }

    public final String getProtocol() {
        return protocol;
    }

    public final String getHost() {
        return host;
    }

    public final int getPort() {
        return port;
    }

    public final java.net.Proxy getProxy() {
        return proxy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (protocol == null)
            return host + ":" + port;

        return protocol + "://" + host + ":" + port;
    }

    public void setSystemProperties(boolean forSocks) {
        String proto = (getProtocol() == null) ? "" : getProtocol() + (forSocks ? "" : ".");
        
        System.setProperty(proto + "proxyHost", getHost());
        System.setProperty(proto + "proxyPort", String.valueOf(getPort()));
    }

    public final void setSystemProperties() {
        setSystemProperties(false);
    }

    public static Proxy parseProxy(java.net.Proxy.Type type, String string) {
        if (string == null)
            return null;

        String protocol = null;
        String host = null;
        int port = 80;

        int p0 = 0;
        int p = string.indexOf("://", p0);
        if (p >= 0) {
            protocol = string.substring(0, p);
            p0 = p + 3;
        }

        p = string.indexOf(":", p0);
        if (p >= 0) {
            host = string.substring(p0, p);
            p0 = p + 3;
            
            port = Integer.parseInt(string.substring(p + 1));
        } else {
            host = string.substring(0);
        }

        return new Proxy(type, protocol, host, port);
    }
}
