package xbrlcore.taxonomy;

import java.util.List;

import xbrlcore.linkbase.CalculationLinkbase;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.PresentationLinkbase;
import xbrlcore.linkbase.ReferenceLinkbase;
import xbrlcore.taxonomy.TupleDefinition.TupleType;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.AttributesImplSer;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;
import xbrlcore.xlink.Resource;
import xbrlcore.xlink.Arc.UseAttribute;

/**
 * Abstraction of taxonomy object creation.
 * This way taxonomy classes can be extended and used by alternative loaders.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public interface TaxonomyObjectFactory {
    public DiscoverableTaxonomySet newDiscoverableTaxonomySet();

    public PresentationLinkbase newPresentationLinkbase(DiscoverableTaxonomySet dts);

    public LabelLinkbase newLabelLinkbase(DiscoverableTaxonomySet dts);

    public DefinitionLinkbase newDefinitionLinkbase(DiscoverableTaxonomySet dts);

    public CalculationLinkbase newCalculationLinkbase(DiscoverableTaxonomySet dts);

    public ReferenceLinkbase newReferenceLinkbase(DiscoverableTaxonomySet dts);

    public TaxonomySchema newTaxonomySchema(DiscoverableTaxonomySet dts, String taxonomySchemaName);

    public Concept newConcept(String name, String id, String type, ConceptTypeRestriction typeRestriction,
                              TaxonomySchema taxonomySchema,
                              String substitutionGroup, boolean isAbstract, boolean nillable, String periodType, String balance, String typedDomainRef, String enumLinkRole);

    public TupleDefinition newTupleDefinition(String name, String id, String type, TupleType tupleType,
                                              TaxonomySchema taxonomySchema,
                                              String substitutionGroup, boolean isAbstract, boolean nillable, String periodType, String balance, String typedDomainRef,
                                              List<String> refs);

    public RoleType newRoleType(String id, String roleURI);

    public Locator newLocator(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, Concept concept);
    public Locator newLocator(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, Resource resource);

    public Resource newResource(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, String lang, String value);

    public Arc newArc(boolean forCalculation,
                      ExtendedLinkElement sourceElement, ExtendedLinkElement targetElement,
                      String arcRole, String extendedLinkRole, String targetRole,
                      String contextElement, AttributesImplSer attributes, String title,
                      float order, UseAttribute useAttribute, int priorityAttribute, float weightAttribute);
}
