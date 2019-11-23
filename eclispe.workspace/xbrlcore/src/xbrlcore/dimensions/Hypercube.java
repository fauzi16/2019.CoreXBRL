package xbrlcore.dimensions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import xbrlcore.constants.GeneralConstants;
import xbrlcore.taxonomy.Concept;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;

/**
 * This class represents an Hypercube as it is described by the Dimensions 1.0
 * Specification which can be obtained from
 * http://www.xbrl.org/SpecRecommendations/.<br/><br/> A Hypercube consists of
 * one or multiple dimensions which are represented by
 * xbrlcore.dimensions.Dimension objects. It describes which dimensions and
 * which domain a primary item can be reported for.<br/><br/> A Hypercube
 * always consists of an XBRL concept representing it and a set of Dimensions.
 * <br/><br/>
 * 
 * @author Daniel Hamm
 * @author Sébastien Kirche
 */
public class Hypercube implements Serializable {

	static final long serialVersionUID = 1068588695733245378L;

	private Concept concept; /* the concept the hypercube refers to */

	private String extendedLinkRole; /* when defining for a particular extended link role */
	
	private Map<String, Set<Dimension>> dimensionSet; /* Dimension objects */

	/**
	 * This method tests for "equality" between two Hypercube objects. They are
	 * equal if:<br/> - the concepts representing both Hypercubes are equal<br/> -
	 * the set of Dimensions of both Hypercubes are equal<br/>
	 * 
	 * @return True if both Hypercube objects are equal, false otherwise.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Hypercube))
			return false;
		Hypercube otherCube = (Hypercube) obj;
		return concept.equals(otherCube.getConcept())
				&& dimensionSet.equals(otherCube.getFullDimensionSet())
				&& (extendedLinkRole == null ? otherCube.getExtendedLinkRole() == null
						: extendedLinkRole.equals(otherCube
								.getExtendedLinkRole()));
	}

	/**
     * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + concept.hashCode();
		hash = hash * 31 + dimensionSet.hashCode();
		hash = hash * 31
				+ (extendedLinkRole != null ? extendedLinkRole.hashCode() : 0);
		return hash;
	}

	/**
	 * 
	 * @param concept
	 *            The Concept object which represents the hypercube.
	 */
	public Hypercube(Concept concept) {
		this.concept = concept;
		dimensionSet = new HashMap<String, Set<Dimension>>();
		extendedLinkRole = null;
	}

	/**
	 * Adds a dimension to the cube.
	 * 
	 * @param dimension The dimension which shall be added.
	 * @param extLinkRole the role that contains the dimension
	 */
	public void addDimension(Dimension dimension, String extLinkRole) {
		Set<Dimension> roleDims;
		roleDims = dimensionSet.get(extLinkRole);
		if (roleDims == null){
			roleDims = new HashSet<Dimension>();
			dimensionSet.put(extLinkRole, roleDims);
		}
		roleDims.add(dimension);
	}

	/**
	 * Gets the set of domain members to a dimension contained in the cube.
	 * 
	 * @param dimension The dimension for which the domain shall be returned.
	 * @param extLinkRole the role that contains the dimension
	 * @return A list with xbrlcore.xlink.ExtendedLinkElement objects
	 *         representing the domain of the dimension.
	 */
	public Set<ExtendedLinkElement> getDimensionDomain(Concept dimension, String extLinkRole) {
		/*TODO is it useful to map to a default role ?
		if (xbrlExtendedLinkRole == null) {
			xbrlExtendedLinkRole = GeneralConstants.XBRL_LINKBASE_DEFAULT_LINKROLE;
		}*/
		
		Set<Dimension> roleDims = dimensionSet.get(extLinkRole);
		if (roleDims == null)
			return null;
		
		for (Dimension currDimension : roleDims) {
			if (currDimension.getConcept().equals(dimension)) {
				return currDimension.getDomainMemberSet();
			}
		}
		return null;
	}

