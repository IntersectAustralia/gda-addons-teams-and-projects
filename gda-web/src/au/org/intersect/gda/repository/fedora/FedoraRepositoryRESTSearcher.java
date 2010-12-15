/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.util.List;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;

/**
 * @version $Rev$
 *
 */
public interface FedoraRepositoryRESTSearcher
{
    /**
     * Send the query uri to the Fedora server to retrieve a list of ResultDTOs
     * that matches the query.
     * 
     * @param uri
     * @return the list of ResultDTO matching the query
     * @throws FedoraSearchException
     */
    List<ResultDTO> search(String uri) throws FedoraSearchException;
    
    /**
     * Construct the query uri given the ResultSearchCriteriaDTO.
     * 
     * @param criteria
     * @return the query uri constructed
     * @throws FedoraSearchException 
     */
    String getCriteriaSearchQuery(ResultSearchCriteriaDTO criteria) throws FedoraSearchException;
    
    /**
     * Construct the query uri to get all results in the repository
     * @param ownerName the username of the results owner
     * @param project id list
     * 
     * @return
     */
    String getRetrieveResultsInProjectQuery(String ownerName, List<String> projIdList);
    
    /**
     * Construct the query uri to return the result that matches the given id
     * @param resultId
     * @return
     * @throws FedoraSearchException 
     */
    String getRetrieveSingleResultQuery(String resultId) throws FedoraSearchException;

    /**
     * Construct the query uri to return all results 
     * 
     * @param includResultsInProject whether results have project id property
     * @return
     */
    String getRetrieveAllResultQuery(boolean includResultsInProject);

    /**
     * Construct the query uri to return all results owned by a user
     * 
     * @param ownerName
     * @param includeResultsInProject
     * @return
     */
    String getRetrieveResultsForUserQuery(String ownerName, boolean includeResultsInProject);
    
    /**
     * @param uri
     * @return
     * @throws FedoraSearchException 
     * @throws FedoraException 
     */
    ResultSearchPagination paginatedSearch(String uri) throws FedoraSearchException, FedoraException;

    /**
     * @param username
     * @param page
     * @param sortColumn
     * @param order
     * @param projIdList
     * @param includeProjectResults
     * @return
     */
    String getRetrievePaginatedResultQuery(String username, Integer page, String sortColumn, Boolean order,
            List<String> projIdList, boolean includeProjectResults);

    /**
     * @param resultSearchCriteriaDTO
     * @param ownerName 
     * @param page
     * @param sortColumn
     * @param order
     * @param projIdList 
     * @return
     * @throws FedoraSearchException 
     */
    String getPaginatedCriteriaSearchQuery(ResultSearchCriteriaDTO resultSearchCriteriaDTO, 
            String ownerName, 
            Integer page,
            String sortColumn, 
            Boolean order, List<String> projIdList) throws FedoraSearchException;

    /**
     * @param resultsToExclude
     * @param username
     * @param page
     * @param sortColumn
     * @param orderDescend
     * @param projIdList
     * @return
     */
    String getRetrievePaginatedResultExcludeResultsQuery(List<String> resultsToExclude, String username, Integer page,
            String sortColumn, Boolean orderDescend, List<String> projIdList);

    /**
     * @param username
     * @param page
     * @param sortColumn
     * @param orderDescend
     * @param searchTerm
     * @return
     * @throws FedoraSearchException 
     */
    String getRetrievePaginatedResultSearchTerm(String username, Integer page, String sortColumn, Boolean orderDescend,
            String searchTerm) throws FedoraSearchException;

    /**
     * @param resultsToExclude
     * @param username
     * @param page
     * @param sortColumn
     * @param orderDescend
     * @param projIdList
     * @param searchTerm
     * @return
     * @throws FedoraSearchException 
     */
    String getRetrievePaginatedResultExcludeResultsQuery(List<String> resultsToExclude, String username, Integer page,
            String sortColumn, Boolean orderDescend, List<String> projIdList, String searchTerm) 
        throws FedoraSearchException;
}
