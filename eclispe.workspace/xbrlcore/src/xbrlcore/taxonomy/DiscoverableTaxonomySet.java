package xbrlcore.taxonomy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xbrlcore.dimensions.Hypercube;
import xbrlcore.exception.XBRLException;
import xbrlcore.linkbase.CalculationLinkbase;
import xbrlcore.linkbase.CalculationPresentationLinkbase;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.Linkbase;
import xbrlcore.linkbase.PresentationLinkbase;
import xbrlcore.linkbase.ReferenceLinkbase;

/**
 * This class represents a discoverable taxonomy set (DTS).<br/><br/> A DTS
 * consists of multiple taxonomies, which themselves consist of a (taxonomy)
 * schema file and different linkbases. For DTS rules of discovery, see XBRL
 * Spec. section 3. <br/><br/>
 * 
 * @author Daniel Hamm
 */
public class DiscoverableTaxonomySet implements Serializable {

	private static final long serialVersionUID = 8343887320619608137L;

	private LabelLinkbase labelLinkbase;

	private PresentationLinkbase presentationLinkbase;

	private DefinitionLinkbase definitionLinkbase;

	private CalculationLinkbase calculationLinkbase;

    private CalculationPresentationLinkbase calculationPresentationLinkbase = new CalculationPresentationLinkbase(this);

    private ReferenceLinkbase referenceLinkbase;

    /* all taxonomies from the DTS */
	private final Map<String, TaxonomySchema> taxonomyMap = new HashMap<String, TaxonomySchema>();

    /* name of the taxonomy */
    private TaxonomySchema topTaxonomy = null;

	/**
	 * Constructor.
	 */
	public DiscoverableTaxonomySet() {
	}

	/**
	 * Calls {@link Linkbase#buildLinkbase()} on all {@link Linkbase}s.
	 * 
	 * @throws XBRLException
	 */
	public void buildLinkbases() throws XBRLException {
	    labelLinkbase.buildLinkbase();
	    presentationLinkbase.buildLinkbase();
	    definitionLinkbase.buildLinkbase();
	    calculationLinkbase.buildLinkbase();
	    referenceLinkbase.buildLinkbase();
	}
	
	/**
	 * This method adds a new (taxonomy) schema to the DTS.
	 * 
	 * @param taxonomySchema
	 *            The new (taxonomy) schema.
	 */
	public void addTaxonomy(TaxonomySchema taxonomySchema) {
		taxonomyMap.put(taxonomySchema.getName(), taxonomySchema);
	}

	/**
	 * Returns a Concept object to a specific ID from the DTS. All the
	 * taxonomies belonging to this DTS are scanned for the element.
	 * 
	 * @param id
	 *            The ID for which the element shall be obtained.
	 * @return The according Concept object from the DTS. If the concept is not
	 *         found in any taxonomy of the DTS, null is returned.
	 */
	public Concept getConceptByID(String id) {
		for (String currTaxonomyName : taxonomyMap.keySet()) {
			TaxonomySchema tmpTaxonomy = taxonomyMap.get(currTaxonomyName);
			Concept tmpElement = tmpTaxonomy.getConceptByID(id);
			if (tmpElement != null) {
				return tmpElement;
			}
		}

		return null;
	}

	/**
	 * Returns a Concept object to a specific name from the DTS. All the
	 * taxonomies belonging to this DTS are scanned for the element.
	 * 
	 * @param name
	 *            The name for which the element shall be obtained.
	 * @return The according Concept object from the DTS. If the concept is not
	 *         found in any taxonomy of the DTS, null is returned.
	 */
	public Concept getConceptByName(String name) {
		for (String currTaxonomyName : taxonomyMap.keySet()) {
			TaxonomySchema tmpTaxonomy = taxonomyMap.get(currTaxonomyName);
			Concept tmpElement = tmpTaxonomy.getConceptByName(name);
			if (tmpElement != null) {
				return tmpElement;
			}
		}

		return null;
	}

    /**
     * Returns a set of all Concept objects belonging to a specific substitution
     * group of this DTS.
     * 
     * @param substitutionGroup
     *            The substitution group all returned concepts shall belong to.
     * @param result target Collection of Concept objects of a certain substitution group.
     */
    public void getConceptBySubstitutionGroup(String substitutionGroup, Collection<Concept> result) {
        for (String currTaxonomyName : taxonomyMap.keySet()) {
            TaxonomySchema tmpTaxonomy = taxonomyMap.get(currTaxonomyName);
            tmpTaxonomy.getConceptBySubstitutionGroup(substitutionGroup, result);
        }
    }

