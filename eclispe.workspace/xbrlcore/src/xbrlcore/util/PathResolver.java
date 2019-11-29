package xbrlcore.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.InputSource;

import xbrlcore.exception.XBRLException;

public class PathResolver {

	public static String separator(String location) {
		/**
		 * if parameter contains windows file separator, so it should be windows path
		 */
		String separator = null;
		
		if (location.contains("\\"))
			separator = "\\";
		else
			separator = "/";

		return separator;
	}

	public static URI getDirURI(String systemId) {
		String separator = PathResolver.separator(systemId);
		
		String dirLocation = systemId.substring(0, 
				systemId.lastIndexOf(separator) + 1);
		if(systemId.startsWith("http")) {
			return URI.create(dirLocation);
		} else {
			try {
				return URI.create(dirLocation);
			} catch(Exception e) {
				return new File(dirLocation).toURI();
			}
		}
	}
	
	public static InputSource resolveInputSource(URL base, String taxonomyResource) throws XBRLException {
		try {
			if(taxonomyResource.startsWith("https:") || taxonomyResource.startsWith("http:") 
					|| taxonomyResource.startsWith("ftp:") || taxonomyResource.startsWith("ftps:")) {
				String localSystemId = resolveURIMapping(taxonomyResource);
				return new InputSource(localSystemId);
			} else {
				return new InputSource(new URL(base, taxonomyResource).openStream());
			}
		} catch (Exception e) {
			throw new XBRLException(e);
		}
	}
	
	private static String resolveURIMapping(String remotePath) {
		Map<String, String> uriMap = new HashMap<String, String>();
		
		/**
		 * Windows OS URI map
		uriMap.put("http://www.xbrl.org/", "C:\\Users\\fauzi16\\Documents\\Work\\Fujitsu\\ID.OJK\\2019.ID.XBRL\\ID.XBRL\\Source\\XRDM\\XRDM-Basic\\CONFIGXBRL\\RAPORTADO\\etc\\ojk\\taxonomy\\www.xbrl.org\\");
		uriMap.put("http://xbrl.ojk.go.id/taxonomy/", "C:\\Users\\fauzi16\\Documents\\Work\\Fujitsu\\ID.OJK\\2019.ID.XBRL\\ID.XBRL\\Source\\XRDM\\XRDM-Basic\\CONFIGXBRL\\EXTRACTEDDIR\\taxonomy\\");
		uriMap.put("http://xbrl.ojk.go.id/instance/", "C:\\Users\\fauzi16\\Documents\\Work\\Fujitsu\\ID.OJK\\2019.ID.XBRL\\ID.XBRL\\Source\\XRDM\\XRDM-Basic\\CONFIGXBRL\\INSTANCE\\");
		uriMap.put("http://xbrl.fujitsu.com", "C:\\Users\\fauzi16\\Documents\\Work\\Fujitsu\\ID.OJK\\2019.ID.XBRL\\ID.XBRL\\Source\\XRDM\\XRDM-Basic\\CONFIGXBRL\\RAPORTADO\\etc\\ojk\\taxonomy\\xbrl.fj.com\\");
		*/
		
		uriMap.put("http://xbrl.ojk.go.id/instance/", "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/base/INSTANCE/");
		uriMap.put("http://www.xbrl.org/", "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/base/taxonomy/global/www.xbrl.org/");
		uriMap.put("http://xbrl.ojk.go.id/taxonomy/", "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/base/taxonomy/");
		
		for(Entry<String, String> entry : uriMap.entrySet()) {
			if(remotePath.startsWith(entry.getKey())) {
				String result =  remotePath.replace(entry.getKey(), entry.getValue());
				if(!File.separator.equals("/")) {
					result = result.replaceAll("/", "\\\\");
				}
				return result;
			}
		}
		return remotePath;
	}
	
	public static void main(String[] args) {
		String filePath = "C:\\Users\\fauzi16\\testfile/image.png";
		System.out.println(filePath.replaceAll("/", ""));
	}
	
	

}
