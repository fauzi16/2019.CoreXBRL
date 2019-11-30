package xbrlcore.taxonomy;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import xbrlcore.constants.ExceptionConstants;
import xbrlcore.exception.TaxonomyCreationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.linkbase.CalculationLinkbase;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.Linkbase;
import xbrlcore.linkbase.PresentationLinkbase;
import xbrlcore.linkbase.ReferenceLinkbase;
import xbrlcore.logging.ConsoleLogInterface;
import xbrlcore.logging.LogInterface;
import xbrlcore.taxonomy.TupleDefinition.TupleType;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.Arc.UseAttribute;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;
import xbrlcore.xlink.Resource;

/**
 * 
 * This class generates an {@link xbrlcore.taxonomy.DiscoverableTaxonomySet} object and
 * all other necessary objects for the representation of a DTS according to a
 * taxonomy file.<br/><br/>
 * 
 * @author Daniel Hamm
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class DefaultTaxonomyLoader extends AbstractTaxonomyLoader<DiscoverableTaxonomySet, TaxonomySchema> {

    private final TaxonomyObjectFactory objectFactory;

    private DiscoverableTaxonomySet dts = null;

    private PresentationLinkbase presLinkbase = null;
    private LabelLinkbase labelLinkbase = null;
    private DefinitionLinkbase defLinkbase = null;
    private CalculationLinkbase calcLinkbase = null;
    private ReferenceLinkbase refLinkbase = null;

    private Map<String, Linkbase> linkbasesMap = null;
    private Map<String, DefaultLinkbaseArcsCollector<Arc>> linkbaseArcsCollectors = null;

    public DefaultTaxonomyLoader() {
        this(new DefaultTaxonomyObjectFactory(), new ConsoleLogInterface());
    }

    public DefaultTaxonomyLoader(TaxonomyObjectFactory objectFactory) {
        this(objectFactory, new ConsoleLogInterface());
    }

    public DefaultTaxonomyLoader(LogInterface log) {
        this(new DefaultTaxonomyObjectFactory(), log);
    }

    public DefaultTaxonomyLoader(TaxonomyObjectFactory objectFactory, LogInterface log) {
        super(log);

        this.objectFactory = objectFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(String taxonomySchemaName, Object userObject) throws XBRLException, TaxonomyCreationException
    {
        dts = objectFactory.newDiscoverableTaxonomySet();

        presLinkbase = objectFactory.newPresentationLinkbase(dts);
        labelLinkbase = objectFactory.newLabelLinkbase(dts);
        defLinkbase = objectFactory.newDefinitionLinkbase(dts);
        calcLinkbase = objectFactory.newCalculationLinkbase(dts);
        refLinkbase = objectFactory.newReferenceLinkbase(dts);

        linkbasesMap = new HashMap<String, Linkbase>();
        linkbasesMap.put(PRESENTATION_LB_NAME, presLinkbase);
        linkbasesMap.put(LABEL_LB_NAME, labelLinkbase);
        linkbasesMap.put(DEFINITION_LB_NAME, defLinkbase);
        linkbasesMap.put(CALCULATION_LB_NAME, calcLinkbase);
        linkbasesMap.put(REFERENCE_LB_NAME, refLinkbase);

        linkbaseArcsCollectors = new HashMap<String, DefaultLinkbaseArcsCollector<Arc>>();
        linkbaseArcsCollectors.put(PRESENTATION_LB_NAME, new DefaultLinkbaseArcsCollector<Arc>(log));
        linkbaseArcsCollectors.put(LABEL_LB_NAME, new DefaultLinkbaseArcsCollector<Arc>(log));
        linkbaseArcsCollectors.put(DEFINITION_LB_NAME, new DefaultLinkbaseArcsCollector<Arc>(log));
        linkbaseArcsCollectors.put(CALCULATION_LB_NAME, new DefaultLinkbaseArcsCollector<Arc>(log));
        linkbaseArcsCollectors.put(REFERENCE_LB_NAME, new DefaultLinkbaseArcsCollector<Arc>(log));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TaxonomySchema buildTaxonomySchema(String taxonomySchemaName, Namespace targetNamespace) throws TaxonomyCreationException {
        TaxonomySchema currTaxSchema = objectFactory.newTaxonomySchema(dts, taxonomySchemaName);
        if (targetNamespace != null) {
            currTaxSchema.setNamespace(targetNamespace);
        }

        // set imported taxonomy schemas
        HashSet<String> tmp = new HashSet<String>();
        getIncludedTaxonomyFileNames(taxonomySchemaName, tmp);
        currTaxSchema.setImportedTaxonomyNames(tmp);

        return currTaxSchema;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createConcept(TaxonomySchema taxonomySchema, String id, String name, String type, ConceptTypeRestriction typeRestriction, String substitutionGroup, String periodType, String balance, boolean abstract_, boolean nillable, String typedDomainRef, String enumLinkRole) throws TaxonomyCreationException {
        Concept concept = objectFactory.newConcept(name, id, type, typeRestriction, taxonomySchema, substitutionGroup, abstract_, nillable, periodType, balance, typedDomainRef, enumLinkRole);
        taxonomySchema.addConcept(concept);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createTuple(TaxonomySchema taxonomySchema, String id, String name, String type, String substitutionGroup, String periodType, String balance, boolean abstract_, boolean nillable, String typedDomainRef, TupleType tupleType, List<String> refs) throws TaxonomyCreationException {
        TupleDefinition tuple = objectFactory.newTupleDefinition(name, id, type, tupleType, taxonomySchema, substitutionGroup, abstract_, nillable, periodType, balance, typedDomainRef, refs);

        taxonomySchema.addConcept(tuple);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createRoleType(TaxonomySchema taxonomySchema, String id, String roleURI, String[] definitions, String[] usedOns) throws TaxonomyCreationException {
        RoleType roleType = objectFactory.newRoleType(id, roleURI);
        roleType.setDefinition(definitions);
        roleType.setUsedOn(usedOns);

        taxonomySchema.addRoleType(roleType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finishTaxonomySchema(TaxonomySchema taxonomySchema, String taxonomySchemaName) throws TaxonomyCreationException {
        dts.addTaxonomy(taxonomySchema);
    }

    private final Linkbase getLinkbase(String name) {
        /*
        if (name.equals(PRESENTATION_LB_NAME))
            return presLinkbase;
        else if (name.equals(LABEL_LB_NAME))
            return labelLinkbase;
        else if (name.equals(DEFINITION_LB_NAME))
            return defLinkbase;
        else if (name.equals(CALCULATION_LB_NAME))
            return calcLinkbase;
        else if (name.equals(REFERENCE_LB_NAME))
            return refLinkbase;

        return null;
        */
        return linkbasesMap.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addExtendedLinkRole(TaxonomySchema taxonomySchema, String taxonomySchemaName, String linkbaseName, String extendedLinkRole) {
        getLinkbase(linkbaseName).addExtendedLinkRole(extendedLinkRole);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLocator(TaxonomySchema taxonomySchema, String taxonomySchemaName, String linkbaseName, String linkbaseSource, String label, String extLinkRole, String id, String role, String title, String conceptName, String elementId) throws TaxonomyCreationException {
        Linkbase linkbase = getLinkbase(linkbaseName);

        Locator newLocator = null;

        if (conceptName != null) {
            Concept refConcept = null;
            if (taxonomySchema != null) {
                refConcept = taxonomySchema.getConceptByID(elementId);
            }
            if (refConcept == null) {
                refConcept = dts.getConceptByID(elementId);
            }

            if (refConcept != null) {
                newLocator = objectFactory.newLocator(id, label, linkbaseSource, extLinkRole, role, title, refConcept);
            } else {
                // now the concept cannot be
                // resolved, try to get an
                // already existing resource
                Resource resource = linkbase.getResource(elementId);
                if (resource == null) {
                    // the location of the
                    // target of this
                    // locator could not be
                    // found
                    throw new TaxonomyCreationException(
                            ExceptionConstants.EX_LINKBASE_LOCATOR_WITHOUT_REF
                                    + ": "
                                    + elementId
                                    + " in Linkbase "
                                    + linkbaseSource);
                }
                newLocator = objectFactory.newLocator(id, label, linkbaseSource, extLinkRole, role, title, resource);
            }
        }

        if (newLocator == null)
            throw new Error("Something went wrong creating Locator " + label);

        linkbase.addExtendedLinkElement(newLocator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object createResource(TaxonomySchema taxonomySchema, String taxonomySchemaName, String linkbaseName, String linkbaseSource, String label, String extLinkRole,
                                    String role, String title, String id, String lang, String value ) throws TaxonomyCreationException {
        Linkbase linkbase = getLinkbase(linkbaseName);

        Resource newResource = objectFactory.newResource(id, label, linkbaseSource, extLinkRole, role, title, lang, value);

        linkbase.addExtendedLinkElement(newResource);

        return newResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void consumeResourceDetails(TaxonomySchema taxonomySchema, String taxonomySchemaName, String linkbaseName, String linkbaseSource, String label, String extLinkRole,
                                          String role, String title, String id, List<Object> resourceElements, Object resource) throws TaxonomyCreationException {
        Resource xres = (Resource)resource;

        /*if (linkbaseName.equals(REFERENCE_LB_NAME))*/ {
            for (Object o : resourceElements) {
                if (o instanceof Element) {
                    Element elem = (Element)o;
                    xres.addRef(elem.getNamespacePrefix(), elem.getName(), elem.getValue());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createArcs(TaxonomySchema taxonomySchema, String taxonomySchemaName, String linkbaseName, String linkbaseSource, String from, String to, String extLinkRole,
                              String arcRole, String targetRole, String title, String contextElementName, Float order,
                              String use, Integer priority, boolean usable, Float weight) throws TaxonomyCreationException {
        Linkbase linkbase = getLinkbase(linkbaseName);
        boolean isCalculation = (linkbase instanceof CalculationLinkbase);
        DefaultLinkbaseArcsCollector<Arc> arcsCollector = linkbaseArcsCollectors.get(linkbaseName);

        List<ExtendedLinkElement> fromElements = linkbase.getExtendedLinkElements(from, extLinkRole, linkbaseSource);
        List<ExtendedLinkElement> toElements = linkbase.getExtendedLinkElements(to, extLinkRole, linkbaseSource);

        /*
         * Create the arcs. Usually these will be
         * one-to-one arcs, but according to Spec.
         * section 3.5.3.9, one-to-many and
         * many-to-many relationships are also
         * possible.
         */
        for (ExtendedLinkElement currFromElement : fromElements) {
            for (ExtendedLinkElement currToElement : toElements) {

                if (order == null)
                    order = Arc.DEFAULT_ORDER;

                if (priority == null)
                    priority = Arc.DEFAULT_PRIORITY_ATTRIBUTE;

                if (weight == null)
                    weight = Arc.DEFAULT_WEIGHT;

                Arc newArc = objectFactory.newArc(isCalculation, currFromElement, currToElement, arcRole, extLinkRole, targetRole, contextElementName, Arc.createDefaultAttributes(), title, order, UseAttribute.parse(use), priority, weight);

                if (newArc.getTargetElement().isLocator()) {
                    ((Locator) newArc.getTargetElement()).setUsable(usable);
                }

                arcsCollector.addArc(newArc);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DiscoverableTaxonomySet finish(TaxonomySchema taxonomySchema, String taxonomyFileName) throws IOException, XBRLException, TaxonomyCreationException {
        for (String linkbaseName : linkbasesMap.keySet()) {
            linkbasesMap.get(linkbaseName).applyArcs(linkbaseArcsCollectors.get(linkbaseName), false);
        }
        linkbaseArcsCollectors = null;

        for (TaxonomySchema ts : dts.getTaxonomyMap().values()) {
            for (Concept concept : ts.getConcepts()) {
                if (concept instanceof TupleDefinition) {
                    ( (TupleDefinition)concept ).collectConcepts(dts);
                }
            }
        }

        //dts.buildLinkbases();

        dts.setTopTaxonomy(taxonomySchema);
        dts.setPresentationLinkbase(presLinkbase);
        dts.setLabelLinkbase(labelLinkbase);
        dts.setDefinitionLinkbase(defLinkbase);
        dts.setCalculationLinkbase(calcLinkbase);
        dts.setReferenceLinkbase(refLinkbase);

        return dts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanup() {
        super.cleanup();
        
        dts = null;

        linkbasesMap = null;

        presLinkbase = null;
        labelLinkbase = null;
        defLinkbase = null;
        calcLinkbase = null;
        refLinkbase = null;
    }
}