    /**
     * Returns a set of all Concept objects belonging to a specific substitution
     * group of this DTS.
     * 
     * @param substitutionGroup
     *            The substitution group all returned concepts shall belong to.
     * @return Set of Concept objects of a certain substitution group.
     */
    public Set<Concept> getConceptBySubstitutionGroup(String substitutionGroup) {
        Set<Concept> returnSet = new HashSet<Concept>();
        getConceptBySubstitutionGroup(substitutionGroup, returnSet);

        return returnSet;
    }

	/**
	 * Returns a list of all the concepts belonging to this DTS (Set of Concept
	 * objects).
	 * 
	 * @return Set with all the concepts of all taxonomies belonging to this
	 *         DTS.
	 */
	public Set<Concept> getConcepts() {
		Set<Concept> returnSet = new HashSet<Concept>();
		for (String currTaxonomyName : taxonomyMap.keySet()) {
			TaxonomySchema tmpTaxonomy = taxonomyMap.get(currTaxonomyName);
			returnSet.addAll(tmpTaxonomy.getConcepts());
		}
		return returnSet;
	}

    /**
     * Returns a map of all the RoleTypes belonging to this DTS 
     * 
     * @return map with all the RoleTypes of all taxonomies belonging to this
     *         DTS.
     */
    public Map<String, RoleType> getRoleTypes() {
        Map<String, RoleType> returnMap = new HashMap<String, RoleType>();
        for (String currTaxonomyName : taxonomyMap.keySet()) {
            TaxonomySchema tmpTaxonomy = taxonomyMap.get(currTaxonomyName);
            returnMap.putAll(tmpTaxonomy.getRoleTypes());
        }
        return returnMap;
    }

    /**
     * Returns sorted list of all the RoleTypes belonging to this DTS 
     * 
     * @param lx
     * 
     * @return sorted list with all the RoleTypes of all taxonomies belonging to this
     *         DTS.
     */
    public List<RoleType> getRoleTypesSorted(final LabelExtractor lx) {
        List<RoleType> roleTypes = new ArrayList<RoleType>();
        for (TaxonomySchema ts : getTaxonomyMap().values()) {
            for (RoleType rt : ts.getRoleTypes().values())
                roleTypes.add(rt);
        }

        Collections.sort(roleTypes, new Comparator<RoleType>() {
            @Override
            public int compare(RoleType rt1, RoleType rt2) {
                String label1 = lx.getLabel(rt1);
                String label2 = lx.getLabel(rt2);

                return label1.compareToIgnoreCase(label2);
            }
        } );

        return roleTypes;
    }

    //FIXME: rewrite the method to return the Concept + improve the parsing of type element 
	/*
	 * Returns the schema element which the member of a typed dimension can be
	 * validated against.
	 * 
	 * @param typedDimensionElement
	 *            The typed dimension element for which the according schema
	 *            element is desired.
	 * @return The schema element validating members of the given typed
	 *         dimension.
	 * @throws XBRLCoreException 
	 */
    /*
     * Since we must not allow a JDOM dependency here, this method has to be moved to somewhere else, if and when it is even used.
     *
	public Element getElementForTypedDimension(Concept typedDimensionElement)
			throws xbrlcore.exception.XBRLCoreException {
		if (typedDimensionElement == null)
			return null;
		if (!typedDimensionElement.isTypedDimension()) {
			throw new XBRLCoreException("Cannot get schema element for "
					+ typedDimensionElement.getName()
					+ " since this is no typed dimension");
		}

		String typedDomainRefAttribute = typedDimensionElement
				.getTypedDomainRef();
		// typedDomainRefAttribute is of form <tax.name># <id>
		String taxonomyName;
		String elementID;
		if (typedDomainRefAttribute.indexOf("#") > 0) {
			taxonomyName = typedDomainRefAttribute.substring(0,
					typedDomainRefAttribute.indexOf("#"));
			elementID = typedDomainRefAttribute.substring(
					typedDomainRefAttribute.indexOf("#") + 1,
					typedDomainRefAttribute.length());
		} else {
			taxonomyName = typedDimensionElement.getTaxonomySchema().getName();
			elementID = typedDomainRefAttribute.substring(1,
					typedDomainRefAttribute.length());
		}

		TaxonomySchema taxSchema = getTaxonomySchema(taxonomyName);
		if (taxSchema == null) {
			throw new XBRLCoreException("Cannot find schema element for "
					+ typedDimensionElement.getName() + ": Unknown taxonomy "
					+ taxonomyName);
		}
		Concept cElementForTypedDimension = taxSchema.getConceptByID(elementID);
		if (cElementForTypedDimension == null) {
			throw new XBRLCoreException("Cannot find schema element for "
					+ typedDimensionElement.getName() + ": Unknown element "
					+ elementID);
		}
		Element elementForTypedDimension = new Element(
				cElementForTypedDimension.getName(), (org.jdom.Namespace)taxSchema.getNamespace().toJDOM());

		return elementForTypedDimension;
	}
	*/

