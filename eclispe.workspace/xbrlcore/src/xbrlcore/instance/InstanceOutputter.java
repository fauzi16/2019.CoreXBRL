package xbrlcore.instance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import xbrlcore.constants.GeneralConstants;
import xbrlcore.constants.NamespaceConstants;
import xbrlcore.dimensions.SingleDimensionType;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.taxonomy.Namespace;
import xbrlcore.taxonomy.TupleDefinition.TupleType;
import static xbrlcore.taxonomy.NamespaceJDOMAdapter.toJDOM;

/**
 * This class creates the XML structure of an Instance object. So by using this
 * class, the content of an instance document - represented in an
 * xbrlcore.instance.Instance object - can be put out. <br/><br/>
 * 
 * @author Daniel Hamm
 *  
 */
public class InstanceOutputter {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    
    //FIXME: seems customer specific
    public static final Namespace ELSTER_SCHEMA_NS = Namespace.getNamespace("http://www.elster.de/2002/XMLSchema");

    private final NumberFormat NF;
    {
    	NF = NumberFormat.getNumberInstance( Locale.US );
        NF.setMinimumFractionDigits(2);
        NF.setMaximumFractionDigits(2);
        NF.setGroupingUsed(false);
    }

    private final Instance instance;

    private boolean addConceptNamespaceToFact = true;

    private SchemaRefMapping schemaRefMapping = new SchemaRefMapping();

    /**
     * Constructor.
     * 
     * @param instance
     *            Instance which shall be put out.
     */
    public InstanceOutputter(Instance instance) {
        this.instance = instance;
    }

    public final Instance getInstance() {
        return instance;
    }

    public void setAddConceptNamespaceToFact(boolean addConceptNamespaceToFact) {
        this.addConceptNamespaceToFact = addConceptNamespaceToFact;
    }

    public final boolean getAddConceptNamespaceToFact() {
        return addConceptNamespaceToFact;
    }

    public void setSchemaRefMapping(SchemaRefMapping schemaRefMapping) {
        if (schemaRefMapping == null) {
            throw new IllegalArgumentException("schemaRefMapping must not be null.");
        }

        this.schemaRefMapping = schemaRefMapping;
    }

    public final SchemaRefMapping getSchemaRefMapping() {
        return schemaRefMapping;
    }

    /**
     * This method saves the xbrlcore.instance.Instance object as an XBRL
     * Instance document file on the file system. The file is UTF-8 encoded.
     * 
     * @param file
     *            Name of the file.
     * 
     * @throws IOException
     */
    public void saveAsFile(File file) throws IOException {
        saveAsFile(file.getParent(), file.getName(), DEFAULT_CHARSET);
    }

    /**
     * This method saves the xbrlcore.instance.Instance object as an XBRL
     * Instance document file on the file system. The file is UTF-8 encoded.
     * 
     * @param filename
     *            Name of the file.
     * 
     * @throws IOException
     */
    public void saveAsFile(String filename) throws IOException {
        saveAsFile(null, null, new File(filename), DEFAULT_CHARSET);
    }

    /**
     * This method saves the xbrlcore.instance.Instance object as an XBRL
     * Instance document file on the file system. The file is UTF-8 encoded.
     * 
     * @param directory
     *            Name of the directory (if it does not end with
     *            File.separatorChar, this is added).
     * @param fileName
     *            Name of the file.
     * @return If method succeeds a File object of the new file is returned,
     *         otherwise NULL.
     * 
     * @throws IOException
     */
    public File saveAsFile(String directory, String fileName) throws IOException {
        if (!directory.endsWith(File.separator))
            directory += File.separator;

        return saveAsFile(directory, fileName, null, DEFAULT_CHARSET);
    }

    /**
     * This method saves the xbrlcore.instance.Instance object as an XBRL
     * Instance document file on the file system. The file is UTF-8 encoded.
     * 
     * @param file
     *            Name of the file.
     * @param charset
     * 
     * @throws IOException
     */
    public void saveAsFile(File file, Charset charset) throws IOException {
        saveAsFile(file.getParent(), file.getName(), charset);
    }

    /**
     * This method saves the xbrlcore.instance.Instance object as an XBRL
     * Instance document file on the file system. The file is UTF-8 encoded.
     * 
     * @param filename
     *            Name of the file.
     * @param charset
     * 
     * @throws IOException
     */
    public void saveAsFile(String filename, Charset charset) throws IOException {
        saveAsFile(null, null, new File(filename), charset);
    }

