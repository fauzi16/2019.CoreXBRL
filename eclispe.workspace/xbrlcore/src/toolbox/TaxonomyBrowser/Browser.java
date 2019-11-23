package toolbox.TaxonomyBrowser;
 
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.PropertyConfigurator;

import xbrlcore.constants.GeneralConstants;
import xbrlcore.dimensions.Dimension;
import xbrlcore.dimensions.Hypercube;
import xbrlcore.exception.TaxonomyCreationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.instance.InstanceFactory;
import xbrlcore.linkbase.CalculationLinkbase;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.Linkbase;
import xbrlcore.linkbase.PresentationLinkbase;
import xbrlcore.linkbase.PresentationLinkbaseElement;
import xbrlcore.linkbase.ReferenceLinkbase;
import xbrlcore.logging.LogInterface;
import xbrlcore.logging.LogInterface.LogLevel;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DTSFactory;
import xbrlcore.taxonomy.DefaultTaxonomyLoader;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.taxonomy.FileLoader;
import xbrlcore.taxonomy.TaxonomySchema;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;

/** 
* Taxonomy browser for XBRLCore
* 
* @author Nicolas Georges
* @author Sébastien Kirche
*/ 
public class Browser extends JFrame implements TreeSelectionListener {
	
	public static final String LINKBASES = "Linkbases";
	public static final String PRESENTATION_LINK = "Presentation Linkbase";
	public static final String DEFINITION_LINK = "Definition Linkbase";
	public static final String CALCULATION_LINK = "Calculation Linkbase";
	public static final String LABEL_LINK = "Label Linkbase";
	public static final String REFERENCE_LINK = "Reference Linkbase";
	public static final String DIMENSIONAL_INFORMATIONS = "Dimensional Informations";
	public static final String NO_ROLE_UNUSED_HC = "no role - unused hypercubes";

	private static final long serialVersionUID = 29211963443635256L;

	private JTree treeNavigator;
	private DefaultTreeModel model;
	private JTable tableProps;
	private JTextField editCurrentSel;
	private JTextField editTaxoPath;
	private JFileChooser fileChooser;
	private JLabel labelTaxo;
	private String currentLang = "";
	
	DTSFactory dtsFactory = null; 
	DefaultTaxonomyLoader dtsLoader;
	
	InstanceFactory instFactory = null;
	DiscoverableTaxonomySet dts = null;
	LabelLinkbase labelLB = null;
	PresentationLinkbase presentationLB = null;
	DefinitionLinkbase definitionLB = null;
	ReferenceLinkbase referenceLB = null;
	File taxoEntryPoint;
	private JComboBox comboLanguages;
	
	private static final LogLevel LOG_LEVEL = LogLevel.INFO;
	//private static final LogInterface log = new xbrlcore.logging.Log4jLogInterface(XbrlTB.class, LOG_LEVEL);
    private static final LogInterface log = new xbrlcore.logging.ConsoleLogInterface(LOG_LEVEL);

    /*
    private static final void log(Object message) {
        log.log(LogLevel.INFO, XbrlTB.class, message);
        log.log(LogLevel.INFO, XbrlTB.class, "\n");
    }
    */

    private static final void debug(Object message) {
        log.log(LogLevel.DEBUG, Browser.class, message);
        log.log(LogLevel.DEBUG, Browser.class, "\n");
    }

	public static void main(String[] args){ 
		PropertyConfigurator.configure("log4j.properties");		
	    WindowUtilities.setNativeLookAndFeel();
		Browser app = new Browser();						
		/**
		* @TODO Check if extension is XSD, if XML then open instance file and use its taxonomy
		*/
		if(args.length > 0){ 	//avoids exception if there is no args
			String taxoFileName = args[0];
			taxoFileName = taxoFileName.replace("\\", "/");		
			debug( "Extracting data from file : " + taxoFileName ); 	
			
			app.parseTaxoThread(new File(taxoFileName));
			debug("");
		}
	} 
	
	/** 
	 * Creator of the window 
	 */ 
	public Browser(){ 
		super("XBRL Taxonomy Browser");
		dtsFactory = DTSFactory.get(); 		
		dtsLoader = new DefaultTaxonomyLoader(log);
		instFactory = InstanceFactory.get();
		createFrame();		
	}
	
