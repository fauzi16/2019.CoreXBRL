package xbrlcore.taxonomy;

import java.io.IOException;

import xbrlcore.exception.TaxonomyCreationException;
import xbrlcore.exception.XBRLException;

/**
 * Locates taxonomy files and loads them through an {@link AbstractTaxonomyLoader}.
 * 
 * @param <ResultType> the data type to return as the concrete taxonomy representation
 * @param <TS> taxonomy scheme type
 * 
 * @author Marvin Froehlich (INFOLOG GmbH)
 */
public interface AbstractTaxonomyLocator<ResultType, TS> {
    public ResultType loadTaxonomy(String taxonomyResource)
                    throws IOException, TaxonomyCreationException, XBRLException;
}