    /**
     * This method saves the xbrlcore.instance.Instance object as an XBRL
     * Instance document file on the file system. The file is UTF-8 encoded.
     * 
     * @param directory
     *            Name of the directory (if it does not end with
     *            File.separatorChar, this is added).
     * @param fileName
     *            Name of the file.
     * @param charset
     * @return If method succeeds a File object of the new file is returned,
     *         otherwise NULL.
     * 
     * @throws IOException
     */
    public File saveAsFile(String directory, String fileName, Charset charset) throws IOException {
        if (!directory.endsWith(File.separator))
            directory += File.separator;

        return saveAsFile(directory, fileName, null, charset);
    }

    private File saveAsFile(String directory, String fileName, File file, Charset charset) throws IOException {
        if ((directory != null) && !directory.endsWith(File.separator))
            directory += File.separator;

        if (file == null) {
            file = new File(directory, fileName);
        }

        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file), charset);

            XMLOutputter serializer = new XMLOutputter();
            Format f = Format.getPrettyFormat();
            f.setEncoding(charset.name());
            serializer.setFormat(f);
            serializer.output(getXML(), out);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e ) {
                }
            }
        }

        return file;
    }

    /**
     * @param charset
     * 
     * @return XML structure of the instance as java.lang.String object.
     */
    public String getXMLString(Charset charset) {
        XMLOutputter serializer = new XMLOutputter();
        Format f = Format.getPrettyFormat();
        f.setEncoding(charset.name());
        serializer.setFormat(f);
        return serializer.outputString(getXML());
    }

    /**
     * 
     * @return XML structure which is the content of the instance.
     */
    public Document getXML() {
        Document document = new Document();

        Element xbrlElement = buildXBRLElement();

        addXBRLElement(document, xbrlElement);

        return document;
    }

    protected void addXBRLElement(Document document, Element xbrlElement) {
        document.setRootElement(xbrlElement);
    }

    protected void addContextElements(Instance instance, Element xbrlElement) {
        for (InstanceContext currContext : instance.getContextMap().values()) {
            xbrlElement.addContent(getContextElement(currContext));
        }
    }

    protected void addUnitElements(Instance instance, Element xbrlElement) {
        for (InstanceUnit currUnit : instance.getUnitMap().values()) {
            xbrlElement.addContent(getUnitElement(currUnit));
        }
    }

    protected void addFactsAndTuples(Instance instance, Element xbrlElement) {
        for (Object o : instance.getFactsAndTuples()) {
            if (o instanceof Fact) {
                xbrlElement.addContent(getFactElement((Fact)o, false));
            } else if (o instanceof Tuple) {
                Element elem = getTupleElement((Tuple)o);
                if (elem != null ) {
                    xbrlElement.addContent(elem);
                }
            }
        }
    }

    /**
     * This method is invoked at the end of concept creation.
     * The default implementation is an empty stub.
     * 
     * @param instance
     * @param xbrlElement
     */
    protected void addCustomElements(Instance instance, Element xbrlElement) {
    }

    private Element buildXBRLElement() {
        Element xbrlElement = getXBRLElement();

        // set schemaRef elements
        getSchemaRefElements(xbrlElement);

        addContextElements(instance, xbrlElement);
        addUnitElements(instance, xbrlElement);
        addFactsAndTuples(instance, xbrlElement);
        addCustomElements(instance, xbrlElement);

        return xbrlElement;
    }

    /**
     * @return Returns root element of the instance including all namespaces and
     *         schema locations.
     */
    private Element getXBRLElement() {
        Element xbrlElement = new Element("xbrl", toJDOM(instance.getInstanceNamespace()));

        // add additinal namespace declarations
        for (Namespace currNamespace : instance.getAdditionalNamespaceSet()) {
            xbrlElement.addNamespaceDeclaration(toJDOM(currNamespace));
        }

        // set value of attribute schemaLocation
        String schemaLocationValue = "";
        Map<String, String> schemaLocationMap = instance.getSchemaLocationMap();
        for (Map.Entry<String, String> currEntry : schemaLocationMap.entrySet()) {
            String schemaURI = currEntry.getKey();
            String schemaName = currEntry.getValue();
            if (schemaLocationValue.length() > 0) {
                schemaLocationValue += " ";
            }
            schemaLocationValue += schemaURI + " " + schemaName;
        }
        if (schemaLocationValue.length() > 0) {
            xbrlElement.setAttribute("schemaLocation", schemaLocationValue);
        }

        if (instance.getComment() != null) {
            xbrlElement.addContent(new Comment(instance.getComment()));
        }
        return xbrlElement;
    }

    /**
     * 
     * Adds Set of schemaRef elements of the instance document, which points
     *         to the according taxonomies.
     * 
     * @param dts
     * 
     * @return the Element.
     */
    protected Element getSchemaRefElement(DiscoverableTaxonomySet dts) {
        org.jdom.Namespace SIMPLE_LINK_NS = toJDOM(instance.getNamespace(NamespaceConstants.LINK_NAMESPACE.getURI()));
        org.jdom.Namespace SIMPLE_XLINK_NS = toJDOM(instance.getNamespace(NamespaceConstants.XLINK_NAMESPACE.getURI()));

        Element schemaRefElement = new Element("schemaRef", SIMPLE_LINK_NS);

        Attribute typeAttribute = new Attribute("type", "simple", SIMPLE_XLINK_NS);
        schemaRefElement.setAttribute(typeAttribute);

        Attribute arcroleAttribute = new Attribute("arcrole", GeneralConstants.XBRL_INSTANCE_LINKBASE_ARCROLE, SIMPLE_XLINK_NS);
        schemaRefElement.setAttribute(arcroleAttribute);

        Attribute hrefAttribute = new Attribute("href", getSchemaRefMapping().getSchemaRefHREF(dts.getTopTaxonomy().getName()), SIMPLE_XLINK_NS);
        schemaRefElement.setAttribute(hrefAttribute);

        return schemaRefElement;
    }

    /**
     * 
     * Adds Set of schemaRef elements of the instance document, which points
     *         to the according taxonomies.
     * 
     * @param parent
     */
    protected void getSchemaRefElements(Element parent) {
        for (DiscoverableTaxonomySet currDts : instance.getDiscoverableTaxonomySet()) {
            Element schemaRefElement = getSchemaRefElement(currDts);

            if (schemaRefElement != null)
                parent.addContent(schemaRefElement);
        }
    }

    /**
     * Creates context structure of a specific context.
     * 
     * @param context
     *            Context for which the XML structure is created.
     * @return XML structure of the according context.
     */
    private Element getContextElement(InstanceContext context) {
        Element contextElement = new Element("context");

        contextElement.setAttribute(new Attribute("id", context.getID()));
        contextElement.setNamespace(toJDOM(instance.getInstanceNamespace()));

        /* set <xbrli:entity> */
        Element elementIdentifier = new Element("identifier");
        elementIdentifier.setNamespace(toJDOM(instance.getInstanceNamespace()));
        if (context.getIdentifierScheme() != null) {
            elementIdentifier.setAttribute(new Attribute("scheme", context
                    .getIdentifierScheme()));
        }
        if (context.getIdentifier() != null) {
            elementIdentifier.setText(context.getIdentifier());
        }

        Element elementEntity = new Element("entity");
        elementEntity.setNamespace(toJDOM(instance.getInstanceNamespace()));
        elementEntity.addContent(elementIdentifier);

        /* set <xbrli:period> */
        Element elementPeriod = new Element("period");
        elementPeriod.setNamespace(toJDOM(instance.getInstanceNamespace()));

        Element elementPeriodType = null;

        if (context.getPeriodValue() != null) {
            if (context.getPeriodValue().equals("forever")) {
                elementPeriodType = new Element("forever");
            } else {
                elementPeriodType = new Element("instant");
                elementPeriodType.setText(context.getPeriodValue());
            }
            elementPeriodType.setNamespace(toJDOM(instance.getInstanceNamespace()));
            elementPeriod.addContent(elementPeriodType);
        } else if (context.getPeriodStartDate() != null
                && context.getPeriodEndDate() != null) {
            Element elementStartDate = new Element("startDate");
            Element elementEndDate = new Element("endDate");

            elementStartDate.setText(context.getPeriodStartDate());
            elementEndDate.setText(context.getPeriodEndDate());

            elementStartDate.setNamespace(toJDOM(instance.getInstanceNamespace()));
            elementEndDate.setNamespace(toJDOM(instance.getInstanceNamespace()));

            elementPeriod.addContent(elementStartDate);
            elementPeriod.addContent(elementEndDate);
        }

        /* set <scenario> and <segment> only when they have child elements */
        Element scenarioElement = new Element("scenario");
        scenarioElement.setNamespace(toJDOM(instance.getInstanceNamespace()));
        for (Element currElement : context.getScenarioElements()) {
            scenarioElement.addContent(currElement.cloneContent());
        }

        Element segmentElement = new Element("segment");
        segmentElement.setNamespace(toJDOM(instance.getInstanceNamespace()));
        for (Element currElement : context.getSegmentElements()) {
            scenarioElement.addContent(currElement.cloneContent());
        }

        /* now set dimensional information */
        List<Integer> scenSegElementList = new ArrayList<Integer>();
        scenSegElementList.add(0, new Integer(GeneralConstants.DIM_SCENARIO));
        scenSegElementList.add(1, new Integer(GeneralConstants.DIM_SEGMENT));

        for (int i = 0; i < scenSegElementList.size(); i++) {
            int currScenSeg = scenSegElementList.get(i).intValue();
            if (context.getDimensionalInformation(currScenSeg) != null) {
                List<SingleDimensionType> allDimensionDomain = context.getDimensionalInformation(
                        currScenSeg).getAllSingleDimensionTypeList();
                for (SingleDimensionType currSDT : allDimensionDomain) {
                    Concept dimensionElement = currSDT.getDimensionConcept();
                    Element currDimElement = null;
                    if (currSDT.isTypedDimension()) {
                        currDimElement = new Element("typedMember");
                    } else {
                        currDimElement = new Element("explicitMember");
                    }
                    currDimElement.setNamespace(toJDOM(instance
                            .getNamespace(NamespaceConstants.XBRLDI_NAMESPACE
                                    .getURI())));
                    /* Attribute type=xlink:href="..." */
                    Attribute hrefAttribute = new Attribute("dimension",
                            dimensionElement.getTaxonomySchema().getNamespace().getPrefix() + ":"
                                    + dimensionElement.getName());
                    currDimElement.setAttribute(hrefAttribute);
                    /* set the value of the element */
                    if (currSDT.isTypedDimension()) {
                        /* the dimension is a typed dimension */
                        if (currSDT.getTypedDimensionElement() != null) {
                            /* now add the correct namespace */
                            Element rootElement = currSDT
                                    .getTypedDimensionElement();
                            /*
                             * if there is a parent element, it must be
                             * detached, otherwise addContent() throws an
                             * Exception
                             */
                            if (rootElement.getParent() != null) {
                                rootElement.detach();
                            }
                            currDimElement.addContent(rootElement);
                        }
                    } else {
                        /* the dimension is an explicit dimension */
                        Concept domainElement = currSDT
                                .getDomainMemberConcept();
                        currDimElement.setText(domainElement.getTaxonomySchema().getNamespace()
                                .getPrefix()
                                + ":" + domainElement.getName());
                    }
                    /*
                     * now add this element to scenarioElement or segment
                     * element
                     */
                    if (currScenSeg == GeneralConstants.DIM_SCENARIO)
                        scenarioElement.addContent(currDimElement);
                    else if (currScenSeg == GeneralConstants.DIM_SEGMENT)
                        segmentElement.addContent(currDimElement);
                }
            }
        }

        /* add segment to the entity element (only if there are child elements) */
        if (segmentElement.getChildren().size() > 0) {
            elementEntity.addContent(segmentElement);
        }

        /* now add everything to the context element */
        contextElement.addContent(elementEntity);
        contextElement.addContent(elementPeriod);
        /* <scenario> only if there are child elements */
        if (scenarioElement.getChildren().size() > 0) {
            contextElement.addContent(scenarioElement);
        }

        return contextElement;
    }

    /**
     * Creates unit structure of a specific unit.
     * 
     * @param unit
     *            Unit for which the XML structure is created.
     * @return XML structure of the according unit.
     */
    private Element getUnitElement(InstanceUnit unit) {
        Element unitElement = new Element("unit");

        unitElement.setAttribute(new Attribute("id", unit.getID()));
        unitElement.setNamespace(toJDOM(instance.getInstanceNamespace()));

        Element measureElement = new Element("measure");
        measureElement.setNamespace(toJDOM(instance.getInstanceNamespace()));
        Namespace ns = instance.getNamespace(unit.getNamespaceURI());
        if (ns == null)
            measureElement.setText(unit.getValue());
        else
            measureElement.setText(ns.getPrefix() + ":" + unit.getValue());

        unitElement.addContent(measureElement);

        return unitElement;
    }

    protected static void addContextToElement(InstanceContext context, Element element) {
        element.setAttribute(new Attribute("contextRef", context.getID()));
    }

    private static void addNumericAttributesToElement(Fact fact, Element element) {
        if (fact.hasDecimals()) {
            element.setAttribute(new Attribute("decimals", String.valueOf(fact.getDecimals())));
        }

        if (fact.hasPrecision()) {
            element.setAttribute(new Attribute("precision", String.valueOf(fact.getPrecision())));
        }

        if (fact.getInstanceUnit() != null) {
            element.setAttribute(new Attribute("unitRef", fact.getInstanceUnit().getID()));
        }
    }

    private String getFactStringValue(Fact fact, Object value) {
        if ((value instanceof Float) || (value instanceof Double)) {
            double dbl = ((Number)value).doubleValue();

            if (Math.abs(dbl) < 0.001)
                dbl = 0.0;

            if (fact.hasDecimals()) {
                NF.setMinimumFractionDigits(fact.getDecimals());
                NF.setMaximumFractionDigits(fact.getDecimals());

                return NF.format(dbl);
            }
        }

        return String.valueOf(value);
    }

    /**
     * Creates element of a specific fact.
     * 
     * @param fact
     *            Fact for which an element is created.
     * @param isTupleElement
     * @return Element of the according fact.
     */
    private Element getFactElement(Fact fact, boolean isTupleElement) {
        Element factElement = new Element(fact.getConcept().getName());

        if (getAddConceptNamespaceToFact()) {
            factElement.setNamespace(toJDOM(fact.getConcept().getTaxonomySchema().getNamespace()));
        }

        Object value = fact.getValue();

        if (value != null) {
            addNumericAttributesToElement(fact, factElement);
        }

        addContextToElement(fact.getInstanceContext(), factElement);

        if ((value == null) || (isTupleElement && (value == Boolean.FALSE))) {
            factElement.setAttribute(new Attribute("nil", "true", toJDOM(NamespaceConstants.XSI_NAMESPACE)));
            addNumericAttributesToElement(fact, factElement);
        } else if (!isTupleElement || (value != Boolean.TRUE)) {
            if (value instanceof Boolean)
                factElement.setText((Boolean)value ? "1" : "0");
            else
                factElement.setText(getFactStringValue(fact, value));
        }

        postProcessFactElement(fact, isTupleElement, factElement);

        return factElement;
    }

    /**
     * 
     * @param fact
     * @param isTupleElement
     * @param factElement
     */
    protected void postProcessFactElement(Fact fact, boolean isTupleElement, Element factElement) {
    }

    /**
     * @param tuple
     * @param item
     * @return is mandatory?
     */
    protected Boolean isTupleItemMandatory(Tuple tuple, Concept item) {
        return null;
    }

    /**
     * Creates element of a specific concept.
     * 
     * @param concept
     * @param context
     * @param nil
     * @return Element of the according concept.
     */
    protected Element getSelectionElement(Concept concept, InstanceContext context, boolean nil) {
        Element conceptElement = new Element(concept.getName());

        if (getAddConceptNamespaceToFact()) {
            conceptElement.setNamespace(toJDOM(concept.getTaxonomySchema().getNamespace()));
        }

        if (context != null) {
            addContextToElement(context, conceptElement);
        }

        if (nil) {
            conceptElement.setAttribute(new Attribute("nil", "true", toJDOM(NamespaceConstants.XSI_NAMESPACE)));
        }

        return conceptElement;
    }

    /**
     * Creates element of a specific fact.
     * 
     * @param tuple
     *            Tuple for which an element is created.
     * @return Element of the according fact.
     */
    private Element getTupleElement(Tuple tuple) {
        Element tupleElement = new Element(tuple.getDefinition().getName());

        if (getAddConceptNamespaceToFact()) {
            tupleElement.setNamespace(toJDOM(tuple.getDefinition().getTaxonomySchema().getNamespace()));
        }

        //addContextToElement(tuple.getInstanceContext(), tupleElement);

        Map<Concept, Object> selection = tuple.getSelection();

        if (tuple.getDefinition().getTupleType() == TupleType.CHOICE) {
            if (selection.size() == 0)
                return null;
        }

        boolean isSequence = tuple.getDefinition().getTupleType().isMulti();

        int numItems = 0;

        int n = tuple.getDefinition().getNumItems();
        for (int i = 0; i < n; i++) {
            Concept item = tuple.getDefinition().getItem(i);

            if (selection.containsKey(item)) {
                Object value = selection.get(item);

                if (value == null) {
                    tupleElement.addContent(getSelectionElement(item, tuple.getInstanceContext(), false));
                    numItems++;
                } else if (value instanceof Fact) {
                    tupleElement.addContent(getFactElement((Fact)value, true));
                    numItems++;
                } else if (value instanceof Tuple) {
                    Element elem = getTupleElement((Tuple)value);
                    if (elem != null) {
                        tupleElement.addContent(elem);
                        numItems++;
                    }
                }
            } else if (isSequence && (isTupleItemMandatory(tuple, item) != Boolean.FALSE)) {
                tupleElement.addContent(getSelectionElement(item, tuple.getInstanceContext(), true));
                numItems++;
            }
        }

        if (numItems == 0)
            return null;

        return tupleElement;
    }
}
