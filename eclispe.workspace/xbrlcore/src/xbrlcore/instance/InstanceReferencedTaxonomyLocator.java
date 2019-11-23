package xbrlcore.instance;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import xbrlcore.exception.TaxonomyCreationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.taxonomy.AbstractTaxonomyLocator;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.taxonomy.TaxonomySchema;

/**
 * Locates taxonomy files.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class InstanceReferencedTaxonomyLocator implements AbstractTaxonomyLocator<DiscoverableTaxonomySet, TaxonomySchema> {
    private final xbrlcore.taxonomy.sax.SAXBuilder xbrlBuilder;

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
            if (base == null)
                return xbrlBuilder.build(new InputSource(taxonomyResource));
            InputSource inputSource = new InputSource(new URL(base, taxonomyResource).openStream());
            if(inputSource.getSystemId() == null)
            	inputSource = new InputSource(base.getFile() + taxonomyResource);
            return xbrlBuilder.build(inputSource);
        } catch (SAXException e) {
            throw new XBRLException(e);
        }
    }
}
