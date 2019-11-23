package xbrlcore.taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xbrlcore.linkbase.Linkbase;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.CalculationArc;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;

/**
 * Generates a tree hierarchy from an {@link Arc}s list's parent-child-structure.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 * 
 * @param <N> the node type
 */
public abstract class TreeGenerator<N> {
    public static enum NodeHierarchyType {
        ROOT,
        GROUP,
        LEAF,
        ;
    }

    private final LabelExtractor lx;

    public TreeGenerator(LabelExtractor lx) {
        this.lx = lx;
    }

    public final LabelExtractor getLabelExtractor() {
        return lx;
    }

    /**
     * Creates a single node instance.
     * 
     * @param lb
     * @param xle
     * @param lx
     * @param weight
     * @param isWeighted
     * 
     * @return the node.
     */
    protected abstract N createNode(Linkbase lb, ExtendedLinkElement xle, LabelExtractor lx, float weight, boolean isWeighted);

    /**
     * Attaches a child node to a parent node.
     * 
     * @param parent
     * @param child
     */
    protected abstract void attachNode(N parent, N child);

    protected abstract void setNodeType(N node, NodeHierarchyType hierType);

    private void setNodeTypes(Map<String, N> rootNodesMap, Map<String, N> elemNodeMap, Map<String, N> leafNodesMap) {
        for (N node : rootNodesMap.values()) {
            if (node != null)
                setNodeType(node, NodeHierarchyType.ROOT);
        }

        for (String label : elemNodeMap.keySet()) {
            N node = elemNodeMap.get(label);

            if ((node != null) && (rootNodesMap.get(label) == null) && (leafNodesMap.get(label) == null))
                setNodeType(node, NodeHierarchyType.GROUP);
        }

        for (N node : leafNodesMap.values()) {
            if (node != null)
                setNodeType(node, NodeHierarchyType.LEAF);
        }
    }

    /**
     * Sorts the list of nodes (optional operation).
     * 
     * @param nodes
     */
    protected abstract void sortNodes(List<N> nodes);

    /**
     * Attaches the root nodes to the tree.
     * 
     * @param lb
     * @param roots
     */
    protected abstract void attachRoots(Linkbase lb, List<N> roots);

    /**
     * If this method returns <code>true</code> and {@link #createNode(Linkbase, ExtendedLinkElement, LabelExtractor, float, boolean)}
     * returns <code>null</code>, this node and all its children are ignored for further processing.
     * In this case you need to deal with null nodes in {@link #attachNode(Object, Object)}.
     * 
     * @return whether to ignore arc elements, for which {@link #createNode(Linkbase, ExtendedLinkElement, LabelExtractor, float, boolean)}
     * returned <code>null</code>.
     */
    protected boolean ignoreNullArcElements() {
        return true;
    }

    /**
     * Creates the tree hierarchy.
     * 
     * @param lb
     * @param arcs
     */
    public void createHierarchy(Linkbase lb, Collection<Arc> arcs) {
        HashMap<String, N> elemNodeMap = new HashMap<String, N>();
        HashMap<String, N> rootNodesMap = new HashMap<String, N>();
        HashMap<String, N> leafNodesMap = new HashMap<String, N>();

        final boolean ignoreNullArcElements = ignoreNullArcElements();

        for (Arc arc : arcs) {
            ExtendedLinkElement src = arc.getSourceElement();
            ExtendedLinkElement trg = arc.getTargetElement();

            // Create a node instance for the source element, if not already exists.
            N n0 = elemNodeMap.get(src.getLabel());
            if (!elemNodeMap.containsKey(src.getLabel())) {
                n0 = createNode(lb, src, lx, 1.0f, false);
                elemNodeMap.put(src.getLabel(), n0);
            }

            if ((n0 != null) || !ignoreNullArcElements) {
                if (leafNodesMap.containsKey(src.getLabel()))
                    leafNodesMap.put(src.getLabel(), null);

                // Create a node instance for the target element, if not already exists.
                N n1 = elemNodeMap.get(trg.getLabel());
                if (!elemNodeMap.containsKey(trg.getLabel()) || ((trg instanceof Locator) && (((Locator)trg).getConcept() != null) && ((Locator)trg).getConcept().isTupleItem())) {
                    n1 = createNode(lb, trg, lx, arc.getWeightAttribute(), (arc instanceof CalculationArc));
                    elemNodeMap.put(trg.getLabel(), n1);
                }

                if (n1 != null) {
                    if (!leafNodesMap.containsKey(trg.getLabel()))
                        leafNodesMap.put(trg.getLabel(), n1);
                }

                // Attach the target element's node to the source element's node.
                attachNode(n0, n1);

                // Record root nodes...
                if ((n0 != null) && !rootNodesMap.containsKey(src.getLabel()))
                    rootNodesMap.put(src.getLabel(), n0);

                if (n1 != null)
                    rootNodesMap.put(trg.getLabel(), null);
            }
        }

        setNodeTypes(rootNodesMap, elemNodeMap, leafNodesMap);

        // Now extract the recorded and remaining root nodes.

        ArrayList<N> roots = new ArrayList<N>();

        for (N n0 : rootNodesMap.values()) {
            if (n0 != null)
                roots.add(n0);
        }

        // ...sort them...
        sortNodes(roots);

        // ...and attach them to the tree.
        attachRoots(lb, roots);
    }

    public void sortElements(List<ExtendedLinkElement> elems) {
        Comparator<ExtendedLinkElement> EXT_LINK_ELEM_COMPARATOR = new Comparator<ExtendedLinkElement>() {
            @Override
            public int compare(ExtendedLinkElement xle1, ExtendedLinkElement xle2) {
                String lbl1 = (lx == null) ? xle1.getLabel() : lx.getLabel(xle1);
                String lbl2 = (lx == null) ? xle2.getLabel() : lx.getLabel(xle2);

                if (lbl1 == null) {
                    if (lbl2 == null)
                        return 0;

                    return -1;
                }

                if (lbl2 == null)
                    return +1;

                return lbl1.compareToIgnoreCase(lbl2);
            }
        };

        Collections.sort(elems, EXT_LINK_ELEM_COMPARATOR);
    }

    /**
     * Finds root elements.
     * 
     * @param arcs
     * @param sort
     * 
     * @return the list of root elements.
     */
    public List<ExtendedLinkElement> findRoots(Collection<Arc> arcs, boolean sort) {
        HashMap<String, ExtendedLinkElement> rootNodesMap = new HashMap<String, ExtendedLinkElement>();

        for (Arc arc : arcs) {
            ExtendedLinkElement src = arc.getSourceElement();
            ExtendedLinkElement trg = arc.getTargetElement();

            // Record root nodes...
            if (!rootNodesMap.containsKey(src.getLabel()))
                rootNodesMap.put(src.getLabel(), src);
            rootNodesMap.put(trg.getLabel(), null);
        }

        // Now extract the recorded and remaining root nodes.

        ArrayList<ExtendedLinkElement> roots = new ArrayList<ExtendedLinkElement>();

        for (ExtendedLinkElement elem : rootNodesMap.values()) {
            if (elem != null)
                roots.add(elem);
        }

        // ...sort them...
        if (sort) {
            sortElements(roots);
        }

        // ...and return the sorted list.
        return roots;
    }
}
