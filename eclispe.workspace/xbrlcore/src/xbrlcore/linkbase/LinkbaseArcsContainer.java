package xbrlcore.linkbase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xbrlcore.taxonomy.AbstractTaxonomyLoader;
import xbrlcore.xlink.Arc.UseAttribute;

/**
 * Container class for arcs when loaded by an implementation of {@link AbstractTaxonomyLoader}.
 * The implementation must handle duplicates and distinguish between regular, overridden and prohibited arcs.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 *
 * @param <A> the arc type implementation
 */
public class LinkbaseArcsContainer<A extends LinkbaseArcsContainer.ArcInterface> {
    public static interface ArcInterface {
        public UseAttribute getUseAttribute();
        public String getExtendedLinkRole();
        public int getPriorityAttribute();
        public float getOrder();
        public String getArcRole();
        public boolean equalSource(ArcInterface obj);
        public boolean equalTarget(ArcInterface obj);
        public boolean equalSourceTarget(ArcInterface obj);
        public boolean overridesArc(ArcInterface overriddenArc);
        public boolean prohibitsArc(ArcInterface prohibitedArc);
        public String getSourceElementLabel();
        public String getTargetElementLabel();
    }

    protected final Map<String, List<A>> arcsBaseSet = new HashMap<String, List<A>>();
    protected final Map<String, List<A>> overridenArcs = new HashMap<String, List<A>>();
    protected final Map<String, List<A>> prohibitingArcs = new HashMap<String, List<A>>();
    protected final Map<String, List<A>> prohibitedArcs = new HashMap<String, List<A>>();

    public final Iterator<Entry<String, List<A>>> getBaseArcs() {
        return arcsBaseSet.entrySet().iterator();
    }

    public final Iterator<Entry<String, List<A>>> getOverriddenArcs() {
        return overridenArcs.entrySet().iterator();
    }

    public final Iterator<Entry<String, List<A>>> getProhibitingArcs() {
        return prohibitingArcs.entrySet().iterator();
    }

    public final Iterator<Entry<String, List<A>>> getProhibitedArcs() {
        return prohibitedArcs.entrySet().iterator();
    }
}
