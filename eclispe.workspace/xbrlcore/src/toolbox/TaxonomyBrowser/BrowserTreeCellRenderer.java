package toolbox.TaxonomyBrowser;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import xbrlcore.constants.GeneralConstants;
import xbrlcore.dimensions.Dimension;
import xbrlcore.dimensions.Hypercube;
import xbrlcore.linkbase.PresentationLinkbaseElement;
import xbrlcore.taxonomy.Concept;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.Locator;
import xbrlcore.xlink.Resource;

/**
 * Custom xbrl tree cell renderer that let us have our icons
 * @author Sébastien Kirche
 *
 */
public class BrowserTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 3415502679404266088L;

	private static Icon iconLinkbase = new ImageIcon("res/linkbase.png");
	private static Icon iconLBPresentation = new ImageIcon("res/lbPresentation.png");
	private static Icon iconLBDefinition = new ImageIcon("res/lbDefinition.png");
	private static Icon iconLBCalculation = new ImageIcon("res/lbCalculation.png");
	private static Icon iconLBLabel = new ImageIcon("res/lbLabel.png");
	private static Icon iconLBReference = new ImageIcon("res/lbReference.png");
	private static Icon iconRole = new ImageIcon("res/role.png");
	private static Icon iconNoRole = new ImageIcon("res/norole.png");
	private static Icon iconLabel = new ImageIcon("res/label.png");
	private static Icon iconLabelTerse = new ImageIcon("res/labelTerse.png");
	private static Icon iconLabelVerbose = new ImageIcon("res/labelVerbose.png");
	private static Icon iconLocator = new ImageIcon("res/locator.png");
	//private Icon iconLocator = new ImageIcon("res/compass.png");
	private static Icon iconPresentation = new ImageIcon("res/presentation.png");
	private static Icon iconCalc = new ImageIcon("res/calculation.png");
	private static Icon iconConcept = new ImageIcon("res/measure.png");
	private static Icon iconArc = new ImageIcon("res/arc.png");
	private static Icon iconReference = new ImageIcon("res/reference.png");
	private static Icon iconDimension = new ImageIcon("res/dimension.png");
	private static Icon iconDimCoord = new ImageIcon("res/coord.png");
	private static Icon iconDimensionExplicit = new ImageIcon("res/dimensionExplicit.png");
	private static Icon iconDimensionTyped = new ImageIcon("res/dimensionTyped.png");
	private static Icon iconDocumentation = new ImageIcon("res/documentation.png");
	private static Icon iconCube = new ImageIcon("res/cube.png");
	private static Icon iconTotal = new ImageIcon("res/total.png");
	private static Icon iconPeriodStart = new ImageIcon("res/periodStart.png");
	private static Icon iconPeriodEnd = new ImageIcon("res/periodEnd.png");
	
	public BrowserTreeCellRenderer() {
		
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel,
            boolean expanded,
            boolean leaf, int row,
            boolean hasFocus) {
		
		DefaultMutableTreeNode node = null;
		XbrlTbUserObject xbrlData = null;
		Object nodeObject = null;
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		if(value instanceof DefaultMutableTreeNode){
			node = (DefaultMutableTreeNode) value;
			nodeObject = node.getUserObject();
			if(nodeObject instanceof XbrlTbUserObject)
				xbrlData = (XbrlTbUserObject) nodeObject;
		}
		
	
		if (xbrlData != null){
			//we are rendering an XbrlTbUserObject
			Object userObject = xbrlData.getUserObject();
			
			if(userObject instanceof Resource){
				Resource res = (Resource) userObject;
				String resRole = res.getRole();
				
				if (resRole.equalsIgnoreCase(GeneralConstants.XBRL_ROLE_LABEL)){
					setIcon(iconLabel);
				} else if (resRole.equalsIgnoreCase(GeneralConstants.XBRL_ROLE_LABEL_TERSE)){
					setIcon(iconLabelTerse);
				} else if (resRole.equalsIgnoreCase(GeneralConstants.XBRL_ROLE_LABEL_VERBOSE)){
					setIcon(iconLabelVerbose);
				} else if (resRole.equalsIgnoreCase(GeneralConstants.XBRL_ROLE_LABEL_TOTAL)){
					setIcon(iconTotal);
				} else if (resRole.equalsIgnoreCase(GeneralConstants.XBRL_ROLE_LABEL_DOCUMENTATION)){
					setIcon(iconDocumentation);
				} else if (resRole.equalsIgnoreCase(GeneralConstants.XBRL_ROLE_REFERENCE)){
					setIcon(iconReference);
				} else if (resRole.equalsIgnoreCase(GeneralConstants.XBRL_ROLE_LABEL_PERIOD_START)){
					setIcon(iconPeriodStart);
				} else if (resRole.equalsIgnoreCase(GeneralConstants.XBRL_ROLE_LABEL_PERIOD_END)){
					setIcon(iconPeriodEnd);
				} else
					System.out.println("unhandled resource icon: " + resRole);
				
			} else if (userObject instanceof Locator){
				setIcon(iconLocator);
			} else if (userObject instanceof PresentationLinkbaseElement){
				setIcon(iconPresentation);
			} else if (userObject instanceof Concept){
				setIcon(iconConcept);
			} else if (userObject instanceof Dimension){
				if (((Dimension) userObject).isTyped())
					setIcon(iconDimensionTyped);
				else
					setIcon(iconDimensionExplicit);
			} else if (userObject instanceof Arc){
				setIcon(iconArc);
			} else if (userObject instanceof Hypercube){
				setIcon(iconCube);
			} else if (userObject instanceof String){
				String str = (String) userObject;
				if (str.equalsIgnoreCase(Browser.LINKBASES)){
					setIcon(iconLinkbase);
				} else if (str.equalsIgnoreCase(Browser.PRESENTATION_LINK)){
					setIcon(iconLBPresentation);
				} else if (str.equalsIgnoreCase(Browser.DEFINITION_LINK)){
					setIcon(iconLBDefinition);
				} else if (str.equalsIgnoreCase(Browser.CALCULATION_LINK)){
					setIcon(iconLBCalculation);
				} else if (str.equalsIgnoreCase(Browser.LABEL_LINK)){
					setIcon(iconLBLabel);
				} else if (str.equalsIgnoreCase(Browser.REFERENCE_LINK)){
					setIcon(iconLBReference);
				} else if (str.equalsIgnoreCase(Browser.DIMENSIONAL_INFORMATIONS)){
					setIcon(iconDimCoord);
				} else if (str.startsWith("http://")){
					setIcon(iconRole);
				} else if (str.startsWith(Browser.NO_ROLE_UNUSED_HC)){
					setIcon(iconNoRole);
				}
					
			} else //if (!(userObject instanceof String))
				System.out.println("unhandled class icon:" + userObject.getClass().getSimpleName());
		
			setToolTipText(userObject.getClass().getSimpleName());
		} else if ((nodeObject instanceof String) && ((String)nodeObject).startsWith("http://")){
			setIcon(iconRole);
		}
		
		
		return this;
	}
}
