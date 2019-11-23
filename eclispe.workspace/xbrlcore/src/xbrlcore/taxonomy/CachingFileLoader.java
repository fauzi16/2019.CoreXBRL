package xbrlcore.taxonomy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

/**
 * <p>
 * The {@link CachingFileLoader} caches all externally (from the internet) loaded files
 * in a local temp folder. This avoids expensive loading of these files again and again.
 * </p>
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class CachingFileLoader extends FileLoader {
    private static final String NOT_FOUND_FILENAME_POSTFIX = "-not_found";

    private final File cacheFolder;
    private final boolean cacheLocalResources;

    public CachingFileLoader(File cacheFolder) {
        this(cacheFolder, null, false);
    }

    public CachingFileLoader(File cacheFolder, Proxy proxy) {
        this(cacheFolder, proxy, false);
    }

    public CachingFileLoader(File cacheFolder, boolean cacheLocalResources) {
        this(cacheFolder, null, cacheLocalResources);
    }

    public CachingFileLoader(File cacheFolder, Proxy proxy, boolean cacheLocalResources) {
        super(proxy);

        if (!cacheFolder.exists())
            throw new IllegalArgumentException("Cache folder \"" + cacheFolder.getAbsolutePath() + "\" not found.");

        if (!cacheFolder.canWrite())
            throw new IllegalArgumentException("Cache folder \"" + cacheFolder.getAbsolutePath() + "\" not writable.");

        this.cacheFolder = cacheFolder;
        this.cacheLocalResources = cacheLocalResources;
    }

    public final boolean getCacheLocalResources() {
        return cacheLocalResources;
    }

    private void downloadFile(URL src, File dst) throws IOException {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;

/*FIXME
        try {
            in = new BufferedInputStream(super.openConnection(src).getInputStream());
        } catch (FileNotFoundException e) {
            File f2 = new File(dst.getParentFile(), dst.getName() + NOT_FOUND_FILENAME_POSTFIX);
            f2.createNewFile();
            return;
        }
*/

        try {
            out = new BufferedOutputStream(new FileOutputStream(dst));

            byte[] buffer = new byte[1024];
            int n = 0;
            while ( ( n = in.read( buffer, 0, Math.min( buffer.length, in.available() + 1 ) ) ) >= 0 )
            {
                if ( n > 0 )
                    out.write( buffer, 0, n );
            }

        } finally {
            if (out != null)
                try { out.close(); } catch (IOException e) {}

            //if (in != null)
                try { in.close(); } catch (IOException e) {}
        }
    }

    /* FIXME
    @Override
    public URLConnection openConnection(URL url) throws IOException {
        if (cacheLocalResources || !url.getProtocol().equals("file")) {
            String name = url.getFile();
            File f = new File(cacheFolder, ((name.charAt(0) == '/') ? name.substring(1) : name).replace('/', File.separatorChar));
            // If the file doesn't already exist in the cache...
            if (!f.exists()) {
                // If it isn't marked as "not found" in the cache...
                File f2 = new File(f.getParentFile(), f.getName() + NOT_FOUND_FILENAME_POSTFIX);
                if (!f2.exists()) {
                    try {
                        f.getParentFile().mkdirs();
                        downloadFile(url, f);
                    } catch (IOException e) {
                        f.delete();
                        throw e;
                    }
                }
            }

            return f.toURI().toURL().openConnection();
        }

        return super.openConnection(url);
    }
    */
}
