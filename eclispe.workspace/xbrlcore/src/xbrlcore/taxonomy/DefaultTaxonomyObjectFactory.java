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
import xbrlcore.xlink.CalculationArc;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;
import xbrlcore.xlink.Resource;
import xbrlcore.xlink.Arc.UseAttribute;

/**
 * Default implementation of the {@link TaxonomyObjectFactory} used by {@link DefaultTaxonomyLoader}.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class DefaultTaxonomyObjectFactory implements TaxonomyObjectFactory {
    @Override
    public DiscoverableTaxonomySet newDiscoverableTaxonomySet() {
        return new DiscoverableTaxonomySet();
    }

    @Override
    public PresentationLinkbase newPresentationLinkbase(DiscoverableTaxonomySet dts) {
        return new PresentationLinkbase(dts);
    }

    @Override
    public LabelLinkbase newLabelLinkbase(DiscoverableTaxonomySet dts) {
        return new LabelLinkbase(dts);
    }

    @Override
    public DefinitionLinkbase newDefinitionLinkbase(DiscoverableTaxonomySet dts) {
        return new DefinitionLinkbase(dts);
    }

    @Override
    public CalculationLinkbase newCalculationLinkbase(DiscoverableTaxonomySet dts) {
        return new CalculationLinkbase(dts);
    }

    @Override
    public ReferenceLinkbase newReferenceLinkbase(DiscoverableTaxonomySet dts) {
        return new ReferenceLinkbase(dts);
    }

    @Override
    public TaxonomySchema newTaxonomySchema(DiscoverableTaxonomySet dts, String taxonomySchemaName) {
        return new TaxonomySchema(dts, taxonomySchemaName);
    }

    @Override
    public Concept newConcept(String name, String id, String type, ConceptTypeRestriction typeRestriction,
                              TaxonomySchema taxonomySchema,
                              String substitutionGroup, boolean isAbstract, boolean nillable, String periodType, String balance, String typedDomainRef, String enumLinkrole) {
        return new Concept(name, id, type, typeRestriction, taxonomySchema, substitutionGroup, isAbstract, nillable, periodType, balance, typedDomainRef, enumLinkrole);
    }

    @Override
    public TupleDefinition newTupleDefinition(String name, String id, String type, TupleType tupleType,
                                              TaxonomySchema taxonomySchema,
                                              String substitutionGroup, boolean isAbstract, boolean nillable, String periodType, String balance, String typedDomainRef,
                                              List<String> refs) {
        return new TupleDefinition(name, id, type, tupleType, taxonomySchema, substitutionGroup, isAbstract, nillable, periodType, balance, typedDomainRef, refs);
    }

    @Override
    public RoleType newRoleType(String id, String roleURI) {
        return new RoleType(id, roleURI);
    }

    @Override
    public Locator newLocator(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, Concept concept) {
        return new Locator(id, label, linkbaseSource, extLinkRole, role, title, concept);
    }

    @Override
    public Locator newLocator(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, Resource resource) {
        return new Locator(id, label, linkbaseSource, extLinkRole, role, title, resource);
    }

    @Override
    public Resource newResource(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, String lang, String value) {
        return new Resource(id, label, linkbaseSource, extLinkRole, role, title, lang, value);
    }

    @Override
    public Arc newArc(boolean forCalculation,
                      ExtendedLinkElement sourceElement, ExtendedLinkElement targetElement,
                      String arcRole, String extendedLinkRole, String targetRole,
                      String contextElement, AttributesImplSer attributes, String title,
                      float order, UseAttribute useAttribute, int priorityAttribute, float weightAttribute) {
        if (forCalculation)
            return new CalculationArc(sourceElement, targetElement,
                                      arcRole, extendedLinkRole, targetRole,
                                      contextElement, attributes, title,
                                      order, useAttribute, priorityAttribute, weightAttribute);
        
        return new Arc(sourceElement, targetElement,
                       arcRole, extendedLinkRole, targetRole,
                       contextElement, attributes, title,
                       order, useAttribute, priorityAttribute, weightAttribute);
    }
}
