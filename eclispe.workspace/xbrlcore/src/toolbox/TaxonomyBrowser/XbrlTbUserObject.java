package toolbox.TaxonomyBrowser;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

import xbrlcore.dimensions.Dimension;
import xbrlcore.dimensions.Hypercube;
import xbrlcore.linkbase.CalculationLinkbase;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.PresentationLinkbase;
import xbrlcore.linkbase.PresentationLinkbaseElement;
import xbrlcore.linkbase.ReferenceLinkbase;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;
import xbrlcore.xlink.Resource;

/**
 * UserObject which help to format correctly depending on the object's type
 * 
 * @author Nicolas Georges
 * @author Sébastien Kirche
 *
 */
public class XbrlTbUserObject{
	static DiscoverableTaxonomySet dts = null;
	static LabelLinkbase labelLB = null;
	static PresentationLinkbase presentationLB = null;
	static DefinitionLinkbase definitionLB = null;
	static CalculationLinkbase calculationLB = null;
	static ReferenceLinkbase referenceLB = null;
	
	private Object userObject;
	private static String language;
	
	public XbrlTbUserObject(){
	}
	
	public XbrlTbUserObject(Object aUserObject, String lang){
		userObject = aUserObject;
		language = lang;
	}
	
	public XbrlTbUserObject(Object aUserObject){
		userObject = aUserObject;
	}
	
	public Object getUserObject(){
		return userObject;
	}
	
