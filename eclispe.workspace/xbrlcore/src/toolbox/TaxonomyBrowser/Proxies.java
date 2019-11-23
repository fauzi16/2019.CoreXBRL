package toolbox.TaxonomyBrowser;


/**
 * Attempts to read proxy information from the system.
 * 
 * @author Marvin Froehlich
 */
public class Proxies {
    private final Proxy http;
    private final Proxy https;
    private final Proxy ftp;
    private final Proxy socks;

    public final Proxy getHTTP() {
        return http;
    }

    public final Proxy getHTTPS() {
        return https;
    }

    public final Proxy getFTP() {
        return ftp;
    }

    public final Proxy getSOCKS() {
        return socks;
    }

    public int setSystemProperties() {
        int result = 0;

        if (getHTTP() != null) {
            getHTTP().setSystemProperties();
            result |= 1;
        }

        if (getHTTPS() != null) {
            getHTTPS().setSystemProperties();
            result |= 2;
        }

        if (getFTP() != null) {
            getFTP().setSystemProperties();
            result |= 4;
        }

        if (getSOCKS() != null) {
            getSOCKS().setSystemProperties(true);
            result |= 8;
        }

        return result;
    }

    private Proxies(Proxy http, Proxy https, Proxy ftp, Proxy socks) {
        this.http = http;
        this.https = https;
        this.ftp = ftp;
        this.socks = socks;
    }

    public static Proxies getProxies() {
        Proxy http = Proxy.parseProxy(java.net.Proxy.Type.HTTP, System.getenv("http_proxy"));
        Proxy https = Proxy.parseProxy(java.net.Proxy.Type.HTTP, System.getenv("https_proxy"));
        Proxy ftp = Proxy.parseProxy(java.net.Proxy.Type.HTTP, System.getenv("ftp_proxy"));
        Proxy socks = Proxy.parseProxy(java.net.Proxy.Type.SOCKS, System.getenv("socks_proxy"));

        return new Proxies(http, https, ftp, socks);
    }
}
