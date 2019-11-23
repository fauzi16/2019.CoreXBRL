package xbrlcore.instance;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import xbrlcore.constants.ExceptionConstants;
import xbrlcore.constants.GeneralConstants;
import xbrlcore.constants.NamespaceConstants;
import xbrlcore.dimensions.MultipleDimensionType;
import xbrlcore.dimensions.SingleDimensionType;
import xbrlcore.exception.InstanceException;
import xbrlcore.exception.XBRLException;
import xbrlcore.taxonomy.AbstractTaxonomyLocator;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.taxonomy.Namespace;
import xbrlcore.taxonomy.TaxonomySchema;
import xbrlcore.taxonomy.TupleDefinition;
import static xbrlcore.taxonomy.NamespaceJDOMAdapter.toJDOM;

/**
 * This class is responsible for creating an Instance object of an instance
 * file. <br/><br/>
 * 
 * @author Daniel Hamm
 * @author Marvin Froehlich
 */
public class InstanceFactory {

    private static InstanceFactory xbrlInstanceFactory;

    private Instance instance;

    private Map<String, InstanceContext> contextMap;

    private Map<String, InstanceUnit> unitMap;

    private List<Namespace> schemaRefNamespaces;

    /**
     * Constructor, private.
     *  
     */
    private InstanceFactory() {
        contextMap = new HashMap<String, InstanceContext>();
        unitMap = new HashMap<String, InstanceUnit>();
        schemaRefNamespaces = new ArrayList<Namespace>();
    }

    /**
     * 
     * @return the only Instance of InstanceFactory object (singleton).
     */
    public static synchronized InstanceFactory get() {
        if (xbrlInstanceFactory == null) {
            xbrlInstanceFactory = new InstanceFactory();
        }
        return xbrlInstanceFactory;
    }


    /**
     * Creates an xbrlcore.instance.Instance object.
     * 
     * @param instanceFilename
     *            Instance file.
     * @return An object of xbrlcore.instance.Instance.
     * @throws IOException
     * @throws InstanceException 
     * @throws XBRLException
     * @throws CloneNotSupportedException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public Instance createInstance(String instanceFilename) throws 
            IOException, InstanceException, XBRLException,
            CloneNotSupportedException, SAXException,
            ParserConfigurationException {
        return createInstance(new File(instanceFilename));
    }

    /**
     * Creates an xbrlcore.instance.Instance object.
     * 
     * @param instanceFile
     *            Instance file.
     * @return An object of xbrlcore.instance.Instance.
     * @throws IOException
     * @throws InstanceException 
     * @throws XBRLException
     * @throws CloneNotSupportedException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public Instance createInstance(File instanceFile) throws 
            IOException, InstanceException, XBRLException,
            CloneNotSupportedException, SAXException,
            ParserConfigurationException {
        return createInstance(instanceFile.toURI().toURL());
    }

    /**
     * Creates an xbrlcore.instance.Instance object.
     * 
     * @param instanceFile
     *            Instance file.
     * @return An object of xbrlcore.instance.Instance.
     * @throws IOException
     * @throws InstanceException 
     * @throws XBRLException
     * @throws CloneNotSupportedException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public Instance createInstance(URL instanceFile) throws 
            IOException, InstanceException, XBRLException,
            CloneNotSupportedException, SAXException,
            ParserConfigurationException {

        URL base = null;
        String f = instanceFile.toString(); //instanceFile.getFile();
        int sp = f.lastIndexOf('/');
        if (sp >= 0) {
            base = new URL(f.substring(0, sp + 1));
        }

        return createInstance(instanceFile, new InstanceReferencedTaxonomyLocator(base));
    }

    /**
     * Creates an xbrlcore.instance.Instance object.
     * 
     * @param instanceFilename
     *            Instance file.
     * @param taxonomyLocator
     * @return An object of xbrlcore.instance.Instance.
     * @throws IOException
     * @throws InstanceException 
     * @throws XBRLException
     * @throws CloneNotSupportedException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public Instance createInstance(String instanceFilename, AbstractTaxonomyLocator<? extends DiscoverableTaxonomySet, ? extends TaxonomySchema> taxonomyLocator) throws 
            IOException, InstanceException, XBRLException,
            CloneNotSupportedException, SAXException,
            ParserConfigurationException {
        return createInstance(new File(instanceFilename), taxonomyLocator);
    }

    /**
     * Creates an xbrlcore.instance.Instance object.
     * 
     * @param instanceFile
     *            Instance file.
     * @param taxonomyLocator
     * @return An object of xbrlcore.instance.Instance.
     * @throws IOException
     * @throws InstanceException 
     * @throws XBRLException
     * @throws CloneNotSupportedException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public Instance createInstance(File instanceFile, AbstractTaxonomyLocator<? extends DiscoverableTaxonomySet, ? extends TaxonomySchema> taxonomyLocator) throws 
            IOException, InstanceException, XBRLException,
            CloneNotSupportedException, SAXException,
            ParserConfigurationException {
        return createInstance(instanceFile.toURI().toURL(), taxonomyLocator);
    }

    /**
     * Creates an xbrlcore.instance.Instance object.
     * 
     * @param instanceFile
     *            Instance file.
     * @param taxonomyLocator
     * @return An object of xbrlcore.instance.Instance.
     * @throws IOException
     * @throws InstanceException 
     * @throws XBRLException
     * @throws CloneNotSupportedException 
     */
    public Instance createInstance(URL instanceFile, AbstractTaxonomyLocator<? extends DiscoverableTaxonomySet, ? extends TaxonomySchema> taxonomyLocator) throws 
            IOException, InstanceException, XBRLException,
            CloneNotSupportedException {

        if (taxonomyLocator == null)
            throw new IllegalArgumentException("taxonomyLocator must not be null.");

        // Initialise XML document with SAX of JDOM
        org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
        org.jdom.Document xmlInstance = null;
        try {
            xmlInstance = builder.build(instanceFile);
        } catch (org.jdom.JDOMException e) {
            throw new InstanceException(e.getMessage());
        }

        org.jdom.Element xbrlRoot = findRootElement(xmlInstance.getRootElement());
        if (xbrlRoot == null)
            throw new InstanceException("Could not find \"xbrl\" root element.");

        /* determine taxonomy names */
        Set<String> taxonomyNameSet = getReferencedSchemaNames(xbrlRoot);

        /* now build the taxonomy */
        Set<DiscoverableTaxonomySet> dtsSet = new HashSet<DiscoverableTaxonomySet>();
        for (String currTaxonomyName : taxonomyNameSet) {
            DiscoverableTaxonomySet currTaxonomy = taxonomyLocator.loadTaxonomy(currTaxonomyName);
            dtsSet.add(currTaxonomy);
        }

        return getInstance(dtsSet, getNameFromURL(instanceFile), xbrlRoot);
    }