	public static void setLang(String lang){
		language = lang;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString(){
		if (userObject==null) return "(null object)";
	  	if ( userObject instanceof Concept ){
	  		Concept lvConcept = (Concept)userObject; 
	  		return getConceptLabel( lvConcept );
	  	}else 
	  	if ( userObject instanceof PresentationLinkbaseElement ){
	  		PresentationLinkbaseElement bvPElement = (PresentationLinkbaseElement) userObject;
				return getConceptLabel( bvPElement.getConcept() );
	  	}else 
	  	if ( userObject instanceof Hypercube ){
	  		Hypercube bvHypercube = (Hypercube) userObject;
	  		return getConceptLabel( bvHypercube.getConcept() );
	  	}else 
	  	if( userObject instanceof Dimension ){
	  		Dimension bvDimension = (Dimension) userObject;
	  		return getConceptLabel( bvDimension.getConcept() );
	  	}else 
	  	if( userObject instanceof ExtendedLinkElement ){
	  		ExtendedLinkElement bvExtendedLinkElement = (ExtendedLinkElement) userObject;
	  		//return bvExtendedLinkElement.getTitle(); //+ ":" + ((Locator)bvExtendedLinkElement).getConcept().getName() + "," + ((Locator)bvExtendedLinkElement).isUsable() + "," + ((Locator)bvExtendedLinkElement).isResource();
	  		if (bvExtendedLinkElement.isLocator()){
	  			return getConceptLabel( ((Locator)bvExtendedLinkElement).getConcept() );
	  		}
	  		if (bvExtendedLinkElement.isResource()){
	  			return ((Resource)bvExtendedLinkElement).getLabel();
	  		}
	  		return bvExtendedLinkElement.getTitle();
	  	}
	  	if(userObject instanceof Arc){
	  		Arc lArc = (Arc)userObject; 
	  		return lArc.getSourceElement().getLabel() + "->" + lArc.getTargetElement().getLabel() ;
	  	}
	
	  	return userObject.toString();
	}
	
	/**
	 * return the label of the given concept
	 * @param concept
	 * @return the label (if any) or the concept name
	 */
	public String getConceptLabel(Concept concept){
		String lvLabel = labelLB.getLabel(concept, language, null/*xbrlcore.constants.GeneralConstants.XBRL_ROLE_LABEL*/); 
		return lvLabel != null ? lvLabel : "(concept) " + concept.getName();
	}
	
	/*
	public Map<String, String> getAttributesMapFromArc(String sExtendedLinkRole, String sPrefix){
		Map<String, String> lMap = new HashMap<String, String>();
		Arc lArc = new Arc(sExtendedLinkRole);
		lMap.put(sPrefix+".ContextElement", lArc.getContextElement() ); 
		lMap.put(sPrefix+".ArcRole", lArc.getArcRole() );
		lMap.put(sPrefix+".Order", String.valueOf(lArc.getOrder()) );
		lMap.put(sPrefix+".ExtendedLinkRole", lArc.getExtendedLinkRole() );
		lMap.put(sPrefix+".PriorityAttribute", String.valueOf(lArc.getPriorityAttribute()) );
		lMap.put(sPrefix+".TargetRole", lArc.getTargetRole() );
		lMap.put(sPrefix+".Use", lArc.getUseAttribute() );
		lMap.put(sPrefix+".Weight", String.valueOf(lArc.getWeightAttribute()) );
		if (lArc.getSourceElement()!=null){
			lMap.put(sPrefix+".SourceElement", lArc.getSourceElement().getLabel() );
		}
		if (lArc.getTargetElement()!=null){
			lMap.put(sPrefix+".TargetElement", lArc.getTargetElement().getLabel() );
		}			
		Attributes lAttributes = lArc.getAttributes();
		for(int i=0; i<lAttributes.getLength();i++){				
			lMap.put( sPrefix+":"+i, lAttributes.getValue(i));
		}

		return lMap;
	}
	*/
	
	public Map<String, String> getAttributesMapFromArc(Arc lArc, String sPrefix){
		Map<String, String> lMap = new HashMap<String, String>();
		if (lArc!=null){
			lMap.put(sPrefix+".ContextElement", lArc.getContextElement() ); 
			lMap.put(sPrefix+".ArcRole", lArc.getArcRole() );
			lMap.put(sPrefix+".Order", String.valueOf(lArc.getOrder()) );
			lMap.put(sPrefix+".ExtendedLinkRole", lArc.getExtendedLinkRole() );
			lMap.put(sPrefix+".PriorityAttribute", String.valueOf(lArc.getPriorityAttribute()) );
			lMap.put(sPrefix+".TargetRole", lArc.getTargetRole() );
			lMap.put(sPrefix+".Use", String.valueOf(lArc.getUseAttribute()) );
			lMap.put(sPrefix+".Weight", String.valueOf(lArc.getWeightAttribute()) );
			if (lArc.getSourceElement()!=null){
				lMap.put(sPrefix+".SourceElement", lArc.getSourceElement().getLabel() );
			}
			if (lArc.getTargetElement()!=null){
				lMap.put(sPrefix+".TargetElement", lArc.getTargetElement().getLabel() );
			}			
			Attributes lAttributes = lArc.getAttributes();
			for(int i=0; i<lAttributes.getLength();i++){				
				lMap.put( sPrefix+":"+lAttributes.getLocalName(i), lAttributes.getValue(i));
			}
		}
		return lMap;
	}
	
	public Map<String, String> getAttributesMapFromLocator(Locator loc, String sPrefix){
		Map<String, String> lMap = new HashMap<String, String>();
		if (loc!=null){
			lMap.put(sPrefix+".Id", loc.getID());
			lMap.put(sPrefix+".Label", loc.getLabel());
			lMap.put(sPrefix+".ConceptName", loc.getConcept().getName());
			lMap.put(sPrefix+".LinkbaseSource", loc.getLinkbaseSource());
			lMap.put(sPrefix+".Role", loc.getRole());
			lMap.put(sPrefix+".Title", loc.getTitle());
			lMap.put(sPrefix+".usable", String.valueOf(loc.isUsable()));
			
			if(loc.isResource()){
				lMap.putAll(getAttributesMapFromResource(loc.getResource(), "linkedResource"));
			}
			
			Arc lArc = definitionLB.getArcForSourceLocator(loc);
			lMap.putAll( getAttributesMapFromArc(lArc, "ArcFromSource"));
			lArc = definitionLB.getArcForTargetLocator(loc);
			lMap.putAll( getAttributesMapFromArc(lArc, "ArcToTarget"));
				 
		}
		return lMap;
	}

	public Map<String, String> getAttributesMapFromResource(Resource res, String sPrefix){
		Map<String, String> lMap = new HashMap<String, String>();
		if (res!=null){
			lMap.put(sPrefix+".Id", res.getID());
			//lMap.put(sPrefix+".ConceptName", res.getConcept().getName());
			lMap.put(sPrefix+".Label", res.getLabel());
			lMap.put(sPrefix+".lang", res.getLang());
			lMap.put(sPrefix+".LinkbaseSource", res.getLinkbaseSource());
			lMap.put(sPrefix+".Role", res.getRole());
			lMap.put(sPrefix+".Title", res.getTitle());
			lMap.put(sPrefix+".value", res.getValue());
		}
		return lMap;
	}

	public Map<String, String> getAttributesMapFromConcept(Concept lConcept, String sPrefix){
		Map<String, String> lMap = new HashMap<String, String>();
		if (lConcept!=null){
			lMap.put(sPrefix+".NodeType", "Concept" );
			lMap.put(sPrefix+".id", lConcept.getID() );
			lMap.put(sPrefix+".name", lConcept.getName() );
			//lMap.put(sPrefix+".namespace", lConcept.getNamespace().toString() );
			lMap.put(sPrefix+".PeriodType", lConcept.getPeriodType() );
			lMap.put(sPrefix+".SubstitutionGroup", lConcept.getSubstitutionGroup() );
			lMap.put(sPrefix+".TaxonomySchemaName", lConcept.getTaxonomySchema().getName() );
			lMap.put(sPrefix+".Type", lConcept.getType().name() );
			lMap.put(sPrefix+".TypedDomainRef", lConcept.getTypedDomainRef() != null ? lConcept.getTypedDomainRef() : "null" );
			lMap.put(sPrefix+".isAbstract", String.valueOf(lConcept.isAbstract()) );
			lMap.put(sPrefix+".isNillable", String.valueOf(lConcept.isNillable()) );
			lMap.put(sPrefix+".isExplicitDimension", String.valueOf(lConcept.isExplicitDimension()) );
			lMap.put(sPrefix+".isNumericItem", String.valueOf(lConcept.isNumericItem()) );
			lMap.put(sPrefix+".isTypedDimension", String.valueOf(lConcept.isTypedDimension()) );	
		}
		return lMap;
	}	
	
	public Map<String, String> getAttributesMap(){
		Map<String, String> lAttributes = new HashMap<String, String>();
		
		if(userObject instanceof Concept){			
			//Enum concepts attributes
			Concept lConcept = (Concept)userObject;
			lAttributes.putAll( getAttributesMapFromConcept( lConcept, "Concept") );	
		}
		
		if(userObject instanceof Hypercube){
			Hypercube lHypercube = (Hypercube)userObject;
			lAttributes.put("NodeType", "Hypercube" );
			
			lAttributes.putAll( getAttributesMapFromConcept( lHypercube.getConcept() , "Concept") );			
			//lAttributes.putAll( getAttributesMapFromArc( lHypercube.getExtendedLinkRole(), "Arc" ) );
		}
		
		if(userObject instanceof ExtendedLinkElement){
			ExtendedLinkElement lExLinkElement = (ExtendedLinkElement)userObject;
			lAttributes.put("NodeType", "ExtendedLinkElement" );
			lAttributes.put("id", lExLinkElement.getID() );
			lAttributes.put("ExtendedLinkRole", lExLinkElement.getExtendedLinkRole() );			
			lAttributes.put("Label", lExLinkElement.getLabel() );
			lAttributes.put("LinkbaseSource", lExLinkElement.getLinkbaseSource() );
			lAttributes.put("Role", lExLinkElement.getRole() );
			lAttributes.put("Title", lExLinkElement.getTitle() );
			lAttributes.put("isLocator", String.valueOf(lExLinkElement.isLocator()) );
			lAttributes.put("isResource", String.valueOf(lExLinkElement.isResource()) );
			
			//lAttributes.putAll( getAttributesMapFromArc( lExLinkElement.getExtendedLinkRole(), "Arc" ) );
			if(lExLinkElement.isLocator()){
				lAttributes.putAll( getAttributesMapFromLocator((Locator)lExLinkElement, "Locator") );
			}
			else if(lExLinkElement.isResource()){ //can be true also for locator
				lAttributes.putAll( getAttributesMapFromResource((Resource)lExLinkElement, "Resource") );
			}
		}
		
		if(userObject instanceof Dimension){			
			//Enum concepts attributes
			Dimension dim = (Dimension)userObject;
			lAttributes.put("NodeType", "Dimension" );			
			if(dim.isTyped()){
				lAttributes.put("TypedElement", dim.getConcept().getTypedDomainRef() /*getTypedElement().getName()*/ );
			}			
			lAttributes.putAll( getAttributesMapFromConcept( dim.getConcept(), "Concept") );			
		}
		
		if ( userObject instanceof PresentationLinkbaseElement ){
			PresentationLinkbaseElement bvPElement = (PresentationLinkbaseElement) userObject;
			lAttributes.put("NodeType", "PresentationLinkbaseElement" );			
			lAttributes.put("ExtendedLinkRole", bvPElement.getExtendedLinkRole() );
			lAttributes.put("Level", String.valueOf(bvPElement.getLevel()) );
			lAttributes.put("NumDirectSuccessor", String.valueOf(bvPElement.getNumDirectSuccessor()) );
			lAttributes.put("PositionDeepestLevel", String.valueOf(bvPElement.getPositionDeepestLevel()) );
			lAttributes.putAll( getAttributesMapFromConcept( bvPElement.getConcept(), "Concept") );
			if (bvPElement.getParentElement()!=null){
				//lAttributes.put("ParentConcept", bvPElement.getParentElement().getId() );
				lAttributes.putAll( getAttributesMapFromConcept( bvPElement.getParentElement(), "ParentConcept") );
			}
			if (bvPElement.getLocator()!=null){
				Locator lLocator = bvPElement.getLocator();				
				lAttributes.putAll( getAttributesMapFromLocator(lLocator, "Locator" ) );
			}
			
			//lAttributes.putAll( getAttributesMapFromArc( bvPElement.getExtendedLinkRole(), "Arc" ) );
		}
		
		if(userObject instanceof Arc){
			Arc bvArc = (Arc)userObject;
			lAttributes.putAll( getAttributesMapFromArc(bvArc, "Arc"));
		}
		
		return lAttributes;
	}
	
	public void export(PrintStream out){
		//It export the current node...
		out.println( userObject.getClass().getSimpleName() + "\t" + toString());
		if(userObject instanceof PresentationLinkbaseElement){
			PresentationLinkbaseElement bvPElement = (PresentationLinkbaseElement)userObject;
			Iterator<Concept> successors = browsePresentationLinkBaseElement(bvPElement, bvPElement.getLevel() - 1).iterator();
			while(successors.hasNext()){
				Concept successor = successors.next();
				out.println( successor.getID() + "\t" + successor.getName() + "\t" + getConceptLabel(successor) );
			}
		}
		
		if(userObject instanceof Dimension){
			Iterator<ExtendedLinkElement> idomainmembers = browseDimension( (Dimension)userObject ).iterator();
			while( idomainmembers.hasNext() ){
				ExtendedLinkElement extLinkElement = idomainmembers.next();
				//out.println( extLinkElement.getId() + "\t" + extLinkElement.getTitle() + "\t" + extLinkElement.getLabel() );
				Concept concept = dts.getConceptByName( extLinkElement.getTitle() );				
				out.println( concept.getID() + "\t" + concept.getName() + "\t" + getConceptLabel(concept) );
			}
		}
	}

	public List<ExtendedLinkElement> browseDimension(Dimension dimension){
		List<ExtendedLinkElement> list = new LinkedList<ExtendedLinkElement>();
		Set<ExtendedLinkElement> lvDomainMemberSet = dimension.getDomainMemberSet();
		for(ExtendedLinkElement bvDomainMember : lvDomainMemberSet){
			list.add( bvDomainMember );
		}						

		return list;
	}
	
	/**
	 * This method return a linear list of all the successors
	 * 
	 * @param pElement
	 * @param lvParentLevel
	 * 
	 * @return List<Concept>
	 */
	public List<Concept> browsePresentationLinkBaseElement(PresentationLinkbaseElement pElement, long lvParentLevel){
		List<Concept> list = new LinkedList<Concept>();		
		if( pElement.getLevel() == lvParentLevel+1 ){		
			list.add( pElement.getConcept() );
			Iterator<Concept> successors = pElement.getSuccessorElements().iterator();
			while(successors.hasNext()){
				Concept successor = successors.next();			
				Iterator<PresentationLinkbaseElement> isubElements = presentationLB.getPresentationList(successor, pElement.getExtendedLinkRole() ).iterator();
				if (!isubElements.hasNext()){
					list.add( successor ); 
				}
				while(isubElements.hasNext()){
					PresentationLinkbaseElement subElement = isubElements.next();
					List<Concept> subList = browsePresentationLinkBaseElement( subElement, pElement.getLevel() );
					list.addAll( subList );
				}
			}		
		}
		return list;
	}
	
	/**
	 * getter for the DTS
	 * @return the DTS
	 */
	public static DiscoverableTaxonomySet getDts() {
		return dts;
	}

	/**
	 * setter for the DTS
	 * @param dts
	 */
	public static void setDts(DiscoverableTaxonomySet dts) {
		XbrlTbUserObject.dts = dts;
	}

	static public void setLabelLinkbase( LabelLinkbase labLB ){
		labelLB = labLB;
	}

	/**
	 * getter for the PresentationLinkbase
	 * @return the definition linkbase
	 */
	public static PresentationLinkbase getPresentationLB() {
		return presentationLB;
	}

	/**
	 * setter for the PresentationLinkbase
	 * @param presLB the linkbase to set for presentation
	 */
	public static void setPresentationLB(PresentationLinkbase presLB) {
		XbrlTbUserObject.presentationLB = presLB;
	}

	/**
	 * getter for the CalculationLinkbase
	 * @return the definition linkbase
	 */
	public static CalculationLinkbase getCalculationLB() {
		return calculationLB;
	}

	/**
	 * setter for the CalculationLinkbase
	 * @param calcLB the linkbase to set for calculation
	 */
	public static void setCalculationLB(CalculationLinkbase calcLB) {
		XbrlTbUserObject.calculationLB = calcLB;
	}

	/**
	 * getter for the DefinitionLinkbase
	 * @return the definition linkbase
	 */
	public static DefinitionLinkbase getDefinitionLB() {
		return definitionLB;
	}

	/**
	 * setter for the DefinitionLinkbase
	 * @param defLB the linkbase to set for definition
	 */
	public static void setDefinitionLB(DefinitionLinkbase defLB) {
		XbrlTbUserObject.definitionLB = defLB;
	}
	
	/**
	 * getter for the ReferenceLinkbase
	 * @return the reference linkbase
	 */
	public static ReferenceLinkbase getReferenceLB(){
		return referenceLB;
	}
	
	/**
	 * setter for the ReferenceLinkbase
	 * @param refLB the linkbase to set for reference
	 */
	public static void setReferenceLB(ReferenceLinkbase refLB){
		XbrlTbUserObject.referenceLB = refLB;
	}
}