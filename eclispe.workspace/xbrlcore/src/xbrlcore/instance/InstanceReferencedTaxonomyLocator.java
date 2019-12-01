package xbrlcore.instance;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import xbrlcore.exception.TaxonomyCreationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.taxonomy.AbstractTaxonomyLocator;
import xbrlcore.taxonomy.DefaultTaxonomyLoader;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.taxonomy.TaxonomySchema;
import xbrlcore.util.PathResolver;

/**
 * Locates taxonomy files.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class InstanceReferencedTaxonomyLocator implements AbstractTaxonomyLocator<DiscoverableTaxonomySet, TaxonomySchema> {
    
	private final xbrlcore.taxonomy.sax.SAXBuilder xbrlBuilder;
	private final DefaultTaxonomyLoader taxonomyLoader =  new DefaultTaxonomyLoader();

    private final URL base;

    public InstanceReferencedTaxonomyLocator(URL base) {
        try {
            this.xbrlBuilder = new xbrlcore.taxonomy.sax.SAXBuilder();
        } catch (SAXException e) {
            throw new Error(e);
        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
        this.base = base;
    }

    @Override
    public DiscoverableTaxonomySet loadTaxonomy(String taxonomyResource)
                    throws IOException, TaxonomyCreationException, XBRLException {
    	
    	try {
    		DiscoverableTaxonomySet dts = _loadTaxonomy(taxonomyResource);
    		dts.getCalculationLinkbase().buildLinkbase();
    		dts.getLabelLinkbase().buildLinkbase();
    		dts.getDefinitionLinkbase().buildLinkbase();
    		dts.getPresentationLinkbase().buildLinkbase();
    		dts.getReferenceLinkbase().buildLinkbase();
    		return dts;
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.out.println("failed of first try to loading taxonomy");
    	}
    	
        try {
        	
            if (base == null)
                return xbrlBuilder.build(new InputSource(taxonomyResource));
            InputSource inputSource = PathResolver.resolveInputSource(base, taxonomyResource);
            if(inputSource.getSystemId() == null)
            	inputSource = new InputSource(base.getFile() + taxonomyResource);
            return xbrlBuilder.build(inputSource);
        } catch (SAXException e) {
            throw new XBRLException(e);
        }
    }
    
    public DiscoverableTaxonomySet _loadTaxonomy(String taxonomyResource) throws Exception {
    	InputSource inputSource = PathResolver.resolveInputSource(base, taxonomyResource);
    	File taxonomyFile = new File(inputSource.getSystemId());
    	return taxonomyLoader.loadTaxonomy(taxonomyFile, null);
    }
    
}
