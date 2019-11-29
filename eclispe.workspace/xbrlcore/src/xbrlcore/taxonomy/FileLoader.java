package xbrlcore.taxonomy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import sun.security.action.GetLongAction;
import sun.util.logging.resources.logging;
import xbrlcore.util.PathResolver;

/**
 * Base class to get a taxonomy / instance file
 * 
 * It can be extended in order to load files from other location than the "Instance directory".
 *
 * @author Nicolas Georges
 * @author Sébastien Kirche
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class FileLoader {
	
	private final Proxy proxy;

    public FileLoader() {
        this(null);
    }

    public FileLoader(Proxy proxy) {
        this.proxy = proxy;
    }

    public final Proxy getProxy() {
        return proxy;
    }

    
    URL getFileURL(String basePath, String namespace, String schemaLocation) throws MalformedURLException {
    	
    	String separator = PathResolver.separator(schemaLocation);
    	
    	int p = schemaLocation.lastIndexOf(separator);
    	
    	if (p < 0){
    		//workaround if not an url (seen in some cases)
    		File local = new File(basePath + schemaLocation);
    		if(local.exists())//<===================================================
    			return local.toURI().toURL();
    	}
    	
        if ((schemaLocation.indexOf("://") > 0) || schemaLocation.startsWith("file:")){
        	
            if (basePath.indexOf("://") >= 0)
                return getCanonicalURL(basePath, schemaLocation);

        	if(p > 0 && schemaLocation.startsWith("http")){
        		//get the file base name
        		String schemaBase = schemaLocation.substring(p+1);
        		File local = new File(basePath + schemaBase);
        		if(local.exists())//<=================================================
        			return local.toURI().toURL();
        	}
        	
            return new URL(schemaLocation);
        }

        if (schemaLocation.startsWith("../"))
            return new File(getCanonicalFile(basePath, schemaLocation)).toURI().toURL();

        File f = new File(schemaLocation);
        if (f.isAbsolute())
            return f.toURI().toURL();

        if (namespace != null)
            return getURL(namespace, schemaLocation);

        if (basePath.indexOf("://") >= 0)
            return new URL(new URL(basePath), schemaLocation);

        return new File(basePath, schemaLocation).toURI().toURL();
    }

    public static URL getCanonicalURL(String path, String file) throws MalformedURLException {
        path = path.replace("\\", "/");
        
        if (path.endsWith( "/" ))
            path = path.substring(0, path.length() - 1);

        String separator = PathResolver.separator(path);
        while (file.startsWith("../")) {
            int p = path.lastIndexOf(separator);
            if (p < 0)
                return new URL(path + "/" + file);

            path = path.substring(0, p);
            file = file.substring(3);
        }

        return new URL(path + "/" + file);
    }

    public static String getCanonicalFile(String path, String file) {
        path = path.replace("\\", "/");

        if (path.endsWith( "/" ))
            path = path.substring(0, path.length() - 1);

        String separator = PathResolver.separator(path);
        while (file.startsWith("../")) {
            int p = path.lastIndexOf(separator);
            if (p < 0)
                return path + "/" + file;

            path = path.substring(0, p);
            file = file.substring(3);
        }

        return path + "/" + file;
    }

    public static final URL getURL(String namespace, String schemaLocation) throws MalformedURLException {
    	String separator = PathResolver.separator(namespace);
    	
    	int p = namespace.lastIndexOf(File.separator);
        if (p > 0)
            namespace = namespace.substring(0, p);

        return new URL(namespace + "/" + schemaLocation);
    }

    public BufferedInputStream getFileContent(URL url) throws IOException {
        URLConnection connection;
        if (getProxy() == null)
            connection = url.openConnection();
        else
            connection = url.openConnection(getProxy());
        
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection)connection;
            if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                httpConn.disconnect();
                
                return null;
            }
            if (httpConn.getContentType() == null || (!httpConn.getContentType().contains("application/xml") && !httpConn.getContentType().contains("text/xml")))
                return null;
        }

        return new BufferedInputStream(connection.getInputStream());
    }
    
}
