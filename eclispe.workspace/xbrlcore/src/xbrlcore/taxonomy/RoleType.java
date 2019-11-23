package xbrlcore.taxonomy;

import java.io.Serializable;

/**
 * This class represents a RoleType within an XBRL Taxonomy as it is
 * described by the XBRL 2.1 Specification, which can be obtained from
 * http://www.xbrl.org/SpecRecommendations/. <br/><br/>
 * 
 * @author Donny Zhang
 */
public class RoleType implements Serializable {

	private static final long serialVersionUID = 6207994631304824560L;

    private final String id;

	private final String roleURI;

    private TaxonomySchema taxoSchema = null;

	private String[] definition;

    private String[] usedOn;

    public RoleType(String id, String roleURI) {
        this.id = id;
        this.roleURI = roleURI;
    }

    /**
     * 
     * @return ID of the RoleType.
     */
    public final String getID() {
        return id;
    }

    /**
     * 
     * @return ID of the RoleType.
     * @deprecated replaced by {@link #getID()} for streamlining reasons.
     */
    @Deprecated
    public final String getId() {
        return getID();
    }

    /**
     * 
     * @return roleURI of the RoleType.
     */
    public final String getRoleURI() {
		return roleURI;
	}

    void setTaxonomySchema(TaxonomySchema taxoSchema) {
        this.taxoSchema = taxoSchema;
    }

	/**
	 * Gets the owner {@link TaxonomySchema}.
	 * 
	 * @return the owner {@link TaxonomySchema}.
	 */
    public final TaxonomySchema getTaxonomySchema() {
	    return taxoSchema;
	}

    /**
     * 
     * @return definition list of the RoleType.
     */
	public final String[] getDefinition() {
		return definition;
	}

    /**
     * @param definition
     *            definition list of the RoleType.
     */
	public void setDefinition(String[] definition) {
		this.definition = definition;
	}

    /**
     * 
     * @return usedOn list of the RoleType.
     */
	public final String[] getUsedOn() {
		return usedOn;
	}

    /**
     * @param usedOn
     *            usedOn list of the RoleType.
     */
	public void setUsedOn(String[] usedOn) {
		this.usedOn = usedOn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return getClass().getSimpleName() + " ( " + getID() + ", " + getRoleURI() + " )";
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoleType)) {
            return false;
        }

        RoleType rt = (RoleType)o;

        return this.roleURI.equals(rt.roleURI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return roleURI.hashCode();
    }
}
