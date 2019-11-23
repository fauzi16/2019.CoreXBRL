package xbrlcore.xlink;

import java.io.Serializable;

import xbrlcore.constants.NamespaceConstants;
import xbrlcore.linkbase.LinkbaseArcsContainer.ArcInterface;

/**
 * This class represents an arc in a linkbase file. Its most important
 * attributes are a source and a target element as well as an arcrole indicating
 * the semantic meaning of the arc. <br/><br/>
 * 
 * @author Daniel Hamm
 */
public class Arc implements Serializable, Comparable<Arc>, ArcInterface {

	private static final long serialVersionUID = -8032592797402796322L;

	public static enum UseAttribute {
	    OPTIONAL,
	    PROHIBITED,
	    ;

	    public static final UseAttribute DEFAULT = UseAttribute.OPTIONAL; // The default of the use attribute is "optional" (Spec. 3.5.3.9.7.1)

	    public static UseAttribute parse(String string) {
	        /*
	         * use can only be "optional" or "prohibited", otherwise it is ignored
	         * (Spec. 3.5.3.9.7.1)
	         */
	        if ("optional".equals(string)) {
	            return UseAttribute.OPTIONAL;
	        } else if ("prohibited".equals(string)) {
	            return UseAttribute.PROHIBITED;
	        }

	        return null;
	    }
	}
	
	public static final int DEFAULT_PRIORITY_ATTRIBUTE = 0; // default of the priority attribute is "0" (Spec. 3.5.3.9.7.2)

	public static final float DEFAULT_ORDER = 0.0f;

    public static final float DEFAULT_WEIGHT = 1.0f;

    public static final AttributesImplSer createDefaultAttributes() { return new AttributesImplSer(); }
    
	private final ExtendedLinkElement sourceElement;

	private final ExtendedLinkElement targetElement;

    private final String arcRole;

    private final String extendedLinkRole;

	private final String targetRole;

	private final String contextElement;

	private final AttributesImplSer attributes;// = new AttributesImpl();

    private final String title;

	// TODO: include order in equals() and hashCode()?
	private final float order;

	private final UseAttribute useAttribute; // = UseAttribute.DEFAULT;

	private final int priorityAttribute; // = DEFAULT_PRIORITY_ATTRIBUTE;

    // TODO: include weightAttribute in equals() and hashCode()?
    private final float weightAttribute; // = DEFAULT_WEIGHT;

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
	public Arc(ExtendedLinkElement sourceElement, ExtendedLinkElement targetElement,
	           String arcRole, String extendedLinkRole, String targetRole,
	           String contextElement, AttributesImplSer attributes, String title,
	           float order, UseAttribute useAttribute, int priorityAttribute, float weightAttribute) {
		this.sourceElement = sourceElement;
		this.targetElement = targetElement;
	    this.arcRole = arcRole;
	    this.extendedLinkRole = extendedLinkRole;
	    this.targetRole = targetRole;
	    this.contextElement = contextElement;
	    this.attributes = attributes;
	    this.title = title;
	    this.order = order;
	    this.useAttribute = useAttribute;
	    this.priorityAttribute = priorityAttribute;
	    this.weightAttribute = weightAttribute;
	}

    /**
     * @return ArcRole of the arc.
     */
    @Override
    public final String getArcRole() {
        return arcRole;
    }

    /**
     * @return Extended link role of the arc.
     */
    @Override
    public final String getExtendedLinkRole() {
        return extendedLinkRole;
    }

    /**
     * @return XLinkElement object from which this arc is linked from.
     */
    public final ExtendedLinkElement getSourceElement() {
        return sourceElement;
    }
    
    /**
     * @return XLinkElement's label object from which this arc is linked from.
     */
    @Override
    public final String getSourceElementLabel() {
        return sourceElement.getLabel();
    }

    /**
     * @return XLinkElement object to which this arc is linked to.
     */
    public final ExtendedLinkElement getTargetElement() {
        return targetElement;
    }

    /**
     * @return XLinkElement's label object to which this arc is linked to.
     */
    @Override
    public final String getTargetElementLabel() {
        return targetElement.getLabel();
    }

    /**
     * @return Target role attribute (if specified).
     */
    public final String getTargetRole() {
        return targetRole;
    }

    /**
     * @return Order of the arc.
     */
    @Override
    public final float getOrder() {
        return order;
    }

    /**
     * @return Content of attribute "use".
     */
    @Override
    public final UseAttribute getUseAttribute() {
        return useAttribute;
    }

    /**
     * @return Attribute "priority" of this arc.
     */
    @Override
    public final int getPriorityAttribute() {
        return priorityAttribute;
    }

    @Override
    public int compareTo(Arc obj) {
        if (priorityAttribute < obj.priorityAttribute)
            return -1;

        if (priorityAttribute > obj.priorityAttribute)
            return 1;

        return 0;
    }

    /**
     * @return Returns the weightAttribute.
     */
    public final float getWeightAttribute() {
        return weightAttribute;
    }

