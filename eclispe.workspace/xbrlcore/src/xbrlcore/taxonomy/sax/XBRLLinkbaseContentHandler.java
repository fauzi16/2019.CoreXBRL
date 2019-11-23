package xbrlcore.taxonomy.sax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import xbrlcore.constants.GeneralConstants;
import xbrlcore.constants.NamespaceConstants;
import xbrlcore.linkbase.CalculationLinkbase;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.Linkbase;
import xbrlcore.linkbase.PresentationLinkbase;
import xbrlcore.linkbase.ReferenceLinkbase;
import xbrlcore.logging.ConsoleLogInterface;
import xbrlcore.taxonomy.DefaultLinkbaseArcsCollector;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.AttributesImplSer;
import xbrlcore.xlink.CalculationArc;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;
import xbrlcore.xlink.Resource;
import xbrlcore.xlink.Arc.UseAttribute;

/**
 * @author d2504hd
 */
public class XBRLLinkbaseContentHandler implements ContentHandler {

    private Linkbase linkbase;

    private final Map<Linkbase, DefaultLinkbaseArcsCollector<Arc>> lbacs = new HashMap<Linkbase, DefaultLinkbaseArcsCollector<Arc>>();

    private String extendedLinkRole;

    private DiscoverableTaxonomySet dts;

    private String linkbaseSource;

    private boolean inLabel = false;

    private AttributesImplSer resourceAtts;

    private Map<Integer, AttributesImplSer> arcMap;

    private Map<String, String> namespaceMapping;

    public XBRLLinkbaseContentHandler() {
    }

    public void setLinkbase(Linkbase linkbase) {
        this.linkbase = linkbase;
        DefaultLinkbaseArcsCollector<Arc> lbac = lbacs.get(linkbase);
        if (lbac == null) {
            lbac = new DefaultLinkbaseArcsCollector<Arc>(new ConsoleLogInterface());
            lbacs.put(linkbase, lbac);
        }
    }

