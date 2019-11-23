package xbrlcore.taxonomy;

/**
 * <p>
 * This class serves as a helper to build a bridge between our local version of a Namespace and the JDOM Namespace.
 * It doesn't do more than providing a single method {@link #toJDOM(Namespace)}, that converts an xbrlcore Namespace
 * to a JDOM namespace.
 * </p>
 * <p>
 * This is necessary to not have a direct link between xbrlcore and JDOM. If this method resided in {@link Namespace} instead of this class,
 * You wouldn't be able to use use this library without JDOM in the classpath, even if you would never actually use it.
 * </p>
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public class NamespaceJDOMAdapter
{
    /**
     * Converts an xbrlcore {@link Namespace} to a JDOM Namespace instance.
     * You may want to do a static import to easier use this method like this.
     * <pre>
     * import static xbrlcore.taxonomy.NamespaceJDOMAdapter.toJDOM;
     * </pre>
     * 
     * @param namespace
     * @return
     */
    public static final org.jdom.Namespace toJDOM(xbrlcore.taxonomy.Namespace namespace) {
        if (namespace.jdomNS == null) {
            namespace.jdomNS = org.jdom.Namespace.getNamespace(namespace.getPrefix(), namespace.getURI());
        }

        return (org.jdom.Namespace)namespace.jdomNS;
    }
}
