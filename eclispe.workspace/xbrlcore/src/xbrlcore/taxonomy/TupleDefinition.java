package xbrlcore.taxonomy;

import java.util.ArrayList;
import java.util.List;

/**
 * Represends a tuple definition of a taxonomy.
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class TupleDefinition extends Concept {
    private static final long serialVersionUID = -3291669512921597588L;

    public static enum TupleType {
        CHOICE( false ),
        MULTIPLE_CHOICE( true ),
        SEQUENCE( true ),
        ;
        
        private final boolean isMulti;
        
        public final boolean isMulti()
        {
            return ( isMulti );
        }
        
        private TupleType( boolean isMulti )
        {
            this.isMulti = isMulti;
        }
    }

    private final TupleType tupleType;

    private List<String> refs = null;
    private List<String[]> itemKeys = null;
    private final ArrayList<Concept> items = new ArrayList<Concept>();

    public TupleDefinition(String name, String id, String type, TupleType tupleType,
                           TaxonomySchema taxonomySchema,
                           String substitutionGroup, boolean isAbstract, boolean nillable, String periodType, String balance,  String typedDomainRef,
                           List<String> refs) {
        super(name, id, type, null, taxonomySchema, substitutionGroup, isAbstract, nillable, periodType, balance, typedDomainRef);

        this.tupleType = tupleType;
        this.refs = refs;
    }

    protected TupleDefinition(String name, String id, String type, TupleType tupleType,
                              TaxonomySchema taxonomySchema,
                              String substitutionGroup, boolean isAbstract, boolean nillable, String periodType, String balance, String typedDomainRef,
                              List<String[]> itemKeys, Object dummy) {
        super(name, id, type, null, taxonomySchema, substitutionGroup, isAbstract, nillable, periodType, balance, typedDomainRef);

        this.tupleType = tupleType;
        this.itemKeys = itemKeys;
    }

    public static TupleDefinition create(String name, String id, String type, TupleType tupleType,
                                         TaxonomySchema taxonomySchema,
                                         String substitutionGroup, boolean isAbstract, boolean nillable, String periodType, String balance, String typedDomainRef, List<String[]> itemKeys) {
        return new TupleDefinition(name, id, type, tupleType, taxonomySchema, substitutionGroup, isAbstract, nillable, periodType, balance, typedDomainRef, itemKeys, (Object)null);
    }

    public void collectConcepts(DiscoverableTaxonomySet dts) {// throws TaxonomyCreationException {
        if (refs != null) {
            if (getTupleType() == TupleType.CHOICE) {
                dts.getConceptBySubstitutionGroup(refs.get(0), items);
            } else { // sequence
                for (String ref : refs) {
                    // TODO: How to detect, where to take it from?
                    int n0 = items.size();
                    dts.getConceptBySubstitutionGroup(ref, items);
                    if (items.size() == n0) {
                        Concept c = dts.getConceptByID(ref.replace(':', '_'));
                        if (c != null)
                            items.add(c);
                    }
                }
            }

            for (Concept c : items) {
                c.setIsTupleItem(true);
            }

            refs = null;
        } else if (itemKeys != null) {
            for (String[] key : itemKeys) {
                items.add(dts.getTaxonomySchema(key[0]).getConceptByID(key[1]));
            }

            for (Concept c : items) {
                c.setIsTupleItem(true);
            }

            itemKeys = null;
        }
    }

    public final TupleType getTupleType() {
        return tupleType;
    }

    public final int getNumItems() {
        return items.size();
    }

    public final Concept getItem(int index) {
        return items.get(index);
    }
}
