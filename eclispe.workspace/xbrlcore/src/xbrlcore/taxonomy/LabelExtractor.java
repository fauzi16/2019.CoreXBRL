package xbrlcore.taxonomy;

import xbrlcore.dimensions.Dimension;
import xbrlcore.dimensions.Hypercube;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.PresentationLinkbaseElement;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;
import xbrlcore.xlink.Resource;

/**
 * Extracts labels from known XBRL objects like {@link Concept}s.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class LabelExtractor {

    private LabelLinkbase llb;
    private String currentLang = null; // de, en

    public LabelExtractor(LabelLinkbase llb) {
        this(llb, null);
    }

    public LabelExtractor(LabelLinkbase llb, String language) {
        this.llb = llb;
        this.currentLang = language;
    }

    public void setLabelLinkbase(LabelLinkbase llb) {
        this.llb = llb;
    }

    public final LabelLinkbase getLabelLinkbase() {
        return llb;
    }

    public void setLanguage(String lang) {
        this.currentLang = lang;
    }

    public final String getLanguage() {
        return currentLang;
    }

    private final String _getLabel(Concept concept, String lang, boolean fallbackToDefault) {
        if (lang == null)
            return getLabelLinkbase().getLabel(concept, xbrlcore.constants.GeneralConstants.XBRL_ROLE_LABEL);

        String lbl = getLabelLinkbase().getLabel2(concept, lang, xbrlcore.constants.GeneralConstants.XBRL_ROLE_LABEL);

        if ((lbl == null) && fallbackToDefault)
            return _getLabel(concept, null, false);

        return lbl;
    }

    private final String _getLabel(Concept concept, boolean fallbackToDefault) {
        return _getLabel(concept, currentLang, fallbackToDefault);
    }

    public String getLabel(Object xbrlObj, boolean fallbackToDefault) {
        if (xbrlObj instanceof Concept)
            return _getLabel((Concept)xbrlObj, fallbackToDefault);

        if (xbrlObj instanceof PresentationLinkbaseElement)
            return _getLabel(( (PresentationLinkbaseElement)xbrlObj ).getConcept(), fallbackToDefault);

        if (xbrlObj instanceof Hypercube)
            return _getLabel(( (Hypercube)xbrlObj ).getConcept(), fallbackToDefault);

        if (xbrlObj instanceof Dimension)
            return _getLabel(( (Dimension)xbrlObj ).getConcept(), fallbackToDefault);

        if (xbrlObj instanceof ExtendedLinkElement) {
            ExtendedLinkElement extLinkElem = (ExtendedLinkElement)xbrlObj;
            if (extLinkElem.isLocator())
                return _getLabel(( (Locator)extLinkElem ).getConcept(), fallbackToDefault);
                //return extLinkElem.getLabel();

            if (extLinkElem.isResource())
                return ( (Resource)extLinkElem ).getLabel();

            return extLinkElem.getTitle();
        }

        if (xbrlObj instanceof Arc) {
            Arc arc = (Arc)xbrlObj;

            return arc.getSourceElement().getLabel() + " -> " + arc.getTargetElement().getLabel();
        }

        if (xbrlObj instanceof TaxonomySchema)
            return ( (TaxonomySchema)xbrlObj ).getName();

        if (xbrlObj instanceof RoleType) {
            String[] def = ((RoleType)xbrlObj).getDefinition();
            if ((def == null) || (def.length == 0)) {
                String id = ( (RoleType)xbrlObj ).getID();

                if (id.startsWith("role_"))
                    id = id.substring(5);

                return id;
            }

            return def[0];
        }

        return String.valueOf(xbrlObj);
    }

    public final String getLabel(Object xbrlObj) {
        return getLabel(xbrlObj, true);
    }
}
