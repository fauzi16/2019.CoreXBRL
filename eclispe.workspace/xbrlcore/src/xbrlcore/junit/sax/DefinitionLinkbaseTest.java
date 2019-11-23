package xbrlcore.junit.sax;

import java.util.Set;

import junit.framework.TestCase;
import xbrlcore.dimensions.Hypercube;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;

public class DefinitionLinkbaseTest extends TestCase {

	DiscoverableTaxonomySet t_dts;

	@Override
	public void setUp() {
		t_dts = TestHelper.getTemplateTaxonomy();
		assertNotNull(t_dts);
	}

	public void testNumberOfHypercubes() {
		DefinitionLinkbase defLinkbase = t_dts.getDefinitionLinkbase();

		Set<Hypercube> hSet = defLinkbase.getHypercubeSet();
		assertEquals(3, hSet.size());
	}
}