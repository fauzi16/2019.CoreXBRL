package xbrlcore.taxonomy;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class ConceptTypeRestriction implements Serializable {

    private static final long serialVersionUID = 8093395401348703362L;

    private final ConceptTypes type;
    private final String patternString;
    private transient Pattern pattern = null;
    private final String attributeGroup;

    public ConceptTypeRestriction(String type, String pattern, String attributeGroup) {
        this.type = ConceptTypes.getFromString(type);
        this.patternString = pattern;
        this.attributeGroup = attributeGroup;
    }

    public final ConceptTypes getType() {
        return type;
    }

    public final String getPatternString() {
        return patternString;
    }

    public final Pattern getPattern() {
        if (patternString == null )
            return null;

        if (pattern == null) {
            pattern = Pattern.compile(patternString);
        }

        return pattern;
    }

    public final String getAttributeGroup() {
        return attributeGroup;
    }
}
