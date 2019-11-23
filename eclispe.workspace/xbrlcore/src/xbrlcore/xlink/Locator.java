package xbrlcore.xlink;

import xbrlcore.taxonomy.Concept;

/**
 * This class represents a locator within a linkbase.<br/><br/> A locator can
 * either refer to a concept of a taxonomy or to a resource within another
 * linkbase. <br/> <br/>
 * 
 * @author Daniel Hamm
 */
public class Locator extends ExtendedLinkElement {

	private static final long serialVersionUID = 3488018552010465175L;

	private final Concept concept;
	private final Resource resource;

    // for the definition linkbase
    private boolean usable = true;

	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param label
	 *            Label of the locator.
	 * @param linkbaseSource 
     * @param extLinkRole
     * @param role
     * @param title
     * @param concept
	 */
	public Locator(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, Concept concept) {
		super(id, label, linkbaseSource, extLinkRole, role, title);

		this.concept = concept;
		this.resource = null;
	}

    /**
     * Constructor.
     * 
     * @param id
     * @param label
     *            Label of the locator.
     * @param linkbaseSource 
     * @param extLinkRole
     * @param role
     * @param title
     * @param resource
     */
    public Locator(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, Resource resource) {
        super(id, label, linkbaseSource, extLinkRole, role, title);

        this.concept = null;
        this.resource = resource;
    }

	/**
	 * Checks if two locators are equal. This is true if and only if: <br/>-
	 * their label attributes are equal <br/>- they are in the same extended
	 * link role <br/>- they both refer either to the same concept or the same
	 * resource
	 * 
	 * @param obj
	 *            The object the current Locator is checked against.
	 * @return True if both locators are equal, false otherwise.
	 */
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Locator))
			return false;
        if (!super.equals(obj))
            return false;

		Locator otherLocator = (Locator) obj;
		return getLabel().equals(otherLocator.getLabel())
				&& (getExtendedLinkRole() == null ? otherLocator
						.getExtendedLinkRole() == null : getExtendedLinkRole()
						.equals(otherLocator.getExtendedLinkRole()))
				&& (concept == null ? otherLocator.getConcept() == null
						: concept.equals(otherLocator.getConcept()))
				&& (resource == null ? otherLocator.getResource() == null
						: resource.equals(otherLocator.getResource()))
				&& (getLinkbaseSource() == null ? otherLocator
						.getLinkbaseSource() == null : getLinkbaseSource()
						.equals(otherLocator.getLinkbaseSource()));
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash * 31 + getLabel().hashCode();
		hash = hash
				* 31
				+ (getExtendedLinkRole() != null ? getExtendedLinkRole()
						.hashCode() : 0);
		hash = hash * 31 + (concept != null ? concept.hashCode() : 0);
		hash = hash * 31 + (resource != null ? resource.hashCode() : 0);
		hash = hash
				* 31
				+ (getLinkbaseSource() != null ? getLinkbaseSource().hashCode()
						: 0);
		return hash;
	}

	/**
	 * @return true
	 */
    @Override
	public boolean isLocator() {
		return true;
	}

    /**
     * @return true if there is an associated resource to the locator
     */
    @Override
    public boolean isResource() {
        return resource != null;
    }

	/**
	 * @return Concept this locator refers to.
	 */
	public final Concept getConcept() {
		return concept;
	}

	/**
	 * @return xbrldt:usable attribute of the according arc.
	 */
	public final boolean isUsable() {
		return usable;
	}

	/**
	 * @param b
	 *            xbrldt:usable attribute of the according arc.
	 */
	public void setUsable(boolean b) {
		usable = b;
	}

	/**
	 * @return Resource this locator refers to.
	 */
	public final Resource getResource() {
		return resource;
	}
}
