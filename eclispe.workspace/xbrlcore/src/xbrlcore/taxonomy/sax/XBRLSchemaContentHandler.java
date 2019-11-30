/*
 * Created on 25.05.2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package xbrlcore.taxonomy.sax;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import xbrlcore.constants.NamespaceConstants;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.Namespace;
import xbrlcore.taxonomy.TaxonomySchema;
import xbrlcore.util.PathResolver;

/**
 * @author d2504hd
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class XBRLSchemaContentHandler implements ContentHandler {

	private TaxonomySchema taxonomySchema;

	private Map<String, String> namespaceMapping;

	private Map<String, String> importedSchemaFiles;

	private Map<String, String> linkbaseFiles;

	public void setTaxonomySchema(TaxonomySchema taxonomySchema) {
		this.taxonomySchema = taxonomySchema;
		this.importedSchemaFiles = new HashMap<String, String>();
		this.linkbaseFiles = new HashMap<String, String>();
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startDocument() throws SAXException {
		if (namespaceMapping == null) {
			namespaceMapping = new HashMap<String, String>();
		} else {
			namespaceMapping = new HashMap<String, String>();
		}
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		// TODO Auto-generated method stub

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
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub

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
	public void setDocumentLocator(Locator arg0) {
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
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		// TODO Auto-generated method stub

	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("schema")) {
			String targetNamespaceURI = atts.getValue("targetNamespace");
			//try to get the prefix from the additionnal namespace instead of forging it
			String targetNamespacePrefix = null;
			/*
			for (int i = 0; i < atts.getLength(); i++){
				if (atts.getQName(i).startsWith("xmlns:")){
					if (atts.getValue(i).equalsIgnoreCase(targetNamespaceURI)){
						targetNamespacePrefix = atts.getQName(i).substring(atts.getQName(i).indexOf(':') + 1);
						break;
					}
				}
			}
			*/
			for (Map.Entry<String, String> pairs : namespaceMapping.entrySet()) {
		        if (pairs.getKey().equalsIgnoreCase(targetNamespaceURI)){
		        	targetNamespacePrefix = pairs.getValue();
		        	break;
		        }
		    }
			//if we did not found the "official" prefix, build one
			if (targetNamespacePrefix == null) {
				String separator = PathResolver.separator(targetNamespaceURI);
				targetNamespacePrefix = "ns_"
						+ targetNamespaceURI.substring(targetNamespaceURI
								.lastIndexOf(separator) + 1, targetNamespaceURI.length());
			}
			taxonomySchema.setNamespace(Namespace.getNamespace(
					targetNamespacePrefix, targetNamespaceURI));
		} else if (localName.equals("element")
				&& namespaceURI.equals(NamespaceConstants.XSD_NAMESPACE_URI)) {
			buildConcept(namespaceURI, localName, qName, atts);

		} else if (localName.equals("import")
				&& namespaceURI.equals(NamespaceConstants.XSD_NAMESPACE_URI)) {
			importedSchemaFiles.put(atts.getValue("namespace"), atts.getValue("schemaLocation"));
		} else if (localName.equals("linkbaseRef")
				&& namespaceURI.equals(NamespaceConstants.XBRL_SCHEMA_LOC_LINKBASE_URI)) {
			linkbaseFiles.put(atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "href"),
			                  atts.getValue(NamespaceConstants.XBRL_SCHEMA_LOC_XLINK_URI, "role"));
		}

	}

	/**
	 * 
	 * @param namespaceURI
	 * @param localName
	 * @param qName
	 * @param atts
	 */
    private void buildConcept(String namespaceURI, String localName,
			String qName, Attributes atts) {
		if (atts.getValue("name") == null || atts.getValue("id") == null)
			return;
		Concept concept = new Concept(atts.getValue("name"),
		                              atts.getValue("id"),
		                              atts.getValue("type"),
		                              null,
		                              taxonomySchema,
		                              atts.getValue("substitutionGroup"),
		                              atts.getValue("abstract") != null && atts.getValue("abstract").equals("true"),
		                              atts.getValue("nillable") != null && atts.getValue("nillable").equals("true"),
		                              atts.getValue(namespaceMapping.get(NamespaceConstants.XBRL_SCHEMA_LOC_INSTANCE_URI) + ":periodType"),
                                      atts.getValue(namespaceMapping.get(NamespaceConstants.XBRL_SCHEMA_LOC_INSTANCE_URI) + ":balance"),
		                              atts.getValue(NamespaceConstants.XBRLDT_URI, "typedDomainRef"),
		                              atts.getValue("enum:linkrole"));
		

		// TODO: This exception must be thrown to the invoking method!
		try {
			taxonomySchema.addConcept(concept);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.toString());
		}
	}

	public Map<String, String> getImportedSchemaFiles() {
		return new HashMap<String, String>(importedSchemaFiles);
	}

	public Map<String, String> getLinkbaseFiles() {
		return new HashMap<String, String>(linkbaseFiles);
	}
}