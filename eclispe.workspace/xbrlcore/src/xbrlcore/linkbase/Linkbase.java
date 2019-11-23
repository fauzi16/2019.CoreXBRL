package xbrlcore.linkbase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import xbrlcore.constants.GeneralConstants;
import xbrlcore.exception.TaxonomyCreationException;
import xbrlcore.exception.XBRLException;
import xbrlcore.taxonomy.Concept;
import xbrlcore.taxonomy.DiscoverableTaxonomySet;
import xbrlcore.xlink.Arc;
import xbrlcore.xlink.ExtendedLinkElement;
import xbrlcore.xlink.Locator;
import xbrlcore.xlink.Resource;

/**
 * This class is a superclass for all classes implementing linkbases. It
 * provides basic linkbase methods each linkbase has to implement. It has to
 * manage a list of xbrlcore.xlink.ExtendedLinkElement and xbrlcore.xlink.Arc
 * objects. <br/><br/>
 * 
 * @author Daniel Hamm
 */
public abstract class Linkbase implements Serializable {

	private static final long serialVersionUID = 6720994841702824990L;

    public static final String DEFAULT_LINK_ROLE = GeneralConstants.XBRL_LINKBASE_DEFAULT_LINKROLE;

	private final DiscoverableTaxonomySet dts; // the taxonomy this linkbase belongs to

	private final String name;

    // different extended link roles of this linkbase
    private final Set<String> extendedLinkRoles = new HashSet<String>();

	private final Set<ExtendedLinkElement> extendedLinkElements = new HashSet<ExtendedLinkElement>();

	/* maps extended link roles to list with arcs for this link role */
	final Map<String, List<Arc>> arcBaseSets = new HashMap<String, List<Arc>>();
	final Map<String, List<Arc>> overridenArcs = new HashMap<String, List<Arc>>();
	final Map<String, List<Arc>> prohibitingArcs = new HashMap<String, List<Arc>>();
	final Map<String, List<Arc>> prohibitedArcs = new HashMap<String, List<Arc>>();

	/**
	 * Constructor.
	 * 
	 * @param dts
	 *            Discoverable Taxonomy Set (DTS) this linkbase refers to.
	 * @param name the name
	 */
	public Linkbase(DiscoverableTaxonomySet dts, String name) {
		this.dts = dts;
		this.name = name;
	}

	public final DiscoverableTaxonomySet getDiscoverableTaxonomySet() {
	    return dts;
	}

	public final String getName() {
	    return name;
	}

	public abstract void buildLinkbase() throws TaxonomyCreationException, XBRLException;

	public abstract String getDefaultArcRole();

    /**
     * Adds a new extended link role to the linkbase.
     * 
     * @param xbrlExtendedLinkRole
     *            New extended link role.
     */
    public void addExtendedLinkRole(String xbrlExtendedLinkRole) {
        extendedLinkRoles.add(xbrlExtendedLinkRole);
    }

    /**
     * @return Set of String objects representing the extended link roles of the
     *         linkbase.
     */
    public final Collection<String> getExtendedLinkRoles() {
        return extendedLinkRoles;
    }

    /**
     * Adds a new ExtendedLinkElement (either a resource or a locator) object to
     * the list of objects.
     * 
     * @param element
     *            The XLinkElement object that shall be added.
     */
    public void addExtendedLinkElement(ExtendedLinkElement element) //throws TaxonomyCreationException
    {
        extendedLinkElements.add(element);
    }

    /**
     * @return List with extendedLinkElement objects.
     */
    public final Set<ExtendedLinkElement> getExtendedLinkElements() {
        return extendedLinkElements;
    }

