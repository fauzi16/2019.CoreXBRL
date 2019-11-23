package xbrlcore.taxonomy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import xbrlcore.logging.LogInterface;
import xbrlcore.logging.LogInterface.LogLevel;

/**
 * This class provides an improved xbrlcore FileLoader for taxonomies : <br>
 *  <ul>
 *  	<li>the given file is searched first in the current directory then in a list of folders (stored in a properties file)
 *  	<li>this FileLoader has caching capabilities and serializes objects in a gzipped file
 *  </ul> 
 * 
 * @author Nicolas Georges
 *
 */
public class GZipCachingFileLoader extends FileLoader {
	private static final Class<?> LOG_CHANNEL = GZipCachingFileLoader.class;
	private final LogInterface log;
	private final String cacheFolder;
	private List<String> folders = new LinkedList<String>();
    
    public GZipCachingFileLoader(LogInterface log) {
        this(log, null);
    }
    
	public GZipCachingFileLoader(LogInterface log, String cacheFolder) {
	    this.log = log;
		java.util.Properties props = new java.util.Properties();
		java.net.URL url = ClassLoader.getSystemResource("fileLoader.properties");
		boolean cacheFolderSet = (cacheFolder != null);
		if (!cacheFolderSet)
		    cacheFolder = "cache";
		try {
			InputStream iStream = null;			
			File fileLoaderPropertiesFile = new File(new File(".").getCanonicalPath(), "fileLoader.properties"); 
			if(fileLoaderPropertiesFile.exists()){
				iStream = new FileInputStream(fileLoaderPropertiesFile);
			}
			else{
				iStream = url.openStream();
			}
			
			props.load(iStream);
			int count = Integer.parseInt( props.get("folder.count").toString() );			
			Object property;
			for(int i=0;i<count;i++){
				property = props.get("folder["+i+"]");
				if(property!=null){
					String folder = property.toString();
					folder = folder.replaceAll("\"", "");
					if(folder.charAt( folder.length()-1 )!= File.separatorChar){
						folder += File.separatorChar;
					}
					folders.add( folder );					
				}
			}
			if (!cacheFolderSet) {
			    if(props.get("cache.folder")!=null){
			        cacheFolder = props.get("cache.folder").toString();
			        if(cacheFolder==null) cacheFolder = "cache";
			    }
			}
			cacheFolder = cacheFolder.replaceAll("\"", "");
			if(cacheFolder.charAt( cacheFolder.length()-1 )!= File.separatorChar){
				cacheFolder += File.separatorChar;
			}			
				
		} catch (Exception e) {	//Exception au lieu de IOException : quand fichier absent : NullPointerException
			e.printStackTrace();
			if (log != null){
	            log.log(LogLevel.ERROR, LOG_CHANNEL, "Exception during fileLoader.properties loading !");
			    log.log(LogLevel.ERROR, LOG_CHANNEL, "An error occured during the taxonomy loader initialization ! (possible access rights problem with the fileLoader.properties file)");
			}
		}
		
		this.cacheFolder = cacheFolder;
	}
	
	/**
	 * Try to get a file in the given folder. If the file is not found in the folder, 
	 * it is searched in a list of possible folders given in the Fileloader.properties file.
	 * 
	 * @return the found file or null if the file was not found
	 */
	/* FIXME
	@Override
	public URL getFile(String basePath, String namespace, String schemaLocation) throws MalformedURLException {
		URL url = super.getFile(basePath, namespace, schemaLocation);
		if(exists(url))
		    return url;
		
		String fileName = new File(schemaLocation).getName();
		
		//Try to look in the Folder list
		for(int i=0;i<folders.size();i++){
			File file = new File(folders.get(i) + fileName);
			if(file.exists())
			    return file.toURI().toURL();
		}
		
		//Not found, so return the non existing file as asked. 
		return new File("qwdokqopwdjkwgoiwefijwefjiwef").toURI().toURL();
	}
	*/
	
