package xbrlcore.linkbase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import xbrlcore.taxonomy.Concept;
import xbrlcore.xlink.Locator;

/**
 * For each Concept in a taxonomy which appears in the presentation linkbase,
 * there is one corresponding PresentationLinkbaseElement. This class represents
 * a concept within the presentation linkbase and contains additional
 * information as for example all successors. <br/><br/>
 * 
 * @author Daniel Hamm
 * 
 */

/* TODO: This class could be changed to a generic LinkbaseElement */
public class PresentationLinkbaseElement implements Serializable {

    private static final long serialVersionUID = -8410690195993834311L;

    private final String extendedLinkRole;

    private final Locator locator;

    private Concept parentElement;

    private int level;

    private int numSuccessorAtDeepestLevel;

    private List<Concept> successorElements;

    private int positionDeepestLevel;

    /**
     * Constructor.
     * 
     * @param extendedLinkRole
     * @param locator
     *            XBRL element this PresentationLinkbaseElement refers to.
     */
    public PresentationLinkbaseElement(String extendedLinkRole, Locator locator) {
        this.extendedLinkRole = extendedLinkRole;
        this.locator = locator;
        parentElement = null;
        level = 0;
        numSuccessorAtDeepestLevel = 0;
        successorElements = new ArrayList<Concept>();
        positionDeepestLevel = -1;
    }

    /**
     * @return the extendedLinkRole.
     */
    public final String getExtendedLinkRole() {
        return extendedLinkRole;
    }
    
    /**
     * @return the locator.
     */
    public final Locator getLocator() {
        return locator;
    }

    /**
     * @return XBRL element of the current element.
     */
    public final Concept getConcept() {
        return locator.getConcept();
    }

    /**
     * @return XBRL element of the parent element.
     */
    public final Concept getParentElement() {
        return parentElement;
    }

    /**
     * @param element
     *            XBRL element of the parent element.
     */
    public void setParentElement(Concept element) {
        parentElement = element;
    }

    /**
     * @return List of Concept objects of the successor elements.
     */
    public final List<Concept> getSuccessorElements() {
        return successorElements;
    }

    /**
     * @param list
     *            List of Concept objects of the successor elements.
     */
    public void setSuccessorElements(List<Concept> list) {
        successorElements = list;
    }

    /**
     * @return Number of direct successors.
     */
    public final int getNumDirectSuccessor() {
        return successorElements.size();
    }

    /**
     * @return Number of successors at the deepest level.
     */
    public final int getNumSuccessorAtDeepestLevel() {
        return numSuccessorAtDeepestLevel;
    }

    /**
     * @param i
     *            Number of successors at the deepest level.
     */
    public void setNumSuccessorAtDeepestLevel(int i) {
        numSuccessorAtDeepestLevel = i;
    }

    /**
     * @return If this element is at the deepest level, this is its position at
     *         the deepest level. If it is not at the deepest level, -1 is
     *         returned.
     */
    public final int getPositionDeepestLevel() {
        return positionDeepestLevel;
    }

    /**
     * @param i
     *            This is the position of this element at the deepest level (if
     *            it is at the deepest level).
     */
    void setPositionDeepestLevel(int i) {
        positionDeepestLevel = i;
    }

    /**
     * @return Level of the current element.
     */
    public final int getLevel() {
        return level;
    }

    /**
     * @param i
     *            Level of the current element.
     */
    public void setLevel(int i) {
        level = i;
    }
}
