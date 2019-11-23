package xbrlcore.instance;

import java.io.Serializable;

import xbrlcore.exception.InstanceException;
import xbrlcore.taxonomy.Concept;

/**
 * This class represents a Fact within an XBRL instance document as it is
 * described by the XBRL 2.1 Specification, which can be obtained from
 * http://www.xbrl.org/SpecRecommendations/. <br/><br/>
 * 
 * @author Daniel Hamm
 */
public class Fact implements Serializable {

    private static final long serialVersionUID = -8027402116197248526L;

    private final Concept concept;

    private InstanceContext instanceContext = null;

    private InstanceUnit instanceUnit = null;

    private Integer decimals = null;

    private Integer precision = null;

    private Object value;

    private String id = null;

    /**
     * Constructor.
     * 
     * @param concept
     *            Concept this fact refers to.
     * @exception InstanceException
     *                This exception is thrown if the concept is NULL or
     *                abstract.
     */
    public Fact(Concept concept) throws InstanceException {
        this(concept, null);
    }

    /**
     * Constructor.
     * 
     * @param concept
     *            Concept this fact refers to.
     * @param value
     *            Value of the fact.
     * @exception InstanceException
     *                This exception is thrown if the concept is NULL or
     *                abstract.
     *  
     */
    /*
     * TODO: It is not checked whether the value is compliant to the data type
     * of the concept!
     */
    public Fact(Concept concept, Object value) throws InstanceException {
        if (concept == null || concept.isAbstract()) {
            throw new InstanceException(
                    "Fact cannot be created: Concept must be not null and not abstract");
        }

        this.concept = concept;
        this.value = value;
    }

    /**
     * 
     * Returns whether two Fact objects are equal or not. They are equal if and
     * only if: <br/>- the XBRL concepts they refer to are equal <br/>- the
     * units they refer to are equal <br/>- the instance contexts they refer to
     * are equal <br/>
     * 
     * @return True if both objects are the same, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Fact))
            return false;
        Fact otherFact = (Fact) obj;
        return concept.equals(otherFact.getConcept())
                && (instanceContext == null ? otherFact.getInstanceContext() == null
                        : instanceContext
                                .equals(otherFact.getInstanceContext()))
                && (instanceUnit == null ? otherFact.getInstanceUnit() == null
                        : instanceUnit.equals(otherFact.getInstanceUnit()));
    }

    /**
     * @return Returns a hash code of this object.
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + concept.hashCode();
        hash = hash * 31
                + (instanceContext != null ? instanceContext.hashCode() : 0);
        hash = hash * 31 + (instanceUnit != null ? instanceUnit.hashCode() : 0);
        return hash;
    }

    /**
     * 
     * @return Concept this fact refers to.
     */
    public Concept getConcept() {
        return concept;
    }

    /**
     * 
     * @return Value of the attribute "decimals" of this fact.
     */
    public int getDecimals() {
        return decimals;
    }

    final boolean hasDecimals() {
        return decimals != null;
    }

    /**
     * Sets the value of the "decimals" attribute of this fact. According to the
     * XBRL 2.1 Spec., a decimal attribute can only be set for numeric items. It
     * must be an integer or the String "INF".
     * 
     * @param decimals
     *            Value of the attribute "decimals" of this fact.
     * @throws InstanceException
     *             Thrown if the type of the attribute is not compliant to the
     *             XBRL 2.1 Spec.
     */
    public void setDecimals(int decimals) throws InstanceException {
        if (!concept.isNumericItem()) {
            throw new InstanceException(
                    "Decimals attribute can only be set for numeric items");
        }
        this.decimals = decimals;
        this.precision = null;
    }

    /**
     * 
     * @return Value of this fact.
     */
    public Object getValue() {
        return value;
    }

    /**
     * 
     * @param value
     *            Value of this fact.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * 
     * @return id of this fact.
     */
    public String getID() {
        return id;
    }

    /**
     * 
     * @param id
     *            id of this fact.
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * 
     * @return The context this fact refers to.
     */
    public InstanceContext getInstanceContext() {
        return instanceContext;
    }

    /**
     * 
     * @param instanceContext
     *            The context this fact refers to.
     */
    public void setInstanceContext(InstanceContext instanceContext) {
        this.instanceContext = instanceContext;
    }

    /**
     * 
     * @return ID of the context this fact refers to.
     */
    public String getContextRef() {
        return (instanceContext != null ? instanceContext.getID() : null);
    }

    /**
     * @return Unit this fact refers to.
     */
    public InstanceUnit getInstanceUnit() {
        return instanceUnit;
    }

    /**
     * According to the XBRL 2.1 Spec., a unit can only be set to a numeric
     * item.
     * 
     * @param unit
     *            Unit this fact refers to.
     * @throws InstanceException
     *             Thrown if a unit is set to a not numeric item.
     */
    public void setInstanceUnit(InstanceUnit unit) throws InstanceException {

        if (!concept.isNumericItem()) {
            throw new InstanceException(
                    "A unit can only be set for numeric items: "
                            + concept.getID() + " is not numeric");
        }
        instanceUnit = unit;

    }

    /**
     * @return Returns the precision.
     */
    public int getPrecision() {
        return precision;
    }

    final boolean hasPrecision() {
        return precision != null;
    }

    /**
     * Sets the value of the "precision" attribute. According to the XBRL 2.1
     * Spec., this attribute can only be set for numeric items and must be an
     * integer greater than or equal 0. The String "INF" is valid as well.
     * 
     * @param precision
     *            The precision to set.
     * @throws InstanceException
     *             Thrown if the precision attribute is not compliant to the
     *             XBRL 2.1 Spec.
     */
    public void setPrecision(int precision) throws InstanceException {
        if (!concept.isNumericItem()) {
            throw new InstanceException(
                    "Precision attribute can only be set for numeric items: "
                            + concept.getID() + " is not numeric");
        }
        this.precision = precision;
        this.decimals = null;
    }
}
