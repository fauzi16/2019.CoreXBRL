package xbrlcore.taxonomy;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import xbrlcore.constants.ExceptionConstants;
import xbrlcore.exception.TaxonomyCreationException;

/**
 * This class represents a simple taxonomy schema. Since it is only the schema,
 * no linkbase can be obtained from this object. Linkbases can only be obtained
 * from a discoverable taxonomy set (DTS). <br/><br/>
 * 
 * @author Daniel Hamm
 */
public class TaxonomySchema implements Serializable {

	static final long serialVersionUID = 8105588643312248113L;

    private final DiscoverableTaxonomySet dts;

	private final String name; // name of the taxonomy

	private final Map<String, Concept> concepts = new HashMap<String, Concept>(); // all the elements in the taxonomy

    private final Map<String, RoleType> roleTypes = new HashMap<String, RoleType>(); // all the roleTypes in the taxonomy

	private Set<String> importedTaxonomyNames = new HashSet<String>(); // names of the taxonomies imported by this taxonomy

	private Namespace namespace;

	/*
	 * maps used extended link roles to the according linkbases in which they
	 * are used
	 */
	private String namespacePrefix;

	private String namespaceUri;

	/**
	 * Constructor.
	 * 
	 * @param dts
	 *            DTS this taxonomy schema belongs to.
	 * @param name the name
	 */
	public TaxonomySchema(DiscoverableTaxonomySet dts, String name) {
        this.dts = dts;
        this.name = name;
	}

	/**
	 * Adds a new element to the taxonomy.
	 * 
	 * @param concept
	 *            New element of the taxonomy.
	 * @throws TaxonomyCreationException
	 *             This exception is thrown if the ID is already available.
	 */
	public void addConcept(Concept concept) throws TaxonomyCreationException {
        //if (getConceptByID(concept.getId()) != null) {
        if (getConceptByName(concept.getName()) != null) {
            throw new TaxonomyCreationException(
                    ExceptionConstants.EX_DOUBLE_ELEMENT + " (" + concept.getName() + ")");
        }// else
        {
            concepts.put(concept.getID(), concept);
        }
	}

    /**
     * Adds a new RoleType to the taxonomy.
     * 
     * @param roleType
     *            New RoleType of the taxonomy.
     */
    public void addRoleType(RoleType roleType) {
        roleTypes.put(roleType.getRoleURI(), roleType);
        roleType.setTaxonomySchema(this);
    }

	/**
	 * Returns a Concept object to a specific ID within this taxonomy schema.
	 * 
	 * @param id
	 *            The ID for which the concept shall be obtained.
	 * @return The according Concept object from this taxonomy. If the concept
	 *         is not found in this taxonomy, null is returned.
	 */
	public Concept getConceptByID(String id) {
		/*
	    for (Concept tmpElement : concepts.values()) {
			if (tmpElement.getId().equals(id)) {
				return tmpElement;
			}
		}
		return null;
		*/
	    return concepts.get(id);
	}

	/**
	 * Returns a Concept object to a specific name within this taxonomy schema.
	 * 
	 * @param name
	 *            The name for which the element shall be obtained.
	 * @return The according Concept object from this taxonomy. If the concept
	 *         is not found in this taxonomy, null is returned.
	 */
	public Concept getConceptByName(String name) {
		for (Concept tmpElement : concepts.values()) {
			if (tmpElement.getName().equals(name)) {
				return tmpElement;
			}
		}
		return null;
	}

	/**
	 * Returns a list of all Concept objects belonging to a specified
	 * substitution group.
	 * 
	 * @param substitutionGroup
	 *            The substitution group all returned concepts shall belong to.
	 * @param result target collection of Concept objects of a certain substitution group.
	 */
	public void getConceptBySubstitutionGroup(String substitutionGroup, Collection<Concept> result) {
		for (Concept currElement : concepts.values()) {
			if (currElement.getSubstitutionGroup() != null
					&& currElement.getSubstitutionGroup().equals(
							substitutionGroup)) {
				result.add(currElement);
			}
		}
	}

