package xbrlcore.instance;

/**
 * <p>
 * A {@link SchemaRefMapping} is used by {@link InstanceOutputter}.
 * The {@link #getSchemaRefHREF(String)} method is queried for the href attribute value of a new &lt;schemaRef&gt; element of instance documents.
 * The default implementation just returns the passed parameter value back.
 * </p>
 * <p>
 * You can extend this class and override the {@link #getSchemaRefHREF(String)} method to add custom behavior.
 * As an example you can map the taxonomyName to some other value. If taxonomyName is &quot;de-gcd-2010-12-16.xsd&quot;,
 * the attribute value should be &quot;http://www.xbrl.de/taxonomies/de-gcd-2010-12-16/de-gcd-2010-12-16-shell.xsd&quot;.
 * To do this, you override the method and add the following code.
 * 
 * <pre>
 * if (taxonomyName.equals(&quot;de-gcd-2010-12-16.xsd&quot;))
 *     return &quot;http://www.xbrl.de/taxonomies/de-gcd-2010-12-16/de-gcd-2010-12-16-shell.xsd&quot;;
 * </pre>
 * </p>
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 * 
 * @see InstanceOutputter#setSchemaRefMapping(SchemaRefMapping)
 */
public class SchemaRefMapping {
    public String getSchemaRefHREF(String taxonomyName) {
        return taxonomyName;
    }
}
