package xbrlcore.junit.linkbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

import xbrlcore.exception.InstanceValidationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.instance.Fact;
import xbrlcore.instance.Instance;
import xbrlcore.instance.InstanceFactory;
import xbrlcore.instance.InstanceValidator;
import xbrlcore.junit.sax.TestHelper;
import xbrlcore.linkbase.CalculationLinkbase;
import xbrlcore.linkbase.CalculationRule;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;

/**
 * @TODO: Add comment.
 * @author Daniel Hamm
 */

public class CalculationLinkbaseTest {

    /**
     * Path used in this test case.
     */
    private static String PATH = "xbrl/test/linkbase_test/";

    /**
     * DTS used in this test case.
     */
    private static DiscoverableTaxonomySet taxCalcDTS;

    /**
     * Instance used in this test case.
     */
    private static Instance instance;

    /**
     * This method is executed before all test cases in this JUnit test.
     */
    
    @BeforeClass
    public static void setUp() {
        try {
            InstanceFactory instanceFactory = InstanceFactory.get();

            // DTSFactory taxonomyFactory = DTSFactory.get();
            // taxCalcDTS = taxonomyFactory.createTaxonomy(new File(PATH
            // + "tax_calc.xsd"));

            taxCalcDTS = TestHelper
                .getDTS("xbrl/test/linkbase_test/tax_calc.xsd");
            instance = instanceFactory.createInstance(new File(PATH
                + "instance.xml"));
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
            fail("Fehler beim Erstellen der Taxonomy pr: " + ex.getMessage());
        }
    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void calcLinkbase() {
        try {
            assertNotNull(taxCalcDTS);
            CalculationLinkbase calcLinkbase = taxCalcDTS
                .getCalculationLinkbase();
            assertNotNull(calcLinkbase);

            Concept primItem = taxCalcDTS.getConceptByID("p0_newItem");
            assertNotNull(primItem);
            List<CalculationRule> calcMap = calcLinkbase.getCalculationRules(primItem,
                "http://www.xbrl.org/2003/role/link");
            assertEquals(2, calcMap.size());
        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
            fail(ex.getMessage());
        }

    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void singleFactToCalculate() {
        try {
            assertNotNull(instance);
            Iterator<DiscoverableTaxonomySet> iterator = instance.getDiscoverableTaxonomySet()
                .iterator();
            @SuppressWarnings("unused")
			CalculationLinkbase calcLinkbase = iterator.next().getCalculationLinkbase();

            InstanceValidator validator = new InstanceValidator(instance);

            Collection<Fact> factSet = instance.getFacts();
            Iterator<Fact> factSetIterator = factSet.iterator();
            while (factSetIterator.hasNext()) {
                Fact currFact = factSetIterator.next();
                if (currFact.getConcept().getID().equals("p0_newItem5")) {
                    try {
                        validator.validateCalculation(currFact);
                    } catch (InstanceValidationException ex) {
                        System.err.println(ex.toString());
                        fail(ex.toString());
                    }
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
            fail(ex.getMessage());
        }

    }

    /**
     * TODO: Add comment.
     */
    @Test
    public void calculateInstance() {
        assertNotNull(instance);
        Iterator<DiscoverableTaxonomySet> iterator = instance.getDiscoverableTaxonomySet().iterator();
        @SuppressWarnings("unused")
		CalculationLinkbase calcLinkbase = iterator.next().getCalculationLinkbase();

        InstanceValidator validator = new InstanceValidator(instance);
        try {
            validator.validate();
        } catch (InstanceValidationException ex) {
            System.err.println(ex.toString());
            fail(ex.toString());
        } catch (XBRLException ex) {
            System.err.println(ex.toString());
            fail(ex.toString());
        }
    }
    
    public static void main(String[] args) throws Exception {
    	//openStreamTest();
    	mainTest();
    }
    
    private static void mainTest() {
    	setUp();
    	CalculationLinkbaseTest test = new CalculationLinkbaseTest();
    	test.calculateInstance();
    }
    
    private static void openStreamTest() throws Exception {
    	String protocolPath = new File(PATH + "instance.xml").toURI().toURL().toString();
    	int lastIndex = protocolPath.lastIndexOf('/');
    	protocolPath = protocolPath.substring(0, lastIndex + 1);
    	protocolPath = "file:///Users/fauzi/xbrl-core-development/eclispe.workspace/xbrlcore/xbrl/test/linkbase_test/";
    	URL base = new URL(protocolPath);
    	String taxonomyResource = "tax_calc.xsd";
    	String fisLocation = base.getFile() + taxonomyResource;
    	FileInputStream fis = new FileInputStream(fisLocation);
    	//InputSource is = new InputSource(new URL(base, taxonomyResource).openStream());
    	InputSource is = new InputSource(fisLocation);
    	System.out.println(is.getSystemId());
    }
    


}
