package xbrlcore.instance;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import xbrlcore.exception.CalculationValidationException;
import xbrlcore.exception.InstanceException;
import xbrlcore.exception.InstanceValidationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.linkbase.CalculationRule;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.taxonomy.Namespace;
import xbrlcore.taxonomy.TaxonomySchema;

/**
 * The purpose of this class is to do some basic instance (XBRL) validation.
 * Currently, the class is incomplete since it does not perform a full
 * validation, but it will be extended in the future. <br/><br/>
 * 
 * @author Daniel Hamm
 */
public class InstanceValidator {

    private Instance instance;

    private File instanceFile;

    private String schemaLocation;

    /**
     * Constructor.
     * 
     * @param instance
     *            Instance to validate.
     */
    public InstanceValidator(final Instance instance) {
        this.instance = instance;
    }

    public InstanceValidator(final File instanceFile) throws 
            IOException, CloneNotSupportedException, XBRLException,
            InstanceException, ParserConfigurationException, SAXException {
        this.instance = InstanceFactory.get().createInstance(instanceFile);
        this.instanceFile = instanceFile;
    }

    /**
     * Validates an instance file against the DTS by using only XML Schema (no
     * additional XBRL validation is performed).
     * 
     * @throws JDOMException
     * @throws XBRLException
     * @throws IOException
     * @throws CloneNotSupportedException
     */
    public final void schemaValidation() throws JDOMException, XBRLException,
            IOException, CloneNotSupportedException {

        /* if schema location is not set, it is tried to be determined */
        if (schemaLocation == null || schemaLocation.length() == 0) {
            schemaLocation = instance.getInstanceNamespace().getURI()
                    + " "
                    + instance.getSchemaForURI(instance.getInstanceNamespace())
                            .getName();

            for (Namespace currNamespace : instance.getAdditionalNamespaceSet()) {
                TaxonomySchema currSchema = instance
                        .getSchemaForURI(currNamespace);
                if (currSchema != null) {
                    schemaLocation += " " + currNamespace.getURI() + " "
                            + currSchema.getName();
                }
            }
        }

        SAXBuilder saxBuilder = new SAXBuilder(
                "org.apache.xerces.parsers.SAXParser", true);
        saxBuilder.setFeature(
                "http://apache.org/xml/features/validation/schema", true);
        saxBuilder
                .setProperty(
                        "http://apache.org/xml/properties/schema/external-schemaLocation",
                        schemaLocation);

        /* if a file is set this is validated, otherwise the object */
        if (instanceFile != null) {
            saxBuilder.build(instanceFile);
        } else {
            saxBuilder.build(new InputSource(new StringReader(
                    new InstanceOutputter(instance).getXMLString(Charset.defaultCharset()))));
        }
    }

    /**
     * Performs all validations this class offers. Currently, this is just the
     * validation against the calculation linkbase (NO XML Schema validation is
     * performed).
     * 
     * @throws InstanceValidationException
     * @throws XBRLException 
     */
    public final void validate() throws InstanceValidationException, XBRLException {
        validateCalculations();
    }

    /**
     * Performs a full validation of the instance against the calculation
     * linkbase of the taxonomy.
     * 
     * @throws InstanceValidationException
     * @throws CalculationValidationException
     * @throws XBRLException 
     */
    public final void validateCalculations() throws InstanceValidationException,
            CalculationValidationException, XBRLException {
        /* get all the facts */
        for (Fact currFact : instance.getFacts()) {
            validateCalculation(currFact);
        }
    }

