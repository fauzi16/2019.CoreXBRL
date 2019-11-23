package xbrlcore.taxonomy;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;

import xbrlcore.linkbase.Linkbase;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.ExtendedLinkElement;

/**
 * {@link TreeGenerator} implementation to generate a tree structure for a Swing {@link JTree}.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class SwingTreeGenerator extends TreeGenerator<XBRLTreeNode> {

    private MutableTreeNode root;

    public SwingTreeGenerator(LabelExtractor lx) {
        this( lx, null );
    }

    public SwingTreeGenerator(LabelExtractor lx, MutableTreeNode root) {
        super(lx);

        this.root = root;
    }

    public void setRoot(MutableTreeNode root) {
        this.root = root;
    }

    public final MutableTreeNode getRoot() {
        return root;
    }

    @Override
    protected XBRLTreeNode createNode(Linkbase lb, ExtendedLinkElement xle, LabelExtractor lx, float weight, boolean isWeighted) {
        return new XBRLTreeNode(lx, xle);
    }

    @Override
    protected void setNodeType(XBRLTreeNode node, NodeHierarchyType hierType) {
    }

    @Override
    protected void attachNode(XBRLTreeNode parent, XBRLTreeNode child) {
        if ((child != null) && (child.getParent() == null)) {
            parent.add(child);
        }
    }

    @Override
    protected void sortNodes(List< XBRLTreeNode > nodes) {
        Collections.sort(nodes, new Comparator<XBRLTreeNode>() {
            @Override
            public int compare(XBRLTreeNode o1, XBRLTreeNode o2) {
                ExtendedLinkElement ele1 = (ExtendedLinkElement)o1.getUserObject();
                ExtendedLinkElement ele2 = (ExtendedLinkElement)o2.getUserObject();

                return ele1.getLabel().compareToIgnoreCase(ele2.getLabel());
            }
        });
    }

    @Override
    protected void attachRoots(Linkbase lb, List< XBRLTreeNode > roots) {
        for (XBRLTreeNode tn : roots) {
            if ((tn != null) && (tn.getParent() == root))
                root.insert(tn, root.getChildCount() - 1);
            else
                root.insert(tn, root.getChildCount());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createHierarchy(Linkbase lb, Collection< Arc > arcs) {
        if (root == null)
            throw new IllegalStateException("root must not be null when creating the hierarchy.");

        super.createHierarchy(lb, arcs);
    }

    /**
     * Creates the tree hierarchy.
     * 
     * @param lb
     * @param arcs
     * @param root
     */
    public void createHierarchy(Linkbase lb, Collection< Arc > arcs, MutableTreeNode root) {
        if (root == null)
            throw new IllegalArgumentException("root must not be null when creating the hierarchy.");

        MutableTreeNode oldRoot = this.root;

        this.root = root;

        super.createHierarchy(lb, arcs);

        this.root = oldRoot;
    }
}
