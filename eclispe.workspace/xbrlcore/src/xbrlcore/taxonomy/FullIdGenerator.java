package xbrlcore.taxonomy;

import java.util.Stack;

/**
 * This class generates full-IDs for concepts in the context of an arc-tree.
 * Concepts can appear as duplicates in an arc-tree, if contained in different
 * sequence-tuples. These concepts are identified in their scope by prefixing
 * their IDs with the sequence names.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 * 
 * @param <N> the node type
 */
public abstract class FullIdGenerator<N> {
    private final String pathDelimiter;

    private final Stack<String> contextStack = new Stack<String>();

    protected FullIdGenerator(String pathDelimiter) {
        this.pathDelimiter = pathDelimiter;
    }

    /**
     * Gets a {@link Concept} from the passed node.
     * This method must never return <code>null</code>.
     * 
     * @param node
     * 
     * @return the {@link Concept} for the given node (never <code>null</code>).
     */
    protected abstract Concept getConcept(N node);

    /**
     * Applies the full-ID to the passed node.
     * 
     * @param fullId the full-ID to apply
     * @param node the target node
     */
    protected abstract void applyFullId(String fullId, N node);

    /**
     * Gets the number of child nodes for the passed node.
     * 
     * @param parent the parent node
     * 
     * @return the number of child nodes for the passed node.
     */
    protected abstract int getNumChildren(N parent);

    /**
     * Gets the i-th child node for the passed node.
     * 
     * @param parent the parent node
     * @param index the index of the child
     * 
     * @return the i-th child node for the passed node.
     */
    protected abstract N getChild(N parent, int index);

    private void buildFullIds(Stack<String> context, N node) {
        Concept concept = getConcept(node);
        boolean isSequence = ((concept instanceof TupleDefinition) && ((TupleDefinition)concept).getTupleType().isMulti());

        String fullId = concept.getName();

        for (int i = context.size() - 1; i >= 0; i--)
            fullId = context.get( i ) + pathDelimiter + fullId;

        applyFullId(fullId, node);

        if (isSequence)
            context.push(concept.getName());

        int n = getNumChildren(node);
        for (int i = 0; i < n; i++)
            buildFullIds(context, getChild(node, i));

        if (isSequence)
            context.pop();
    }

    /**
     * This method is called right before the build is started.
     */
    protected void prepareBuild() {
        contextStack.clear();
    }

    /**
     * Generates one unique full-ID for each node in the tree.
     * 
     * @param node the parent node
     */
    public final void buildFullIds(N node) {
        prepareBuild();

        buildFullIds(contextStack, node);
    }
}