    public void applyArcs(LinkbaseArcsContainer<Arc> container, boolean copyLists) {
        Iterator<Entry<String, List<Arc>>> it = null;

        it = container.getBaseArcs();
        while (it.hasNext()) {
            Entry<String, List<Arc>> e = it.next();
            if (copyLists)
                arcBaseSets.put(e.getKey(), new ArrayList<Arc>(e.getValue()));
            else
                arcBaseSets.put(e.getKey(), e.getValue());
        }

        it = container.getOverriddenArcs();
        while (it.hasNext()) {
            Entry<String, List<Arc>> e = it.next();
            if (copyLists)
                overridenArcs.put(e.getKey(), new ArrayList<Arc>(e.getValue()));
            else
                overridenArcs.put(e.getKey(), e.getValue());
        }

        it = container.getProhibitingArcs();
        while (it.hasNext()) {
            Entry<String, List<Arc>> e = it.next();
            if (copyLists)
                prohibitingArcs.put(e.getKey(), new ArrayList<Arc>(e.getValue()));
            else
                prohibitingArcs.put(e.getKey(), e.getValue());
        }

        it = container.getProhibitedArcs();
        while (it.hasNext()) {
            Entry<String, List<Arc>> e = it.next();
            if (copyLists)
                prohibitedArcs.put(e.getKey(), new ArrayList<Arc>(e.getValue()));
            else
                prohibitedArcs.put(e.getKey(), e.getValue());
        }
    }

    /**
     * @return List with Arc objects.
     */
    public final Map<String, List<Arc>> getArcBaseSets() {
        return arcBaseSets;
    }

    /**
     * Returns a base set of arcs (list of Arc objects) from the default link
     * role (http://www.xbrl.org/2003/role/link).
     * 
     * @return Base set of arcs from the default link role.
     */
    public final List<Arc> getArcBaseSet() {
        return getArcBaseSet(DEFAULT_LINK_ROLE);
    }

	/**
	 * Returns a base set of arcs (list of Arc objects) to a specific extended
	 * link role.
	 * 
	 * @param extendedLinkRole
	 *            Extended link role of the base set.
	 * @return Base set of the specified extended link role.
	 */
	public final List<Arc> getArcBaseSet(String extendedLinkRole) {
		return arcBaseSets.get(extendedLinkRole);
	}

	/**
	 * Returns a base set of arcs (list of Arc objects) with a given arcrole in
	 * a given exended link role.
	 * 
	 * @param arcrole
	 *            Role of the arc.
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase. If NULL, the default link
	 *            role is taken.
	 * @return List of arc objects that match to the given parameters.
	 */
	public final List<Arc> getArcBaseSet(String arcrole, String extendedLinkRole) {
        return getArcBaseSet(arcrole, extendedLinkRole, new ArrayList<Arc>());
	}

    /**
     * Returns a base set of arcs (list of Arc objects) with a given arcrole in
     * a given exended link role.
     * 
     * @param arcrole
     *            Role of the arc.
     * @param extendedLinkRole
     *            Extended link role of the linkbase. If NULL, the default link
     *            role is taken.
     * @param target the target collection to add arcs to
     * @param <Arcs> type restriction
     * @return List of arc objects that match to the given parameters.
     */
    public <Arcs extends Collection<Arc>> Arcs getArcBaseSet(String arcrole, String extendedLinkRole, Arcs target) {
        if (extendedLinkRole == null) {
            extendedLinkRole = DEFAULT_LINK_ROLE;
        }

        List<Arc> tmpArcBaseSet = getArcBaseSet(extendedLinkRole);
        if (tmpArcBaseSet != null) {
            for (Arc xArc : tmpArcBaseSet) {
                if (xArc.getArcRole().equals(arcrole)) {
                    target.add(xArc);
                }
            }
        }
        return target;
    }

	/**
	 * Returns a base set of arcs (list of Arc objects) with a given list of arc
	 * roles (list of String objects) in a given extended link role.
	 * 
	 * @param arcrole
	 *            List of arc roles (String objects).
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase.
	 * @return List of arc objects that match to the given parameters.
	 */
	public List<Arc> getArcBaseSet(List<String> arcrole, String extendedLinkRole) {
		return getArcBaseSet(arcrole, extendedLinkRole, new ArrayList<Arc>());
	}

    /**
     * Returns a base set of arcs (list of Arc objects) with a given list of arc
     * roles (list of String objects) in a given extended link role.
     * 
     * @param arcrole
     *            List of arc roles (String objects).
     * @param extendedLinkRole
     *            Extended link role of the linkbase.
     * @param target the target collection to add arcs to
     * @param <Arcs> type restriction
     * @return List of arc objects that match to the given parameters.
     */
    public <Arcs extends Collection<Arc>> Arcs getArcBaseSet(List<String> arcrole, String extendedLinkRole, Arcs target) {
        for (int i = 0; i < arcrole.size(); i++) {
            String currentArcrole = arcrole.get(i);
            getArcBaseSet(currentArcrole, extendedLinkRole, target);
        }
        return target;
    }

