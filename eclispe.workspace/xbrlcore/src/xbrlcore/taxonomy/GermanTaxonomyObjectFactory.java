package xbrlcore.taxonomy;

import xbrlcore.xlink.GermanResource;

/**
 * Default implementation of the {@link TaxonomyObjectFactory} for Germany used by {@link DefaultTaxonomyLoader}.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class GermanTaxonomyObjectFactory extends DefaultTaxonomyObjectFactory {

    @Override
    public GermanResource newResource(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, String lang, String value) {
        return new GermanResource(id, label, linkbaseSource, extLinkRole, role, title, lang, value);
    }
}
