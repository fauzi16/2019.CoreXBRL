package xbrlcore.taxonomy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xbrlcore.linkbase.Linkbase;
import xbrlcore.linkbase.LinkbaseArcsContainer;
import xbrlcore.logging.LogInterface;
import xbrlcore.logging.LogInterface.LogLevel;
import xbrlcore.xlink.Arc.UseAttribute;

/**
 * Default implementation of a {@link LinkbaseArcsContainer} used by {@link DefaultTaxonomyLoader}.
 * 
 * @author Daniel Hamm
 * @author Marvin Froehlich (INFOLOG GmbH)
 * 
 * @param <A> the arc type implementation
 */
public class DefaultLinkbaseArcsCollector<A extends LinkbaseArcsContainer.ArcInterface> extends LinkbaseArcsContainer<A> {

    private final LogInterface log;

    public DefaultLinkbaseArcsCollector(LogInterface log) {
        this.log = log;
    }

    /**
     * Adds a new Arc to the linkbase. The order of the arc is taken into
     * account, so it is placed at the correct position within its base set. It
     * is also determined whether this arc is a prohibiting or overriding arc
     * for another one.
     * 
     * @param newArc
     *            The Arc object that shall be added.
     */
    public void addArc(A newArc) {
        // Check whether this is a prohibiting arc
        if (newArc.getUseAttribute() == UseAttribute.PROHIBITED) {
            addProhibitedArc(newArc);
            return;
        }

        List<A> tmpArcBaseSet = arcsBaseSet.get(newArc.getExtendedLinkRole());
        if (tmpArcBaseSet == null) {
            // first arc of this extended link role
            tmpArcBaseSet = new ArrayList<A>();
            arcsBaseSet.put(newArc.getExtendedLinkRole(), tmpArcBaseSet);
            tmpArcBaseSet.add(newArc);
            return;
        }

        // Check whether newArc is already available
        for (A tmpArc : tmpArcBaseSet) {
            if (tmpArc.equalSourceTarget(newArc)
                    && tmpArc.getPriorityAttribute() == newArc.getPriorityAttribute()
                    && tmpArc.getUseAttribute() == newArc.getUseAttribute()) {
                log.log(LogLevel.DEBUG, Linkbase.class, "Duplicate arc: " +
                            newArc.getSourceElementLabel() + " => " +
                            newArc.getTargetElementLabel() + " (" +
                            newArc.getArcRole() + ") in " +
                            newArc.getExtendedLinkRole());
                return;
            }
        }

        // Check whether this arc is already prohibited by another one.
        List<A> prohibitingArcList = prohibitingArcs.get(newArc.getExtendedLinkRole());
        if (prohibitingArcList != null) {
            for (int i = 0; i < prohibitingArcList.size(); i++) {
                A prohibitingArc = prohibitingArcList.get(i);
                if (prohibitingArc.prohibitsArc(newArc)) {
                    // newArc is already prohibited, hence just stop the processing
                    // add it to list of prohibited arcs
                    List<A> tmpProhibitedArcList = prohibitingArcs.get(newArc.getExtendedLinkRole());
                    if (tmpProhibitedArcList == null) {
                        tmpProhibitedArcList = new ArrayList<A>();
                        prohibitedArcs.put(newArc.getExtendedLinkRole(), tmpProhibitedArcList);
                    }
                    tmpProhibitedArcList.add(newArc);
                    return;
                }
            }
        }

        // newArc must be placed to the correct position, according to the order
        boolean sourceAvailable = false;
        int numSourceAvailable = -1;
        A startArc = null;
        A endArc = null;

        // now put arc to the base set of arcsBaseSet
        // check if there already is an arc with the same source element
        for (int i = 0; i < tmpArcBaseSet.size(); i++) {
            A currArc = tmpArcBaseSet.get(i);
            if (currArc.equalSource(newArc)) {
                sourceAvailable = true;
                numSourceAvailable = i;
                startArc = currArc;
                break;
            }
        }

        if (!sourceAvailable) {
            // first arc with this source, put it at the end
            tmpArcBaseSet.add(tmpArcBaseSet.size(), newArc);
        } else {
            // now check at which position the new arc has to be added
            while (true) {
                // check whether one of startArc and newArc is an overriding link for the other
                startArc = tmpArcBaseSet.get(numSourceAvailable);

                if (newArc.overridesArc(startArc)) {
                    // remove startArc and put newArc in the base set
                    tmpArcBaseSet.add(numSourceAvailable, newArc);
                    tmpArcBaseSet.remove(startArc);
                    List<A> tmpOverridenArcList = overridenArcs.get(startArc.getExtendedLinkRole());
                    if (tmpOverridenArcList == null) {
                        tmpOverridenArcList = new ArrayList<A>();
                        overridenArcs.put(startArc.getExtendedLinkRole(), tmpOverridenArcList);
                    }
                    tmpOverridenArcList.add(startArc);
                    // processing is finished now and method returns
                    break;
                } else if (startArc.overridesArc(newArc)) {
                    // newArc is overriden and therefore not taken into account for the base set
                    List<A> tmpOverridenArcList = overridenArcs.get(startArc.getExtendedLinkRole());
                    if (tmpOverridenArcList == null) {
                        tmpOverridenArcList = new ArrayList<A>();
                        overridenArcs.put(newArc.getExtendedLinkRole(), tmpOverridenArcList);
                    }
                    tmpOverridenArcList.add(newArc);
                    // processing is finished now and method returns
                    break;
                }

                // startArc and newArc are not overriding arcs

                // if numSourceAvailable is last position, put new arc at the end
                if (numSourceAvailable >= tmpArcBaseSet.size() - 1) {
                    if (newArc.getOrder() >= startArc.getOrder()) {
                        tmpArcBaseSet.add(tmpArcBaseSet.size(), newArc);
                    } else {
                        tmpArcBaseSet.add(tmpArcBaseSet.size() - 1, newArc);
                    }
                    break;
                }
                endArc = tmpArcBaseSet.get(numSourceAvailable + 1);

                // if the new arc is before startArc and both have the same
                // source, put it at position numSourceAvailable
                if (startArc.getOrder() > newArc.getOrder()
                        && newArc.equalSource(startArc)) {
                    tmpArcBaseSet.add(numSourceAvailable, newArc);
                    break;
                }

                // if arc at numSourceAvailable+1 has another source, put newArc
                // at position numSourceAvailable+1
                if (!newArc.equalSource(endArc)) {
                    tmpArcBaseSet.add(numSourceAvailable + 1, newArc);
                    break;
                }

                // if the new arc is between startArc and endArc, put it between both
                if (startArc.getOrder() <= newArc.getOrder()
                        && newArc.getOrder() < endArc.getOrder()) {
                    tmpArcBaseSet.add(numSourceAvailable + 1, newArc);
                    break;
                }
                // else continue with the next two arcsBaseSet
                //else
                {
                    numSourceAvailable++;
                }
            }
        }
    }