    /**
     * Returns all overriden arcs (list of Arc objects) of the default link
     * role.
     * 
     * @return List of overriden arcs in the default link role.
     */
    public final List<Arc> getOverridenArcs() {
        return getOverridenArcs(DEFAULT_LINK_ROLE);
    }

	/**
	 * Returns all overriden arcs (list of Arc objects) of a specific extended
	 * link role.
	 * 
	 * @param extendedLinkRole
	 *            Extended link role of the base set.
	 * @return List of overriden arcs in the specified extended link role.
	 */
	public final List<Arc> getOverridenArcs(String extendedLinkRole) {
		return overridenArcs.get(extendedLinkRole);
	}

    /**
     * Returns all prohibiting arcs (list of Arc objects) of the default link
     * role.
     * 
     * @return List of prohibiting arcs in the default link role.
     */
    public final List<Arc> getProhibitingArcs() {
        return getProhibitingArcs(DEFAULT_LINK_ROLE);
    }

	/**
	 * Returns all prohibiting arcs (list of Arc objects) of a specific extended
	 * link role.
	 * 
	 * @param extendedLinkRole
	 *            Extended link role of the prohibiting arcs.
	 * @return List of prohibiting arcs in the specified extended link role.
	 */
	public final List<Arc> getProhibitingArcs(String extendedLinkRole) {
		return prohibitingArcs.get(extendedLinkRole);
	}

    /**
     * Returns all prohibited arcs (list of Arc objects) of the default link
     * role.
     * 
     * @return List of prohibited arcs in the default link role.
     */
    public final List<Arc> getProhibitedArcs() {
        return getProhibitedArcs(DEFAULT_LINK_ROLE);
    }

	/**
	 * Returns all prohibited arcs (list of Arc objects) of a specific extended
	 * link role.
	 * 
	 * @param extendedLinkRole
	 *            Extended link role of the prohibited arcs.
	 * @return List of prohibited arcs in the specified extended link role.
	 */
	public final List<Arc> getProhibitedArcs(String extendedLinkRole) {
		return prohibitedArcs.get(extendedLinkRole);
	}

	/**
	 * Returns a resource for a certain ID.
	 * 
	 * @param id
	 *            ID for which the resource is taken.
	 * @return The according xbrlcore.link.Resource object which matches the
	 *         given ID.
	 */
	public Resource getResource(String id) {
		for (ExtendedLinkElement currExLinkElement : extendedLinkElements) {
			if (currExLinkElement.isResource()
					&& currExLinkElement.getID() != null
					&& currExLinkElement.getID().equals(id)) {
				return (Resource) currExLinkElement;
			}
		}
		return null;
	}

	/**
	 * Returns the according extended link element objects to a given label
	 * within a given extended link role. According to Spec. section 3.5.3.9,
	 * there can be multiple same labels within one extended link role, since
	 * one-to-many and many-to-many arc representations are possible.
	 * 
	 * @param label
	 *            Label of the extended link element object.
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase.
	 * @param linkbaseSource 
	 * @return List with all ExtendedLinkElement objects matching the given
	 *         parameters.
	 */
	public List<ExtendedLinkElement> getExtendedLinkElements(String label, String extendedLinkRole,
			String linkbaseSource) {
		if (extendedLinkRole == null) {
			extendedLinkRole = DEFAULT_LINK_ROLE;
		}

        List<ExtendedLinkElement> resultList = new ArrayList<ExtendedLinkElement>();

		for (ExtendedLinkElement extendedLinkElement : extendedLinkElements) {
			if (extendedLinkElement.getLabel().equals(label)
					&& extendedLinkElement.getExtendedLinkRole().equals(
							extendedLinkRole)) {
				/*
				 * check if they are from the same linkbase file, only if
				 * linkbaseSource is set
				 */
				if ((linkbaseSource != null && extendedLinkElement
						.getLinkbaseSource().equals(linkbaseSource))
						|| linkbaseSource == null) {

					resultList.add(extendedLinkElement);
				}

			}
		}
		return resultList;
	}