    public void setDTS(DiscoverableTaxonomySet dts) {
        this.dts = dts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub
        if (arcMap == null) {
            arcMap = new HashMap<Integer, AttributesImplSer>();
        } else {
            arcMap.clear();
        }
        if (namespaceMapping == null) {
            namespaceMapping = new HashMap<String, String>();
        } else {
            namespaceMapping.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skippedEntity(String arg0) throws SAXException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentLocator(org.xml.sax.Locator arg0) {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startPrefixMapping(String prefix, String url)
            throws SAXException {
        namespaceMapping.put(url, prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endPrefixMapping(String arg0) throws SAXException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String namespaceURI, String localName,
            String qName, org.xml.sax.Attributes atts) throws SAXException {
        if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_LABEL)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            // check whether we are in correct linkbase
            if (!(linkbase instanceof LabelLinkbase)) {
                // TODO: Exception werfen
            }
            extendedLinkRole = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "role");
            linkbase.addExtendedLinkRole(extendedLinkRole);
            //FIXME: handle of roleRef now we have RoleType
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_PRESENTATION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            // check whether we are in correct linkbase
            if (!(linkbase instanceof PresentationLinkbase)) {
                // TODO: Exception werfen
            }
            extendedLinkRole = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "role");
            linkbase.addExtendedLinkRole(extendedLinkRole);
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_DEFINITION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            // check whether we are in correct linkbase
            if (!(linkbase instanceof DefinitionLinkbase)) {
                // TODO: Exception werfen
            }
            extendedLinkRole = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "role");
            linkbase.addExtendedLinkRole(extendedLinkRole);
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_CALCULATION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            // check whether we are in correct linkbase
            if (!(linkbase instanceof CalculationLinkbase)) {
                // TODO: Exception werfen
            }
            extendedLinkRole = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "role");
            linkbase.addExtendedLinkRole(extendedLinkRole);
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_REFERENCE)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            // check whether we are in correct linkbase
            if (!(linkbase instanceof ReferenceLinkbase)) {
                // TODO: Exception werfen
            }
            extendedLinkRole = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "role");
            linkbase.addExtendedLinkRole(extendedLinkRole);
        }

        else if (localName.equals("loc")
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            String type = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "type");
            if (type.equals("locator")) {
                buildLocator(convert(atts));
            } else {
                // TODO: Exception werfen?
            }
        } else if (localName.equals("label")
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            /* check whether we are in label linkbase */
            if (!(linkbase instanceof LabelLinkbase)) {
                // TODO: Exception werfen
            }
            String type = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "type");
            if (type.equals("resource")) {
                inLabel = true;
                resourceAtts = convert(atts);
            } else {
                // TODO: Exception werfen
            }
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_ARC_LABEL)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            /* check whether we are in label linkbase */
            if (!(linkbase instanceof LabelLinkbase)) {
                // TODO: Exception werfen
            }

            /* create new object to save attribute data */
            arcMap.put(new Integer(arcMap.size()), new AttributesImplSer(atts));
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_ARC_PRESENTATION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            if (!(linkbase instanceof PresentationLinkbase)) {
                // TODO: Exception werfen
            }
            arcMap.put(new Integer(arcMap.size()), new AttributesImplSer(atts));
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_ARC_DEFINITION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            if (!(linkbase instanceof DefinitionLinkbase)) {
                // TODO: Exception werfen
            }
            arcMap.put(new Integer(arcMap.size()), new AttributesImplSer(atts));
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_ARC_CALCULATION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            if (!(linkbase instanceof CalculationLinkbase)) {
                // TODO: Exception werfen
            }
            arcMap.put(new Integer(arcMap.size()), new AttributesImplSer(atts));
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_ARC_REFERENCE)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            if (!(linkbase instanceof ReferenceLinkbase)) {
                // TODO: Exception werfen
            }
            arcMap.put(new Integer(arcMap.size()), new AttributesImplSer(atts));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        if (inLabel) {
            buildResource(new String(arg0, arg1, arg2));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_LABEL)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            /* create all arcs now */
            buildArcs();
            extendedLinkRole = null;
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_PRESENTATION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            buildArcs();
            extendedLinkRole = null;
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_DEFINITION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            buildArcs();
            extendedLinkRole = null;
        } else if (localName.equals(GeneralConstants.XBRL_LINKBASE_LINK_CALCULATION)
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            buildArcs();
            extendedLinkRole = null;
        } else if (localName.equals("label")
                && namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
            inLabel = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        for (Entry<Linkbase, DefaultLinkbaseArcsCollector<Arc>> e : lbacs.entrySet()) {
            e.getKey().applyArcs(e.getValue(), false);
        }

        lbacs.clear();
    }

    private void buildArcs() {
        for (AttributesImplSer atts : arcMap.values()) {
            atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "from");

            String fromAttribute = atts.getValue(
                    NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "from");
            String toAttribute = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "to");

            List<ExtendedLinkElement> fromElements = linkbase.getExtendedLinkElements(fromAttribute,
                    extendedLinkRole, linkbaseSource);
            List<ExtendedLinkElement> toElements = linkbase.getExtendedLinkElements(toAttribute,
                    extendedLinkRole, linkbaseSource);

            for (int fromNumber = 0; fromNumber < fromElements.size(); fromNumber++) {
                ExtendedLinkElement currFromElement = fromElements.get(fromNumber);
                for (int toNumber = 0; toNumber < toElements.size(); toNumber++) {
                    ExtendedLinkElement currToElement = toElements.get(toNumber);
                    String arcRole = atts.getValue(
                            NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "arcrole");
                    String title = atts.getValue(
                            NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "title");
                    String targetRole = atts.getValue(
                            NamespaceConstants.XBRLDT_URI, "targetRole");
                    String contextElement = atts.getValue(
                            NamespaceConstants.XBRLDT_URI, "contextElement");
                    String order = atts.getValue("order");
                    String priority = atts.getValue("priority");
                    String use = atts.getValue("use");
                    String usable = atts.getValue(
                            NamespaceConstants.XBRLDT_URI, "usable");
                    String weight = atts.getValue("weight");
                    Arc newArc = ( linkbase instanceof CalculationLinkbase )
                                 ? new CalculationArc(currFromElement, currToElement, arcRole, extendedLinkRole, targetRole, contextElement, atts, title, (order == null) ? Arc.DEFAULT_ORDER : Float.parseFloat(order), UseAttribute.parse(use), (priority == null) ? Arc.DEFAULT_PRIORITY_ATTRIBUTE : Integer.parseInt(priority), (weight == null) ? Arc.DEFAULT_WEIGHT : Float.parseFloat(weight))
                                 : new Arc(currFromElement, currToElement, arcRole, extendedLinkRole, targetRole, contextElement, atts, title, (order == null) ? Arc.DEFAULT_ORDER : Float.parseFloat(order), UseAttribute.parse(use), (priority == null) ? Arc.DEFAULT_PRIORITY_ATTRIBUTE : Integer.parseInt(priority), (weight == null) ? Arc.DEFAULT_WEIGHT : Float.parseFloat(weight));
                    if (usable != null && usable.equals("false")) {
                        if (newArc.getTargetElement().isLocator()) {
                            ((Locator) newArc.getTargetElement()).setUsable(false);
                        }
                    }

                    lbacs.get(linkbase).addArc(newArc);
                }
            }
        }
    }

    private void buildResource(String value)/* throws TaxonomyCreationException*/ {
        String label = resourceAtts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "label");
        String title = resourceAtts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "title");
        // a resource has to be created
        String role = resourceAtts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "role");
        String id = resourceAtts.getValue("id");
        String lang = resourceAtts.getValue(NamespaceConstants.XML_NS_URI, "lang");

        Resource newResource = new Resource(id, label, linkbaseSource, extendedLinkRole, role, title, lang, value);

        linkbase.addExtendedLinkElement(newResource);
    }

    private void buildLocator(AttributesImplSer atts)/* throws TaxonomyCreationException*/ {
        String label = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "label");
        String role = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "role");
        String title = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "title");

        // a locator has to be created
        String conceptName = atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "href");
        Locator newLocator;
        // try to obtain the concept
        String elementId = conceptName.substring(conceptName.indexOf("#") + 1, conceptName.length());
        Concept concept = dts.getConceptByID(elementId);
        if (concept != null) {
            newLocator = new Locator("", label, linkbaseSource, extendedLinkRole, role, title, concept);
        } else {
            // the href does not point to a concept, but to another resource into another linkbase
            String targetLinkbase  = conceptName.substring(0, conceptName.indexOf('#'));
            Resource resource = new Resource("", elementId, targetLinkbase, null, null, null, null, null);
            // try to get an already existing resource
            // TODO: Wie??
            /*
            throw new TaxonomyCreationException(
                    ExceptionConstants.EX_LINKBASE_LOCATOR_WITHOUT_REF + ": "
                            + elementId + " in Linkbase " + linkbaseSource);
            */
            newLocator = new Locator("", label, linkbaseSource, extendedLinkRole, role, title, resource);
        }

        linkbase.addExtendedLinkElement(newLocator);
    }

    private static AttributesImplSer convert(org.xml.sax.Attributes atts) {
        return new AttributesImplSer(atts);
    }
}
