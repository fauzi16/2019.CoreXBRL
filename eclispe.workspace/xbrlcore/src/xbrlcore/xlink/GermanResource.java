package xbrlcore.xlink;

import java.util.HashMap;
import java.util.Map;

import xbrlcore.constants.NamespaceConstants;

/**
 * This class represents a resource with german extensions within a linkbase.<br/><br/>
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class GermanResource extends Resource {

    private static final long serialVersionUID = 242256937469630043L;

    private final Map<String, String> hgbRefs = new HashMap<String, String>();

    /**
     * Constructor.
     * 
     * @param id
     * @param label
     *            Label of this resource.
     * @param linkbaseSource 
     * @param extLinkRole
     * @param role
     * @param title
     * @param lang
     * @param value
     */
    public GermanResource(String id, String label, String linkbaseSource, String extLinkRole, String role, String title, String lang, String value) {
        super(id, label, linkbaseSource, extLinkRole, role, title, lang, value);
    }

    @Override
    public void addRef(String namespacePrefix, String name, String value) {
        if (namespacePrefix.equals(NamespaceConstants.HGBREF_NAMESPACE.getPrefix()))
            hgbRefs.put(name, value);
        else
            super.addRef(namespacePrefix, name, value);
    }

    public final String getHGBRef(String name) {
        return hgbRefs.get(name);
    }

    public final Map<String, String> getHGBRefs() {
        return hgbRefs;
    }

    public final boolean getLegalFormEU() {
        return Boolean.parseBoolean(hgbRefs.get("legalFormEU"));
    }

    public final boolean getLegalFormKst() {
        return Boolean.parseBoolean(hgbRefs.get("legalFormKSt"));
    }

    public final boolean getLegalFormPg() {
        return Boolean.parseBoolean(hgbRefs.get("legalFormPG"));
    }

    public final boolean getLegalFormSEAG() {
        return Boolean.parseBoolean(hgbRefs.get("legalFormSEAG"));
    }

    public final boolean getLegalFormVVaG() {
        return Boolean.parseBoolean(hgbRefs.get("legalFormVVaG"));
    }

    public final boolean getLegalFormBNaU() {
        return Boolean.parseBoolean(hgbRefs.get("legalFormBNaU"));
    }

    public final boolean getLegalFormOerV() {
        return Boolean.parseBoolean(hgbRefs.get("legalFormOerV"));
    }

    public final String getTypeOperatingResult() {
        return hgbRefs.get("typeOperatingResult");
    }

    public final boolean getFiscalRequirement() {
        String value = hgbRefs.get("fiscalRequirement");
        if (value == null)
            return false;

        return (value.trim().length() > 0);
    }

    public final String getFiscalRequirementDetail() {
        String value = hgbRefs.get("fiscalRequirement");
        if (value == null)
            return "";

        return value;
    }

    public final String getNotPermittedFor() {
        return hgbRefs.get("notPermittedFor");
    }

    public final String getValidSince() {
        String value = hgbRefs.get("validSince");
        if (value == null)
            return null;

        return value.trim();
    }

    public final String getValidThrough() {
        String value = hgbRefs.get("validThrough");
        if (value == null)
            return null;

        return value.trim();
    }

    public final String getFiscalValidSince() {
        String value = hgbRefs.get("fiscalValidSince");
        if (value == null)
            return null;

        return value.trim();
    }

    public final String getFiscalValidThrough() {
        String value = hgbRefs.get("fiscalValidThrough");
        if (value == null)
            return null;

        return value.trim();
    }

    @Override
    public boolean isGenericPosition() {
        return (getValue() != null) && getValue().contains("nicht zuordenbar");
    }
}