	/**
	 * Returns only those extended link elements (list of ExtendedLinkElement)
	 * which are part of the base set of arcs.
	 * 
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase
	 * @return List with all ExtendedLinkElement objects matching the given
	 *         parameters. Furthermore, they must be part of the base set of
	 *         arcs.
	 */
	public List<ExtendedLinkElement> getExtendedLinkElementsFromBaseSet(String extendedLinkRole) {
		List<ExtendedLinkElement> resultList = new ArrayList<ExtendedLinkElement>();
		Set<ExtendedLinkElement> usedExtendedLinkElements = new HashSet<ExtendedLinkElement>();
		if (extendedLinkRole == null) {
			extendedLinkRole = DEFAULT_LINK_ROLE;
		}

		List<Arc> arcList = getArcBaseSet(extendedLinkRole);
		if (arcList != null) {
    		for (int i = 0; i < arcList.size(); i++) {
    			Arc currArc = arcList.get(i);
    			ExtendedLinkElement sourceElement = currArc.getSourceElement();
    			ExtendedLinkElement targetElement = currArc.getTargetElement();
    			if (usedExtendedLinkElements.add(sourceElement)) {
    				resultList.add(sourceElement);
    			}
    			if (usedExtendedLinkElements.add(targetElement)) {
    				resultList.add(targetElement);
    			}
    		}
		}

		return resultList;
	}

	/**
	 * Returns a locator from a specific base set to a specific concept. To each
	 * concept, there can be only one locator within one extended link role AND
	 * within one base set of arcs. If there are mulitple locators, not all of
	 * the corresponding arcs are part of the base set (i.e. one arc is
	 * overriding another).
	 * 
	 * @param concept
	 *            The concept for which all XLinkElement objects shall be
	 *            determined.
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase.
	 * @return Locator of the concept from the base set of arcs in the given
	 *         extended link role.
	 */
	public Locator getExtendedLinkElementFromBaseSet(
			Concept concept, String extendedLinkRole) {
		if (extendedLinkRole == null) {
			extendedLinkRole = DEFAULT_LINK_ROLE;
		}

		List<Arc> tmpArcBaseSet = getArcBaseSet(extendedLinkRole);

		if (tmpArcBaseSet == null) {
			return null;
		}

		for (int i = 0; i < tmpArcBaseSet.size(); i++) {
			Arc currArc = tmpArcBaseSet.get(i);
			if (currArc.getSourceElement().isLocator()
					&& ((Locator) currArc.getSourceElement()).getConcept()
							.equals(concept)) {
				return (Locator) currArc.getSourceElement();
			} else if (currArc.getTargetElement().isLocator()
					&& ((Locator) currArc.getTargetElement()).getConcept()
							.equals(concept)) {
				return (Locator) currArc.getTargetElement();
			}
		}
		return null;
	}

	/**
	 * Returns all extended link element objects a certain concept is linked to.
	 * That is, the concept is the "from" element, and the return list contains
	 * all the "to" elements, but only those of arcs belonging to the base set
	 * of arcs (i.e. overriden links are not taken into account).
	 * 
	 * @param concept
	 *            Concept which is the source of the links.
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase.
	 * @return List of extended link element objects which the given concept is
	 *         linked to.
	 */
	public List<ExtendedLinkElement> getTargetExtendedLinkElements(Concept concept,
			String extendedLinkRole) {
		if (extendedLinkRole == null) {
			extendedLinkRole = DEFAULT_LINK_ROLE;
		}
		List<ExtendedLinkElement> resultList = new ArrayList<ExtendedLinkElement>();
		List<Arc> tmpArcBaseSet = getArcBaseSet(extendedLinkRole);

		if (tmpArcBaseSet != null) {
			for (int i = 0; i < tmpArcBaseSet.size(); i++) {
				Arc arc = tmpArcBaseSet.get(i);
				ExtendedLinkElement sourceLinkElement = arc.getSourceElement();
				if (arc.getExtendedLinkRole().equals(extendedLinkRole)
						&& sourceLinkElement.isLocator()) {
					Locator currLoc = (Locator) sourceLinkElement;

					if (currLoc.getConcept().equals(concept)) {
						resultList.add(resultList.size(), arc
								.getTargetElement());
					}
				}
			}
		}
		return resultList;
	}