    /**
     * Builds an instance.
     * 
     * @param dtsSet
     *            Set of discoverable taxonomy sets this instance refers to.
     * @return An object of xbrlcore.instance.Instance.
     * @throws InstanceException
     */
    @SuppressWarnings("unchecked")
    private Instance getInstance(Set<DiscoverableTaxonomySet> dtsSet, String fileName, org.jdom.Element xbrlRoot)
            throws InstanceException, CloneNotSupportedException {

        instance = new Instance(dtsSet);
        instance.setFileName(fileName);

        /* set instance namespace */
        setInstanceNamespace(xbrlRoot);

        /* set additional namespaces of the root element */
        setAdditionalNamespaces_JDOM(xbrlRoot.getAdditionalNamespaces());

        /* set additional namespaces of the schemaRef elements */
        setAdditionalNamespaces(schemaRefNamespaces);

        /* determine the schema location */
        setSchemaLocation(xbrlRoot);

        /* set context elements */
        setContextElements(xbrlRoot);

        /* set unit elements */
        setUnitElements(xbrlRoot);

        /* set facts and tuples */
        setFactsAndTuples(xbrlRoot);

        return instance;
    }

    private org.jdom.Element findRootElement(org.jdom.Element parent) {
        //return parent;
        
        if (parent.getNamespacePrefix().equals(NamespaceConstants.XBRLI_NAMESPACE.getPrefix()) &&
            parent.getNamespaceURI().equals(NamespaceConstants.XBRLI_NAMESPACE.getURI()) &&
            parent.getName().equals("xbrl")) {
            return parent;
        }

        @SuppressWarnings("unchecked")
        List<org.jdom.Element> elementList = parent.getChildren();
        if (elementList != null) {
            for (org.jdom.Element child : elementList) {
                org.jdom.Element result = findRootElement(child);
                if (result != null)
                    return result;
            }
        }

        return null;
    }

