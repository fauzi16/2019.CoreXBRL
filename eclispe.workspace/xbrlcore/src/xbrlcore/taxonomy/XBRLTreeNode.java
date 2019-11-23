package xbrlcore.taxonomy;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Simple {@link DefaultMutableTreeNode} extension, that is used by {@link SwingTreeGenerator}.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class XBRLTreeNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 2271393725463974103L;

    private final LabelExtractor lx;

    public XBRLTreeNode(LabelExtractor lx, Object xbrlObj) {
        super(xbrlObj);

        this.lx = lx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return lx.getLabel(getUserObject());
    }
}