	public void chooseTaxonomy(){
	    if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
	        return;
	      }
	    File file = fileChooser.getSelectedFile();	    
	    parseTaxoThread(file);	    
	}
	
	/**
	 * Start a thread to perform the loading of the taxonomy
	 * @param taxo
	 */
	public void parseTaxoThread(File taxo){
		taxoEntryPoint = taxo;
		new Thread(new Runnable(){
            @Override
			public void run(){
				parseTaxonomy();
			}
		}).start();
	}
	
    @Override
	public void valueChanged(TreeSelectionEvent event){
		Object currentNode = treeNavigator.getLastSelectedPathComponent();
		if (currentNode == null) return ;
		String lvValue = currentNode.toString();
  	  	
		editCurrentSel.setText("Current Selection: " + lvValue);
    
		DefaultMutableTreeNode dtn = (DefaultMutableTreeNode)currentNode;
		if (dtn.getUserObject() instanceof XbrlTbUserObject){
			XbrlPropsTableModel model = (XbrlPropsTableModel)this.tableProps.getModel();
			model.setTable((XbrlTbUserObject)dtn.getUserObject());
		}
	}	
	
	/**
	 * Construction of the GUI
	 */
	public void createFrame(){		
		fileChooser = new JFileChooser(System.getProperty("user.dir"));	    
		TaxonomyFilter taxoFilter = new TaxonomyFilter();
	    fileChooser.addChoosableFileFilter(taxoFilter);
	    fileChooser.setFileFilter(taxoFilter);
		
		addWindowListener(new ExitListener());
		Container content = getContentPane();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Empty"); // with that we will have an empty tree without default nodes colors/sports/food
		model = new DefaultTreeModel(root);
		treeNavigator = new JTree(model);
		//treeNavigator.setRootVisible(false);
		treeNavigator.addTreeSelectionListener(this);
		treeNavigator.setCellRenderer(new BrowserTreeCellRenderer());
		ToolTipManager.sharedInstance().registerComponent(treeNavigator);
    
		XbrlPropsTableModel tableModel =	new XbrlPropsTableModel(){

			private static final long serialVersionUID = -3084762345188824252L;

				//Describes the processing for entering values in the table model.
                @Override
				public void setValueAt(Object value, int row, int col){
					/*if(listValue.get(row) instanceof Item) {
					//Obtains the current XBRL instance value.
					Item item = (Item)listValue.get(row);
					//Updates the input value to the XBRL instance.
					//If the value has been updated, true is returned; otherwise, false is returned.
					if (updateNewValue(item, value)){
					//Reports any changes in the table model.
					fireTableCellUpdated(row, col);
					}
					}
					*/
				}
		};

		tableModel.ParentClass = this;
		tableProps = new JTable(tableModel);
		tableProps.getTableHeader().setReorderingAllowed(false);
		tableProps.setPreferredScrollableViewportSize( new java.awt.Dimension(800,70) );
    
		editCurrentSel = new JTextField("Current Selection: NONE");    
   
		JSplitPane jpTreeProps = new JSplitPane( JSplitPane.VERTICAL_SPLIT, 
												   new JScrollPane(treeNavigator), 
												   new JScrollPane(tableProps) );

		JButton btnLoadTaxo =  new JButton("Load Taxonomy");
		btnLoadTaxo.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e){
					chooseTaxonomy();
				}
			}
		);
    
		JButton btnExportSelectedNode = new JButton("Export");
		btnExportSelectedNode.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e){
					exportSelectedNode();
				}
			}
		);
    
		JPanel headerPanel = new JPanel(); 
		headerPanel.setLayout( new BoxLayout( headerPanel, BoxLayout.LINE_AXIS ) );
		labelTaxo = new JLabel("Taxonomy: ");
		headerPanel.add( labelTaxo );
		editTaxoPath = new JTextField("");
		editTaxoPath.setEditable(false);
		headerPanel.add( editTaxoPath );
		headerPanel.add( btnLoadTaxo );    
		headerPanel.add( btnExportSelectedNode );
		content.add( headerPanel , BorderLayout.NORTH);
		content.add(jpTreeProps, BorderLayout.CENTER);
		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.LINE_AXIS));
		comboLanguages = new JComboBox();
		comboLanguages.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent arg0) {
				currentLang = (String) comboLanguages.getSelectedItem();
				debug(currentLang);
				fillTree();
			}
		});
		footerPanel.add(editCurrentSel);
		footerPanel.add(comboLanguages);
		content.add(footerPanel, BorderLayout.SOUTH);    
		
    	//Allow to drop files...
    	new FileDrop( this, new FileDrop.Listener(){
    		public void filesDropped( File[] files ){
    			for (File file : files) {
    				parseTaxoThread(file);
    				break;	//Just treat the first file for now...
    			}
    		}
    	});

    	//setMinimumSize(new java.awt.Dimension().setSize(300, 200));
		setSize(800, 600);
		setVisible(true);		
	}
	
	/**
	 * Export the current selected tree node into a text file
	 */
	private void exportSelectedNode() {		
	  	Object currentNode = treeNavigator.getLastSelectedPathComponent();
	  	if (currentNode == null) return ;
	  	String lvValue = currentNode.toString();
	    DefaultMutableTreeNode dtn = (DefaultMutableTreeNode)currentNode;
    	currentNode = dtn.getUserObject();
    
	    editCurrentSel.setText("Exporting " + lvValue + "...");
	    
	    fileChooser.setSelectedFile( new File(lvValue+".txt") );
	    int returnValue = fileChooser.showSaveDialog( this );
	    if (returnValue == JFileChooser.CANCEL_OPTION ){
	    	editCurrentSel.setText( "Export of " + lvValue + " canceled.");
	    	return;
	    }
	    
	    //Export content...
	    try {
			FileOutputStream output =  new FileOutputStream( fileChooser.getSelectedFile() ) ;
			PrintStream outputstream = new PrintStream( output );
		    if (dtn.getUserObject() instanceof XbrlTbUserObject){
		    	//XBRLTableModel model = (XBRLTableModel)this.table.getModel();
		    	((XbrlTbUserObject)dtn.getUserObject()).export(outputstream);
		    }
		    else{
		    	outputstream.println( currentNode.toString() );
		    }
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    editCurrentSel.setText( lvValue + " exported. ");
	}
		
	public DefaultMutableTreeNode getDimInfo(){
		DefaultMutableTreeNode lvDimInfo = new DefaultMutableTreeNode(new XbrlTbUserObject(DIMENSIONAL_INFORMATIONS));
		DefaultMutableTreeNode lvSections= new DefaultMutableTreeNode("Sections");
		
		Collection<String> lvExtendedLinkRoleSet = definitionLB.getExtendedLinkRoles();
		for(String bvExtendedLinkRole : lvExtendedLinkRoleSet){
			String lsRoleName = bvExtendedLinkRole;
			lsRoleName = lsRoleName.substring( lsRoleName.lastIndexOf("/") +1);
			DefaultMutableTreeNode lvExtendedLinkRoleNode = new DefaultMutableTreeNode( lsRoleName );
			// xlink:arcrole="http://xbrl.org/int/dim/arcrole/all" for contextElement="scenario"
			List<Arc> lvArcSet = definitionLB.getArcBaseSet( GeneralConstants.XBRL_ARCROLE_HYPERCUBE_ALL, bvExtendedLinkRole );
			DefaultMutableTreeNode lvScenarios = new DefaultMutableTreeNode("Scenarios");
			for(int i=0;i<lvArcSet.size();i++){
				Arc lArc =  lvArcSet.get(i);
				DefaultMutableTreeNode lvArcNode = new DefaultMutableTreeNode( new XbrlTbUserObject( lArc ) );
				
				DefaultMutableTreeNode lvArcSourceNode = new DefaultMutableTreeNode( new XbrlTbUserObject( lArc.getSourceElement() ) );
				DefaultMutableTreeNode lvArcTargetNode = new DefaultMutableTreeNode( new XbrlTbUserObject( lArc.getTargetElement() ) );
				lvArcNode.add( lvArcSourceNode );
				lvArcNode.add( lvArcTargetNode );
				lvScenarios.add( lvArcNode );
			}			
			lvExtendedLinkRoleNode.add( lvScenarios );
			if (lvScenarios.getChildCount()>0){
				lvSections.add( lvExtendedLinkRoleNode );
			}			
		}
		
		lvDimInfo.add( lvSections );
		return lvDimInfo;
	}
	
	public DefaultMutableTreeNode getCalculationLinkbase(){
		DefaultMutableTreeNode lvCalculationNode = new DefaultMutableTreeNode(new XbrlTbUserObject(CALCULATION_LINK));
		CalculationLinkbase lvCalculation = dts.getCalculationLinkbase();
		if (lvCalculation!=null){
			XbrlTbUserObject.setCalculationLB(lvCalculation);
			Set<Concept> lsConcepts = dts.getConcepts();
			for( Concept bvConcept : lsConcepts ){
//				Map lmCalculations = lvCalculation.getCalculations(bvConcept, "http://www.xbrl.org/2003/role/link")
//				lmCalculations.entrySet()
				DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode( new XbrlTbUserObject( bvConcept ) );
				
				lvCalculationNode.add( treeNode );
			}
		}
		//TODO: add count
		return lvCalculationNode;
	}
	
	public DefaultMutableTreeNode getDefinitionLinkbaseTreeNode(){
		DefaultMutableTreeNode definitionNode = new DefaultMutableTreeNode(new XbrlTbUserObject(DEFINITION_LINK));			
		if (definitionLB != null){
			XbrlTbUserObject.setDefinitionLB(definitionLB);

			//the set of hypercubes
			DefaultMutableTreeNode hcSetNode = new DefaultMutableTreeNode("Hypercubes Set");
			Collection<String> defExtLinkRoles = definitionLB.getExtendedLinkRoles(); 
			Set<Hypercube> hypercubeSet = definitionLB.getHypercubeSet();
			Set<Hypercube> hcNotYetListed = new HashSet(hypercubeSet);
			/* we need to iterate on extended link roles
			 * as the same hypercube may have different sets of dimensions in different xlink roles
			 * We also keep the list of hypercubes for which there is nothing to display in any role
			 * these are listed in the "no role" section as they seem to have no hypercube-dimension arc
			 */
			for (String extLinkRole : defExtLinkRoles){
				DefaultMutableTreeNode extLinkNode = new DefaultMutableTreeNode(extLinkRole);
				for (Hypercube hc : hypercubeSet){
					if (definitionLB.getArcsFromConcept(hc.getConcept(), GeneralConstants.XBRL_ARCROLE_HYPERCUBE_DIMENSION, extLinkRole) == null){
						continue;
					} else if (hcNotYetListed.contains(hc)){
						hcNotYetListed.remove(hc);
					}
					extLinkNode.add(makeHcTreeNode(hc, extLinkRole));
				}
				if (extLinkNode.getChildCount() > 0)
					hcSetNode.add(extLinkNode);
			}
			if (hcNotYetListed.size() > 0){
				DefaultMutableTreeNode emptyRole = new DefaultMutableTreeNode(new XbrlTbUserObject(NO_ROLE_UNUSED_HC));
				for (Hypercube hc : hcNotYetListed){
					emptyRole.add(makeHcTreeNode(hc, null));
				}
				hcSetNode.add(emptyRole);
			}
			definitionNode.add( hcSetNode );
			
			//Now, look for DimensionConceptSet...
			DefaultMutableTreeNode dimensionsNode = new DefaultMutableTreeNode("DimensionSet");
			Set<Concept> lvDimensionSet = definitionLB.getDimensionConceptSet();
			for( Concept bvConcept : lvDimensionSet){
				DefaultMutableTreeNode dimNode = new DefaultMutableTreeNode( new XbrlTbUserObject(bvConcept) );
				Concept lvDimensionElement = definitionLB.getDimensionElement(bvConcept);
				dimensionsNode.add( dimNode );
				if (lvDimensionElement!=null){
					DefaultMutableTreeNode lvDimensionElementNode = new DefaultMutableTreeNode( new XbrlTbUserObject(lvDimensionElement) );
					dimensionsNode.add( lvDimensionElementNode );
				}					
			}
			definitionNode.add( dimensionsNode );
				
			//Now ExtendedLinkRoles
			DefaultMutableTreeNode xLinkRolesNode = new DefaultMutableTreeNode("Extended Link Roles, Arcs and Elements");
			for(String role : defExtLinkRoles){
				DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(role);
				List<ExtendedLinkElement> roleElems = definitionLB.getExtendedLinkElementsFromBaseSet( role );
				for(ExtendedLinkElement elem : roleElems){
					DefaultMutableTreeNode lvExtendedLinkElementInRoleNode = new DefaultMutableTreeNode(new XbrlTbUserObject(elem));						
					roleNode.add( lvExtendedLinkElementInRoleNode );
				}
				
				// xlink:arcrole="http://xbrl.org/int/dim/arcrole/all" for contextElement="scenario"
				List<Arc> lvArcSet = definitionLB.getArcBaseSet( role );
				if (lvArcSet != null){
					for(int i=0;i<lvArcSet.size();i++){
						DefaultMutableTreeNode lvArcNode = new DefaultMutableTreeNode( new XbrlTbUserObject( lvArcSet.get(i) ) );					
						roleNode.add( lvArcNode );
					}
				}
				xLinkRolesNode.add( roleNode );
				
			}
			definitionNode.add( xLinkRolesNode );

			/*
			//Now ExtendedLinkElements
			DefaultMutableTreeNode lvExtendedLinkElementsNode = new DefaultMutableTreeNode("ExtendedLinkElements");
			Set<ExtendedLinkElement> lvExtendedLinkElementSet = definitionLB.getExtendedLinkElements();
			for(ExtendedLinkElement bvExtendedLinkElement : lvExtendedLinkElementSet){
				DefaultMutableTreeNode lvExtendedLinkElementNode = new DefaultMutableTreeNode(new XbrlTbUserObject(bvExtendedLinkElement) );
				lvExtendedLinkElementsNode.add( lvExtendedLinkElementNode );
			}
			definitionNode.add( lvExtendedLinkElementsNode );
			*/
				
			//Now ArcBaseSet
			fillLinkbaseArcs(definitionNode, definitionLB, "Arcs Set");
		}
		else{
		    debug( "Empty List of DefinitionLinkbase" );
		}		
		
		return definitionNode;
	}

	/**
	 * Add the different {@link Arc} sets from a {@link Linkbase} 
	 * to the given parent tree node
	 * @param parentNode
	 * @param linkbase
	 * @param nodeLabel
	 */
	private void fillLinkbaseArcs(DefaultMutableTreeNode parentNode, Linkbase linkbase, String nodeLabel) {
		DefaultMutableTreeNode arcBaseSetNode = new DefaultMutableTreeNode(nodeLabel);
		for (String extLinkRole : linkbase.getExtendedLinkRoles()){
			DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(extLinkRole);
			fillArcList(roleNode, "Arc Base Set", linkbase.getArcBaseSet(extLinkRole));
			fillArcList(roleNode, "Overriden Arcs", linkbase.getOverridenArcs(extLinkRole));
			fillArcList(roleNode, "Prohibiting Arcs", linkbase.getProhibitingArcs(extLinkRole));
			fillArcList(roleNode, "Prohibited Arcs", linkbase.getProhibitedArcs(extLinkRole));
			arcBaseSetNode.add(roleNode);
		}
		parentNode.add(arcBaseSetNode);
	}

	/**
	 * Add a new {@link Arc} set content to the given tree node 
	 * only if the list of arcs is non-empty 
	 * @param parentNode parent tree node
	 * @param baseName a label associated to the arc list
	 * @param arcs the list of arcs from a linkbase
	 */
	private void fillArcList(DefaultMutableTreeNode parentNode, String baseName, List<Arc> arcs) {
		if (arcs != null){
			DefaultMutableTreeNode baseNode = new DefaultMutableTreeNode(baseName);
			for (Arc arc : arcs){
				baseNode.add(new DefaultMutableTreeNode(new XbrlTbUserObject(arc)));
			}
			parentNode.add(baseNode);
		}
	}
	
	/**
	 * Return a {@link DefaultMutableTreeNode} for the given {@link Hypercube}
	 * @param extLinkRole TODO
	 */
	private DefaultMutableTreeNode makeHcTreeNode(Hypercube hc, String extLinkRole){
		DefaultMutableTreeNode hcNode = new DefaultMutableTreeNode( new XbrlTbUserObject(hc) );					
		Set<Dimension> hcDimSet = hc.getDimensionSet(extLinkRole);
		if(hcDimSet != null){
			for(Dimension dim : hcDimSet){
				DefaultMutableTreeNode dimNode = new DefaultMutableTreeNode( new XbrlTbUserObject(dim) );
				
				if (!dim.isTyped()){
					//we want the dimension domain item
					
					List<Arc> dimDomList = definitionLB.getArcsFromConcept(dim.getConcept(), GeneralConstants.XBRL_ARCROLE_DIMENSION_DOMAIN, extLinkRole);
					//if we do not find the domain in the current extended role, try in default one
					if (dimDomList == null)
						dimDomList = definitionLB.getArcsFromConcept(dim.getConcept(), GeneralConstants.XBRL_ARCROLE_DIMENSION_DOMAIN, GeneralConstants.XBRL_LINKBASE_DEFAULT_LINKROLE);
					if(dimDomList != null){
						//there are sometimes empty dimensions
						Concept dimensionDomain = ((Locator)dimDomList.get(0).getTargetElement()).getConcept();
						DefaultMutableTreeNode domainNode = new DefaultMutableTreeNode(new XbrlTbUserObject(dimensionDomain));
						
						Set<ExtendedLinkElement> domainMemberSet = dim.getDomainMemberSet();
						for (ExtendedLinkElement domainMember : domainMemberSet){
							if (domainMember.isLocator() && (((Locator)domainMember).getConcept()).equals(dimensionDomain))
								continue;
							DefaultMutableTreeNode memberNode = new DefaultMutableTreeNode( new XbrlTbUserObject(domainMember) );
							domainNode.add( memberNode );
						}
						dimNode.add(domainNode);
					}
				}
				hcNode.add( dimNode );
			}					
		} else
			System.out.println(hc.getConcept().getName() + " has no dimension in " + extLinkRole);
		return hcNode;
	}
		
	private MutableTreeNode getTaxonomyStructureTreeNode() {
		DefaultMutableTreeNode taxoNode = new DefaultMutableTreeNode("Structure");
		String top = dts.getTopTaxonomy().getName();
		DefaultMutableTreeNode dtsRoot = new DefaultMutableTreeNode(top);
		
		Map<String, TaxonomySchema> taxoMap = dts.getTaxonomyMap();
		for (String t : taxoMap.keySet()){
			DefaultMutableTreeNode tn = new DefaultMutableTreeNode(t);
			for (String s : taxoMap.get(t).getImportedTaxonomyNames()){
				DefaultMutableTreeNode sn = new DefaultMutableTreeNode(s);
				tn.add(sn);
			}
				
			dtsRoot.add(tn);
		}
		
		taxoNode.add(dtsRoot);
		//for (dts.getTopTaxonomy())
		
		return taxoNode;
	}

	public MutableTreeNode getPresentationLinkbaseTreeNode(){
		XbrlTbUserObject.setLabelLinkbase( labelLB );
		XbrlTbUserObject.setPresentationLB( presentationLB );
		DefaultMutableTreeNode presentationNode = new DefaultMutableTreeNode(new XbrlTbUserObject(PRESENTATION_LINK));
		if (presentationLB!=null){				
		    Collection<String> presentExtRoles = presentationLB.getExtendedLinkRoles();
			for (String role : presentExtRoles){ 
				DefaultMutableTreeNode xlinkNode = new DefaultMutableTreeNode( new XbrlTbUserObject(role) );
				debug( "<ExtendedLinkRoles name=\"" + role + "\">" );
				enumPresentationLinkbaseElements( presentationLB.getPresentationLinkbaseElementRoot( role ), role, xlinkNode, 0 );
				debug( "</ExtendedLinkRoles>");
				presentationNode.add( xlinkNode );
			}
			
			//add the arcs
			fillLinkbaseArcs(presentationNode, presentationLB, "Arc Set");
		}
		else{
		    debug("Empty List of PresentationLinkbase");
		}
		
		return presentationNode;
	}	
	
	public DefaultMutableTreeNode getLabelsLinkbaseTreeNode(){
		XbrlTbUserObject.setLabelLinkbase(labelLB);
		DefaultMutableTreeNode labelNode = new DefaultMutableTreeNode(new XbrlTbUserObject(LABEL_LINK));
		DefaultMutableTreeNode extLinkRoleNode;		
		if (labelLB != null){
			Collection<String> extendedLinkRoles = labelLB.getExtendedLinkRoles();
			for (String extLinkRole : extendedLinkRoles){
				extLinkRoleNode = new DefaultMutableTreeNode(new XbrlTbUserObject(extLinkRole));
				debug( "<ExtendedLinkRoles name=\"" + extLinkRole + "\">" );
				
				//first pass : get sources (xsd / xml files)
				Set<String> sources = new HashSet<String>();
				for (ExtendedLinkElement el : labelLB.getExtendedLinkElementsFromBaseSet(extLinkRole)){
					sources.add(el.getLinkbaseSource());
				}
				//for each source, enumerate the extended link elements
				for (String src : sources){
					DefaultMutableTreeNode srcNode = new DefaultMutableTreeNode(src);
					for (ExtendedLinkElement el : labelLB.getExtendedLinkElementsFromBaseSet(extLinkRole)){
						if (el.getLinkbaseSource().equals(src)){
							srcNode.add(new DefaultMutableTreeNode(new XbrlTbUserObject(el)));
						}
					}
					extLinkRoleNode.add(srcNode);
				}
				debug( "</ExtendedLinkRoles>");
				labelNode.add( extLinkRoleNode );
			}
			
			//put the arcs
			//fillLinkbaseArcs(labelNode, labelLB, "Arcs Set");
		}
		else{
		    debug("Empty list of LabelLinkbase");
		}
		return labelNode;
	}
	
	public DefaultMutableTreeNode getReferenceLinkbaseTreeNode(){
		XbrlTbUserObject.setReferenceLB(referenceLB);
		DefaultMutableTreeNode referenceNode = new DefaultMutableTreeNode(new XbrlTbUserObject(REFERENCE_LINK));
		DefaultMutableTreeNode extLinkRoleNode;
		if (referenceLB != null){
			Collection<String> extendedLinkRoles = referenceLB.getExtendedLinkRoles();
			for (String extLinkRole : extendedLinkRoles){
				extLinkRoleNode = new DefaultMutableTreeNode(new XbrlTbUserObject(extLinkRole));
				debug( "<ExtendedLinkRoles name=\"" + extLinkRole + "\">" );
				//first pass : get sources (xsd / xml files)
				Set<String> sources = new HashSet<String>();
				for (ExtendedLinkElement el : referenceLB.getExtendedLinkElementsFromBaseSet(extLinkRole)){
					sources.add(el.getLinkbaseSource());
				}
				//for each source, enumerate the extended link elements
				for (String src : sources){
					DefaultMutableTreeNode srcNode = new DefaultMutableTreeNode(src);
					for (ExtendedLinkElement el : referenceLB.getExtendedLinkElementsFromBaseSet(extLinkRole)){
						if (el.getLinkbaseSource().equals(src)){
							srcNode.add(new DefaultMutableTreeNode(new XbrlTbUserObject(el)));
						}
					}
					extLinkRoleNode.add(srcNode);
				}
				debug( "</ExtendedLinkRoles>");
				referenceNode.add( extLinkRoleNode );
			}
			
		}
		else{
			debug("Empty list of ReferenceLinkbase");
		}
		return referenceNode;
	}
	
	/**
	 *  
	 * @param lvPERoots
	 * @param asExtendedLinkRole
	 * @param lvExtendedLinkRole
	 * @param lvParentLevel
	 */
	public void enumPresentationLinkbaseElements( List<PresentationLinkbaseElement> lvPERoots, String asExtendedLinkRole, DefaultMutableTreeNode lvExtendedLinkRole, int lvParentLevel ){
		if (lvPERoots==null) return;
				
		Iterator<PresentationLinkbaseElement> liPERoots = lvPERoots.iterator();
		while( liPERoots.hasNext() ){				
			PresentationLinkbaseElement bvPElement = liPERoots.next();				
			Concept lvParentElement = bvPElement.getParentElement();				
			debug( "<concept>" );
			debug( bvPElement.getConcept().getName() 														
											  + "\t" + labelLB.getLabel( bvPElement.getConcept() , xbrlcore.constants.GeneralConstants.XBRL_ROLE_LABEL )
											  + (LOG_LEVEL.ordinal() <= LogLevel.DEBUG.ordinal() ? ""
												 + "\t" + bvPElement.getLevel() 
												 + "\t" + ((lvParentElement==null)?"":lvParentElement.getName())														
												 + "\t" + bvPElement.getConcept().getID()
												 + "\t" + bvPElement.getNumDirectSuccessor()
												 + "\t" + bvPElement.getNumSuccessorAtDeepestLevel()
												 + "\t" + bvPElement.getPositionDeepestLevel()										
												 : "" )
											  );
			
			if( bvPElement.getLevel() == lvParentLevel+1 ){
				DefaultMutableTreeNode lvConceptNode = new DefaultMutableTreeNode( new XbrlTbUserObject(bvPElement) );
				enumerateConcepts( bvPElement.getSuccessorElements(), asExtendedLinkRole, lvConceptNode, bvPElement.getLevel() );										
				lvExtendedLinkRole.add( lvConceptNode );
			}				
			debug( "</concept>" );				
		}		
	}

	/**
	 *  
	 * @param lvConcepts
	 * @param asExtendedLinkRole
	 * @param lvConceptNode
	 * @param lvParentLevel
	 */
	public void enumerateConcepts( List<Concept> lvConcepts, String asExtendedLinkRole, DefaultMutableTreeNode lvConceptNode, int lvParentLevel ){
		Iterator<Concept> liConcepts = lvConcepts.iterator();
		debug("<enumerateConcepts>");
		while( liConcepts.hasNext() ){			
			Concept bvConcept = liConcepts.next();

			//System.out.println( "[" + bvConcept.getName() +"]" );
			debug("<enumconcept name=\"" + bvConcept.getName() +"\">" );			
			if (presentationLB!=null){				
				enumPresentationLinkbaseElements( presentationLB.getPresentationList( bvConcept, asExtendedLinkRole ), asExtendedLinkRole, lvConceptNode/*lvChildConceptNode*/, lvParentLevel );
			}	
			debug("</enumconcept>" );
		}
		debug("</enumerateConcepts>");
	}
		
	public void parseTaxonomy(){
		try{ 
			editTaxoPath.setText( taxoEntryPoint.getCanonicalPath() );
			//initialize objects
			
			//uses our caching file loader
			
			java.net.Proxy proxy = null;
			//java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress("myproxy.intranet.de", 8080));
//			dts = dtsFactory.loadTaxonomy(taxoEntryPoint, null /*no user object*/, new FileLoader());
			dts = dtsLoader.loadTaxonomy(taxoEntryPoint, null /*no user object*/, new FileLoader());
			dts.buildLinkbases();
			labelLB = dts.getLabelLinkbase();
			presentationLB = dts.getPresentationLinkbase();
			definitionLB = dts.getDefinitionLinkbase();
			referenceLB = dts.getReferenceLinkbase();
			XbrlTbUserObject.setDts(dts);
			fillLanguages();
			fillTree();
			
		}
		catch (TaxonomyCreationException ex){ 
			ex.printStackTrace(); 
			return;
		}
		catch(FileNotFoundException fnfe){
			JOptionPane.showConfirmDialog((Component)null, fnfe.getMessage(), "File not found!", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		catch (IOException ex){ 
			ex.printStackTrace(); 
			return;
		}
		catch (XBRLException ex){ 
			ex.printStackTrace(); 
			return;
		} 
	}

	private void fillLanguages() {
		comboLanguages.removeAllItems();
		for(String lang : labelLB.getLanguageSet()){
			comboLanguages.addItem(lang);
		}
		if (comboLanguages.getItemCount() > 0)
			currentLang = (String) comboLanguages.getItemAt(0);
	}

	private void fillTree() {
		XbrlTbUserObject.setLang(currentLang);
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Taxonomy");
		//FIXME: unfinished 
		//root.add(getTaxonomyStructureTreeNode());
		
	    DefaultMutableTreeNode bases = new DefaultMutableTreeNode(new XbrlTbUserObject(LINKBASES));
	    bases.add(getPresentationLinkbaseTreeNode());
	    bases.add(getDefinitionLinkbaseTreeNode());
		//FIXME: broken for now... root.add(getCalculationLinkbase());
	    bases.add(getLabelsLinkbaseTreeNode());
	    bases.add(getReferenceLinkbaseTreeNode());
	    bases.add(getDimInfo());
	    root.add(bases);
		//root.add( GetImportedTaxonomy() );
		//...
		treeNavigator.setModel( new DefaultTreeModel(root) );  
		treeNavigator.expandPath(new TreePath(bases.getPath()));
	}	
 
} 
