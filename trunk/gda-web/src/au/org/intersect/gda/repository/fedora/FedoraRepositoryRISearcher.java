/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;

/**
 * @version $Rev$
 *
 */
public interface FedoraRepositoryRISearcher
{
    
    String QUERY_LANGUAGE_ITQL = "iTQL";
    String RETURN_FORMAT_CSV = "CSV";
    
    // Reg exp to get child result ids from the response of the query -- project 1
    // will match the child id
    int CHILDREN_SEARCH_QUERY_ID_GROUP = 1;
    String CHILDREN_SEARCH_QUERY_PATTERN_REGEX = "^GDA:\\d+,info:fedora/(GDA:\\d+)$";
    
    // Reg exp to get result ids from the response of a criteria search -- project 1
    // will match the result id
    int CRITERIA_SEARCH_QUERY_ID_GROUP = 1;
    String CRITERIA_SEARCH_QUERY_PATTERN_REGEX = "^info:fedora/(GDA:\\d+)$";
        
    /**
     * Generate an iTQL search query to find the children of the specified parent
     * @param parentId The parent result id
     */
    public String getChildrenSearchITQLQuery(String parentId);
    
    /**
     * Generate an itql search query to retrieve the ids of results that match the
     * given criteria
     * @param criteria The search criteria
     * @return The query string
     */
    public String getCriteriaSearchITQLQuery(ResultSearchCriteriaDTO criteria);
    
    /**
     * Search the Fedora Repository Resource Index, requesting tuples. Uses default
     * values for unspecified parameters
     * 
     * @param lang The query language (eg iTQL or sparql)
     * @param format The output format (eg CSV, Simple, Sparql, TSV, count)
     * @param query The query to run
     * @return The tuples as strings
     */
    public String[] getTuples(String lang, 
                              String format, 
                              String query) throws FedoraException;
    
    /**
     * Search the Fedora Repository Resource Index, requesting tuples
     * 
     * @param flush Flush recently added triples to the triplestore ("true" or "false")
     * @param lang The query language (eg iTQL or sparql)
     * @param format The output format (eg CSV, Simple, Sparql, TSV, count)
     * @param limit Limit the number of tuples returned (numeric value)
     * @param distinct Only return distinct tuples ("on" or "off")
     * @param stream Stream the results right away ("on" or "off")
     * @param query The query to run
     * @return The tuples as strings
     */
    public String[] getTuples(String flush,
                              String lang, 
                              String format, 
                              String limit, 
                              String distinct,
                              String stream,
                              String query) throws FedoraException;
}
