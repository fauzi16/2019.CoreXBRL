package xbrlcore.taxonomy;

import java.io.IOException;

import xbrlcore.exception.TaxonomyCreationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.logging.Log4jLogInterface;

/**
 * This class just serves backwards compatiblity.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
@Deprecated
public class DTSFactory extends DefaultTaxonomyLoader {

    private static DTSFactory dtsFactory;
    public static Log4jLogInterface logInterface = new Log4jLogInterface(DTSFactory.class);

    private DTSFactory() {
        super(new DefaultTaxonomyObjectFactory(), logInterface);
    }

    /**
     * Returns an object of the class DTSFactory. This is the only way to create
     * such an object, the constructor cannot be invoked.
     * 
     * @return An instance of this object.
     */
    public static synchronized DTSFactory get() {
        if (dtsFactory == null) {
            dtsFactory = new DTSFactory();
        }
        return dtsFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DiscoverableTaxonomySet finish(TaxonomySchema taxonomySchema, String taxonomyFileName) throws IOException, XBRLException, TaxonomyCreationException {
        DiscoverableTaxonomySet dts = super.finish(taxonomySchema, taxonomyFileName);

        dts.buildLinkbases();

        return dts;
    }
}
