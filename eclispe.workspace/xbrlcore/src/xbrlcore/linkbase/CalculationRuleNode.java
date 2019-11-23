package xbrlcore.linkbase;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xbrlcore.taxonomy.Concept;

/**
 * Defines the calculation rules for a {@link Concept} from {@link CalculationLinkbase}
 * with the extension to {@link CalculationRule} of tree node information (children).
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class CalculationRuleNode extends CalculationRule implements Iterable<CalculationRuleNode> {

    private static final long serialVersionUID = 5942748944839492341L;

    public static interface DirtyNodeNotifier {
        public void notify(CalculationRuleNode node);
    }

    private final boolean isWeighted;

    private double value = 0.0;
    private double weightedValue = 0.0;
    private boolean isUserValue = false;
    private boolean isNull = true;
    private boolean dirty = true;

    private Object id;

    private CalculationRuleNode parent = null;
    private final List<CalculationRuleNode> children = new ArrayList<CalculationRuleNode>();

    protected CalculationRuleNode(Object id, Concept concept, float weight, boolean isWeighted) {
        super(concept, weight);

        this.isWeighted = isWeighted;
        this.id = id;
    }

    public void setID(Object id) {
        this.id = id;
    }

    public final Object getID() {
        return id;
    }

    /**
     * @return id
     * @deprecated replaced by {@link #getID()} for streamlining reasons.
     */
    @Deprecated
    public final Object getId() {
        return getID();
    }

    public final boolean isWeighted() {
        return isWeighted;
    }

    public final boolean hasUserValue() {
        return isUserValue;
    }

    public final boolean isNull() {
        calculate(false);

        return isNull;
    }

    public final CalculationRuleNode getParent() {
        return parent;
    }

    protected void addChild(CalculationRuleNode child) {
        children.add(child);
        child.parent = this;
    }

    public final int getNumChildren() {
        return children.size();
    }

    public final boolean hasChildren() {
        return (children.size() > 0);
    }

    public final CalculationRuleNode getChild(int index) {
        return children.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<CalculationRuleNode> iterator() {
        return children.iterator();
    }

    private void setDirty(DirtyNodeNotifier notifier) {
        this.dirty = true;

        if (notifier != null) {
            notifier.notify(this);
        }

        if (parent != null) {
            parent.setDirty(notifier);
        }
    }

    public void resetValue(DirtyNodeNotifier notifier) {
        this.value = 0.0;
        this.weightedValue = 0.0;
        this.isNull = true;
        this.isUserValue = false;
        this.dirty = hasChildren();

        if (parent != null) {
            parent.setDirty(notifier);
        }
    }

    public final void resetValue() {
        resetValue(null);
    }

    public void setValue(double value, boolean useWeight, DirtyNodeNotifier notifier) {
        
        this.value = value;
        this.weightedValue = (useWeight && isWeighted()) ? value * getWeight() : value;
        if (this.value == -0.0)
            this.value = 0.0;
        if (this.weightedValue == -0.0)
            this.weightedValue = 0.0;
        if (hasChildren()) {
            this.isUserValue = true;
        }
        this.isNull = false;
        this.dirty = false;

        if (parent != null) {
            parent.setDirty(notifier);
        }
    }

    public final void setValue(double value) {
        setValue(value, false, null);
    }

    public final double getValue() {
        return value;
    }

    public final double getValueForAggregation() {
        return weightedValue;
    }

    public double calculate(boolean force) {
        if (!isUserValue && hasChildren() && (dirty || force)) {
            this.value = 0.0;
            this.isNull = true;

            for (int i = 0; i < children.size(); i++) {
                CalculationRuleNode child = children.get(i);
                if (child.isWeighted()) {
                    child.calculate(force);
                    this.value += child.getValueForAggregation();
                    if (this.isNull && !child.isNull)
                        this.isNull = false;
                }
            }

            if (isWeighted()) {
                this.weightedValue = this.value * getWeight();
            } else {
                this.weightedValue = this.value;
            }

            if (this.value == -0.0)
                this.value = 0.0;

            if (this.weightedValue == -0.0)
                this.weightedValue = 0.0;

            this.dirty = false;
        }

        return value;
    }

    public final double getCalculatedValue() {
        return calculate(false);
    }

    public void dump(int indent, PrintStream ps) {
        for (int i = 0; i < indent; i++)
            ps.print("  ");

        if (isWeighted())
            calculate(false);

        ps.println(getID() + ": " + getValue() + ( isWeighted() ? " (" + getValue() + " * " + getWeight() + " = " + getValueForAggregation() + ")" : "" ) + "; " + (isUserValue ? "USER_VALUE" : "CALCULATED_VALUE") + (dirty ? ", DIRTY" : ""));
        
        if (hasChildren()) {
            for (int i = 0; i < children.size(); i++) {
                children.get(i).dump(indent + 1, ps);
            }
        }
    }

    public final void dump() {
        dump(0, System.out);
    }
}