    /**
     * Determines which taxonomy an instance refers to.
     * 
     * @param currDocument
     *            Structure of the instance file.
     * @return Set of names of the taxonomy the instance refers to.
     */
    private Set<String> getReferencedSchemaNames(org.jdom.Element xbrlRoot) {
        Set<String> referencedSchemaNameSet = new HashSet<String>();
        //org.jdom.Element xbrlRoot = getXBRLRootElement(docObject);
        @SuppressWarnings("unchecked")
        List<org.jdom.Element> elementList = xbrlRoot.getChildren();
        for (org.jdom.Element currElement : elementList) {
            if (currElement.getName().equals("schemaRef")) {
                String href = currElement.getAttributeValue("href", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                referencedSchemaNameSet.add(href);
                // set namespaces of schemaRef element
                schemaRefNamespaces = currElement.getAdditionalNamespaces();
            }
        }
        return referencedSchemaNameSet;
    }

    /**
     * Sets the namespace of the instance.
     *  
     */
    private void setInstanceNamespace(org.jdom.Element xbrlRoot) {
        //org.jdom.Element xbrlRoot = getXBRLRootElement(xmlInstance);
        org.jdom.Namespace jdomNS = xbrlRoot.getNamespace();
        Namespace instanceNamespace = Namespace.getNamespace(jdomNS.getPrefix(), jdomNS.getURI()); 
        instance.setInstanceNamespace(instanceNamespace);
    }

    /**
     * Sets additional namespaces needed in this instance.
     *  
     */
    private void setAdditionalNamespaces_JDOM(Collection<org.jdom.Namespace> additionalNamespaces) {
        for (org.jdom.Namespace currentNamespace : additionalNamespaces) {
            if (instance.getNamespace(currentNamespace.getURI()) == null) {
                xbrlcore.taxonomy.Namespace ns =
                            xbrlcore.taxonomy.Namespace.getNamespace(
                               currentNamespace.getPrefix(),
                               currentNamespace.getURI());
                instance.addNamespace(ns);
            }
        }
    }

    /**
     * Sets additional namespaces needed in this instance.
     *  
     */
    private void setAdditionalNamespaces(Collection<Namespace> additionalNamespaces) {
        for (Namespace currentNamespace : additionalNamespaces) {
            if (instance.getNamespace(currentNamespace.getURI()) == null) {
                instance.addNamespace(currentNamespace);
            }
        }
    }

    /**
     * Sets schema location information defined in this instance.
     * 
     *  
     */
    private void setSchemaLocation(org.jdom.Element xbrlRoot) {
        //org.jdom.Element xbrlRoot = getXBRLRootElement(xmlInstance);
        if (xbrlRoot.getAttributes().size() > 0
                && xbrlRoot.getAttribute(
                        "schemaLocation",
                        toJDOM(instance.getNamespace(NamespaceConstants.XSI_NAMESPACE
                                .getURI()))) != null) {
            String schemaLocationValue = xbrlRoot
                    .getAttributeValue(
                            "schemaLocation",
                            toJDOM(instance
                                    .getNamespace(NamespaceConstants.XSI_NAMESPACE
                                            .getURI())));
            while (schemaLocationValue.indexOf(" ") > 0) {
                String schemaLocationURI = schemaLocationValue.substring(0,
                        schemaLocationValue.indexOf(" "));
                schemaLocationValue = schemaLocationValue.substring(
                        schemaLocationValue.indexOf(" "), schemaLocationValue
                                .length());
                schemaLocationValue = schemaLocationValue.trim();
                String schemaLocationPrefix = null;
                if (schemaLocationValue.indexOf(" ") > 0) {
                    schemaLocationPrefix = schemaLocationValue.substring(0,
                            schemaLocationValue.indexOf(" "));
                    schemaLocationValue = schemaLocationValue.substring(
                            schemaLocationValue.indexOf(" "),
                            schemaLocationValue.length());
                    schemaLocationValue = schemaLocationValue.trim();
                } else {
                    schemaLocationPrefix = schemaLocationValue;
                }
                instance.addSchemaLocation(schemaLocationURI,
                        schemaLocationPrefix);
            }
        }
    }

    public String getLocalValue(String value) {
        if(value == null) {
            return value;
        }
        return value.substring(value.indexOf(":") + 1, value.length());
    }

    public String getValueNamespace(String value) {
        if(value == null) {
            return value;
        }
        return value.substring(0, value.indexOf(":"));
    }

    /**
     * Sets unit elements defined in this instance.
     * 
     * @throws InstanceException
     */
    private void setUnitElements(org.jdom.Element xbrlRoot) throws InstanceException {
        //org.jdom.Element xbrlRoot = getXBRLRootElement(xmlInstance);
        @SuppressWarnings("unchecked")
        List<org.jdom.Element> unitElementList = xbrlRoot.getChildren("unit",
                toJDOM(instance.getInstanceNamespace()));
        for (org.jdom.Element currUnitElement : unitElementList) {
            String id = currUnitElement.getAttributeValue("id");

            if (id == null || id.length() == 0) {
                throw new InstanceException(
                        ExceptionConstants.EX_INSTANCE_CREATION_NOID_UNIT);
            }

            InstanceUnit currUnit = new InstanceUnit(id);
            @SuppressWarnings("unchecked")
            List<org.jdom.Element> measureList = currUnitElement.getChildren("measure", toJDOM(instance.getInstanceNamespace()));
            Iterator<org.jdom.Element> measureListIterator = measureList.iterator();
            while (measureListIterator.hasNext()) {
                org.jdom.Element currMeasureElement = measureListIterator.next();
                if(currUnit.getValue() == null) {
                    currUnit.setValue(getLocalValue(currMeasureElement.getValue()));
                } else {
                    currUnit.setValue(getLocalValue(currUnit.getValue()) + "*" + getLocalValue(currMeasureElement.getValue()));
                }
                currUnit.setNamespaceURI(instance.getNamespaceURI(getValueNamespace(currMeasureElement.getValue())));
            }
            org.jdom.Element divideElement = currUnitElement.getChild("divide", toJDOM(instance.getInstanceNamespace()));
            if(divideElement != null) {
                org.jdom.Element unitNumeratorElement = divideElement.getChild("unitNumerator", toJDOM(instance.getInstanceNamespace()));
                org.jdom.Element unitDenominatorElement = divideElement.getChild("unitDenominator", toJDOM(instance.getInstanceNamespace()));

                org.jdom.Element unitNumeratorMeasureElement = unitNumeratorElement.getChild("measure", toJDOM(instance.getInstanceNamespace()));
                org.jdom.Element unitDenominatorMeasureElement = unitDenominatorElement.getChild("measure", toJDOM(instance.getInstanceNamespace()));

                currUnit.setValue(getLocalValue(unitNumeratorMeasureElement.getValue()) + "/" + getLocalValue(unitDenominatorMeasureElement.getValue()));
                //TODO: need some refactoring, the namespace should be attached to a measure class inside the unit
                currUnit.setNamespaceURI("");
            }

            unitMap.put(id, currUnit);
        }
    }

    /**
     * Sets context elements of this instance.
     * 
     * @throws InstanceException
     */
    private void setContextElements(org.jdom.Element xbrlRoot) throws InstanceException,
            CloneNotSupportedException {
        //org.jdom.Element xbrlRoot = getXBRLRootElement(xmlInstance);
        @SuppressWarnings("unchecked")
        List<org.jdom.Element> contextElementList = xbrlRoot.getChildren(
                "context", toJDOM(instance.getInstanceNamespace()));
        for (org.jdom.Element currContextElement : contextElementList) {
            String id = currContextElement.getAttributeValue("id");

            if (id == null || id.length() == 0) {
                throw new InstanceException(
                        ExceptionConstants.EX_INSTANCE_CREATION_NOID_CONTEXT);
            }

            InstanceContext currContext = new InstanceContext(id);

            /* set identifier scheme and identifier */
            org.jdom.Element identifierElement = currContextElement.getChild("entity",
                    toJDOM(instance.getInstanceNamespace())).getChild("identifier",
                    toJDOM(instance.getInstanceNamespace()));
            currContext.setIdentifierScheme(identifierElement
                    .getAttributeValue("scheme"));
            currContext.setIdentifier(identifierElement.getValue());

            /* set period type and period */
            org.jdom.Element periodElement = currContextElement.getChild("period",
                    toJDOM(instance.getInstanceNamespace()));
            if (periodElement != null) {
                if (periodElement.getChild("startDate", toJDOM(instance
                        .getInstanceNamespace())) != null
                        && periodElement.getChild("endDate", toJDOM(instance
                                .getInstanceNamespace())) != null) {
                    currContext.setPeriodStartDate(periodElement.getChild(
                            "startDate", toJDOM(instance.getInstanceNamespace()))
                            .getText());
                    currContext.setPeriodEndDate(periodElement.getChild(
                            "endDate", toJDOM(instance.getInstanceNamespace()))
                            .getText());
                } else if (periodElement.getChild("instant", toJDOM(instance
                        .getInstanceNamespace())) != null) {
                    if (periodElement.getChild("instant",
                            toJDOM(instance.getInstanceNamespace())).getChild(
                            "forever", toJDOM(instance.getInstanceNamespace())) != null) {
                        currContext.setPeriodValue("forever");
                    } else {
                        currContext.setPeriodValue(periodElement.getChild(
                                "instant", toJDOM(instance.getInstanceNamespace()))
                                .getText());
                    }
                }
            }

            /*
             * set multidimensional information - parse both <scenario> and
             * <segment> element
             */
            List<org.jdom.Element> scenSegElementList = new ArrayList<org.jdom.Element>();
            org.jdom.Element scenarioElement = currContextElement.getChild("scenario",
                    toJDOM(instance.getInstanceNamespace()));
            /* <segment> is a child element of <entity> */
            org.jdom.Element segmentElement = currContextElement.getChild("entity",
                    toJDOM(instance.getInstanceNamespace())).getChild("segment",
                    toJDOM(instance.getInstanceNamespace()));
            if (scenarioElement != null) {
                scenSegElementList.add(scenSegElementList.size(),
                        scenarioElement);
                for (Object elem : scenarioElement.getChildren())
                    currContext.addScenarioElement((org.jdom.Element) elem);
            }
            if (segmentElement != null) {
                scenSegElementList.add(scenSegElementList.size(),
                        segmentElement);
                for (Object elem : segmentElement.getChildren())
                    currContext.addSegmentElement((org.jdom.Element) elem);
            }

            for (int i = 0; i < scenSegElementList.size(); i++) {
                org.jdom.Element currElement = scenSegElementList.get(i);
                @SuppressWarnings("unchecked")
                List<org.jdom.Element> explicitMemberElementList = currElement
                        .getChildren(
                                "explicitMember",
                                toJDOM(instance
                                        .getNamespace(NamespaceConstants.XBRLDI_NAMESPACE
                                                .getURI())));
                @SuppressWarnings("unchecked")
                List<org.jdom.Element> typedMemberElementList = currElement
                        .getChildren(
                                "typedMember",
                                toJDOM(instance
                                        .getNamespace(NamespaceConstants.XBRLDI_NAMESPACE
                                                .getURI())));
                Iterator<org.jdom.Element> explicitMemberElementListIterator = explicitMemberElementList
                        .iterator();
                Iterator<org.jdom.Element> typedMemberElementListIterator = typedMemberElementList
                        .iterator();
                MultipleDimensionType mdt = null;
                /* set explicit member */
                while (explicitMemberElementListIterator.hasNext()) {
                    org.jdom.Element currExplicitMemberElement = explicitMemberElementListIterator.next();

                    /* determine dimension element */
                    String dimensionAttribute = currExplicitMemberElement
                            .getAttributeValue("dimension");
                    String prefix = dimensionAttribute.substring(0,
                            dimensionAttribute.indexOf(":"));
                    String dimensionElementName = dimensionAttribute.substring(
                            dimensionAttribute.indexOf(":") + 1,
                            dimensionAttribute.length());
                    org.jdom.Namespace currExplicitMemberNamespace = currExplicitMemberElement.getNamespace(prefix);
                    if (instance.getSchemaForURI(Namespace.getNamespace(currExplicitMemberNamespace.getPrefix(), currExplicitMemberNamespace.getURI())) == null) {
                        throw new InstanceException(
                                ExceptionConstants.EX_INSTANCE_CREATION_NO_SCHEMA_PREFIX + prefix);
                    }
                    Concept dimensionElement = instance.getConceptByName(currExplicitMemberNamespace, dimensionElementName);

                    /* determine domain member element */
                    String value = currExplicitMemberElement.getValue();
                    String domainMemberElementName = value.substring(value
                            .indexOf(":") + 1, value.length());
                    Concept domainMemberElement = instance.getConceptByName(null, domainMemberElementName);

                    if (dimensionElement == null || domainMemberElement == null) {
                        throw new InstanceException(
                                ExceptionConstants.EX_INSTANCE_CREATION_DIMENSIONS
                                        + id);
                    }

                    if (mdt == null) {
                        mdt = new MultipleDimensionType(dimensionElement,
                                domainMemberElement);
                    } else {
                        mdt
                                .addPredecessorDimensionDomain(new SingleDimensionType(
                                        dimensionElement, domainMemberElement));
                    }
                }
                /* set typed member */
                while (typedMemberElementListIterator.hasNext()) {
                    org.jdom.Element currTypedMemberElement = typedMemberElementListIterator.next();

                    /* determine dimension element */
                    String dimensionAttribute = currTypedMemberElement
                            .getAttributeValue("dimension");
                    String prefix = dimensionAttribute.substring(0,
                            dimensionAttribute.indexOf(":"));
                    String dimensionElementName = dimensionAttribute.substring(
                            dimensionAttribute.indexOf(":") + 1,
                            dimensionAttribute.length());
                    org.jdom.Namespace currTypedMemberNamespace = currTypedMemberElement.getNamespace(prefix);
                    Concept dimensionElement = instance.getConceptByName(currTypedMemberNamespace, dimensionElementName);

                    /*
                     * SingleDimensionType represtents the typed dimension
                     * element and its content
                     */
                    SingleDimensionType sdt = null;

                    /* set typed dimension element */
                    if (currTypedMemberElement.getChildren().size() != 0) {
                        org.jdom.Element childElement = (org.jdom.Element) currTypedMemberElement
                                .getChildren().get(0);
                        sdt = new SingleDimensionType(dimensionElement,
                                childElement);
                    }

                    if (mdt == null) {
                        mdt = new MultipleDimensionType(sdt);
                    } else {
                        mdt.addPredecessorDimensionDomain(sdt);
                    }
                }

                if (mdt != null && currElement.getName().equals("scenario")) {
                    currContext.setDimensionalInformation(mdt,
                            GeneralConstants.DIM_SCENARIO);
                } else if (mdt != null
                        && currElement.getName().equals("segment")) {
                    currContext.setDimensionalInformation(mdt,
                            GeneralConstants.DIM_SEGMENT);
                }
            }

            contextMap.put(id, currContext);
            instance.addContext(currContext);
        }

    }

    /**
     * Sets facts of the instance.
     * 
     * @throws InstanceException
     */
    private void setFactsAndTuples(org.jdom.Element xbrlRoot) throws InstanceException {
        //org.jdom.Element xbrlRoot = getXBRLRootElement(xmlInstance);
        @SuppressWarnings("unchecked")
        List<org.jdom.Element> factElementList = xbrlRoot.getChildren();
        for (org.jdom.Element currFactElement : factElementList) {
            if (!currFactElement.getName().equals("context")
                    && !currFactElement.getName().equals("schemaRef")
                    && !currFactElement.getName().equals("unit")) {

                Concept currFactConcept = instance.getConceptByName(currFactElement.getNamespace(), currFactElement.getName());
if ( currFactConcept == null )
    System.err.println( ">>skipped element: " + currFactElement.getNamespacePrefix() + ":" + currFactElement.getName() );
//else
//    System.err.println( ">>good element: " + currFactElement.getNamespacePrefix() + ":" + currFactElement.getName() );

                if (currFactConcept != null) {
                    if (currFactConcept instanceof TupleDefinition) {
                        Tuple newTuple = createTuple((TupleDefinition)currFactConcept, currFactElement);
                        instance.addTuple(newTuple);
                    } else { // is item?
                        Fact newFact = createFact(currFactConcept, currFactElement);
                        instance.addFact(newFact);
                    }
                }
            }
        }
    }

    /*
    private org.jdom.Element getXBRLRootElement(Object docObject) {
        org.jdom.Document doc = (org.jdom.Document)docObject;
        org.jdom.Element xbrl = doc.getRootElement().getChild("xbrl", toJDOM(NamespaceConstants.XBRLI_NAMESPACE));

        return xbrl;
    }
    */

    private Fact createFact(Concept concept, org.jdom.Element element) throws InstanceException {
        if (concept == null) {
            throw new InstanceException(
                    ExceptionConstants.EX_INSTANCE_CREATION_FACT + element.getName());
        }
        // now it is a fact element
        Fact newFact = new Fact(concept);

        // check if it refers to a valid context and unit
        String contextID = element.getAttributeValue("contextRef");
        InstanceContext ctx = contextMap.get(contextID);
        if (ctx == null) {
            throw new InstanceException(
            ExceptionConstants.EX_INSTANCE_CREATION_NO_CONTEXT + element.getName());
        }

        newFact.setInstanceContext(ctx);
        if (element.getAttributeValue("id") != null) {
            newFact.setID(element.getAttributeValue("id"));
        }

        if (concept.isNumericItem()) {
            String unitID = element.getAttributeValue("unitRef");
            InstanceUnit unit = unitMap.get(unitID);
                newFact.setInstanceUnit(unit);

            // set remaining information
            if (element.getAttributeValue("decimals") != null) {
                newFact.setDecimals(Integer.parseInt(element.getAttributeValue("decimals")));
            }
            if (element.getAttributeValue("precision") != null) {
                newFact.setPrecision(Integer.parseInt(element.getAttributeValue("precision")));
            }
        }
        if (element.getContentSize() == 0) {
            newFact.setValue(null);
        } else {
            newFact.setValue(element.getValue());
        }
        return newFact;
    }

    private Tuple createTuple(TupleDefinition tupleDef, org.jdom.Element element) throws InstanceException {
        if (tupleDef == null) {
            throw new InstanceException(
                    ExceptionConstants.EX_INSTANCE_CREATION_FACT + element.getName());
        }

        // now it is a tuple element
        Tuple tuple = new Tuple(tupleDef);

        // check if it refers to a valid context and unit
        String contextID = element.getAttributeValue("contextRef");
        if (contextID == null) {
            // Does not work on nested tuples
            //contextID = ((org.jdom.Element)element.getChildren().get(0)).getAttributeValue("contextRef");

            // Traverse Tuplestructure until contextID is found
            // Iterator<org.jdom.Element> itr = element.getDescendants(); //Does not work, must be checked if instance of jdom.Element 
            @SuppressWarnings("rawtypes")
			Iterator itr = element.getDescendants();
            while (itr.hasNext()) {
                Object obj = itr.next();
                if (!(obj instanceof org.jdom.Element))
                    continue;
                org.jdom.Element currElement = (org.jdom.Element)obj;
                contextID = currElement.getAttributeValue("contextRef");
                if (contextID != null)
                    break;
            }
        }
        if (contextID == null) {
            throw new InstanceException(
                "contextRef attribute not found for element: " + element.getName());
        }
        InstanceContext ctx = contextMap.get(contextID);
        if (ctx == null) {
            throw new InstanceException(
            ExceptionConstants.EX_INSTANCE_CREATION_NO_CONTEXT + element.getName() + " in " + contextID);
        }

        tuple.setInstanceContext(ctx);
        
        /*
        if (element.getAttributeValue("id") != null) {
            tuple.setID(element.getAttributeValue("id"));
        }
        */

        @SuppressWarnings("unchecked")
        List<org.jdom.Element> childElementList = element.getChildren();
        for (org.jdom.Element currElement : childElementList) {
            Concept currConcept = instance.getConceptByName(currElement.getNamespace(), currElement.getName());

            if (currConcept instanceof TupleDefinition) {
                Tuple newChildTuple = createTuple((TupleDefinition)currConcept, currElement);
                tuple.addSelection(currConcept, newChildTuple);
            } else { // is item?
                Fact newFact = createFact(currConcept, currElement);
                tuple.addSelection(currConcept, newFact);
            }
        }
        return tuple;
    }

    protected static File getFileFromURL(URL url) throws MalformedURLException {
        if (!url.getProtocol().equals("file"))
            throw new IllegalArgumentException("The passed URL does not point to a file.");
        
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    protected static String getNameFromURL(URL url) {
        String s = url.toString();
        if (s.length() <= 1)
            return s;
        int p = s.lastIndexOf('/');
        if (p == s.length() - 1) {
            int p2 = s.lastIndexOf('/', p - 1);
            if (p2 >= 0) {
                if (p2 == p - 1)
                    return "";
                
                return s.substring(p + 1, p);
            }
            
            return s.substring(0, p);
        }
        
        if (p >= 0)
            return s.substring(p + 1);
        
        return s;
    }
}
