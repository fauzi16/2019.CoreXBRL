package xbrlcore.playground;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import xbrlcore.dimensions.Dimension;
import xbrlcore.dimensions.Hypercube;
import xbrlcore.taxonomy.DefaultTaxonomyLoader;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;

public class PlaygroundDimension {

	/**
	private static final String BASE_URL = "C:\\Users\\fauzi16\\Documents\\Work\\Fujitsu\\ID.OJK\\2019.ID.XBRL\\ID.XBRL\\Source\\XRDM\\XRDM-Basic\\CONFIGXBRL\\EXTRACTEDDIR\\taxonomy\\view\\pp\\2016-06-11\\bulanan\\gabungan\\531020300";
	*/
	
	/*
	 * Single Dimension Domain of Dimensional Taxonomy
	 */
	private static final String BASE_URL = "/Users/fauzi/xbrl-reporting-manager/2019.XBRL.ReportingManager/xbrlrootfolder/base/taxonomy/xbrl.ojk.go.id/view/pp/2016-06-11/bulanan/gabungan/531020300";
	private static final String TAXONOMY_LOCATION = "531020300-2016-06-11.xsd";
	
	

	public static void main(String[] args) throws Exception {
		drillHypercube();
	}

	public static void drillHypercube() throws Exception {
		File taxonomyFile = new File(BASE_URL + File.separator + TAXONOMY_LOCATION);
		DiscoverableTaxonomySet dts = new DefaultTaxonomyLoader().loadTaxonomy(taxonomyFile, null);
		dts.getDefinitionLinkbase().buildLinkbase();
		Set<Hypercube> hypes = dts.getDefinitionLinkbase().getHypercubeSet();
		for (Hypercube hype : hypes) { 
			Map<String, Set<Dimension>> dimensionSetMap = hype.getDimensionSetMap();
			for (Entry<String, Set<Dimension>> dimSet : dimensionSetMap.entrySet()) {
				System.out
						.println("=============================== " + dimSet.getKey() + " ===========================");
				Set<Dimension> dims = dimSet.getValue();
				for (Dimension dim : dims) {
					System.out.println("|========================== " + dim.getConcept()
							+ " =============================");

					Set<ExtendedLinkElement> elements = dim.getDomainMemberSet();
					for (ExtendedLinkElement element : elements) {
						Locator l = (Locator) element;
						System.out.println(l.getConcept());
					}
				}
			}

		}
	}

}
