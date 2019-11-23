package xbrlcore.linkbase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xbrlcore.constants.GeneralConstants;
import xbrlcore.exception.XBRLException;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.Locator;

/**
 * This class represents the calculation linkbase of a DTS. This linkbase
 * defines calculation rules between concepts. <br/><br/>
 * 
 * @author Daniel Hamm
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class CalculationLinkbase extends Linkbase {

	private static final long serialVersionUID = -8646600566684543133L;
	
	public static final String NAME = "calculation";

	public static final String DEFAULT_ARC_ROLE = null; // TODO

	/**
	 * Constructor.
	 * 
	 * @param dts
	 *            Discoverable Taxonomy Set this linkbase belongs to.
	 */
	public CalculationLinkbase(DiscoverableTaxonomySet dts) {
		super(dts, NAME);
	}

	@Override
	public void buildLinkbase() {
	}

	@Override
	public String getDefaultArcRole() {
	    return DEFAULT_ARC_ROLE;
	}

	/**
	 * This method returns calculation rules for a certain concept.
	 * 
	 * @param concept
	 *            Concept for which the calculation rules shall be obtained.
	 * @param extendedLinkRole
	 *            Extended link role from which calculation rules shall be
	 *            obtained.
	 * @return A List with {@link CalculationRule}s or <code>null</code>, if no rule is defined.
	 * @throws XBRLException 
	 */
	public List<CalculationRule> getCalculationRules(Concept concept, String extendedLinkRole)
			throws XBRLException {
		List<Arc> arcList = getArcsFromConcept(concept,
				GeneralConstants.XBRL_SUMMATION_ITEM_ARCROLE, extendedLinkRole);

        if (arcList == null) {
            // The concept is not part of the extended link role
            return null;
        }

        List<CalculationRule> result = new ArrayList<CalculationRule>();

		for (int i = 0; i < arcList.size(); i++) {
			Arc currArc = arcList.get(i);
			float currWeightAttribute = currArc.getWeightAttribute();
			Concept currTargetConcept = ((Locator) currArc.getTargetElement()).getConcept();
			result.add(new CalculationRule(currTargetConcept, currWeightAttribute));
		}
		return result;
	}

    /**
     * This method returns calculation rules for a certain concept. The key of
     * the returned map is the according concept of the calculation, the value
     * is the weight attribute determining the calculation rule. If there is no
     * rule defined, an empty Map is returned.
     * 
     * @param concept
     *            Concept for which the calculation rules shall be obtained.
     * @param extendedLinkRole
     *            Extended link role from which calculation rules shall be
     *            obtained.
     * @return A Map with concepts as keys and their according weight attribute
     *         (calc. rule) as values.
     * @throws XBRLException 
     * 
     * @deprecated use {@link #getCalculationRules(Concept, String)} instead.
     */
	@Deprecated
    public Map<Concept, Float> getCalculations(Concept concept, String extendedLinkRole)
            throws XBRLException {
        Map<Concept, Float> returnMap = new HashMap<Concept, Float>();

        Locator ex = getExtendedLinkElementFromBaseSet(concept, extendedLinkRole);
        if (ex == null) {
            // The concept is not part of the extended link role
            return returnMap;
        }

        List<Arc> arcList = getTargetArcsFromExtendedLinkElement(ex,
                GeneralConstants.XBRL_SUMMATION_ITEM_ARCROLE, extendedLinkRole);

        for (int i = 0; i < arcList.size(); i++) {
            Arc currArc = arcList.get(i);
            float currWeightAttribute = currArc.getWeightAttribute();
            Concept currTargetConcept = ((Locator) currArc.getTargetElement()).getConcept();
            returnMap.put(currTargetConcept, new Float(currWeightAttribute));
        }
        return returnMap;
    }
}
