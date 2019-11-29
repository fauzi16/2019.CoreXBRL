package xbrlcore.playground;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.xml.sax.InputSource;

import xbrlcore.instance.InstanceReferencedTaxonomyLocator;
import xbrlcore.linkbase.LabelLinkbase;
import xbrlcore.linkbase.PresentationLinkbase;
import xbrlcore.linkbase.PresentationLinkbaseElement;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;

public class Playground {

	private static final String BASE_FOLDER = "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/base/taxonomy/xbrl.ojk.go.id/view/pp/2019-06-03/tahunan/gabungan/000020600/";
	private static final String TAXONOMY_FILE = "000020600-2019-06-03.xsd";
	private static final String EXTENDED_LINKROLE = "http://xbrl.ojk.go.id/taxonomy/role/F000020600";
	
	public static void main(String[] args) throws Exception {
		printOutListOfConcept();
		//findOutPathOfInputSource();
	}
	
	public static void printOutListOfConcept() throws Exception {
		URL base = new File(BASE_FOLDER).toURI().toURL();
		InstanceReferencedTaxonomyLocator locator = new InstanceReferencedTaxonomyLocator(base);
		DiscoverableTaxonomySet dts = locator.loadTaxonomy(TAXONOMY_FILE);
		PresentationLinkbase pl =  dts.getPresentationLinkbase();
		LabelLinkbase ll = dts.getLabelLinkbase();
		PresentationLinkbaseElement element = 
				pl.getPresentationLinkbaseElementRoot(EXTENDED_LINKROLE).get(0);
		drillPresentationLinkbaseElement(pl, ll, element.getConcept(), "-");
	}
	
	public static void drillPresentationLinkbaseElement(PresentationLinkbase pl, LabelLinkbase ll,
			Concept concept, String treeNode) {
		PresentationLinkbaseElement element = pl.getPresentationLinkbaseElement(concept, EXTENDED_LINKROLE);
		System.out.println(treeNode + " " + concept.getName() + " : " + ll.getLabel(concept, "id", null));
		if(element.getSuccessorElements() != null && !element.getSuccessorElements().isEmpty()) {
			List<Concept> childs = element.getSuccessorElements();
			treeNode += "-";
			for(Concept child : childs) {
				drillPresentationLinkbaseElement(pl, ll, child, treeNode);
			}
		}
	}
	
	
	
	public static void findOutPathOfInputSource() {
		String taxonomyFile = "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/base/taxonomy/xbrl.ojk.go.id/view/pp/2019-06-03/tahunan/gabungan/000020600/000020600-2019-06-03.xsd";
		InputSource is = new InputSource(taxonomyFile);
		System.out.println(is.getSystemId());
	}
	
}