    /**
     * Returns a list of all Concept objects belonging to a specified
     * substitution group.
     * 
     * @param substitutionGroup
     *            The substitution group all returned concepts shall belong to.
     * @return List of Concept objects of a certain substitution group.
     */
    public Set<Concept> getConceptBySubstitutionGroup(String substitutionGroup) {
        Set<Concept> resultSet = new HashSet<Concept>();
        getConceptBySubstitutionGroup(substitutionGroup, resultSet);

        return resultSet;
    }

	/**
	 * Adds the name of an imported taxonomy.
	 * 
	 * @param name
	 *            The name of the imported taxonomy.
	 */
	public void addImportedTaxonomyName(String name) {
		importedTaxonomyNames.add(name);
	}

	/**
	 * @return Returns a set of all concepts of this taxonomy schema (Concept
	 *         objects).
	 */
	public final Collection<Concept> getConcepts() {
		return concepts.values();
	}

    /**
     * @return Returns a map of all RoleTypes of this taxonomy schema (RoleType
     *         objects).
     */
    public final Map<String, RoleType> getRoleTypes() {
        return roleTypes;
    }

	/**
	 * @return Returns the name of the imported taxonomies of the current
	 *         taxonomy.
	 */
	public Set<String> getImportedTaxonomyNames() {
		return importedTaxonomyNames;
	}

	/**
	 * @return Returns the name of the current taxonomy.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @return Returns the namespace for this taxonomy schema.
	 */
	public final Namespace getNamespace() {
		return namespace != null ? namespace : Namespace.getNamespace(
				namespacePrefix, namespaceUri);
	}

	/**
	 * @param namespace
	 *            Namespace for this taxonomy schema.
	 */
	public void setNamespace(Namespace namespace) {
		this.namespace = namespace;
		namespacePrefix = namespace.getPrefix();
		namespaceUri = namespace.getURI();
	}

	/**
	 * @return Returns the Discoverable Taxonomy Set (DTS) this taxonomy schema
	 *         belongs to.
	 */
	public final DiscoverableTaxonomySet getDTS() {
		return dts;
	}

	/**
	 * Checks whether this schema (or one of its imported schemas) imports
	 * another schema. So this method follows the import hierarchy of different
	 * schemas.
	 * 
	 * @param importedTaxonomySchema
	 *            Schema which is checked whether it is imported by this schema
	 *            or one of its imported schemas.
	 * @return True if the given schema is imported, false otherwise.
	 */
	public boolean importsTaxonomySchema(TaxonomySchema importedTaxonomySchema) {
		return importedTaxonomySchema != null ? importedTaxonomyNames
				.contains(importedTaxonomySchema.getName()) : false;
	}

	/**
	 * @param importedTaxonomyNames
	 *            The importedTaxonomyNames to set.
	 */
	public void setImportedTaxonomyNames(Set<String> importedTaxonomyNames) {
		this.importedTaxonomyNames = importedTaxonomyNames;
	}

	/**
	 * Tests whether this TaxonomySchema object is equal to another object.
	 * Currently, it is only tested whether both names are equal.
	 * 
	 * @return True if the names of both schemas are identical.
	 */
	/* TODO: Probably the test should be more detailed */
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TaxonomySchema))
			return false;
		TaxonomySchema otherSchema = (TaxonomySchema) obj;
		return (name != null ? name.equals(otherSchema.getName()) : super
				.equals(obj));
	}

	/**
	 * @return Returns a hash code of this object.
	 */
    @Override
	public int hashCode() {
		return (name == null ? super.hashCode() : name.hashCode());
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ( getName() + " (" + getConcepts().size() + " concepts, " + roleTypes.size() + " roleTypes)" );
    }
}