    /**
     * Performs a validation of only one fact of the instance against the
     * calculation linkbase of the taxonomy.
     * 
     * @param fact
     *            Fact which is validated against the calculation linkbase.
     * @throws InstanceValidationException
     * @throws CalculationValidationException
     * @throws XBRLException 
     */
    public final void validateCalculation(final Fact fact)
            throws InstanceValidationException, CalculationValidationException,
            XBRLException {
        DiscoverableTaxonomySet currDTS = null;
        /* get all the taxonomies this instance refers to */
        for (DiscoverableTaxonomySet tmpDTS : instance.getDiscoverableTaxonomySet()) {
            if (tmpDTS.getConceptByID(fact.getConcept().getID()) != null) {
                currDTS = tmpDTS;
                break;
            }
        }
        if (currDTS == null) {
            String msg = "Error: Could not find taxonomy schema for fact "
                    + fact.getConcept().getName() + " in instance "
                    + instance.getFileName();
            InstanceValidationException ex = new InstanceValidationException(
                    msg);
            throw ex;
        }
        if (currDTS.getCalculationLinkbase() == null) {
            /* no calculations defined */
            return;
        }
        /*
         * check for every extended link role whether there are calculation
         * rules defined
         */
        Collection<String> extendedLinkRoleSet = currDTS.getCalculationLinkbase().getExtendedLinkRoles();
        for (String currExtendedLinkRole : extendedLinkRoleSet) {
            List<CalculationRule> calculationRules = currDTS.getCalculationLinkbase()
                    .getCalculationRules(fact.getConcept(), currExtendedLinkRole);
            if (calculationRules != null && calculationRules.size() > 0) {
                /*
                 * there are calculation rules defined for this concept, check
                 * whether the numbers are correct
                 */
                //BigDecimal expectedResult = null;
                BigDecimal calculatedResult = null;
                /*
                String factStringValue = fact.getValue();
                if ((factStringValue != null) && (factStringValue.trim().length() != 0)) {
                    try {
                        expectedResult = new BigDecimal(factStringValue.replaceAll(",", "."));
                    } catch (NumberFormatException ex) {
                        throw ex;
                    }
                }
                */
                // calculate currentResult
                @SuppressWarnings( "unused" )
                boolean isMissingFact = false;
                for (CalculationRule currRule : calculationRules) {
                    Concept tmpConcept = currRule.getConcept();
                    float currWeight = currRule.getWeight();

                    /* get the value of this fact */
                    BigDecimal newValue = new BigDecimal(.0F);
                    Fact newFact = instance.getFact(tmpConcept, fact.getInstanceContext());
                    if (newFact == null) {
                        isMissingFact = true;
                        String factContextID = fact.getInstanceContext().getID();
                        System.out.println("INFO!!! validateCalculation({" + currExtendedLinkRole + "}" + fact.getConcept().getID() + "[" + factContextID +"]): Missing " + tmpConcept.getID() + "[" + factContextID + "]");
                        continue;
                        /* TODO:
                        CalculationValidationException ex = new CalculationValidationException("");
                        ex.setDts(currDTS);
                        ex.setMissingValues(true);
                        ex.setMissingConcept(tmpConcept);
                        throw ex;
                        */
                    }
                    //String newFactStringValue = newFact.getValue();
                    /*if ((newFactStringValue != null) && (newFactStringValue.trim().length() != 0))*/ {
                        /*
                        try {
                            newValue = new BigDecimal(newFactStringValue.trim().replaceAll(",", "."));
                        } catch (NumberFormatException ex) {
                            throw ex;
                        }
                        */
                        // calculate newValue to calculatedResults
                        if (calculatedResult == null) {
                            calculatedResult = new BigDecimal(.0F);
                        }
                        calculatedResult = calculatedResult.add(newValue
                            .multiply(new BigDecimal(currWeight)));
                    }
                }
                /*
                // now compare both results
                if (expectedResult != null
                        && calculatedResult != null
                        && expectedResult.compareTo(calculatedResult) != 0)
                    System.out.println("ERROR!!! validate Calculation failed{" + currExtendedLinkRole + "}: " + fact.getConcept().getId() + "[" + fact.getInstanceContext().getId() +"] = " + expectedResult + " is NOT Equal To " + calculatedResult);
                */
            }
        }
    }

}
