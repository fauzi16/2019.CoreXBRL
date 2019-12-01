package xbrlcore.playground;

import java.io.File;
import java.util.List;

import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DefaultTaxonomyLoader;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;

public class PlaygroundExtensibleEnumeration {

	/**
	private static final String TAXONOMY_FILE = "C:\\Users\\fauzi16\\Documents\\Work\\Fujitsu\\ID.OJK\\2019.ID.XBRL\\ID.XBRL\\Source\\XRDM\\XRDM-Basic\\CONFIGXBRL\\EXTRACTEDDIR\\taxonomy\\view\\pp\\2016-06-11\\bulanan\\gabungan\\001020300\\001020300-2016-06-11.xsd";
	*/
	
	private static final String TAXONOMY_FILE = "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/base/taxonomy/xbrl.ojk.go.id/view/pp/2016-06-11/bulanan/gabungan/001020300/001020300-2016-06-11.xsd";
	
	public static void main(String[] args) throws Exception {
		File taxonomyFile = new File(TAXONOMY_FILE);
		DefaultTaxonomyLoader dtl = new DefaultTaxonomyLoader();
		DiscoverableTaxonomySet dts = dtl.loadTaxonomy(taxonomyFile, null );
		Concept extEnumerationConcept = dts.getConceptByID("ST_ld2");
		System.out.println(extEnumerationConcept);
		System.out.println(extEnumerationConcept.getEnumLinkRole());
		DefinitionLinkbase definitionLinkbase = dts.getDefinitionLinkbase();
		definitionLinkbase.buildLinkbase();
		List<ExtendedLinkElement> elements = definitionLinkbase.getExtendedLinkElementsFromBaseSet(extEnumerationConcept.getEnumLinkRole());
		for(ExtendedLinkElement element : elements) {
			Locator locator = (Locator) element;
			System.out.println(locator.getConcept());
		}
	}
	
}
