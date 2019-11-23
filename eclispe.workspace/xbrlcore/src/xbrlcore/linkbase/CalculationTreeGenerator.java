package xbrlcore.linkbase;

import java.util.List;

import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.LabelExtractor;
import xbrlcore.taxonomy.TreeGenerator;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;

/**
 *
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class CalculationTreeGenerator extends TreeGenerator<CalculationRuleNode> {

    private CalculationRuleNode[] roots = null;

    public CalculationTreeGenerator() {
        super(null);
    }

    public CalculationTreeGenerator(LabelExtractor lx) {
        super(lx);
    }

    /**
     * 
     * @param lb
     * @param id
     * @param xle
     * @param concept
     * @param lx
     * @param weight
     * @param isWeighted
     * @return the created node
     */
    protected CalculationRuleNode createNode(Linkbase lb, Object id, ExtendedLinkElement xle, Concept concept, LabelExtractor lx, float weight, boolean isWeighted) {
        if (concept == null)
            return null;

        return new CalculationRuleNode(id, concept, weight, isWeighted);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final CalculationRuleNode createNode(Linkbase lb, ExtendedLinkElement xle, LabelExtractor lx, float weight, boolean isWeighted) {
        return createNode(lb, null, xle, ((Locator)xle).getConcept(), lx, weight, isWeighted);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachNode(CalculationRuleNode parent, CalculationRuleNode child) {
        if (child != null) {
            parent.addChild(child);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setNodeType(CalculationRuleNode node, xbrlcore.taxonomy.TreeGenerator.NodeHierarchyType hierType) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sortNodes(List<CalculationRuleNode> nodes) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachRoots(Linkbase lb, List<CalculationRuleNode> roots) {
        this.roots = roots.toArray(new CalculationRuleNode[roots.size()]);
    }

    public final CalculationRuleNode[] getRoots() {
        return roots;
    }
}