	private static boolean exists(URL url) {
        boolean result = false;
	    try {
    	    InputStream in = url.openStream();
    	    if (in != null) {
    	        result = true;
    	        in.close();
    	    }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    return result;
	}
	
	/**
	 * Serialize or deserialize an object.
	 * 
	 * @param mode "load" or "save"
	 * @param object object to (de)serialize
	 * @param key name of the caching file. It will be placed in the cache folder with a .cwz extension
	 * @return the object (de)serialized
	 */
	public Object serializeObject(String mode, Object object, String key){
		key = cacheFolder + key + ".cwz";
		if(mode.equals("save")){
			//Save mode
			if(new File(key).exists()){ return null; }
			FilterOutputStream cwpout = null;
			GZIPOutputStream gzos;
			ObjectOutputStream oos;
			try{
				//if the cache dir does not exists we (try to) create it first
				File fileCacheFolder = new File(cacheFolder);
				if(!fileCacheFolder.exists())
					fileCacheFolder.mkdirs();

				log.log(LogLevel.INFO, LOG_CHANNEL, "Caching file " + key);
				FileOutputStream fout = new FileOutputStream(key);	
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				//GZIPOutputStream gzos= new GZIPOutputStream(cwpout);
				cwpout = getOutputStream(bout, -1, String.format("Compressing data for : %s", key.substring(key.lastIndexOf('\\')+1)));
				gzos = new GZIPOutputStream(cwpout);
				oos = new ObjectOutputStream(gzos);
				oos.writeObject(object);
				oos.close();
				/*CWOutputProgressMonitor*/ cwpout = getOutputStream(fout, bout.size(), String.format("Saving cache file : %s", key.substring(key.lastIndexOf('\\')+1)));
				cwpout.write(bout.toByteArray());
				cwpout.close();
                log.log(LogLevel.INFO, LOG_CHANNEL, "File " + key + " cached.");
			}
			catch (Exception e){
				e.printStackTrace();
				if (cwpout != null)
					try {
						cwpout.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				object = null;
			}
		}else{
			//Load mode
			FilterInputStream cwpin = null;
			GZIPInputStream gzis;
			ObjectInputStream ois;
			
	        if(!new File(key).exists()){ return null; }
			try {
                log.log(LogLevel.INFO, LOG_CHANNEL, "Loading cached file " + key);
			    FileInputStream fin = new FileInputStream(key);
			    cwpin = getInputStream(fin, String.format("Loading cache file : %s", key.substring(key.lastIndexOf('\\')+1)));  
			    gzis = new GZIPInputStream(cwpin);
			    ois = new ObjectInputStream(gzis);
			    object = ois.readObject();
			    ois.close();
                log.log(LogLevel.INFO, LOG_CHANNEL, "Cached file " + key + " loaded.");
			} catch (InvalidClassException ice){
				if (cwpin != null)
					try {
						cwpin.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				log.log(LogLevel.DEBUG, LOG_CHANNEL, "obsolete cache file for " + key);
				new File(key).delete();
			} catch (Exception e) { 
				e.printStackTrace();
				if (cwpin != null)
					try {
						cwpin.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				object = null;
			}			
		}
		return object;
	}

	/**
	 * This is, where to return something like CWOutputProgressMonitor in a subclass.
	 * 
	 * @param out
	 * @param size
	 * @param title
	 * @return
	 */
	protected FilterOutputStream getOutputStream(OutputStream out, int size, String title) {
	    return new FilterOutputStream(out);
	}

    /**
     * This is, where to return something like CWInputProgressMonitor in a subclass.
     * 
     * @param in
     * @param title
     * @return
     */
    protected FilterInputStream getInputStream(InputStream in, String title) {
        return new FilterInputStream(in) {
        };
    }
	
	/**
	 * Getter for the cache folder 
	 * @return the name of the folder
	 */
	public final String getCacheFolder() {
		return cacheFolder;
	}

	/**
	 * Getter for the list of folders where the files to load are searched
	 * @return a list of paths (Strings)
	 */
	public List<String> getFolders() {
		return folders;
	}	
}