	/**
	 * Returns all extended link element objects a certain concept is linked
	 * from. That is, the concept is the "to" element, and the return list
	 * contains all the "from" elements, but only those of arcs belonging to the
	 * base set of arcs (i.e. overriden links are not taken into account).
	 * 
	 * @param concept
	 *            Concept which is the target of the links.
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase.
	 * @return List of extended link element objects which are linked to the
	 *         given concept.
	 */
	public List<ExtendedLinkElement> getSourceExtendedLinkElements(Concept concept,
			String extendedLinkRole) {
		if (extendedLinkRole == null) {
			extendedLinkRole = DEFAULT_LINK_ROLE;
		}
		List<ExtendedLinkElement> resultList = new ArrayList<ExtendedLinkElement>();
		List<Arc> tmpArcBaseSet = getArcBaseSet(extendedLinkRole);
		if (tmpArcBaseSet != null) {
			for (Arc arc : tmpArcBaseSet) {
				ExtendedLinkElement targetLinkElement = arc.getTargetElement();
				if (arc.getExtendedLinkRole().equals(extendedLinkRole)
						&& targetLinkElement.isLocator()) {
					Locator currLoc = (Locator) targetLinkElement;
					if (currLoc.getConcept().equals(concept)) {
						resultList.add(arc.getSourceElement());
					}
				}
			}
		}
		return resultList;
	}

	/**
	 * Returns a list with the complete "source network" of extended link
	 * elements in the given extended link role for the given concept. So if
	 * there is a link (= an arc) from concept a to concept b, and one from
	 * concept b to concept c, the "source network" of extended link elements
	 * for concept c would be the according extended link element for concept b
	 * and concept a.
	 * 
	 * @param concept
	 *            XBRL concept for which the source network shall be obtained.
	 * @param arcRole
	 *            The arcRole attribute of the links.
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase.
	 * @return List with ExtendedLinkElement objects of the source network of
	 *         the given concept.
	 */
	public Set<ExtendedLinkElement> buildSourceNetwork(Concept concept, String arcRole,
			String extendedLinkRole) {
		Set<ExtendedLinkElement> resultSet = new HashSet<ExtendedLinkElement>();
		if (extendedLinkRole == null) {
			extendedLinkRole = DEFAULT_LINK_ROLE;
		}

		ExtendedLinkElement linkElement = getExtendedLinkElementFromBaseSet(
				concept, extendedLinkRole);

		if (linkElement == null) {
			return null;
		}

		resultSet = collectSourceNetwork(linkElement, arcRole,
				extendedLinkRole, resultSet);

		return resultSet;
	}

    /**
     * Helping method for buildSourceNetwork. The method is working recursively.
     */
    private Set<ExtendedLinkElement> collectSourceNetwork(ExtendedLinkElement xLinkElement,
            String arcRole, String extendedLinkRole, Set<ExtendedLinkElement> resultSet) {
        resultSet.add(xLinkElement);
        List<ExtendedLinkElement> sourceElementList = getSourceExtendedLinkElements(
                ((Locator) xLinkElement).getConcept(), extendedLinkRole);
        for (ExtendedLinkElement currExLinkElement : sourceElementList) {
            collectSourceNetwork(currExLinkElement, arcRole, extendedLinkRole,
                    resultSet);
        }
        return resultSet;
    }

	/**
	 * Returns a list with all the extended link elements in the given extended
	 * link role which are linked to a certain concept. That means, if a concept
	 * a is linked to concept b and c, and concept b is linked to concept d and
	 * e, the "target network" for concept a would contain the extended link
	 * element objects of concept b, c, d and e.
	 * 
	 * @param concept
	 *            XBRL concept for which the target network shall be obtained.
	 * @param arcRole
	 *            The arcRole attribute of the links.
	 * @param extendedLinkRole
	 *            Extended link role of the linkbase.
	 * @return List with ExtendedLinkElement objects of the target network of
	 *         the given concept.
	 * @throws XBRLException 
	 */
	public Set<ExtendedLinkElement> buildTargetNetwork(Concept concept, String arcRole,
			String extendedLinkRole) throws XBRLException {
		Set<ExtendedLinkElement> resultSet = new HashSet<ExtendedLinkElement>();
		if (extendedLinkRole == null) {
			extendedLinkRole = DEFAULT_LINK_ROLE;
		}

		ExtendedLinkElement linkElement = getExtendedLinkElementFromBaseSet(
				concept, extendedLinkRole);

        //if (linkElement == null) {
		if (!(linkElement instanceof Locator)) {
			return null;
		}

		resultSet = collectTargetNetwork((Locator)linkElement, arcRole,
				extendedLinkRole, resultSet);

		/* remove the first element */
		resultSet.remove(getExtendedLinkElementFromBaseSet(concept,
				extendedLinkRole));
		return resultSet;
	}

