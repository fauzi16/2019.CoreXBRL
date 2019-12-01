package xbrlcore.taxonomy;

import static xbrlcore.taxonomy.NamespaceJDOMAdapter.toJDOM;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import xbrlcore.constants.GeneralConstants;
import xbrlcore.constants.NamespaceConstants;
import xbrlcore.exception.TaxonomyCreationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.linkbase.CalculationLinkbase;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.PresentationLinkbase;
import xbrlcore.linkbase.ReferenceLinkbase;
import xbrlcore.logging.LogInterface;
import xbrlcore.logging.LogInterface.LogLevel;
import xbrlcore.taxonomy.TupleDefinition.TupleType;
import xbrlcore.util.PathResolver;

/**
 * 
 * This class loads a taxonomy from xml files and invokes abstract methods
 * and passes the loaded data in abstract form to delegate any further work
 * to sub classes.<br/><br/>
 * 
 * @param <ResultType> the data type to return as the concrete taxonomy representation
 * @param <TS> taxonomy scheme type
 * 
 * @author Daniel Hamm
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public abstract class AbstractTaxonomyLoader<ResultType, TS> {

    protected final LogInterface log;
    protected static final Class<?> LOG_CHANNEL = AbstractTaxonomyLoader.class;

    protected static final String PRESENTATION_LB_NAME = PresentationLinkbase.NAME;
    protected static final String LABEL_LB_NAME = LabelLinkbase.NAME;
    protected static final String DEFINITION_LB_NAME = DefinitionLinkbase.NAME;
    protected static final String CALCULATION_LB_NAME = CalculationLinkbase.NAME;
    protected static final String REFERENCE_LB_NAME = ReferenceLinkbase.NAME;
    
    private FileLoader fileLoader = new FileLoader();

    private List<String> loadedTaxonomies;
    private List<String> builtTaxonomies;
    private Map<String, List<String>> includedTaxonomies;

    private String[] additionalTaxonomyPostfixes = null;

    private SAXBuilder saxBuilder;

    private Map<String, Document> taxonomyNameToDocument;
    private Map<String, TS> taxonomyNameToSchema;
    
    private Map<String, Set<URL>> builtLinkbases;
    private Map<String, Set<URL>> builtArcs;
    
    private Map<String, Set<String>> taxonomyNameToRoleTypes;
    private Map<String, String> taxonomyNameToRefBase;

    private Map<URL, Document> instanceNameToDocument;

    private Map<String, List<Element>> taxonomyNameToLinkBaseRefElements;

    protected AbstractTaxonomyLoader(LogInterface log) {
        this.log = log;
    }

    protected static final boolean sleep() {
    	//no need to slow down taxonomy building while FINREP is so huge to parse
    	return true;
/*    	
        try {
            Thread.sleep(10L);

            return true;
        } catch (InterruptedException e) {
            return false;
        }
*/
    }

    /**
     * Initializes some objects.
     * 
     * @param taxonomySchemaName
     * @param userObject some user defined object coming from the loadTaxonomy method
     * 
     * @throws TaxonomyCreationException
     * @throws XBRLException 
     */
    protected abstract void init(String taxonomySchemaName, Object userObject) throws XBRLException, TaxonomyCreationException;

    /**
     * Creates and returns a discoverable taxonomy set (DTS), represented
     * through a DiscoverableTaxonomySet object. The DTS is created based on the
     * given file, which is expected to be an XBRL taxonomy. If this taxonomy
     * imports other taxonomies, they are also created by this method and can be
     * obtained afterwards via the according methods.
     * 
     * @param taxonomyFile File containing the XBRL taxonomy.
     * @param userObject some user defined object passed to the concrete implementation of the {@link #init(String, Object)} method.
     * @return Discoverable taxonomy set, based on the given file, including all
     *         imported taxonomies.
     * @throws IOException
     * @throws TaxonomyCreationException
     * @throws XBRLException 
     */
    public final ResultType loadTaxonomy(File taxonomyFile, Object userObject)
            throws IOException, TaxonomyCreationException, XBRLException {
        return loadTaxonomy(taxonomyFile, userObject, null);
    }

    /**
     * Creates and returns a discoverable taxonomy set (DTS), represented
     * through a DiscoverableTaxonomySet object. The DTS is created based on the
     * given file, which is expected to be an XBRL taxonomy. If this taxonomy
     * imports other taxonomies, they are also created by this method and can be
     * obtained afterwards via the according methods.
     * 
     * @param taxonomyFile File containing the XBRL taxonomy.
     * @param userObject some user defined object passed to the concrete implementation of the {@link #init(String, Object)} method.
     * @param fileLoader the file loader
     * @return Discoverable taxonomy set, based on the given file, including all
     *         imported taxonomies.
     * @throws IOException
     * @throws TaxonomyCreationException
     * @throws XBRLException 
     */
    public final ResultType loadTaxonomy(File taxonomyFile, Object userObject, FileLoader fileLoader)
            throws IOException, TaxonomyCreationException, XBRLException {
        return loadTaxonomy(taxonomyFile.toURI().toURL(), userObject, fileLoader);
    }

    /**
     * Creates and returns a discoverable taxonomy set (DTS), represented
     * through a DiscoverableTaxonomySet object. The DTS is created based on the
     * given file, which is expected to be an XBRL taxonomy. If this taxonomy
     * imports other taxonomies, they are also created by this method and can be
     * obtained afterwards via the according methods.
     * 
     * @param taxonomyFile File containing the XBRL taxonomy.
     * @param userObject some user defined object passed to the concrete implementation of the {@link #init(String, Object)} method.
     * @return Discoverable taxonomy set, based on the given file, including all
     *         imported taxonomies.
     * @throws IOException
     * @throws TaxonomyCreationException
     * @throws XBRLException 
     */
    public final ResultType loadTaxonomy(URL taxonomyFile, Object userObject)
            throws IOException, TaxonomyCreationException, XBRLException {
        return loadTaxonomy(taxonomyFile, userObject, null);
    }

    /**
     * Sets an array of additional taxonomy filename postfixes to load from.
     * 
     * @param postfixes
     */
    public void setAdditionalTaxonomyPostfixes(String[] postfixes) {
        this.additionalTaxonomyPostfixes = postfixes;
    }

    /**
     * Creates and returns a discoverable taxonomy set (DTS), represented
     * through a DiscoverableTaxonomySet object. The DTS is created based on the
     * given file, which is expected to be an XBRL taxonomy. If this taxonomy
     * imports other taxonomies, they are also created by this method and can be
     * obtained afterwards via the according methods.
     * 
     * @param taxonomyFile File containing the XBRL taxonomy.
     * @param userObject some user defined object passed to the concrete implementation of the {@link #init(String, Object)} method.
     * @param fileLoader the file loader
     * @return Discoverable taxonomy set, based on the given file, including all
     *         imported taxonomies.
     * @throws IOException
     * @throws TaxonomyCreationException
     * @throws XBRLException 
     */
    public ResultType loadTaxonomy(URL taxonomyFile, Object userObject, FileLoader fileLoader)
            throws IOException, TaxonomyCreationException, XBRLException {

        long t00 = System.nanoTime();
        String taxonomyFileName = getNameFromURL(taxonomyFile);

        log.log(LogLevel.INFO, LOG_CHANNEL, "Processing discoverable taxonomy set " + taxonomyFileName + "...");

        if (fileLoader != null)
            this.fileLoader = fileLoader;

        loadedTaxonomies = new ArrayList<String>();
        builtTaxonomies = new ArrayList<String>();
        includedTaxonomies = new HashMap<String, List<String>>();
        saxBuilder = new SAXBuilder();
        taxonomyNameToDocument = new HashMap<String, Document>();
        taxonomyNameToSchema = new HashMap<String, TS>();
        builtLinkbases = new HashMap<String, Set<URL>>();
        builtArcs = new HashMap<String, Set<URL>>();
        taxonomyNameToRoleTypes = new HashMap<String, Set<String>>();
        taxonomyNameToRefBase = new HashMap<String, String>();
        instanceNameToDocument = new HashMap<URL, Document>();
        taxonomyNameToLinkBaseRefElements = new HashMap<String, List<Element>>();

        sleep();
        init(taxonomyFileName, userObject);
        sleep();

        _loadTaxonomy(taxonomyFile, false);

        ResultType result = finish(getTaxonomySchema(taxonomyFileName), taxonomyFileName);

        log.log(LogLevel.INFO, LOG_CHANNEL, "loaded taxonomies: ----------------------");
        for(String s : loadedTaxonomies)
        	log.log(LogLevel.INFO, LOG_CHANNEL, s);

        log.log(LogLevel.INFO, LOG_CHANNEL, "loaded taxonomyNameToDocument: ----------------------");
        for(String s : taxonomyNameToDocument.keySet())
        	log.log(LogLevel.INFO, LOG_CHANNEL, s);
        
        log.log(LogLevel.INFO, LOG_CHANNEL, "built linkbases: ----------------------");
        for(String s : builtLinkbases.keySet()){
        	log.log(LogLevel.INFO, LOG_CHANNEL, s);
        	Set<URL> b = builtLinkbases.get(s);
        	for(URL u : b)
        		log.log(LogLevel.INFO, LOG_CHANNEL, "lb = " + u);
        }

        cleanup();
        log.log(LogLevel.INFO, LOG_CHANNEL, "> Total: " + ( ( System.nanoTime() - t00 ) / 1000000000.0f ) + " seconds");

        return result;
    }

    private void _loadTaxonomy(URL taxonomyFile, boolean imported)
            throws IOException, TaxonomyCreationException, XBRLException {

        try {
            long t0 = System.nanoTime();

            String taxonomyFileName = getNameFromURL(taxonomyFile);
            String refBaseURL = getRefURLBase(taxonomyFile);

            log.log(LogLevel.INFO, LOG_CHANNEL, "Processing discoverable taxonomy set " + taxonomyFileName + "...");

            loadedTaxonomies.add(taxonomyFileName);
            taxonomyNameToRefBase.put(taxonomyFileName, refBaseURL);

            log.log(LogLevel.DEBUG, LOG_CHANNEL, "Collecting imported taxonomies...");
            Set<URL> importedTaxonomies = new HashSet<URL>();
            collectIncludedTaxonomies(taxonomyFile, refBaseURL, false, includedTaxonomies, importedTaxonomies, loadedTaxonomies);

            sleep();

            if (additionalTaxonomyPostfixes != null) {
                for (String postFix : additionalTaxonomyPostfixes) {
                    URL url = getRefURLTaxo(refBaseURL, postFix);
                    String name = getNameFromURL(url);
                    if (!loadedTaxonomies.contains(name)) {
                        loadedTaxonomies.add(name);
                        taxonomyNameToRefBase.put(name, refBaseURL);
                        collectIncludedTaxonomies(url, refBaseURL, true, includedTaxonomies, importedTaxonomies, loadedTaxonomies);
                    }
                }
            }

            sleep();

            log.log(LogLevel.DEBUG, LOG_CHANNEL, "> Read XML files to JDOM structures: " + ( ( System.nanoTime() - t0 ) / 1000000000.0f ) + " seconds");
            t0 = System.nanoTime();
            // Collect references... FIXME: why when they are also collected later ?
            Set<String> references = new HashSet<String>();
            int firstTaxoIndex = 0;
            do {
                references.clear();
                buildLinkbase(REFERENCE_LB_NAME, GeneralConstants.XBRL_LINKBASE_ROLE_REFERENCE, GeneralConstants.XBRL_LINKBASE_LINK_REFERENCE, null, loadedTaxonomies, firstTaxoIndex, references);
                for (String reference : references) {
                    if (!reference.equals(taxonomyFileName)) {
                        collectIncludedTaxonomies(this.fileLoader.getFileURL(getParentAddress(taxonomyFile), null, reference), refBaseURL, false, includedTaxonomies, importedTaxonomies, loadedTaxonomies);
                        if (!loadedTaxonomies.contains(reference)) {
                            loadedTaxonomies.add(reference);
                            taxonomyNameToRefBase.put(reference, refBaseURL);
                        }
                    }
                }
                firstTaxoIndex = loadedTaxonomies.size();
            } while (!references.isEmpty());

            sleep();

            builtLinkbases.clear();

            for (URL importedTaxonomy : importedTaxonomies) {

                List<String> tmpLoadedTaxonomies = new ArrayList<String>();
                tmpLoadedTaxonomies.addAll(loadedTaxonomies);
                loadedTaxonomies.clear();

                sleep();
                _loadTaxonomy(importedTaxonomy, true);
                sleep();

                for (String lt : loadedTaxonomies) {
                    if (!tmpLoadedTaxonomies.contains(lt)) {
                        tmpLoadedTaxonomies.add(lt);
                    }
                }
                loadedTaxonomies.clear();
                loadedTaxonomies.addAll(tmpLoadedTaxonomies);
            }

            sleep();

            log.log(LogLevel.DEBUG, LOG_CHANNEL, "> Collect reference link base file names and read XML files to JDOM structures: " + ( ( System.nanoTime() - t0 ) / 1000000000.0f ) + " seconds");
            log.log(LogLevel.DEBUG, LOG_CHANNEL, "Building taxonomy schemas...");
            List<String> taxonomiesToBuild = new ArrayList<String>();
            for (String lt : loadedTaxonomies) {
                if (!builtTaxonomies.contains(lt)) {
                    builtTaxonomies.add(lt);
                    taxonomiesToBuild.add(lt);
                }
            }
            buildTaxonomySchemas(taxonomiesToBuild);

            sleep();

            t0 = System.nanoTime();

            if (!imported) {
            //List<URL> taxoFiles = new ArrayList<URL>();
            //taxoFiles.add(taxonomyFile);
            //taxoFiles.addAll(importedTaxonomies);
            //for (URL taxoFile : taxoFiles) {
                //refBaseURL = getRefURLBase(taxoFile);

                log.log(LogLevel.DEBUG, LOG_CHANNEL, "Interpreting presentation link base and storing to maps/lists...");
                buildLinkbase(PRESENTATION_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_PRESENTATION,
                        GeneralConstants.XBRL_LINKBASE_LINK_PRESENTATION, refBaseURL, loadedTaxonomies, 0, null);
                log.log(LogLevel.DEBUG, LOG_CHANNEL, "Interpreting label link base and storing to maps/lists...");
                sleep();
                buildLinkbase(LABEL_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_LABEL,
                        GeneralConstants.XBRL_LINKBASE_LINK_LABEL, refBaseURL, loadedTaxonomies, 0, null);
                log.log(LogLevel.DEBUG, LOG_CHANNEL, "Interpreting definition link base and storing to maps/lists...");
                sleep();
                buildLinkbase(DEFINITION_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_DEFINITION,
                        GeneralConstants.XBRL_LINKBASE_LINK_DEFINITION, refBaseURL, loadedTaxonomies, 0, null);
                log.log(LogLevel.DEBUG, LOG_CHANNEL, "Interpreting calculation link base and storing to maps/lists...");
                sleep();
                buildLinkbase(CALCULATION_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_CALCULATION,
                        GeneralConstants.XBRL_LINKBASE_LINK_CALCULATION, refBaseURL, loadedTaxonomies, 0, null);
                log.log(LogLevel.DEBUG, LOG_CHANNEL, "Interpreting reference link base and storing to maps/lists...");
                sleep();
                buildLinkbase(REFERENCE_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_REFERENCE,
                        GeneralConstants.XBRL_LINKBASE_LINK_REFERENCE, refBaseURL, loadedTaxonomies, 0, null);
                log.log(LogLevel.DEBUG, LOG_CHANNEL, "> Interpret link bases and store to maps/lists: " + ( ( System.nanoTime() - t0 ) / 1000000000.0f ) + " seconds");
                sleep();
                t0 = System.nanoTime();
            //}
            }

            if (!imported) {
                beforeBuildArcs(null, null, null, null);

                buildArcs(PRESENTATION_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_PRESENTATION,
                        GeneralConstants.XBRL_LINKBASE_LINK_PRESENTATION,
                        GeneralConstants.XBRL_LINKBASE_ARC_PRESENTATION);
                //System.out.println( "time: " + ( ( System.nanoTime() - t0 ) / 1000000000.0f ) );
                sleep();
                //t0 = System.nanoTime();
                buildArcs(LABEL_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_LABEL,
                        GeneralConstants.XBRL_LINKBASE_LINK_LABEL,
                        GeneralConstants.XBRL_LINKBASE_ARC_LABEL);
                //System.out.println( "time: " + ( ( System.nanoTime() - t0 ) / 1000000000.0f ) );
                sleep();
                //t0 = System.nanoTime();
                buildArcs(DEFINITION_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_DEFINITION,
                        GeneralConstants.XBRL_LINKBASE_LINK_DEFINITION,
                        GeneralConstants.XBRL_LINKBASE_ARC_DEFINITION);
                //System.out.println( "time: " + ( ( System.nanoTime() - t0 ) / 1000000000.0f ) );
                sleep();
                //t0 = System.nanoTime();
                buildArcs(CALCULATION_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_CALCULATION,
                        GeneralConstants.XBRL_LINKBASE_LINK_CALCULATION,
                        GeneralConstants.XBRL_LINKBASE_ARC_CALCULATION);
                //System.out.println( "time: " + ( ( System.nanoTime() - t0 ) / 1000000000.0f ) );
                sleep();
                //t0 = System.nanoTime();
                buildArcs(REFERENCE_LB_NAME,
                        GeneralConstants.XBRL_LINKBASE_ROLE_REFERENCE,
                        GeneralConstants.XBRL_LINKBASE_LINK_REFERENCE,
                        GeneralConstants.XBRL_LINKBASE_ARC_REFERENCE);
                log.log(LogLevel.DEBUG, LOG_CHANNEL, "> Interpret Arcs and store to maps/lists: " + ( ( System.nanoTime() - t0 ) / 1000000000.0f ) + " seconds");
                sleep();
                t0 = System.nanoTime();

                afterBuildArcs(null, null, null, null);
            }
        } catch (JDOMException e) {
            throw new XBRLException(e);
        }
    }

    private static String deriveNamespacePrefix(String uri) {
        // TODO: FIXME: Make this international!
        if (uri.toLowerCase().indexOf("gaap") >= 0)
            return "de-gaap-ci";

        if (uri.toLowerCase().indexOf("gcd") >= 0)
            return "de-gcd";

        String separator = PathResolver.separator(uri);
        
        String prefix = //"ns_" +
            uri.substring(uri.lastIndexOf(separator) + 1, uri.length());

        return prefix;
    }

    private void buildTaxonomySchemas(Iterable<String> loadedTaxonomies) throws //JDOMException,
            TaxonomyCreationException {

        for (String currTaxonomySchemaName : loadedTaxonomies) {
            // TODO: trim the schema name to keep only after last '/' ?
            log.log(LogLevel.INFO, LOG_CHANNEL, "Processing taxonomy schema " + currTaxonomySchemaName + "...");
            Document taxonomySource = taxonomyNameToDocument.get(currTaxonomySchemaName);
            if (taxonomySource != null ) {
                String targetNamespaceURI = taxonomySource.getRootElement().getAttributeValue("targetNamespace");
                Namespace targetNamespace = null;
                if (targetNamespaceURI != null) {
                    String targetNamespacePrefix = deriveNamespacePrefix(targetNamespaceURI);
                    targetNamespace = Namespace.getNamespace(targetNamespacePrefix, targetNamespaceURI);
                }

                TS taxonomySchema = buildTaxonomySchema(currTaxonomySchemaName, targetNamespace);
                taxonomyNameToSchema.put(currTaxonomySchemaName, taxonomySchema);

                sleep();

                Element rootElement = taxonomySource.getRootElement();
                @SuppressWarnings( "unchecked" )
                List<Element> conceptElementList = rootElement.getChildren("element", toJDOM(NamespaceConstants.XSD_NAMESPACE));
                createConcepts(taxonomySchema, conceptElementList);

                Set<String> roleTypeIDs = new HashSet<String>();
                getRoleTypes(taxonomySource, taxonomySchema, roleTypeIDs);

                taxonomyNameToRoleTypes.put(currTaxonomySchemaName, roleTypeIDs);

                finishTaxonomySchema(taxonomySchema, currTaxonomySchemaName);

                sleep();
            }
        }
    }

    protected abstract TS buildTaxonomySchema(String taxonomySchemaName, Namespace targetNamespace) throws TaxonomyCreationException;

    private ConceptTypeRestriction parseComplexTypeSimpleContent(Element conceptElement) {
        Element complexType = conceptElement.getChild("complexType", toJDOM(NamespaceConstants.XSD_NAMESPACE));

        if (complexType == null)
            return null;

        Element simpleContent = complexType.getChild("simpleContent", toJDOM(NamespaceConstants.XSD_NAMESPACE));

        if (simpleContent == null)
            return null;

        Element restriction = simpleContent.getChild("restriction", toJDOM(NamespaceConstants.XSD_NAMESPACE));

        if (restriction == null)
            return null;

        String baseAttrib = restriction.getAttributeValue( "base" );

        Element patternElem = restriction.getChild("pattern", toJDOM(NamespaceConstants.XSD_NAMESPACE));

        String pattern = null;
        if (patternElem != null)
            pattern = patternElem.getAttributeValue("value");

        Element attributeGroupElem = restriction.getChild("attributeGroup", toJDOM(NamespaceConstants.XSD_NAMESPACE));

        String attributeGroup = null;
        if (attributeGroupElem != null)
            attributeGroup = attributeGroupElem.getAttributeValue("ref");

        return new ConceptTypeRestriction(baseAttrib, pattern, attributeGroup);
    }

    private void createConcepts(TS taxonomySchema, List<Element> conceptElementList) throws TaxonomyCreationException {
        beforeCreateConcepts(taxonomySchema, conceptElementList);

        int i = 0;
        for (Element currConceptElement : conceptElementList) {
            String id = currConceptElement.getAttributeValue("id");
            if (id != null) {
                String name = currConceptElement.getAttributeValue("name");
                String type = currConceptElement.getAttributeValue("type");
                String substitutionGroup = currConceptElement.getAttributeValue("substitutionGroup");
                String periodType = currConceptElement.getAttributeValue("periodType", toJDOM(NamespaceConstants.XBRLI_NAMESPACE));
                String balance = currConceptElement.getAttributeValue("balance", toJDOM(NamespaceConstants.XBRLI_NAMESPACE));
                boolean abstract_ = Boolean.parseBoolean(currConceptElement.getAttributeValue("abstract"));
                boolean nillable = Boolean.parseBoolean(currConceptElement.getAttributeValue("nillable"));
                String typedDomainRef = currConceptElement.getAttributeValue("typedDomainRef", toJDOM(NamespaceConstants.XBRLDT_NAMESPACE));
                String enumLinkrole = currConceptElement.getAttributeValue("linkrole", toJDOM(NamespaceConstants.ENUM_NAMESPACE));

                if ("xbrli:tuple".equalsIgnoreCase(substitutionGroup)) {
                    Element complexType = currConceptElement.getChild("complexType", toJDOM(NamespaceConstants.XSD_NAMESPACE));
                    if (complexType == null) {
                        throw new TaxonomyCreationException("<complexType> expected under tuple element \"" + id + "\", but missing.");
                    }

                    try{
                        parseTuple(taxonomySchema, id, name, type, substitutionGroup, periodType, balance, abstract_, nillable, typedDomainRef, complexType);
                    }catch(TaxonomyCreationException ex){
                    	log.log(LogLevel.WARNING, LOG_CHANNEL, ex.getMessage());
                    }
                } else {
                    ConceptTypeRestriction typeRestriction = parseComplexTypeSimpleContent(currConceptElement);

                    createConcept(taxonomySchema, id, name, type, typeRestriction, substitutionGroup, periodType, balance, abstract_, nillable, typedDomainRef, enumLinkrole);
                }
            }

            if ((++i % 20) == 0)
                sleep();
        }

        afterCreateConcepts(taxonomySchema, conceptElementList);

        sleep();
    }

    /**
     * This method is invoked right before any concept/tuple is created.
     * 
     * @param taxonomySchema
     * @param conceptElementList
     * 
     * @throws TaxonomyCreationException
     */
    protected void beforeCreateConcepts(TS taxonomySchema, List<Element> conceptElementList) throws TaxonomyCreationException {
    }

    /**
     * This method is invoked right after all concepts/tuples have been created.
     * 
     * @param taxonomySchema
     * @param conceptElementList
     * 
     * @throws TaxonomyCreationException
     */
    protected void afterCreateConcepts(TS taxonomySchema, List<Element> conceptElementList) throws TaxonomyCreationException {
    }

    private static final String getElementNameFromTT(TupleType tt) throws TaxonomyCreationException {
        //return tt.name().toLowerCase().replace( "_", " " );
        switch (tt) {
            case CHOICE:
            case MULTIPLE_CHOICE:
                return "choice";
            case SEQUENCE:
                return "sequence";
        }

        throw new TaxonomyCreationException("Unsupported tuple type " + tt.name());
    }

    private static TupleType parseTupleType(String id, Element tupleRoot) throws TaxonomyCreationException {
    	
        for (TupleType tt : TupleType.values()) {
            Element child = tupleRoot.getChild(getElementNameFromTT(tt), toJDOM(NamespaceConstants.XSD_NAMESPACE));
            if (child != null) {
                if (tt == TupleType.CHOICE && "unbounded".equals(child.getAttributeValue("maxOccurs")))
                    return TupleType.MULTIPLE_CHOICE;

                return tt;
            }
        }

        throw new TaxonomyCreationException("Unknown type for tuple element \"" + id + "\".");
    }

    private void parseTuple(TS taxonomySchema, String id, String name, String type, String substitutionGroup, String periodType, String balance, boolean abstract_, boolean nillable, String typedDomainRef, Element complexType) throws TaxonomyCreationException {
    	TupleType tupleType = null;
    	Element tupleRoot = null;
    	Element complexContent = complexType.getChild("complexContent", toJDOM(NamespaceConstants.XSD_NAMESPACE));
    	if (complexContent == null) {
    		tupleType = parseTupleType(id, complexType);
    		tupleRoot = complexType;
    	} else {
    		tupleRoot = complexContent.getChild("restriction", toJDOM(NamespaceConstants.XSD_NAMESPACE));
        	if (tupleRoot == null)
        		throw new TaxonomyCreationException("tuple" + id + " has no <restriction>!");
        	
        	tupleType = parseTupleType(id, tupleRoot);
    		
    	}
    		
        ArrayList<String> refs = new ArrayList<String>();

        Element listElem = tupleRoot.getChild(getElementNameFromTT(tupleType), toJDOM(NamespaceConstants.XSD_NAMESPACE));
        @SuppressWarnings("unchecked")
		List<Element> elementsList = listElem.getChildren("element", toJDOM(NamespaceConstants.XSD_NAMESPACE));
        for (Element element : elementsList) {
            refs.add(element.getAttributeValue("ref"));
        }

        createTuple(taxonomySchema, id, name, type, substitutionGroup, periodType, balance, abstract_, nillable, typedDomainRef, tupleType, refs);
    }

    protected abstract void createConcept(TS taxonomySchema, String id, String name, String type, ConceptTypeRestriction typeRestriction, String substitutionGroup, String periodType, String balance, boolean abstract_, boolean nillable, String typedDomainRef, String enumLinkrole) throws TaxonomyCreationException;

    protected abstract void createTuple(TS taxonomySchema, String id, String name, String type, String substitutionGroup, String periodType, String balance, boolean abstract_, boolean nillable, String typedDomainRef, TupleType tupleType, List<String> refs) throws TaxonomyCreationException;

    private void getRoleTypes(Document taxonomySource, TS taxonomySchema, Collection<String> roleTypeIDs) throws TaxonomyCreationException {
        // set roleTypes
        Element appInfoElement = getAppInfoElement(taxonomySource);
        if (appInfoElement != null) {
            @SuppressWarnings( "unchecked" )
            List<Element> roleTypeElementList = appInfoElement.getChildren("roleType", toJDOM(NamespaceConstants.LINK_NAMESPACE));
            int i = 0;
            for (Element currRoleTypeElement : roleTypeElementList) {
                String currRoleTypeElementRoleURI = currRoleTypeElement.getAttributeValue("roleURI");
                if ((currRoleTypeElementRoleURI != null) && (currRoleTypeElementRoleURI.length() > 0)) {
                    String id = currRoleTypeElement.getAttributeValue("id");

                    List<String> roleTypeDefinitionList = null;
                    @SuppressWarnings( "unchecked" )
                    List<Element> roleTypeDefinitionElementList = currRoleTypeElement.getChildren("definition", toJDOM(NamespaceConstants.LINK_NAMESPACE));
                    if(roleTypeDefinitionElementList != null) {
                        roleTypeDefinitionList = new ArrayList<String>();
                        for (Element currRoleTypeDefinitionElement : roleTypeDefinitionElementList) {
                            roleTypeDefinitionList.add(currRoleTypeDefinitionElement.getValue());
                        }
                    }

                    List<String> roleTypeUsedOnList = null;
                    @SuppressWarnings( "unchecked" )
                    List<Element> roleTypeUsedOnElementList = currRoleTypeElement.getChildren("usedOn", toJDOM(NamespaceConstants.LINK_NAMESPACE));
                    if (roleTypeUsedOnElementList != null) {
                        roleTypeUsedOnList = new ArrayList<String>();
                        for (int iUsedOn = 0; iUsedOn < roleTypeUsedOnElementList.size(); iUsedOn++) {
                            Element currRoleTypeUsedOnElement = roleTypeUsedOnElementList.get(iUsedOn);
                            roleTypeUsedOnList.add(currRoleTypeUsedOnElement.getValue());
                        }
                    }

                    createRoleType(taxonomySchema, id, currRoleTypeElementRoleURI, roleTypeDefinitionList.toArray(new String[roleTypeDefinitionList.size()]), roleTypeUsedOnList.toArray(new String[roleTypeUsedOnList.size()]));

                    String rtid = id;
                    if (rtid.startsWith("role_"))
                        rtid = rtid.substring(5);
                    roleTypeIDs.add(rtid);

                    if ((++i % 20) == 0)
                        sleep();
                }
            }

            sleep();
        }
    }

    protected abstract void createRoleType(TS taxonomySchema, String id, String roleURI, String[] definitions, String[] usedOns) throws TaxonomyCreationException;

    protected abstract void finishTaxonomySchema(TS taxonomySchema, String taxonomySchemaName) throws TaxonomyCreationException;

    protected static List<Element> getLinkBaseRefElements(Document taxonomySource) {
        Element appInfoElement = getAppInfoElement(taxonomySource);
        if (appInfoElement == null)
            return null;

        @SuppressWarnings( "unchecked" )
        List<Element> linkbaseRefList = appInfoElement.getChildren("linkbaseRef", toJDOM(NamespaceConstants.LINK_NAMESPACE));
        return linkbaseRefList;
    }

    protected static Element getAppInfoElement(Document taxonomySource) {
        if (taxonomySource == null)
            return null;

        Element rootElement = taxonomySource.getRootElement();
        Element annotationElement = rootElement.getChild("annotation", toJDOM(NamespaceConstants.XSD_NAMESPACE));
        if (annotationElement == null)
            return null;

        return annotationElement.getChild("appinfo", toJDOM(NamespaceConstants.XSD_NAMESPACE));
    }

    protected static URL getRelativeResource(String baseURI, String resource) throws MalformedURLException {
    	String separator = PathResolver.separator(baseURI);
    	
    	int p = baseURI.lastIndexOf(separator);
        String uri;
        if (p >= 0)
            uri = FileLoader.getCanonicalFile(baseURI.substring(0, p + 1), resource);
        else
            uri = resource;

        return new URL(uri);
    }

    private boolean buildLinkbase(String name, String role, String extendedLinkRole,
            String refURLBase, List<String> loadedTaxonomies, int firstTaxoIndex, Collection<String> references) throws IOException, JDOMException,
            TaxonomyCreationException {

        for (int i = firstTaxoIndex; i < loadedTaxonomies.size(); i++) {
            String currTaxonomySchemaName = loadedTaxonomies.get(i);
            TS taxonomySchema = getTaxonomySchema(currTaxonomySchemaName);
            Document taxonomySource = taxonomyNameToDocument.get(currTaxonomySchemaName);

            if (!taxonomyNameToLinkBaseRefElements.containsKey(currTaxonomySchemaName)) {
                taxonomyNameToLinkBaseRefElements.put(currTaxonomySchemaName, getLinkBaseRefElements(taxonomySource));
            }
            List<Element> linkbaseRefList = taxonomyNameToLinkBaseRefElements.get(currTaxonomySchemaName);
            if (linkbaseRefList != null) {
                for (Element currLinkbaseRefElement : linkbaseRefList) {
                	String roleAttributeValue = currLinkbaseRefElement.getAttributeValue("role", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                    if (roleAttributeValue != null && roleAttributeValue.equals(role)) {
                        String linkbaseSource = currLinkbaseRefElement
                                .getAttributeValue("href", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                        URL url = getRelativeResource(taxonomySource.getBaseURI(), linkbaseSource);
                        buildLinkbase(taxonomySchema, currTaxonomySchemaName, name, extendedLinkRole, url, linkbaseSource, references);
                    }
                }
            }

            Set<String> roleTypes = taxonomyNameToRoleTypes.get(currTaxonomySchemaName);
            if (roleTypes != null ) {
                for (String roleType : roleTypes) {
                    try {
                        URL url = getRefURL(refURLBase, name, roleType);
                        if (url != null) {
                            buildLinkbase(taxonomySchema, currTaxonomySchemaName, name, extendedLinkRole, url, getNameFromURL(url), references);
                        }
                    } catch (MalformedURLException e) {
                    }
                }
            }

            try {
                URL url = getRefURL(refURLBase, "dimensions-" + name, null);
                if (url != null) {
                    buildLinkbase(taxonomySchema, currTaxonomySchemaName, name, extendedLinkRole, url, getNameFromURL(url), references);
                }
            } catch (MalformedURLException e) {
            }
        }

        return true;
    }

    private boolean buildLinkbase(TS taxonomySchema, String taxonomySchemaName, String name, String extendedLinkRole,
            URL url, String linkbaseSource, Collection<String> references) throws IOException, JDOMException,
            TaxonomyCreationException {

        Set<URL> builtLinkbases_ = builtLinkbases.get(name);
        if (builtLinkbases_ == null) {
            builtLinkbases_ = new HashSet<URL>();
            builtLinkbases_.add(url);
            builtLinkbases.put(name, builtLinkbases_);
        } else if (!builtLinkbases_.contains(url)) {
            builtLinkbases_.add(url);
        } else /*if (references == null)*/ {
            return true;
        }

        Document linkbaseDocument = instanceNameToDocument.get(url);
        if (linkbaseDocument == null) {
            try {
                InputStream is = fileLoader.getFileContent(url);
                if (is == null)
                    return false;

                if (references == null)
                    log.log(LogLevel.INFO, LOG_CHANNEL, "Building linkbase document " + linkbaseSource + "...");
    
                linkbaseDocument = saxBuilder.build(is);
                instanceNameToDocument.put(url, linkbaseDocument);
            } catch (FileNotFoundException e) {
                return false;
            } catch (ConnectException e) {
                return false;
            }
        } else {
            if (references == null)
                log.log(LogLevel.INFO, LOG_CHANNEL, "Building linkbase document " + linkbaseSource + "...");
        }

        beforeCreateExtendedLinkElements(taxonomySchema, taxonomySchemaName, name);

        // collect extended link roles
        @SuppressWarnings( "unchecked" )
        List<Element> extendedLinkRolesList = linkbaseDocument.getRootElement()
                .getChildren(extendedLinkRole, toJDOM(NamespaceConstants.LINK_NAMESPACE));
        int i = 0;
        for (Element newExtendedLinkRoleElement : extendedLinkRolesList) {
            String currExtendedLinkRole = newExtendedLinkRoleElement
                    .getAttributeValue("role", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
            if (references == null)
                addExtendedLinkRole(taxonomySchema, taxonomySchemaName, name, currExtendedLinkRole);
            @SuppressWarnings( "unchecked" )
            List<Element> linkbaseElements = newExtendedLinkRoleElement.getChildren();
            for (Element currLinkbaseElement : linkbaseElements) {
                String typeAttrValue = currLinkbaseElement
                        .getAttributeValue("type", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                if (typeAttrValue != null && (typeAttrValue.equals("locator") || typeAttrValue.equals("resource"))) {
                    // create extended link element
                    String label = currLinkbaseElement.getAttributeValue("label", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                    if (label == null || label.length() == 0) {
                        // TODO: throw exception!
                        log.log(LogLevel.ERROR, LOG_CHANNEL, "Could not find label for extended link element");
                    }
                    if (typeAttrValue.equals("locator")) {
                        // a locator has to be created

                        String conceptName = currLinkbaseElement.getAttributeValue("href", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                        if (conceptName == null) {
                            // TODO: throw exception
                            log.log(LogLevel.ERROR, LOG_CHANNEL, "Could not find concept the label refers to");
                        } else {
                            try {
                                conceptName = java.net.URLDecoder.decode(conceptName, "UTF-8");
                            } catch (Exception e) {
                                // TODO: throw exception
                                e.printStackTrace();
                            }
                        }

                        if (references != null) {
                            if (conceptName != null) {
                                // We only want to load the referenced linkbases here.
                                String reference = conceptName.substring(0, conceptName.indexOf("#"));
                                references.add(reference);
                            }
                        } else {
                            String elementId = null;
                            if (conceptName != null) {
                                // concept name is in form "taxonomy#elementID" - only elementID is needed
                                elementId = conceptName.substring(conceptName.indexOf("#") + 1);
                            }

                            String id = currLinkbaseElement.getAttributeValue("id");
                            String role = currLinkbaseElement.getAttributeValue("role", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                            String title = currLinkbaseElement.getAttributeValue("title", toJDOM(NamespaceConstants.XLINK_NAMESPACE));

                            try{
                                createLocator(taxonomySchema, taxonomySchemaName, name, linkbaseSource, label, currExtendedLinkRole, id, role, title, conceptName, elementId);
                            }catch(TaxonomyCreationException tex){
                            	log.log(LogLevel.WARNING, LOG_CHANNEL, tex.getMessage()); 
                            }
                        }
                    } else if (references == null) {
                        // a resource has to be created
                        String role = currLinkbaseElement.getAttributeValue("role", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                        String title = currLinkbaseElement.getAttributeValue("title", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                        String id = currLinkbaseElement.getAttributeValue("id");
                        String lang = currLinkbaseElement.getAttributeValue("lang", toJDOM(NamespaceConstants.XML_NAMESPACE));
                        String value = currLinkbaseElement.getValue();

                        Object resource = createResource(taxonomySchema, taxonomySchemaName, name, linkbaseSource, label, currExtendedLinkRole, role, title, id, lang, value);
                        consumeResourceDetails(taxonomySchema, taxonomySchemaName, name, linkbaseSource, label, currExtendedLinkRole, role, title, id, currLinkbaseElement.getChildren(), resource);
                    }
                }

                if ((++i % 20) == 0)
                    sleep();
            }
        }

        afterCreateExtendedLinkElements(taxonomySchema, taxonomySchemaName, name);

        return true;
    }

    /**
     * This method is invoked right before any extended link element (locator/resource) is created.
     * 
     * @param taxonomySchema
     * @param taxonomySchemaName
     * @param linkbaseName
     * 
     * @throws TaxonomyCreationException
     */
    protected void beforeCreateExtendedLinkElements(TS taxonomySchema, String taxonomySchemaName, String linkbaseName) throws TaxonomyCreationException {
    }

    /**
     * This method is invoked right after all extended link elements (locators/resources) have been created.
     * 
     * @param taxonomySchema
     * @param taxonomySchemaName
     * @param linkbaseName
     * 
     * @throws TaxonomyCreationException
     */
    protected void afterCreateExtendedLinkElements(TS taxonomySchema, String taxonomySchemaName, String linkbaseName) throws TaxonomyCreationException {
    }

    protected abstract void addExtendedLinkRole(TS taxonomySchema, String taxonomySchemaName, String linkbaseName, String extendedLinkRole) throws TaxonomyCreationException;

    protected abstract void createLocator(TS taxonomySchema, String taxonomySchemaName, String linkbaseName, String linkbaseSource, String label, String extLinkRole, String id, String role, String title, String conceptName, String elementId) throws TaxonomyCreationException;

    protected abstract Object createResource(TS taxonomySchema, String taxonomySchemaName, String linkbaseName, String linkbaseSource, String label, String extLinkRole, String role, String title, String id, String lang, String value) throws TaxonomyCreationException;

    protected abstract void consumeResourceDetails(TS taxonomySchema, String taxonomySchemaName, String linkbaseName, String linkbaseSource, String label, String extLinkRole, String role, String title, String id, List<Object> resourceElements, Object resource) throws TaxonomyCreationException;

    private void buildArcs(String linkbaseName, String role,
            String xbrlExtendedLinkRole, String arcName) throws IOException,
            JDOMException, TaxonomyCreationException {
        log.log(LogLevel.INFO, LOG_CHANNEL, "Building Arcs for " + linkbaseName + " link base...");

        beforeBuildArcs(linkbaseName, role, xbrlExtendedLinkRole, arcName);

        for (String currTaxonomySchemaName : loadedTaxonomies) {
            TS taxonomySchema = getTaxonomySchema(currTaxonomySchemaName);
            Document taxonomySource = taxonomyNameToDocument.get(currTaxonomySchemaName);
            
            if (!taxonomyNameToLinkBaseRefElements.containsKey(currTaxonomySchemaName)) {
                taxonomyNameToLinkBaseRefElements.put(currTaxonomySchemaName, getLinkBaseRefElements(taxonomySource));
            }
            List<Element> linkbaseRefList = taxonomyNameToLinkBaseRefElements.get(currTaxonomySchemaName);
            if (linkbaseRefList == null )
                continue;

            for (Element currLinkbaseRefElement : linkbaseRefList) {
            	String roleAttributeValue = currLinkbaseRefElement.getAttributeValue("role", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                if (roleAttributeValue != null && roleAttributeValue.equals(role)) {
                    String linkbaseSource = currLinkbaseRefElement.getAttributeValue("href", toJDOM(NamespaceConstants.XLINK_NAMESPACE));

                    URL url = getRelativeResource(taxonomySource.getBaseURI(), linkbaseSource);
                    buildArcs(taxonomySchema, currTaxonomySchemaName, linkbaseName, url, linkbaseSource, xbrlExtendedLinkRole, arcName);
                }
            }

            Set<String> roleTypes = taxonomyNameToRoleTypes.get(currTaxonomySchemaName);
            String refURLBase = taxonomyNameToRefBase.get(currTaxonomySchemaName);
            if (roleTypes != null ) {
                for (String roleType : roleTypes) {
                    try {
                        URL url = getRefURL(refURLBase, linkbaseName, roleType);
                        if (url != null) {
                            buildArcs(taxonomySchema, currTaxonomySchemaName, linkbaseName, url, getNameFromURL(url), xbrlExtendedLinkRole, arcName);
                        }
                    } catch (MalformedURLException e) {
                    	e.printStackTrace();//this should not happen?
                    }
                }
            }

            try {
                URL url = getRefURL(refURLBase, "dimensions-" + linkbaseName, null);
                if (url != null) {
                    buildArcs(taxonomySchema, currTaxonomySchemaName, linkbaseName, url, getNameFromURL(url), xbrlExtendedLinkRole, arcName);
                }
            } catch (MalformedURLException e) {
            }
        }

        afterBuildArcs(linkbaseName, role, xbrlExtendedLinkRole, arcName);
    }

    /**
     * This method is invoked before any arc is created.
     * 
     * @param linkbaseName
     * @param role
     * @param xbrlExtendedLinkRole
     * @param arcName
     * 
     * @throws TaxonomyCreationException
     */
    protected void beforeBuildArcs(String linkbaseName, String role, String xbrlExtendedLinkRole, String arcName) throws TaxonomyCreationException {
    }

    /**
     * This method is invoked after all arcs have been created.
     * 
     * @param linkbaseName
     * @param role
     * @param xbrlExtendedLinkRole
     * @param arcName
     * 
     * @throws TaxonomyCreationException
     */
    protected void afterBuildArcs(String linkbaseName, String role, String xbrlExtendedLinkRole, String arcName) throws TaxonomyCreationException {
    }

    private void buildArcs(TS taxonomySchema, String taxonomySchemaName, String linkbaseName, URL url, String linkbaseSource,
            String xbrlExtendedLinkRole, String arcName) throws IOException,
            JDOMException, TaxonomyCreationException {

        Set<URL> builtArcs_ = builtArcs.get(linkbaseName);
        if (builtArcs_ == null) {
            builtArcs_ = new HashSet<URL>();
            builtArcs_.add(url);
            builtArcs.put(linkbaseName, builtArcs_);
        } else if (!builtArcs_.contains(url)) {
            builtArcs_.add(url);
        } else {
            return;
        }

        Document linkbaseDocument = instanceNameToDocument.get(url);
        if (linkbaseDocument == null) {
            try {
                InputStream is = fileLoader.getFileContent(url);
                if (is == null)
                    return;

                //log.log(LogLevel.INFO, LOG_CHANNEL, "Building arcs for linkbase document: " + linkbaseSource + "...");
                
                linkbaseDocument = saxBuilder.build(is);
                instanceNameToDocument.put(url, linkbaseDocument);
            } catch (FileNotFoundException e) {
                return;
            } catch (ConnectException e) {
                return;
            }
        } else {
            //log.log(LogLevel.INFO, LOG_CHANNEL, "Building arcs for linkbase document: " + linkbaseSource + "...");
        }

        @SuppressWarnings( "unchecked" )
        List<Element> extendedLinkRolesList = linkbaseDocument.getRootElement()
                .getChildren(xbrlExtendedLinkRole, toJDOM(NamespaceConstants.LINK_NAMESPACE));
        int i = 0;
        for (Element newExtendedLinkRoleElement : extendedLinkRolesList) {
            String currExtendedLinkRole = newExtendedLinkRoleElement
                    .getAttributeValue("role", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
            @SuppressWarnings( "unchecked" )
            List<Element> arcElementsList = newExtendedLinkRoleElement
                    .getChildren(arcName, toJDOM(NamespaceConstants.LINK_NAMESPACE));
            for (Element currArcElement : arcElementsList) {
                // create a new Arc
                String fromAttribute = currArcElement.getAttributeValue("from", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                String toAttribute = currArcElement.getAttributeValue("to", toJDOM(NamespaceConstants.XLINK_NAMESPACE));

                String arcRole = currArcElement.getAttributeValue("arcrole", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                String targetRole = currArcElement.getAttributeValue("targetRole", toJDOM(NamespaceConstants.XBRLDT_NAMESPACE));
                String title = currArcElement.getAttributeValue("title", toJDOM(NamespaceConstants.XLINK_NAMESPACE));
                String contextElementName = currArcElement.getAttributeValue("contextElement", toJDOM(NamespaceConstants.XBRLDT_NAMESPACE));
                String order_ = currArcElement.getAttributeValue("order");
                Float order = ( order_ != null ) ? Float.valueOf( order_ ) : null;
                String use = currArcElement.getAttributeValue("use");
                String priority_ = currArcElement.getAttributeValue("priority");
                Integer priority = ( priority_ != null ) ? Integer.valueOf( priority_ ) : null;
                boolean usable = Boolean.valueOf( currArcElement.getAttributeValue("usable", toJDOM(NamespaceConstants.XBRLDT_NAMESPACE)) );
                String weight_ = currArcElement.getAttributeValue("weight");
                Float weight = ( weight_ != null ) ? Float.valueOf( weight_ ) : null;

                createArcs(taxonomySchema, taxonomySchemaName, linkbaseName, linkbaseSource, fromAttribute, toAttribute, currExtendedLinkRole,
                           arcRole, targetRole, title, contextElementName, order, use, priority, usable, weight);

                if ((++i % 20) == 0)
                    sleep();
            }
        }
    }

    protected abstract void createArcs(TS taxonomySchema, String taxonomySchemaName, String linkbaseName, String linkbaseSource, String from, String to, String extLinkRole,
                                       String arcRole, String targetRole, String title, String contextElementName, Float order,
                                       String use, Integer priority, boolean usable, Float weight) throws TaxonomyCreationException;

    /**
     * Hooks things to do after taxonomy is loaded
     * 
     * @param taxonomySchema
     * @param taxonomyFileName
     * @return
     * @throws IOException
     * @throws XBRLException
     * @throws TaxonomyCreationException
     */
    protected abstract ResultType finish(TS taxonomySchema, String taxonomyFileName) throws IOException, XBRLException, TaxonomyCreationException;

    protected void cleanup() {
        fileLoader = null;
        loadedTaxonomies = null;
        builtTaxonomies = null;
        includedTaxonomies = null;
        saxBuilder = null;
        taxonomyNameToDocument = null;
        builtArcs = null;
        builtLinkbases = null;
        taxonomyNameToSchema = null;
        taxonomyNameToRoleTypes = null;
        taxonomyNameToRefBase = null;
        instanceNameToDocument = null;
        taxonomyNameToLinkBaseRefElements = null;
    }

    protected TS getTaxonomySchema(String taxonomySchemaName) {
        return taxonomyNameToSchema.get(taxonomySchemaName);
    }

    protected void getIncludedTaxonomyFileNames(String taxonomyName, Collection<String> result) {
        List<String> imports = includedTaxonomies.get(taxonomyName);
        if (imports != null)
            result.addAll(imports);
    }

    /**
     * Get and process the taxonomy schema files recursively
     * 
     * @param taxonomyFile
     * @param refBaseURL
     * @param isExplicitTry
     * @param includedTaxonomies
     * @param importedTaxonomies
     * @param loadedTaxonomies
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    private boolean collectIncludedTaxonomies(URL taxonomyFile, String refBaseURL, boolean isExplicitTry, Map<String, List<String>> includedTaxonomies, Set<URL> importedTaxonomies, List<String> loadedTaxonomies)
            throws IOException, JDOMException {
        String name = getNameFromURL(taxonomyFile);
        String parentAddr = getParentAddress(taxonomyFile);
        
        if (taxonomyNameToDocument.containsKey(name))
            return true;

        Document tmpTaxDocument = null;

        try {
        	InputStream in = fileLoader.getFileContent(taxonomyFile);
            if (in == null) {
                if (!isExplicitTry) {
                    log.log(LogLevel.WARNING, LOG_CHANNEL, "Referenced file not found: " + taxonomyFile);
                }
                taxonomyNameToDocument.put(name, tmpTaxDocument);
                return false;
            }

            tmpTaxDocument = saxBuilder.build(in);
            tmpTaxDocument.setBaseURI(taxonomyFile.toURI().toString());
            dumpFileInfo(taxonomyFile, tmpTaxDocument);
            taxonomyNameToDocument.put(name, tmpTaxDocument);
        } catch (IOException e) {
            if (!isExplicitTry) {
                log.log(LogLevel.WARNING, LOG_CHANNEL, "Referenced file not found: " + taxonomyFile);
            }
            taxonomyNameToDocument.put(name, tmpTaxDocument);
            return false;
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage());
        }

        List<URL> tmpIncludedTaxonomyNames = new ArrayList<URL>();
        getImportedTaxonomyFileNames(parentAddr, tmpTaxDocument, tmpIncludedTaxonomyNames, importedTaxonomies);
        if (tmpIncludedTaxonomyNames.size() > 0) {
            List<String> includedTaxonomies_ = includedTaxonomies.get(name);
            if (includedTaxonomies_ == null) {
                includedTaxonomies_ = new ArrayList<String>();
                includedTaxonomies.put(name, includedTaxonomies_);
            }

            for (URL url : tmpIncludedTaxonomyNames) {
                String name2 = getNameFromURL(url);
                if (!loadedTaxonomies.contains(name2)) {
                    loadedTaxonomies.add(0, name2);
                    taxonomyNameToRefBase.put(name2, refBaseURL);
                }
                if (!includedTaxonomies_.contains(name2)) {
                    includedTaxonomies_.add(name2);
                }
            }

            for (URL url : tmpIncludedTaxonomyNames) {
            	//FIXME ?
                /*if (url.getProtocol().equals("file")) {
                    try {
                        URL url2 = new File(taxPath, new File(url.toURI()).getName()).toURI().toURL();
                        if (!collectImportedTaxonomies(url2))
                            includedTaxonomies.remove(getStringFromURL(url2));
                    } catch (URISyntaxException e) {
                        throw new IOException(e);
                    }
                } else*/ {
                    if (!collectIncludedTaxonomies(url, refBaseURL, false, includedTaxonomies, importedTaxonomies, loadedTaxonomies))
                        {}//includedTaxonomies.remove(getNameFromURL(url));
                }
            }
        }

        return true;
    }

    /**
     * Get all the taxonomy schemas &lt;imported&gt; and &lt;included&gt; from the given taxonomy document.
     * 
     * @param taxonomySource
     * @param includedTaxonomyNames
     * @param importedTaxonomyNames
     * @throws IOException
     */
    private void getImportedTaxonomyFileNames(String parentDir, Document taxonomySource, Collection<URL> includedTaxonomyNames, Collection<URL> importedTaxonomyNames) throws IOException {
        Element rootElement = taxonomySource.getRootElement();
        @SuppressWarnings( "unchecked" )
        List<Element> children = rootElement.getChildren("include", toJDOM(NamespaceConstants.XSD_NAMESPACE));
        for (Element currElement : children) {
            String schemaLocation = currElement.getAttributeValue("schemaLocation");

            /* FIXME: url dadaptation
            String relPath = taxPath;
            try {
                if ((taxonomySource.getBaseURI() != null) && !taxonomySource.getBaseURI().isEmpty()) {
                    URL baseURL = new URL(taxonomySource.getBaseURI());
                    if (baseURL.getProtocol().equals("file"))
                        relPath = new File(baseURL.toURI()).getParentFile().getAbsolutePath();
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
            URL file = fileLoader.getFileURL(relPath, null, schemaLocation);
            if (file != null)*/
                includedTaxonomyNames.add(fileLoader.getFileURL(parentDir, null, schemaLocation));
        }

        children = rootElement.getChildren("import", toJDOM(NamespaceConstants.XSD_NAMESPACE));
        for (Element currElement : children) {
            String namespace = currElement.getAttributeValue("namespace");
            String schemaLocation = currElement.getAttributeValue("schemaLocation");

            /* FIXME: url adaptation
            String relPath = taxPath;
            try {
                if ((taxonomySource.getBaseURI() != null) && !taxonomySource.getBaseURI().isEmpty()) {
                    URL baseURL = new URL(taxonomySource.getBaseURI());
                    if (baseURL.getProtocol().equals("file"))
                        relPath = new File(baseURL.toURI()).getParentFile().getAbsolutePath();
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
            URL file = fileLoader.getFileURL(relPath, namespace, schemaLocation);
            if (file != null) {
                //importedTaxonomyNames.add(file);
                if (!schemaLocation.startsWith("http://") && !schemaLocation.startsWith("https://"))
                    importedTaxonomyNames.add(file);
                else
                    includedTaxonomyNames.add(file);
            }
            */
            if (!schemaLocation.startsWith("http://") && !schemaLocation.startsWith("https://"))
                importedTaxonomyNames.add(fileLoader.getFileURL(parentDir, namespace, schemaLocation));
            else
                includedTaxonomyNames.add(fileLoader.getFileURL(parentDir, namespace, schemaLocation));
            
            
        }
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
        
        String separator = PathResolver.separator(s);
        
        int p = s.lastIndexOf(separator);
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

    protected String getRefURLBase(URL url) {
    	String s = url.toString();
    	
    	String separator = PathResolver.separator(s);
    	
        int p = s.lastIndexOf(separator);
        if (p >= 0)
            s = s.substring(0, p);

        return s;
    }

    protected String getParentAddress(URL url){
    	String parent = "";
    	
    	String separator = PathResolver.separator(url.getPath());
    	
    	if (url.getProtocol().equals("file"))
			try {
				parent = getFileFromURL(url).getParentFile().getAbsolutePath() + File.separator;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else if (url.getProtocol().equals("http") || url.getProtocol().equals("https"))
            parent = url.toString().substring(0, url.toString().lastIndexOf(separator) + 1);
    	
    	return parent;
    }
    
    protected URL getRefURL(String base, String linkbaseName, String roleType) {
        try {
            return new URL(base + "-" + linkbaseName + ( roleType == null ? "" : "-" + roleType ) + ".xml");
        } catch (MalformedURLException e ) {
            return null;
        }
    }

    protected URL getRefURLTaxo(String base, String postfix) {
        try {
            return new URL(base + "-" + postfix + ".xsd");
        } catch (MalformedURLException e ) {
            return null;
        }
    }

    private void dumpFileInfo(URL file, Document document) {
        try {
            log.log(LogLevel.INFO, LOG_CHANNEL, file);
            log.log(LogLevel.INFO, LOG_CHANNEL, "  file size: " + getFileSize(file) + ", element count: " + countElements(document.getRootElement()));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private long getFileSize(URL file) throws IOException, URISyntaxException {
        if (file.getProtocol().equals("file"))
            return new File(file.toURI()).length();

        long size = 0;
        BufferedInputStream in = null;

        try {
            in = fileLoader.getFileContent(file);
            byte[] buffer = new byte[1024];
            int n = 0;
            while ((n = in.read(buffer, 0, Math.min(in.available() + 1, buffer.length))) >= 0) {
                size += n;
            }
        } finally {
            if (in != null)
                in.close();
        }

        return size;
    }

    private int countElements(Element parent) {
        int n = 1;
        for (Object o : parent.getChildren()) {
            n += countElements((Element)o);
        }
        return n;
    }
}
