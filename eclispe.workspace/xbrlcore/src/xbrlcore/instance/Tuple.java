package xbrlcore.instance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import xbrlcore.exception.InstanceException;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.TupleDefinition;

/**
 * This class represents a Tuple within an XBRL instance document as it is
 * described by the XBRL 2.1 Specification, which can be obtained from
 * http://www.xbrl.org/SpecRecommendations/. <br/><br/>
 * 
 * @author Donny Zhang
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class Tuple implements Serializable {

    private static final long serialVersionUID = -6417452592594243589L;

    private final TupleDefinition tupleDef;

    private InstanceContext instanceContext = null;

    private final Map<Concept, Object> selection = new HashMap<Concept, Object>();

    /**
     * Constructor.
     * 
     * @param tupleDef
     *            TupleDefinition this tuple refers to
     * @exception InstanceException
     *                This exception is thrown if the concept is NULL or
     *                abstract.
     */
    public Tuple(TupleDefinition tupleDef) throws InstanceException {
        if (tupleDef == null || tupleDef.isAbstract()) {
            throw new InstanceException(
                    "Tuple cannot be created: TupleDefinition (Concept) must not be null or abstract");
        }
        this.tupleDef = tupleDef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + tupleDef.hashCode();
        //hash = hash * 31 + (id != null ? id.hashCode() : 0);
        hash = hash * 31 + (selection != null ? selection.hashCode() : 0);
        //hash = hash * 31 + (factSet != null ? factSet.hashCode() : 0);
        //hash = hash * 31 + (tupleSet != null ? tupleSet.hashCode() : 0);
        return hash;
    }

    /**
     * 
     * @return TupleDefinition this tuple refers to.
     */
    public final TupleDefinition getDefinition() {
        return tupleDef;
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
     * 
     * @return selection of this tuple.
     */
    public final Map<Concept, Object> getSelection() {
        return selection;
    }

    /**
     * 
     * @param item
     *            selected item
     * @param value
     *            value for this selected item
     */
    private void _addSelection(Concept item, Object value) {
        if (selection.containsKey(item)) {
            Object oldValue = selection.get(item);
            if (oldValue == null) {
                if (value != null)
                    throw new IllegalStateException("The passed item is already part of this tuple's selection.");
            } else if (!oldValue.equals(value)) {
                throw new IllegalStateException("The passed item is already part of this tuple's selection.");
            }
        } else {
            selection.put(item, value);
        }
    }

    /**
     * 
     * @param item
     *            selected item
     * @param value
     *            value for this selected item
     */
    public void addSelection(Concept item, Fact value) {
        _addSelection(item, value);
    }

    /**
     * 
     * @param item
     *            selected item
     * @param value
     *            value for this selected item
     */
    public void addSelection(Concept item, Tuple value) {
        _addSelection(item, value);
    }

    /**
     * 
     * @param item
     *            selected item
     */
    public void addSelection(Concept item) {
        _addSelection(item, null);
    }
}
