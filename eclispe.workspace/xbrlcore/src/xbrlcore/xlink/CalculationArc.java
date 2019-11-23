package xbrlcore.xlink;

/**
 * This class represents an arc in a calculation linkbase file. <br/><br/>
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class CalculationArc extends Arc {

    private static final long serialVersionUID = -7879980567425026611L;

	/**
	 * Constructor.
	 * 
     * @param sourceElement
     * @param targetElement
     * @param arcRole
     *            Arc role of the arc.
     * @param extendedLinkRole
     *            Extended link role of the arc.
     * @param targetRole
     * @param contextElement
     * @param attributes
     * @param title
     * @param order
     * @param useAttribute
     * @param priorityAttribute
     * @param weightAttribute
	 */
	public CalculationArc(ExtendedLinkElement sourceElement, ExtendedLinkElement targetElement,
	                      String arcRole, String extendedLinkRole, String targetRole,
	                      String contextElement, AttributesImplSer attributes, String title,
	                      float order, UseAttribute useAttribute, int priorityAttribute, float weightAttribute) {
		super(sourceElement, targetElement,
              arcRole, extendedLinkRole, targetRole,
              contextElement, attributes, title,
              order, useAttribute, priorityAttribute, weightAttribute);
	}
}