    /**
     * @return Returns the contextElement.
     */
    public final String getContextElement() {
        return contextElement;
    }

    /**
     * @return Returns the attributes.
     */
    public final AttributesImplSer getAttributes() {
        return attributes;
    }

    /**
     * @return Returns the title.
     */
    public final String getTitle() {
        return title;
    }

    /**
     * @return Returns a hash code for this object.
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + sourceElement.hashCode();
        hash = hash * 31 + targetElement.hashCode();
        hash = hash * 31 + extendedLinkRole.hashCode();
        hash = hash * 31 + useAttribute.hashCode();
        hash = hash * 31 + priorityAttribute;
        return hash;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public String toString() {
        return "ELR: \"" + extendedLinkRole + "\"; Arcrole: \"" + arcRole
                + "\"; Title: \"" + title + "\"; From: \""
                + sourceElement.getLabel() + "\"; To: \""
                + targetElement.getLabel() + "\"";
    }

	/**
	 * Checks if two Arc objects are equal. This is true if and only if: <br/>-
	 * both have the same source element <br/>- both have the same target
	 * element <br/>- both are within the same extended link role <br/>- both
	 * have the same value in their "use" attribute <br/>- both have the same
	 * value in their "priority" attribute <br/>-the non-XLink attributes of
	 * both are s-equal
	 * 
	 * @param obj
	 *            The object the current Arc is checked against.
	 * @return True if both Arc objects are equal, false otherwise.
	 */
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Arc))
			return false;
		Arc otherArc = (Arc) obj;
		return sourceElement.equals(otherArc.getSourceElement())
				&& targetElement.equals(otherArc.getTargetElement())
				&& extendedLinkRole.equals(otherArc.getExtendedLinkRole())
				&& (useAttribute == otherArc.getUseAttribute())
				&& priorityAttribute == otherArc.getPriorityAttribute()
				&& hasSameAttributes(otherArc);
	}

    public boolean equalsIgnoringLinkbaseSourceAndWeight(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Arc))
            return false;
        Arc otherArc = (Arc) obj;
        return sourceElement.equalsIgnoringLinkbaseSource(otherArc.getSourceElement())
                && targetElement.equalsIgnoringLinkbaseSource(otherArc.getTargetElement())
                && extendedLinkRole.equals(otherArc.getExtendedLinkRole())
                && (useAttribute == otherArc.getUseAttribute())
                && priorityAttribute == otherArc.getPriorityAttribute()
                && hasSameAttributes(otherArc);
    }

	/**
	 * Checks if two Arc objects have the same source. This is true if and only
	 * if: <br/>- the source locators of both arcs refer to the same concept
	 * <br/>- both arcs are in the same extended link role
	 * 
	 * @param obj
	 *            The object the current Arc is checked against.
	 * @return True if the check succeeds, false otherwise.
	 */
    @Override
	public boolean equalSource(ArcInterface obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Arc))
			return false;
		Arc otherArc = (Arc) obj;

		if (sourceElement.isLocator()
				&& otherArc.getSourceElement().isLocator()) {
			Locator sourceLocator = (Locator) sourceElement;
			Locator otherSourceLoctator = (Locator) otherArc.getSourceElement();
			return sourceLocator.getConcept().equals(
					otherSourceLoctator.getConcept())
					&& extendedLinkRole.equals(otherArc.getExtendedLinkRole());
		}
		return false;
	}

	/**
	 * Checks if two Arc objects have the same target. This is true if and only
	 * if: <br/>- the target locators of both arcs refer to the same concept
	 * <br/>- both arcs are in the same extended link role
	 * 
	 * @param obj
	 *            The object the current Arc is checked against.
	 * @return True if the check succeeds, false otherwise.
	 */
    @Override
	public boolean equalTarget(ArcInterface obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Arc))
			return false;
		Arc otherArc = (Arc) obj;

		if (targetElement.isLocator()
				&& otherArc.getTargetElement().isLocator()) {
			Locator targetLocator = (Locator) sourceElement;
			Locator otherTargetLoctator = (Locator) otherArc.getSourceElement();
			return targetLocator.getConcept().equals(
					otherTargetLoctator.getConcept())
					&& extendedLinkRole.equals(otherArc.getExtendedLinkRole());
		}// else
		{
			return targetElement.equals(otherArc.getTargetElement())
					&& extendedLinkRole.equals(otherArc.getExtendedLinkRole());
		}
	}

	/**
	 * Checks if two Arc objects have the same source and the same target
	 * element. This is true if and only if: <br/>- the source locators of both
	 * arcs refer to the same concept <br/>- the target locators of both arcs
	 * refer to the same concept <br/>- both arcs are in the same extended link
	 * role
	 * 
	 * @param obj
	 *            The object the current Arc is checked against.
	 * @return True if the check succeeds, false otherwise.
	 */
    @Override
	public boolean equalSourceTarget(ArcInterface obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Arc))
			return false;
		Arc otherArc = (Arc) obj;

		if (sourceElement.isLocator() && targetElement.isLocator()
				&& otherArc.getSourceElement().isLocator()
				&& otherArc.getTargetElement().isLocator()) {
			Locator sourceLocator = (Locator) sourceElement;
			Locator targetLocator = (Locator) targetElement;
			Locator otherSourceLocator = (Locator) otherArc.getSourceElement();
			Locator otherTargetLocator = (Locator) otherArc.getTargetElement();
            return sourceLocator.getConcept().equals(
                    otherSourceLocator.getConcept())
                    && (targetLocator.getConcept() != null 
                    && targetLocator.getConcept().equals(
                            otherTargetLocator.getConcept()))
                    && extendedLinkRole.equals(otherArc.getExtendedLinkRole());
		}// else
		{
			return sourceElement.equals(otherArc.getSourceElement())
					&& targetElement.equals(otherArc.getTargetElement())
					&& extendedLinkRole.equals(otherArc.getExtendedLinkRole());
		}
	}

	/**
	 * This method checks if this arc overrides another arc.
	 * 
	 * @param overriddenArc
	 *            Arc which is probably overridden by this arc.
	 * @return True if this arc overrides overridden arc, otherwise false. The
	 *         implementation follows the Spec. chapter 3.5.3.9.7.4 and
	 *         3.5.3.9.7.5
	 */
    @Override
	public boolean overridesArc(ArcInterface overriddenArc) {
		Arc overriddenArc_ = (Arc) overriddenArc;

	    if (targetElement.isLocator()
				&& overriddenArc_.getTargetElement().isLocator()) {
			// a locator is overridden
			if (priorityAttribute >= overriddenArc_.getPriorityAttribute()
					&& isEquivalentRelationship(overriddenArc_)) {
				return true;
			}
		} else if (targetElement.isResource()
				&& overriddenArc_.getTargetElement().isResource()) {
			// TODO: a resource is overridden - this is not supported yet
		}
		return false;
	}

	/**
	 * This method checks if this arc prohibits another one.
	 * 
	 * @param prohibitedArc
	 *            Arc which is probably prohibited by this arc.
	 * @return True if this arc prohibits prohibitedArc, otherwise false. The
	 *         implementation follows the Spec. chapter 3.5.3.9.7.4 and
	 *         3.5.3.9.7.5
	 */
	@Override
	public boolean prohibitsArc(ArcInterface prohibitedArc) {
	    Arc prohibitedArc_ = (Arc) prohibitedArc;

		if ((useAttribute == UseAttribute.PROHIBITED)
				&& isEquivalentRelationship(prohibitedArc_)
				&& prohibitedArc_.getPriorityAttribute() <= priorityAttribute) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether two arcs form an "Equivalent Relationship" as described in
	 * the Spec. chapter 3.5.3.9.7.4.
	 * 
	 * @param equivalentArc
	 *            Arc against the relationship is checked.
	 * @return True if both arcs (this one and equivalentArc) from an
	 *         "Equivalent Relationship" as described in chapter 3.5.2.9.7.4 in
	 *         the Spec.
	 */
	public boolean isEquivalentRelationship(Arc equivalentArc) {
		return equalSourceTarget(equivalentArc)
				&& hasSameAttributes(equivalentArc);
	}

	/**
	 * Checks whether two arcs have s-equal non-XLink attributes (except "use"
	 * and "priority"), which is one requirement to be of equivalent
	 * relationship as described in Spec. chapter 3.5.3.9.7.4.
	 * 
	 * @param arcToCompare
	 *            Arc which attributes are compared to the attributes of this
	 *            arc.
	 * @return True if the value of any non-XLink attribute except "use" and
	 *         "priority" is s-equal to the same non-XLink attribute on the
	 *         other arc.
	 */
	public boolean hasSameAttributes(Arc arcToCompare) {
	    AttributesImplSer attributeListToCompare = arcToCompare.getAttributes();

		nextAttribute: for (int i = 0; i < attributes.getLength(); i++) {
            String currName = attributes.getLocalName(i);
            String currNS = attributes.getURI(i);
            String currValue = attributes.getValue(i);

            if (!currName.equals("use") && !currName.equals("priority")
                    && !currNS.equals(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI)) {
				/*
				 * there must be an s-equal attribute in the other arc, see
				 * Spec. 3.5.3.9.7.4
				 */
                for (int j = 0; j < attributeListToCompare.getLength(); j++) {
                    String compareName = attributeListToCompare.getLocalName(j);
                    String compareNS = attributeListToCompare.getURI(j);
                    String compareValue = attributeListToCompare.getValue(j);

                    if (compareName.equals(currName)
                            && compareNS.equals(currNS)
                            && compareValue.equals(currValue)) {
                        continue nextAttribute;
                    }
                }
                return false;
			}// else
				continue nextAttribute;
		}

		return true;
	}
}
