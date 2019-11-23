package xbrlcore.linkbase;

import java.io.Serializable;

import xbrlcore.taxonomy.Concept;

/**
 * Defines the calculation rules for a {@link Concept} from {@link CalculationLinkbase}.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class CalculationRule implements Serializable {

    private static final long serialVersionUID = -2401762022257641781L;

    private final Concept concept;
    private final float weight;

    protected CalculationRule(Concept concept, float weight) {
        this.concept = concept;
        this.weight = (weight == -0.0f ? 0.0f : weight);
    }

    public final Concept getConcept() {
        return ( concept );
    }

    public final float getWeight() {
        return ( weight );
    }
}
