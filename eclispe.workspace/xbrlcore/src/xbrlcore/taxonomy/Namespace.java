package xbrlcore.taxonomy;

import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Abstraction of a namespace in replacement of the JDOM namespace
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 *
 */
public class Namespace implements javax.xml.stream.events.Namespace, XMLEvent, Serializable {

    private static final long serialVersionUID = -2004998767354209684L;

    private static final String XMLNS_ATTRIBUTE = "xmlns";

    public Namespace(String namespaceURI) {
        this("", namespaceURI);
    }

    public Namespace(String prefix, String namespaceURI) {
        this(XMLNS_ATTRIBUTE, prefix, namespaceURI);
    }

    private Namespace(String prefix, String localName, String value) {
        this(prefix, null,localName, value, null);
    }

    private Namespace(String prefix, String namespaceURI, String localName,
                      String value, String attributeType) {
        this.defaultDeclaration = (prefix == null) || prefix.isEmpty();
        _QName = new QName(namespaceURI, localName,prefix);
        _value = value;
        _attributeType = (attributeType == null) ? "CDATA": attributeType;
    }

    // Event type this event corresponds to
    private final int _eventType = NAMESPACE; //ATTRIBUTE;
    private transient final Location _location = null;

    @Override
    public int getEventType() {
        return _eventType;
    }

    @Override
    public boolean isStartElement() {
        return _eventType == START_ELEMENT;
    }

    @Override
    public boolean isEndElement() {
        return _eventType == END_ELEMENT;
    }

    @Override
    public boolean isEntityReference() {
        return _eventType == ENTITY_REFERENCE;
    }

    @Override
    public boolean isProcessingInstruction() {
        return _eventType == PROCESSING_INSTRUCTION;
    }

    @Override
    public boolean isStartDocument() {
        return _eventType == START_DOCUMENT;
    }

    @Override
    public boolean isEndDocument() {
        return _eventType == END_DOCUMENT;
    }

    @Override
    public Location getLocation(){
        return _location;
    }

    /*
    public void setLocation(Location loc){
        _location = loc;
    }
    */

    public String getSystemId() {
        if(_location == null)
            return "";

        return _location.getSystemId();
    }

    @Override
    public Characters asCharacters() {
        if (!isCharacters())
            //throw new ClassCastException(CommonResourceBundle.getInstance().getString("message.charactersCast", new Object[]{getEventTypeString()}));
            throw new Error("These are no characters.");

        return (Characters)this;
    }

    @Override
    public EndElement asEndElement() {
        if (!isEndElement())
            //throw new ClassCastException(CommonResourceBundle.getInstance().getString("message.endElementCase", new Object[]{getEventTypeString()}));
            throw new Error("This is not an end element.");

        return (EndElement)this;
    }

    @Override
    public StartElement asStartElement() {
        if (!isStartElement())
            //throw new ClassCastException(CommonResourceBundle.getInstance().getString("message.startElementCase", new Object[]{getEventTypeString()}));
            throw new Error("This is not a start element.");

        return (StartElement)this;
    }

    @Override
    public QName getSchemaType() {
        return null;
    }

    @Override
    public boolean isAttribute() {
        return _eventType == ATTRIBUTE;
    }

    @Override
    public boolean isCharacters() {
        return _eventType == CHARACTERS;
    }

    @Override
    public boolean isNamespace() {
        return true; //_eventType == NAMESPACE;
    }

    @Override
    public void writeAsEncodedUnicode(Writer writer) throws javax.xml.stream.XMLStreamException {
    }

    /*
    private String getEventTypeString() {
        switch (_eventType){
            case START_ELEMENT:
                return "StartElementEvent";
            case END_ELEMENT:
                return "EndElementEvent";
            case PROCESSING_INSTRUCTION:
                return "ProcessingInstructionEvent";
            case CHARACTERS:
                return "CharacterEvent";
            case COMMENT:
                return "CommentEvent";
            case START_DOCUMENT:
                return "StartDocumentEvent";
            case END_DOCUMENT:
                return "EndDocumentEvent";
            case ENTITY_REFERENCE:
                return "EntityReferenceEvent";
            case ATTRIBUTE:
                return "AttributeBase";
            case DTD:
                return "DTDEvent";
            case CDATA:
                return "CDATA";
        }

        return "UNKNOWN_EVENT_TYPE";
    }
    */

    //an Attribute consists of a qualified name and value
    private final QName _QName;
    private final String _value;

    private String _attributeType = null;
    //A flag indicating whether this attribute was actually specified in the start-tag
    //of its element or was defaulted from the schema.
    private boolean _specified = false;

    /*
    public void setName(QName name){
        _QName = name ;
    }
    */

    @Override
    public QName getName() {
        return _QName;
    }

    /*
    public void setValue(String value){
        _value = value;
    }
    */

    public String getLocalName() {
        return _QName.getLocalPart();
    }

    @Override
    public String getValue() {
        return _value;
    }

    /*
    public void setAttributeType(String attributeType){
        _attributeType = attributeType ;
    }
    */

    @Override
    public String getDTDType() {
        return _attributeType;
    }

    @Override
    public boolean isSpecified() {
        return _specified ;
    }

    /*
    public void setSpecified(boolean isSpecified){
        _specified = isSpecified ;
    }
    */

    @Override
    public String toString() {
        String prefix = _QName.getPrefix();
        if (!prefix.isEmpty())
            return prefix + ":" + _QName.getLocalPart() + "='" + _value + "'";

        return _QName.getLocalPart() + "='" + _value + "'";
    }    

    private final boolean defaultDeclaration;

    @Override
    public String getPrefix() {
        if (isDefaultNamespaceDeclaration())
            return "";
        
        return getLocalName();
    }

    @Override
    public String getNamespaceURI() {
        return getValue();
    }

    public String getURI() {
        return getValue();
    }

    @Override
    public boolean isDefaultNamespaceDeclaration() {
        return defaultDeclaration;
    }

    Object jdomNS = null;

    /*
    public Object toJDOM() {
        if (jdomNS == null) {
            //jdomNS =org.jdom.Namespace.getNamespace( rs.getString( "prefix" ), rs.getString( "url" ) )
            try {
                Class<?> jdns = Class.forName("org.jdom.Namespace");
                java.lang.reflect.Method m = jdns.getDeclaredMethod("getNamespace", String.class, String.class);
    
                jdomNS = m.invoke(null, getPrefix(), getNamespaceURI());
            } catch (ClassNotFoundException e) {
                throw new Error(e);
            } catch (NoSuchMethodException e) {
                throw new Error(e);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw new Error(e);
            } catch (IllegalAccessException e) {
                throw new Error(e);
            }
        }

        return jdomNS;
    }
    */

    private static final Map<String, Namespace> map = new HashMap<String, Namespace>();

    public static final Namespace getNamespace(String prefix, String namespaceURI) {
        Namespace ns = map.get(prefix + "---" + namespaceURI);

        if (ns == null) {
            ns = new Namespace(prefix, namespaceURI);
            map.put(prefix + "---" + namespaceURI, ns);
        }

        return ns;
    }

    public static final Namespace getNamespace(String namespaceURI) {
        return getNamespace("", namespaceURI);
    }
}
