package xbrlcore.constants;

import xbrlcore.taxonomy.Namespace;

/**
 * This class defines some constants for namespaces and schema files /
 * locations.<br/><br/>
 * 
 * @author Daniel Hamm
 */
public class NamespaceConstants {

	/* schema & namespaces locations*/
    public static final String XBRL_SCHEMA_LOC_INSTANCE_URI = "http://www.xbrl.org/2003/instance";

    public static final String XBRL_SCHEMA_NAME_XBRLI = "xbrl-instance-2003-12-31.xsd";

    public static final String XSD_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

    public static final String XBRL_SCHEMA_LOC_LINKBASE_URI = "http://www.xbrl.org/2003/linkbase";

    public static final String XBRL_SCHEMA_NAME_LINKBASE = "xbrl-linkbase-2003-12-31.xsd";

    public static final String XBRL_SCHEMA_LOC_XLINK_URI = "http://www.w3.org/1999/xlink";

    public static final String XBRL_SCHEMA_NAME_XLINK = "xlink-2003-12-31.xsd";

    public static final String XBRL_SCHEMA_LOC_XBRLDI_URI = "http://xbrl.org/2005/xbrldi";

    public static final String XBRL_SCHEMA_NAME_XBRLDI = "xbrldi-2005.xsd";

    public static final String XBRLDT_URI = "http://xbrl.org/2005/xbrldt";

    public static final String XBRLDI_NS_URI = "http://xbrl.org/2006/xbrldi";

	public static final String ISO4217_NS_URI = "http://www.xbrl.org/2003/iso4217";

	public static final String XMLSCHEMA_INSTANCE_URI = "http://www.w3.org/2001/XMLSchema-instance";

	public static final String XML_NS_URI = "http://www.w3.org/XML/1998/namespace";

	public static final String XLINK_NS_URI = "http://www.xbrl.org/2003/XLink";

    public static final String REF_URI = "http://www.xbrl.org/2024/ref";

    public static final String XHTML_URI = "http://www.w3.org/1999/xhtml";

    public static final String COREP_SCHEMA_LOC_DT_URI = "http://xbrl.c-ebs.org/dt";

    public static final String COREP_SCHEMA_NAME_DT = "dt-2005-06-20.xsd";

    public static final String HGBREF_URI = "http://www.xbrl.de/2008/ref";
    
    public static final String ENUM_URI = "http://xbrl.org/2014/extensible-enumerations";

    // Prefixes
    
    public static final String ENUM_NAMESPACE_PREFIX = "enum";

    public static final String XBRLI_NAMESPACE_PREFIX = "xbrli";

    public static final String XLINK_NAMESPACE_PREFIX = "xlink";

    public static final String XBRLDT_NAMESPACE_PREFIX = "xbrldt";

    public static final String XBRLDI_NAMESPACE_PREFIX = "xbrldi";

    public static final String ISO4217_NAMESPACE_PREFIX = "iso4217";

    public static final String XSD_NAMESPACE_PREFIX = "xsd";
    
    public static final String XSI_NAMESPACE_PREFIX = "xsi";

    public static final String LINK_NAMESPACE_PREFIX = "link";

    public static final String XL_NAMESPACE_PREFIX = "xl";

    public static final String XML_NAMESPACE_PREFIX = "xml";

    public static final String DT_NAMESPACE_PREFIX = "dt";

    public static final String REF_NAMESPACE_PREFIX = "ref";

    public static final String HGBREF_NAMESPACE_PREFIX = "hgbref";

    public static final String XHTML_NAMESPACE_PREFIX = "xhtml";

	// namespaces

	public static final Namespace XBRLI_NAMESPACE = Namespace.getNamespace(
			XBRLI_NAMESPACE_PREFIX, XBRL_SCHEMA_LOC_INSTANCE_URI);

	public static final Namespace XLINK_NAMESPACE = Namespace.getNamespace(
			XLINK_NAMESPACE_PREFIX, XBRL_SCHEMA_LOC_XLINK_URI);

	public static final Namespace XBRLDT_NAMESPACE = Namespace.getNamespace(
			XBRLDT_NAMESPACE_PREFIX, XBRLDT_URI);

	public static final Namespace XBRLDI_NAMESPACE = Namespace.getNamespace(
			XBRLDI_NAMESPACE_PREFIX, XBRLDI_NS_URI);

	public static final Namespace ISO4217_NAMESPACE = Namespace.getNamespace(
			ISO4217_NAMESPACE_PREFIX, ISO4217_NS_URI);

	public static final Namespace XSD_NAMESPACE = Namespace.getNamespace(
			XSD_NAMESPACE_PREFIX, XSD_NAMESPACE_URI);
	
	public static final Namespace ENUM_NAMESPACE = Namespace.getNamespace(
			ENUM_NAMESPACE_PREFIX, ENUM_URI);
	
	/*
	public static final Namespace XSD_NAMESPACE_NO_PREFIX = Namespace.getNamespace(
			"", XSD_NAMESPACE_URI);
	*/
	
	public static final Namespace XSI_NAMESPACE = Namespace.getNamespace(
			XSI_NAMESPACE_PREFIX, XMLSCHEMA_INSTANCE_URI);

	public static final Namespace LINK_NAMESPACE = Namespace.getNamespace(
			LINK_NAMESPACE_PREFIX, XBRL_SCHEMA_LOC_LINKBASE_URI);

	public static final Namespace XL_NAMESPACE = Namespace.getNamespace(
			XL_NAMESPACE_PREFIX, XLINK_NS_URI);

	public static final Namespace XML_NAMESPACE = Namespace.getNamespace(
			XML_NAMESPACE_PREFIX, XML_NS_URI);

    public static final Namespace DT_NAMESPACE = Namespace.getNamespace(
    		DT_NAMESPACE_PREFIX, COREP_SCHEMA_LOC_DT_URI);

    public static final Namespace REF_NAMESPACE = Namespace.getNamespace(
            REF_NAMESPACE_PREFIX, REF_URI);

    public static final Namespace HGBREF_NAMESPACE = Namespace.getNamespace(
            HGBREF_NAMESPACE_PREFIX, HGBREF_URI);

    public static final Namespace XHTML_NAMESPACE = Namespace.getNamespace(
            XHTML_NAMESPACE_PREFIX, XHTML_URI);
}