	/**
	 * Helping method for buildTargetNetwork. The method is working recursively.
	 */
	private Set<ExtendedLinkElement> collectTargetNetwork(Locator xLinkElement,
			String arcRole, String extendedLinkRole, Set<ExtendedLinkElement> resultSet) {
		resultSet.add(xLinkElement);

		List<Arc> arcs = getTargetArcsFromExtendedLinkElement(xLinkElement, arcRole,
				extendedLinkRole);
		for (Arc currArc : arcs) {
			ExtendedLinkElement xLinkElementTo = currArc.getTargetElement();
			if (xLinkElementTo instanceof Locator) {
    			if (currArc.getTargetRole() != null) {
    				/*
    				 * arc is pointing to a different extended link role
    				 * (xbrldt:targetRole="...")
    				 */
    				collectTargetNetwork((Locator)xLinkElementTo, arcRole,
    				        currArc.getTargetRole(), resultSet);
    			} else {
    				collectTargetNetwork((Locator)xLinkElementTo, arcRole,
    				        currArc.getExtendedLinkRole(), resultSet);
    			}
			}
		}
		return resultSet;
	}

	/**
	 * Returns an Arc for a given source locator (only base set of arcs is taken
	 * into account).
	 * 
	 * @param loc
	 *            Source locator for which the arc shall be obtained.
	 * @return Arc with the given source locator.
	 */
	public Arc getArcForSourceLocator(Locator loc) {
		if (loc == null)
			return null;
		List<Arc> tmpArcBaseSet = getArcBaseSet(loc.getExtendedLinkRole());
		if (tmpArcBaseSet != null) {
			for ( Arc arc : tmpArcBaseSet) {
				if (arc.getSourceElement().equals(loc)) {
					return arc;
				}
			}
		}
		return null;
	}

	/**
	 * Returns an Arc for a given target locator (only base set of arcs is taken
	 * into account).
	 * 
	 * @param loc
	 *            Target locator for which the arc shall be obtained.
	 * @return Arc with the given target locator.
	 */
	public Arc getArcForTargetLocator(Locator loc) {
		if (loc == null)
			return null;
		List<Arc> tmpArcBaseSet = getArcBaseSet(loc.getExtendedLinkRole());
		if (tmpArcBaseSet != null) {
			for (Arc arc : tmpArcBaseSet) {
				Locator target = (Locator) arc.getTargetElement();
				if (loc.getConcept().equals(target.getConcept())) {
					return arc;
				}
			}
		}
		return null;
	}

