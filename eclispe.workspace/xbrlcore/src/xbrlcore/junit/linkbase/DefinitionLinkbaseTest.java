/*
 * Created on 18.02.2006
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package xbrlcore.junit.linkbase;

import java.util.Iterator;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import xbrlcore.dimensions.Hypercube;
import xbrlcore.dimensions.MultipleDimensionType;
import xbrlcore.dimensions.Dimension;
import xbrlcore.dimensions.SingleDimensionType;
import xbrlcore.junit.sax.TestHelper;
import xbrlcore.linkbase.DefinitionLinkbase;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.taxonomy.TaxonomySchema;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;
import static org.junit.Assert.*;

public final class DefinitionLinkbaseTest {
    
    /**
     * DTS used in this test case.
     */
    private static DiscoverableTaxonomySet stTaxonomy;

    /**
     * Definition linkbase used in this test case.
     */
    private static DefinitionLinkbase definitionLinkbase;

    /**
     * This method is executed before all test cases in this JUnit test.
     */
    @BeforeClass
    public static void setUp() {
        try {
            // DTSFactory taxonomyFactory = DTSFactory.get();
            // File stTaxonomyFile = new File(PATH+"t-st-2006-12-31.xsd");
            // stTaxonomy = taxonomyFactory.createTaxonomy(stTaxonomyFile);
            // definitionLinkbase = stTaxonomy.getDefinitionLinkbase();

            stTaxonomy = TestHelper.getDTS("xbrl/test/taxonomy_original/t-st-2006-12-31.xsd");
            definitionLinkbase = stTaxonomy.getDefinitionLinkbase();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
            fail("Fehler beim Erstellen der Taxonomy pr: " + ex.toString());
        }
    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void hypercube() {
        assertNotNull(definitionLinkbase);

        Set<Hypercube> hypercubeSet = definitionLinkbase.getHypercubeSet();
        assertEquals(3, hypercubeSet.size());

        Hypercube hcEuropa = stTaxonomy.getHypercube(stTaxonomy
            .getConceptByID("t-st_hcEuropa"));
        Hypercube hcAsien = stTaxonomy.getHypercube(stTaxonomy
            .getConceptByID("t-st_hcAsien"));
        Hypercube hcAmerika = stTaxonomy.getHypercube(stTaxonomy
            .getConceptByID("t-st_hcAmerika"));

        assertNotNull(hcEuropa);
        assertNotNull(hcAsien);
        assertNotNull(hcAmerika);

        Set<Dimension> hcEuropaDimensions = hcEuropa.getDimensionSet("http://www.xbrl-supervision.org/test/t-st/hcEuropa");
        assertEquals(2, hcEuropaDimensions.size());

        Iterator<Dimension> hcEuropaDimensionsIterator = hcEuropaDimensions.iterator();
        while (hcEuropaDimensionsIterator.hasNext()) {
            Dimension currDim = hcEuropaDimensionsIterator.next();
            Concept currDimElement = currDim.getConcept();
            assertTrue(currDimElement.getID().equals("t-st_KontinentDimension")
                || currDimElement.getID().equals("d-la_LaenderDimension"));
            if (currDimElement.getID().equals("t-st_KontinentDimension")) {
                Set<ExtendedLinkElement> dimensionDomain = hcEuropa
                    .getDimensionDomain(currDimElement, "http://www.xbrl-supervision.org/test/t-st/hcEuropa");
                assertEquals(2, dimensionDomain.size());
                Concept koKontinente = stTaxonomy
                    .getConceptByID("d-ko_Kontinente");
                Concept koEuropa = stTaxonomy.getConceptByID("d-ko_Europa");
                assertNotNull(koKontinente);
                assertNotNull(koEuropa);
                Iterator<ExtendedLinkElement> dimensionDomainIterator = dimensionDomain.iterator();
                while (dimensionDomainIterator.hasNext()) {
                    ExtendedLinkElement currXLinkElement = dimensionDomainIterator.next();
                    Concept currDomainElement = ((Locator) currXLinkElement)
                        .getConcept();
                    assertTrue(currDomainElement.getID().equals(
                        "d-ko_Kontinente")
                        || currDomainElement.getID().equals("d-ko_Europa"));
                }
            } else if (currDimElement.getID().equals("t-st_LaenderDimension")) {
                Set<ExtendedLinkElement> dimensionDomain = hcEuropa
                    .getDimensionDomain(currDimElement, "http://www.xbrl-supervision.org/test/t-st/hcEuropa");
                assertEquals(4, dimensionDomain.size());
                Iterator<ExtendedLinkElement> dimensionDomainIterator = dimensionDomain.iterator();
                while (dimensionDomainIterator.hasNext()) {
                	ExtendedLinkElement currDomainElement = dimensionDomainIterator.next();
                    assertTrue(currDomainElement.getID().equals("d-la_Laender")
                        || currDomainElement.getID().equals("d-la_Deutschland")
                        || currDomainElement.getID().equals("d-la_Spanien")
                        || currDomainElement.getID().equals("d-la_England"));
                }
            }
        }

        assertNotNull(stTaxonomy.getConceptByID("d-la_LaenderDimension"));
        assertTrue(hcEuropa.containsDimension(stTaxonomy
            .getConceptByID("d-la_LaenderDimension"), "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));

        assertTrue(hcEuropa.containsDimensionDomain(stTaxonomy
            .getConceptByID("d-la_LaenderDimension"), stTaxonomy
            .getConceptByID("d-la_Deutschland"), "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertTrue(hcAsien.containsDimensionDomain(stTaxonomy
            .getConceptByID("t-st_KontinentDimension"), stTaxonomy
            .getConceptByID("d-ko_Asien"), "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertFalse(hcAmerika.containsDimensionDomain(stTaxonomy
            .getConceptByID("t-st_KontinentDimension"), stTaxonomy
            .getConceptByID("d-la_Deutschland"), "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));

        assertTrue(hcAsien.containsDimensionDomain(stTaxonomy
            .getConceptByID("t-st_KontinentDimension"), stTaxonomy
            .getConceptByID("d-ko_Kontinente"), "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertFalse(hcAsien.containsUsableDimensionDomain(stTaxonomy
            .getConceptByID("t-st_KontinentDimension"), stTaxonomy
            .getConceptByID("d-ko_Kontinente"), "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void dimensions() {
        /*
         * contains - Dimension: t-st_KontinentDimension Domain: d-ko_Europa
         * Dimension: t-st_LaenderDimension Domain: d-la_Deutschland
         */
        try {
            Concept dimKontinent = stTaxonomy
                .getConceptByID("t-st_KontinentDimension");
            Concept elemEuropa = stTaxonomy.getConceptByID("d-ko_Europa");
            assertNotNull(dimKontinent);
            assertNotNull(elemEuropa);

            Concept dimLaender = stTaxonomy
                .getConceptByID("d-la_LaenderDimension");
            Concept elemDeutschland = stTaxonomy
                .getConceptByID("d-la_Deutschland");
            assertNotNull(dimLaender);
            assertNotNull(elemDeutschland);

            MultipleDimensionType mdt1 = new MultipleDimensionType(dimLaender,
                elemDeutschland);
            mdt1.addPredecessorDimensionDomain(new SingleDimensionType(
                dimKontinent, elemEuropa));

            Concept primaryEssen = stTaxonomy.getConceptByID("p-pr_Essen");
            assertNotNull(primaryEssen);

            assertTrue(definitionLinkbase.dimensionAllowed(primaryEssen, mdt1, "http://www.xbrl-supervision.org/test/t-st/hcEuropa", 
                "scenario"));
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void getDimensionTaxonomy() {
        Concept elementKontinent = stTaxonomy
            .getConceptByID("t-st_KontinentDimension");
        Concept elementLand = stTaxonomy
            .getConceptByID("d-la_LaenderDimension");
        Concept elementCube = stTaxonomy.getConceptByID("t-st_hcEuropa");

        Set<TaxonomySchema> dimensionTaxonomySet = stTaxonomy.getDefinitionLinkbase()
            .getDimensionTaxonomy(elementKontinent);
        Set<TaxonomySchema> dimensionTaxonomyLandSet = stTaxonomy.getDefinitionLinkbase()
            .getDimensionTaxonomy(elementLand);

        Iterator<TaxonomySchema> dimensionTaxonomyIterator = dimensionTaxonomySet.iterator();
        Iterator<TaxonomySchema> dimensionTaxonomyLandIterator = dimensionTaxonomyLandSet.iterator();

        dimensionTaxonomyIterator.hasNext();
        dimensionTaxonomyLandIterator.hasNext();

        TaxonomySchema dimensionTaxonomy = dimensionTaxonomyIterator.next();
        TaxonomySchema dimensionTaxonomyLand = dimensionTaxonomyLandIterator.next();

        assertNotNull(dimensionTaxonomy);
        assertNotNull(dimensionTaxonomyLand);
        assertNotNull(elementCube);
        assertEquals("d-ko-2006-12-31.xsd", dimensionTaxonomy.getName());
        assertEquals("d-la-2006-12-31.xsd", dimensionTaxonomyLand.getName());
    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void getDimensionElements() {
        Set<Concept> dimensionsSt = stTaxonomy.getDimensionConceptSet();
        assertEquals(2, dimensionsSt.size());
    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void getDimensionElement() {
        Concept d1 = stTaxonomy.getConceptByID("d-ko_Asien");
        Concept d2 = stTaxonomy.getConceptByID("d-la_Deutschland");
        Concept d3 = stTaxonomy.getConceptByID("t-st_hcEuropa");

        Concept D1 = stTaxonomy.getDefinitionLinkbase().getDimensionElement(d1);
        Concept D2 = stTaxonomy.getDefinitionLinkbase().getDimensionElement(d2);
        Concept D3 = stTaxonomy.getDefinitionLinkbase().getDimensionElement(d3);
        Concept D4 = stTaxonomy.getDefinitionLinkbase().getDimensionElement(null);

        assertEquals("t-st_KontinentDimension", D1.getID());
        assertEquals("d-la_LaenderDimension", D2.getID());
        assertNull(D3);
        assertNull(D4);
    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void isUsableDomainMemberOfDimension() {
        Concept d1 = stTaxonomy.getConceptByID("d-ko_Asien");
        Concept d2 = stTaxonomy.getConceptByID("d-la_Deutschland");

        Concept D1 = stTaxonomy.getConceptByID("t-st_KontinentDimension");
        Concept D2 = stTaxonomy.getConceptByID("d-la_LaenderDimension");

        assertTrue(stTaxonomy.getDefinitionLinkbase()
            .isUsableDomainMemberOfDimension(D1, d1, "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertTrue(stTaxonomy.getDefinitionLinkbase()
            .isUsableDomainMemberOfDimension(D2, d2, "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertFalse(stTaxonomy.getDefinitionLinkbase()
            .isUsableDomainMemberOfDimension(null, d1, "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertFalse(stTaxonomy.getDefinitionLinkbase()
            .isUsableDomainMemberOfDimension(D1, null, "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertFalse(stTaxonomy.getDefinitionLinkbase()
            .isUsableDomainMemberOfDimension(null, null, "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertFalse(stTaxonomy.getDefinitionLinkbase()
            .isUsableDomainMemberOfDimension(D1, d2, "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
        assertFalse(stTaxonomy.getDefinitionLinkbase()
            .isUsableDomainMemberOfDimension(D2, d1, "http://www.xbrl-supervision.org/test/t-st/hcEuropa"));
    }
}