    /**
     * Adds a prohibiting arc to the linkbase and removes all according
     * prohibited arcs from the base set.
     * 
     * @param newArc
     *            Prohibiting arc.
     */
    private void addProhibitedArc(A newArc) {
        List<A> tmpArcBaseSet = arcsBaseSet.get(newArc.getExtendedLinkRole());
        if (tmpArcBaseSet == null) {
            // first arc of this extended link role
            tmpArcBaseSet = new ArrayList<A>();
            arcsBaseSet.put(newArc.getExtendedLinkRole(), tmpArcBaseSet);
        }

        // add this arc to the prohibiting arcs
        List<A> tmpProhibitingArcList = prohibitingArcs.get(newArc.getExtendedLinkRole());
        if (tmpProhibitingArcList == null) {
            tmpProhibitingArcList = new ArrayList<A>();
            prohibitingArcs.put(newArc.getExtendedLinkRole(), tmpProhibitingArcList);
        }
        tmpProhibitingArcList.add(newArc);

        // now check all the possible arcs whether they are prohibited
        Iterator<A> possibleProhibitedArcIterator = tmpArcBaseSet.iterator();
        /*
         * Arcs which shall be removed are stored in a separate list, since is
         * is not possible to remove elements from tmpArcBaseSet during the
         * iteration
         */
        List<A> arcsToRemove = new ArrayList<A>();
        while (possibleProhibitedArcIterator.hasNext()) {
            A possibleProhibitedArc = possibleProhibitedArcIterator.next();
            if (newArc.prohibitsArc(possibleProhibitedArc)) {
                // possibleProhibitedArc is prohibited => remove it from base set
                arcsToRemove.add(possibleProhibitedArc);
                /* and add it to list of prohibited arcs */
                List<A> tmpProhibitedArcList = prohibitedArcs.get(possibleProhibitedArc.getExtendedLinkRole());
                if (tmpProhibitedArcList == null) {
                    tmpProhibitedArcList = new ArrayList<A>();
                    prohibitedArcs.put(possibleProhibitedArc.getExtendedLinkRole(), tmpProhibitedArcList);
                }
                tmpProhibitedArcList.add(possibleProhibitedArc);
            }
        }
        // now remove all the prohibited arcs from the base set
        for (int i = 0; i < arcsToRemove.size(); i++) {
            tmpArcBaseSet.remove(arcsToRemove.get(i));
        }
    }
}
