package xbrlcore.linkbase;

import java.util.ArrayList;
import java.util.List;

import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.ExtendedLinkElement;

/**
 * This class represents the calculation linkbase of a DTS. This linkbase
 * defines calculation rules between concepts.<br />
 * This class keeps the complete structure from {@link PresentationLinkbase}
 * with weights from {@link CalculationLinkbase} where available.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class CalculationPresentationLinkbase extends Linkbase {

    private static final long serialVersionUID = -5499069447977515624L;

    public static final String NAME = "calculationpresentation";

    public static final String DEFAULT_ARC_ROLE = null; // TODO

    private boolean linkbaseBuilt = false;

    /**
     * Constructor.
     * 
     * @param dts
     *            Discoverable Taxonomy Set this linkbase belongs to.
     */
    public CalculationPresentationLinkbase(DiscoverableTaxonomySet dts) {
        super(dts, NAME);
    }

    @Override
    public void buildLinkbase() {
        if (linkbaseBuilt)
            return;

        DiscoverableTaxonomySet dts = getDiscoverableTaxonomySet();
        PresentationLinkbase plb = dts.getPresentationLinkbase();
        CalculationLinkbase clb = dts.getCalculationLinkbase();

        for (String extendedLinkRole : plb.getExtendedLinkRoles())
            addExtendedLinkRole(extendedLinkRole);

        for (String elr : plb.arcBaseSets.keySet()) {
            List<Arc> thisArcs = new ArrayList<Arc>();
            this.arcBaseSets.put(elr, thisArcs);
            List<Arc> calcArcs = clb.getArcBaseSet(elr);
            for (Arc presArc : plb.arcBaseSets.get(elr)) {
                boolean found = false;
                if (calcArcs != null) {
                    for (Arc calcArc : calcArcs) {
                        if (calcArc.equalsIgnoringLinkbaseSourceAndWeight(presArc)) {
                            thisArcs.add(calcArc);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found)
                    thisArcs.add(presArc);
            }
        }

        /*
        for (String elr : plb.overridenArcs.keySet()) {
            List<Arc> thisArcs = new ArrayList<Arc>();
            this.overridenArcs.put(elr, thisArcs);
            List<Arc> calcArcs = clb.getArcBaseSet(elr);
            for (Arc presArc : plb.overridenArcs.get(elr)) {
                boolean found = false;
                if (calcArcs != null) {
                    for (Arc calcArc : calcArcs) {
                        if (calcArc.equalsIgnoringLinkbaseSourceAndWeight(presArc)) {
                            thisArcs.add(calcArc);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found)
                    thisArcs.add(presArc);
            }
        }

        for (String elr : plb.prohibitingArcs.keySet()) {
            List<Arc> thisArcs = new ArrayList<Arc>();
            this.prohibitingArcs.put(elr, thisArcs);
            List<Arc> calcArcs = clb.getArcBaseSet(elr);
            for (Arc presArc : plb.prohibitingArcs.get(elr)) {
                boolean found = false;
                if (calcArcs != null) {
                    for (Arc calcArc : calcArcs) {
                        if (calcArc.equalsIgnoringLinkbaseSourceAndWeight(presArc)) {
                            thisArcs.add(calcArc);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found)
                    thisArcs.add(presArc);
            }
        }

        for (String elr : plb.prohibitedArcs.keySet()) {
            List<Arc> thisArcs = new ArrayList<Arc>();
            this.prohibitedArcs.put(elr, thisArcs);
            List<Arc> calcArcs = clb.getArcBaseSet(elr);
            for (Arc presArc : plb.prohibitedArcs.get(elr)) {
                boolean found = false;
                if (calcArcs != null) {
                    for (Arc calcArc : calcArcs) {
                        if (calcArc.equalsIgnoringLinkbaseSourceAndWeight(presArc)) {
                            thisArcs.add(calcArc);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found)
                    thisArcs.add(presArc);
            }
        }
        */

        // Make sure, all non-arced elements are in the list.
        for (ExtendedLinkElement xlElem : plb.getExtendedLinkElements()) {
            if (!getExtendedLinkElements().contains(xlElem))
                addExtendedLinkElement(xlElem);
        }

        linkbaseBuilt = true;
    }

    @Override
    public String getDefaultArcRole() {
        return DEFAULT_ARC_ROLE;
    }
}