    /**
     * Gets all the arc objects, which point FROM the given {@link Concept} to
     * any other one.
     * 
     * @param concept
     *            The concept FROM which the arc must point
     * @param arcRole
     *            The arcrole attribute of the links. If NULL, the arcrole is
     *            not taken into account.
     * @param extendedLinkRole
     *            The extended link role of the linkbase. If NULL, the default
     *            link role is taken.
     * @return List with arc objects which belongs to the target network of the
     *         given extended link element.
     */
    public List<Arc> getArcsFromConcept(
            Concept concept, String arcRole,
            String extendedLinkRole) {
        if (extendedLinkRole == null) {
            extendedLinkRole = DEFAULT_LINK_ROLE;
        }
        List<Arc> tmpArcBaseSet = getArcBaseSet(extendedLinkRole);

        List<Arc> resultList = null;
        if (tmpArcBaseSet != null) {
            for (Arc arc : tmpArcBaseSet) {
                ExtendedLinkElement arcSourceElement = arc.getSourceElement();
                Concept arcSourceConcept = ((Locator) arcSourceElement).getConcept();
                if (arcSourceConcept.equals(concept)) {
                    if ((arcRole == null) || arcRole.equals(arc.getArcRole())) {
                        if (resultList == null)
                            resultList = new ArrayList<Arc>();
                        resultList.add(arc);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * Gets all the arc objects, which point FROM the given {@link Concept} to
     * any other one.
     * 
     * @param concept
     *            The concept FROM which the arc must point
     * @param extendedLinkRole
     *            The extended link role of the linkbase. If NULL, the default
     *            link role is taken.
     * @return List with arc objects which belongs to the target network of the
     *         given extended link element.
     */
    public List<Arc> getArcsFromConcept(
            Concept concept,
            String extendedLinkRole) {
        return getArcsFromConcept(concept, getDefaultArcRole(), extendedLinkRole);
    }

    /**
     * Gets all the arc objects, which point TO the given {@link Concept} from
     * any other one.
     * 
     * @param concept
     *            The concept TO which the arc must point
     * @param arcRole
     *            The arcrole attribute of the links. If NULL, the arcrole is
     *            not taken into account.
     * @param extendedLinkRole
     *            The extended link role of the linkbase. If NULL, the default
     *            link role is taken.
     * @return List with arc objects which belongs to the target network of the
     *         given extended link element.
     */
    public List<Arc> getArcsToConcept(
            Concept concept, String arcRole,
            String extendedLinkRole) {
        if (extendedLinkRole == null) {
            extendedLinkRole = DEFAULT_LINK_ROLE;
        }
        List<Arc> tmpArcBaseSet = getArcBaseSet(extendedLinkRole);

        List<Arc> resultList = null;
        if (tmpArcBaseSet != null) {
            for (Arc arc : tmpArcBaseSet) {
                ExtendedLinkElement arcTargetElement = arc.getTargetElement();
                Concept arcTargetConcept = ((Locator) arcTargetElement).getConcept();
                if (arcTargetConcept.equals(concept)) {
                    if ((arcRole == null) || arcRole.equals(arc.getArcRole())) {
                        if (resultList == null)
                            resultList = new ArrayList<Arc>();
                        resultList.add(arc);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * Gets all the arc objects, which point TO the given {@link Concept} from
     * any other one.
     * 
     * @param concept
     *            The concept TO which the arc must point
     * @param extendedLinkRole
     *            The extended link role of the linkbase. If NULL, the default
     *            link role is taken.
     * @return List with arc objects which belongs to the target network of the
     *         given extended link element.
     */
    public List<Arc> getArcsToConcept(
            Concept concept,
            String extendedLinkRole) {
        return getArcsToConcept(concept, getDefaultArcRole(), extendedLinkRole);
    }

	/**
	 * Gets all the arc objects which link from a given extended link element to
	 * any other one.
	 * 
	 * @param xLinkElement
	 *            The extended link element for which the target network shall
	 *            be obtained.
	 * @param arcRole
	 *            The arcrole attribute of the links. If NULL, the arcrole is
	 *            not taken into account.
	 * @param extendedLinkRole
	 *            The extended link role of the linkbase. If NULL, the default
	 *            link role is taken.
	 * @return List with arc objects which belongs to the target network of the
	 *         given extended link element.
	 */
	public List<Arc> getTargetArcsFromExtendedLinkElement(
			Locator xLinkElement, String arcRole,
			String extendedLinkRole) {
	    List<Arc> resultList = getArcsFromConcept(xLinkElement.getConcept(), arcRole, extendedLinkRole);
	    if (resultList == null)
	        resultList = new ArrayList<Arc>();

		return resultList;
	}

    /**
     * Gets all the arc objects which link from a given extended link element to
     * any other one.
     * 
     * @param xLinkElement
     *            The extended link element for which the target network shall
     *            be obtained.
     * @param extendedLinkRole
     *            The extended link role of the linkbase. If NULL, the default
     *            link role is taken.
     * @return List with arc objects which belongs to the target network of the
     *         given extended link element.
     */
    public List<Arc> getTargetArcsFromExtendedLinkElement(
            Locator xLinkElement,
            String extendedLinkRole) {
        return getTargetArcsFromExtendedLinkElement(xLinkElement, getDefaultArcRole(), extendedLinkRole);
    }
}
