package xbrlcore.taxonomy;

/**
 * List of known concept types.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public enum ConceptTypes {

    STRING("xbrli:stringItemType", false, false),
    
    DECIMAL("xbrli:decimalItemType", false, true),
    FRACTION("xbrli:fractionItemType", true, true),
    FLOAT("xbrli:floatItemType", true, true),
    DOUBLE("xbrli:doubleItemType", true, true),
    INTEGER("xbrli:integerItemType", false, true),
    NON_POSITIVE_INTEGER("xbrli:nonPositiveIntegerItemType", false, true),
    NEGATIVE_INTEGER("xbrli:negativeIntegerItemType", false, true),
    POSITIVE_INTEGER("xbrli:positiveIntegerItemType", false, true),
    NON_NEGATIVE_INTEGER("xbrli:nonNegativeIntegerItemType", false, true),
    UNSIGNED_LONG("xbrli:unsignedLongItemType", false, true),
    UNSIGNED_INT("xbrli:unsignedIntItemType", false, true),
    LONG("xbrli:longItemType", false, true),
    INT("xbrli:intItemType", false, true),
    SHORT("xbrli:shortItemType", false, true),
    UNSIGNED_SHORT("xbrli:unsignedShortItemType", false, true),
    BYTE("xbrli:byteItemType", false, true),
    UNSIGNED_BYTE("xbrli:unsignedByteItemType", false, true),
    
    BOOLEAN("xbrli:booleanItemType", false, false),
    DATE("xbrli:dateItemType", false, false),
    
    MONETARY("xbrli:monetaryItemType", false, true),
    NON_NEGATIVE_MONETARY("dt:nonNegativeMonetaryItemType", false, true),
    SHARES("xbrli:sharesItemType", false, true),
    PURE("xbrli:pureItemType", false, true),
    
    MULTIPLE_FACTOR("p-mi:multiplicationFactorItemType", false, true),
    NUMBER_OF_OVERSHOOTINGS("p-mi:numberOfOvershootingsItemType", false, true),
    
    DOMAIN_ITEM_TYPE("nonnum:domainItemType", false, false),
    
    QNAME("xbrli:QNameItemType", false, false),

    GYEAR("", false, true),
    LANGUAGE("", false, false),

    UNDEFINED("undefined", false, false),
    ;

    private final String string;
    private final boolean fractional;
    private final boolean numeric;

    public final String getString() {
        return string;
    }

    public final boolean isFractional() {
        return fractional;
    }

    public final boolean isNumeric() {
        return numeric;
    }

    private ConceptTypes(String string, boolean fractional, boolean numeric) {
        this.string = string;
        this.fractional = fractional;
        this.numeric = numeric;
    }

    public static ConceptTypes getFromString(String string) {
        // TODO: try to find a better method to determine type for inherited types
        // c.f corep concept p-mi_NumberOfOvershootingsDuringPrevious250WorkingDays from p-mi-2006-07-01.xsd
        //           that is p-mi:numberOfOvershootingsItemType inherited from xbrli:nonNegativeIntegerItemType
        if (string != null) {
            for ( ConceptTypes type : ConceptTypes.values() ) {
                if ( type.string.equals( string ) )
                    return type;
            }
        }

        return UNDEFINED;
    }
}