	/**
	 * Returns all the concepts (Set of Concept objects) of this DTS
	 * representing dimensional elements.
	 * 
	 * @return Set of Concept objects of substitution group
	 *         xbrldt:dimensionItem.
	 */
	public Set<Concept> getDimensionConceptSet() {
		if (definitionLinkbase == null) {
			return null;
		}// else
		{
			return definitionLinkbase.getDimensionConceptSet();
		}
	}

	/**
	 * Returns the hypercube to a specific concept.
	 * 
	 * @param concept
	 *            Concept to which the hypercube shall be obtained.
	 * @return The hypercube matching to the given element.
	 */
	public Hypercube getHypercube(Concept concept) {
		if (definitionLinkbase == null) {
			return null;
		}
		return definitionLinkbase.getHypercube(concept);
	}

	/**
	 * Returns a specific (taxonomy) schema object.
	 * 
	 * @param name
	 *            Name of the (taxonomy) schema object.
	 * @return According TaxonomySchema object.
	 */
	public TaxonomySchema getTaxonomySchema(String name) {
		return taxonomyMap.get(name);
	}

	/**
	 * @return Returns the label linkbase of the DTS.
	 */
	public LabelLinkbase getLabelLinkbase() {
		return labelLinkbase;
	}

	/**
	 * @param linkbase
	 *            Label linkbase of the DTS.
	 */
	public void setLabelLinkbase(LabelLinkbase linkbase) {
		labelLinkbase = linkbase;
	}

	/**
	 * @return Returns presentation linkbase of the DTS.
	 */
	public PresentationLinkbase getPresentationLinkbase() {
		return presentationLinkbase;
	}

	/**
	 * @param linkbase
	 *            Presentation linkbase of the DTS.
	 */
	public void setPresentationLinkbase(PresentationLinkbase linkbase) {
		presentationLinkbase = linkbase;
	}

	/**
	 * @return Returns the definition linkbase of the DTS.
	 */
	public DefinitionLinkbase getDefinitionLinkbase() {
		return definitionLinkbase;
	}

	/**
	 * @param linkbase
	 *            Definition linkbase of the DTS.
	 */
	public void setDefinitionLinkbase(DefinitionLinkbase linkbase) {
		definitionLinkbase = linkbase;
	}

    /**
     * @return Returns the reference linkbase of the DTS.
     */
    public ReferenceLinkbase getReferenceLinkbase() {
        return referenceLinkbase;
    }

    /**
     * @param linkbase
     *            Reference linkbase of the DTS.
     */
    public void setReferenceLinkbase(ReferenceLinkbase linkbase) {
        referenceLinkbase = linkbase;
    }

	/**
	 * @return (Taxonomy) schema which has been the beginning of the DTS
	 *         discovery process of this DTS. For details on this discovery
	 *         process, see Spec. section 3.
	 */
	public TaxonomySchema getTopTaxonomy() {
		return topTaxonomy;
	}

	/**
	 * @param schema
	 *            (Taxonomy) schema which has been the beginning of the DTS
	 *            discovery process of this DTS. For details on this discovery
	 *            process, see Spec. section 3.
	 */
	public void setTopTaxonomy(TaxonomySchema schema) {
		topTaxonomy = schema;
	}

	/**
	 * @return Returns map with all the taxonomy schema objects belonging to
	 *         this DTS. The key is the name, the value is the according
	 *         TaxonomySchema object.
	 */
	public Map<String, TaxonomySchema> getTaxonomyMap() {
		return taxonomyMap;
	}

	/**
	 * @return Returns the calculationLinkbase.
	 */
	public CalculationLinkbase getCalculationLinkbase() {
		return calculationLinkbase;
	}

	/**
	 * @param calcLinkbase
	 *            The calculationLinkbase to set.
	 */
	public void setCalculationLinkbase(CalculationLinkbase calcLinkbase) {
		this.calculationLinkbase = calcLinkbase;
	}

    /**
     * @return Returns the calculationLinkbase.
     */
    public CalculationPresentationLinkbase getCalculationPresentationLinkbase() {
        calculationPresentationLinkbase.buildLinkbase();

        return calculationPresentationLinkbase;
    }
}