	/**
	 * Specifies whether a certain dimension is part of that cube.
	 * 
	 * @param dimension The Concept object representing the according dimension.
	 * @param extLinkRole the role that contains the dimension
	 * @return True if the dimension is part of that cube, otherwise false.
	 */
	public boolean containsDimension(Concept dimension, String extLinkRole) {
		Set<Dimension> roleDims = dimensionSet.get(extLinkRole);
		if (roleDims == null)
			return false;
		
		for (Dimension currDimension : roleDims) {
			if (currDimension.getConcept().equals(dimension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Specifies whether a certain dimension with a certain domain member is
	 * part of that cube. This method does not take into account whether the
	 * given domain member has an xbrldt:usable attribute or not (use
	 * containsUsableDimensionDomain instead).
	 * 
	 * @param dimension
	 *            The Concept object representing the according dimension.
	 * @param domainMember
	 *            The Concept object representing the according domain.
	 * @return True if the dimension/domain combination is part of that cube,
	 *         otherwise false. If the dimension is a typed dimension, the
	 *         method always returns true.
	 */
	public boolean containsDimensionDomain(Concept dimension, Concept domainMember, String extLinkRole){
		Set<Dimension> roleDims = dimensionSet.get(extLinkRole);
		if (roleDims == null)
			return false;
		
	    for (Dimension currDimension : roleDims) {
			if (currDimension.getConcept().equals(dimension)) {
				/*
				 * TODO: a typed dimension always returns "true", so ALL
				 * elements can be domain member of a typed dimension. Later it
				 * must be checked whether the element is compliant with the
				 * according schema element.
				 */
				return (currDimension.isTyped() ? true : currDimension
						.containsDomainMember(domainMember, false));
			}
		}
		return false;
	}

	/**
	 * Specifies whether a certain dimension with a certain domain member is
	 * part of that cube. This method takes into account whether the given
	 * domain member has an xbrldt:usable attribute or not. If the usable
	 * attribute is set to false, it returns false.
	 * 
	 * @param dimension
	 *            The Concept object representing the according dimension.
	 * @param domainMember
	 *            The Concept object representing the according domain.
	 * @return True if the dimension/domain combination is part of that cube,
	 *         otherwise false. If the dimension is a typed dimension, the
	 *         method always returns true.
	 */
	public boolean containsUsableDimensionDomain(Concept dimension,	Concept domainMember, String extLinkRole) {
		Set<Dimension> roleDims = dimensionSet.get(extLinkRole);
		if (roleDims == null)
			return false;

		for (Dimension currDimension : roleDims) {
			if (currDimension.getConcept().equals(dimension)) {
				/*
				 * TODO: a typed dimension always returns "true", so ALL
				 * elements can be domain member of a typed dimension. Later it
				 * must be checked whether the element is compliant with the
				 * according schema element.
				 */
				return (currDimension.isTyped() ? true : currDimension
						.containsDomainMember(domainMember, true));
			}
		}
		return false;
	}

	/**
	 * Adds all the dimensions / domain member of a second hypercube to this
	 * hypercube. This is done in the following way: If the second hypercube has
	 * a dimension which is not contained in this cube, the dimension and all
	 * its domain members are added to this cube. If the second hypercube has a
	 * dimension which is already contained in this cube, all domain members of
	 * this dimension of the second cube which are not part of the same
	 * dimension of this cube are added to this cube.
	 * 
	 * @param newCube The second cube whose dimension/domain members are added to
	 *            this cube.
	 * @param extLinkRole the extended link role from which we want to get the dimensions
	 * @throws CloneNotSupportedException
	 */
	@SuppressWarnings( "unchecked" )
    public void addHypercube(Hypercube newCube, String extLinkRole)
			throws CloneNotSupportedException {
		/* go through all the dimensions of newCube */
		for (Dimension newCubeDimension : newCube.getDimensionSet(extLinkRole)) {
			/* if it is contained in this cube, add only the domain members */
			if (containsDimension(newCubeDimension.getConcept(), extLinkRole)) {
				Dimension thisDimension = getDimension(newCubeDimension.getConcept(), extLinkRole);
				thisDimension
						.addDomainMemberSet((Set<ExtendedLinkElement>) ((HashSet<ExtendedLinkElement>) newCubeDimension
								.getDomainMemberSet()).clone());
			}
			/*
			 * if it is not contained in this cube, add it including all the
			 * domain members
			 */
			else {
				dimensionSet.get(extLinkRole).add(newCubeDimension.clone());
			}
		}
	}

	/**
	 * 
	 * @return String object describing this hypercube.
	 */
	@Override
	public String toString() {
		String id = (concept != null ? concept.getID() : "anonymous");
		String str = "Hypercube " + id + "\n";
		for (String role : dimensionSet.keySet()){
			str += "\textended link role:" + role + "\n";
			for (Dimension currDim : dimensionSet.get(role)) {
				str += "\t\tDimension: " + currDim.getConcept().getID() + "\n";
				for (ExtendedLinkElement currLink : currDim.getDomainMemberSet()) {
				    Concept currCon = ((Locator) currLink)
							.getConcept();
					str += "\t\t\tDomain Member: " + currCon.getID() + "\n";
				}
			}
		}
		str += "Hypercube " + id + " finished\n";
		return str;
	}

	/**
	 * Checks whether this hypercube contains specific dimension/domain member
	 * combinations.
	 * 
	 * @param mdt
	 *            Object describing the specific dimension/domain member
	 *            combinations for which this hypercube is checkted.
	 * @return True if and only if this hypercube contains all the
	 *         dimension/domain member combinations described in the given
	 *         MultipleDimensionType object. The hypercube also must not contain
	 *         additinal dimension/domain member combinations than those which
	 *         are contained in the given MultipleDimensinType object. Otherwise
	 *         false is returned.
	 */
	public boolean hasDimensionCombination(MultipleDimensionType mdt, String extLinkRole) {
		Map<Concept, Concept> dimensionDomainMap = mdt.getAllDimensionDomainMap();
		if (dimensionDomainMap.size() != dimensionSet.get(extLinkRole).size()) {
			return false;
		}
		Set<Map.Entry<Concept, Concept>> dimensionDomainEntrySet = dimensionDomainMap.entrySet();
		for (Map.Entry<Concept, Concept> currEntry : dimensionDomainEntrySet) {
			Concept currDimConcept = currEntry.getKey();
			Concept currDomConcept = currEntry.getValue();
			if (!containsUsableDimensionDomain(currDimConcept, currDomConcept, extLinkRole)) {
				return false;
			}
		}
		return true;
	}
	
	void sub (int arg){
		
	}

    /**
	 * Returns a certain dimension from the cube.
	 * 
	 * @param dimensionElement
	 *            Concept object representing the dimension.
	 * @param extLinkRole the extended link role that that parts the cube
	 * @return Dimension object matching the given Concept object.
	 */
	public Dimension getDimension(Concept dimensionElement, String extLinkRole) {
		Set<Dimension> roleDims = dimensionSet.get(extLinkRole);
		if (roleDims == null)
			return null;
		
		for (Dimension currDimension : roleDims) {
			if (currDimension.getConcept().equals(dimensionElement)) {
				return currDimension;
			}
		}
		return null;
	}

	/**
	 * @return Extended link role of this hypercube.
	 */
	public String getExtendedLinkRole() {
		return extendedLinkRole;
	}

	/**
	 * @param string
	 *            Extended link role of this hypercube.
	 */
	public void setExtendedLinkRole(String string) {
		extendedLinkRole = string;
	}

	/**
	 * @return Concept object representing this hypercube.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @return Set with Dimension objects representing all the
	 *         set of Dimensions this hypercube consists of.
	 */
	public Map<String, Set<Dimension>> getFullDimensionSet() {
		return dimensionSet;
	}

	/**
	 * @return Set of {@link Dimension} objects representing all the
	 *         set of Dimensions this hypercube consists of, 
	 *         relative to the given extended link role 
	 */
	public Set<Dimension> getDimensionSet(String extendedLinkRole){
		return dimensionSet.get(extendedLinkRole);
	}
	
    /**
     * 
     * @return Set with Concept objects representing all the dimensions this
     *         hypercube consists of.
     */
    public Set<Concept> getDimensionConceptSet(String extLinkRole) {
        Set<Concept> dimensionConceptSet = new HashSet<Concept>();
        for (Dimension d : dimensionSet.get(extLinkRole)) {
            dimensionConceptSet.add(d.getConcept());
        }
        return dimensionConceptSet;
    }

    /**
     * 
     * @return True if this hypercube contains at least one typed dimension,
     *         otherwise false.
     */
    public boolean containsTypedDimension(String extLinkRole) {
        for (Dimension d : dimensionSet.get(extLinkRole)) {
            if (d.isTyped()) {
                return true;
            }
        }
        return false;
    }
}
