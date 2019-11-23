package xbrlcore.xlink;

import java.io.Serializable;

import xbrlcore.constants.GeneralConstants;

/**
 * This class is an abstract super class for possible extended link elements,
 * namely locators and resources. <br/><br/>
 * 
 * @author Daniel Hamm
 */
public abstract class ExtendedLinkElement implements Serializable {

	private static final long serialVersionUID = -6966857625345165480L;

    private final String id;

	private final String label;

    private final String linkbaseSource;

    private final String extendedLinkRole;

	private final String role;

	private final String title;

	/**
	 * Constructor.
	 * 
     * @param id
	 * @param label
	 *            Label for this extended link element.
	 * @param linkbaseSource 
     * @param extLinkRole
     * @param role
     * @param title
	 */
	public ExtendedLinkElement(String id, String label, String linkbaseSource, String extLinkRole, String role, String title) {
        this.id = id;
		this.label = label;
		this.linkbaseSource = linkbaseSource;
		this.extendedLinkRole = extLinkRole;
		this.role = role;
		this.title = title;
	}

	/**
	 * Checks whether two extended link elements are equal. This is true if and
	 * only if: <br/>- both must be either locators or resources <br/>- both
	 * have the same label attribute <br/>- both have the same role attribute
	 * <br/>- both have the same title attribute <br/>- both have the same id
	 * attribute <br/>- both belong to the same extended link role <br/>
	 * 
	 * @param obj
	 *            The object the current ExtendedLinkElement is checked against.
	 * @return True if both objects are equal, false otherwise.
	 */
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExtendedLinkElement))
			return false;
		ExtendedLinkElement otherElement = (ExtendedLinkElement) obj;
		return isLocator() == otherElement.isLocator()
				&& isResource() == otherElement.isResource()
				&& label.equals(otherElement.getLabel())
				&& (role == null ? otherElement.getRole() == null : role
						.equals(otherElement.getRole()))
				&& (title == null ? otherElement.getTitle() == null : title
						.equals(otherElement.getTitle()))
				&& (id == null ? otherElement.getID() == null : id
						.equals(otherElement.getID()))
				&& (extendedLinkRole == null ? otherElement
						.getExtendedLinkRole() == null : extendedLinkRole
						.equals(otherElement.getExtendedLinkRole()))
				&& (linkbaseSource == null ? otherElement.getLinkbaseSource() == null
						: linkbaseSource.equals(otherElement
								.getLinkbaseSource()));
	}

    public boolean equalsIgnoringLinkbaseSource(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ExtendedLinkElement))
            return false;
        ExtendedLinkElement otherElement = (ExtendedLinkElement) obj;
        return isLocator() == otherElement.isLocator()
                && isResource() == otherElement.isResource()
                && label.equals(otherElement.getLabel())
                && (role == null ? otherElement.getRole() == null : role
                        .equals(otherElement.getRole()))
                && (title == null ? otherElement.getTitle() == null : title
                        .equals(otherElement.getTitle()))
                && (id == null ? otherElement.getID() == null : id
                        .equals(otherElement.getID()))
                && (extendedLinkRole == null ? otherElement
                        .getExtendedLinkRole() == null : extendedLinkRole
                        .equals(otherElement.getExtendedLinkRole()));
    }

    public boolean represents(String label, String extendedLinkRole, String linkbaseSource) {
        if (extendedLinkRole == null) {
            extendedLinkRole = GeneralConstants.XBRL_LINKBASE_DEFAULT_LINKROLE;
        }
        if (this.getLabel().equals(label) && this.getExtendedLinkRole().equals(extendedLinkRole)) {
            /*
             * check if they are from the same linkbase file, only if
             * linkbaseSource is set
             */
            if ((linkbaseSource != null && this.getLinkbaseSource().equals(linkbaseSource))
                    || linkbaseSource == null) {

                return true;
            }

        }
        
        return false;
    }

	/**
	 * @return Returns a hash code for this object.
	 */
    @Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + label.hashCode();
		hash = hash * 31 + (role != null ? role.hashCode() : 0);
		hash = hash * 31 + (title != null ? title.hashCode() : 0);
		hash = hash * 31 + (id != null ? id.hashCode() : 0);
		hash = hash * 31
				+ (extendedLinkRole != null ? extendedLinkRole.hashCode() : 0);
		return hash;
	}

    /**
     * 
     * @return true if this element is a locator, false otherwise.
     */
    public abstract boolean isLocator();

    /**
     * 
     * @return true if this element is a resource, false otherwise.
     */
    public abstract boolean isResource();

    /**
     * @return The ID of this element.
     */
    public final String getID() {
        return id;
    }

    /**
     * @return The ID of this element.
     * @deprecated replaced by {@link #getID()} for streamlining reasons.
     */
    @Deprecated
    public final String getId() {
        return getID();
    }

    /**
     * @return xlink:label attribute of this extended link element.
     */
    public final String getLabel() {
        return label;
    }

    /**
     * @return Returns the linkbaseSource.
     */
    public final String getLinkbaseSource() {
        return linkbaseSource;
    }

    /**
     * @return Extended link role in which this element appears.
     */
    public final String getExtendedLinkRole() {
        return extendedLinkRole;
    }

	/**
	 * @return xlink:role attribute of this extended link element.
	 */
	public final String getRole() {
		return role;
	}

	/**
	 * @return xlink:title attribute of this extended link element.
	 */
	public final String getTitle() {
		return title;
	}
}
