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
import java.util.Map;

import org.w3c.dom.Document;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;

/**
 * @version $Rev$
 *
 */
public interface FedoraRepositoryGetter
{

    /**
     * @param ownerName 
     * @param includeProjectResults
     * @return
     * @throws FedoraException 
     */
    List<ResultDTO> getResultsByOwner(String ownerName, boolean includeProjectResults) throws FedoraException;

    /**
     * @param ownerName 
     * @param projIdList
     * @return
     * @throws FedoraException 
     */
    List<ResultDTO> getResultsInProjects(String ownerName, List<String> projIdList) throws FedoraException;
    
    /**
     * @param id
     * @return
     * @throws FedoraException 
     */
    ResultDTO getResult(String id) throws FedoraException;

    /**
     * @return a map containing datastream id as key and the string content 
     * as value
     * @throws FedoraException 
     */
    Map<String, String> getAllDataStreams(String objectId) throws FedoraException;

    /**
     * @param objectId 
     *        pid of a created result
     * @param dsId
     *        the datastream id
     * @return the content of the datastream
     * @throws FedoraException 
     */
    Document getDataStream(String objectId, String dsId) throws FedoraException;

    /**
     * @param criteria
     * @return
     * @throws FedoraException 
     */
    List<ResultDTO> getResultsByCriteria(ResultSearchCriteriaDTO criteria) throws FedoraException;
    
    /**
     * @param resultId
     * @param prop
     * @return
     * @throws FedoraException
     */
    String getProperty(String resultId, FedoraResultProperties prop) throws FedoraException;
    
    boolean hasProperty(String resultId, FedoraResultProperties prop) throws FedoraException;

    /**
     * @param includeProjectResults
     * @return
     * @throws FedoraException
     */
    List<ResultDTO> getAllResults(boolean includeProjectResults) throws FedoraException;
    /**
     * @param username
     * @param page
     * @param sortColumn
     * @param order
     * @param projIdList
     * @param includeProjectResults 
     * @return
     * @throws FedoraException 
     */
    ResultSearchPagination getPaginatedResults(String username, Integer page, String sortColumn, Boolean order,
            List<String> projIdList, boolean includeProjectResults) throws FedoraException;

    /**
     * @param resultSearchCriteriaDTO
     * @param ownerName 
     * @param page
     * @param sortColumn
     * @param order
     * @param projIdList 
     * @return
     * @throws FedoraException 
     */
    ResultSearchPagination getResultsByCriteria(ResultSearchCriteriaDTO resultSearchCriteriaDTO, 
            String ownerName, 
            Integer page,
            String sortColumn, 
            Boolean order, List<String> projIdList) throws FedoraException;

    /**
     * @param resultsToExclude
     * @param username
     * @param page
     * @param sortColumn
     * @param orderDescend
     * @param projIdList
     * @return
     * @throws FedoraException 
     */
    ResultSearchPagination getPaginatedResultsExcludeResults(List<String> resultsToExclude, String username,
            Integer page, String sortColumn, Boolean orderDescend, List<String> projIdList) throws FedoraException;

    /**
     * @param username
     * @param page
     * @param sortColumn
     * @param orderDescend
     * @param searchTerm
     * @return
     * @throws FedoraException 
     */
    ResultSearchPagination getPaginatedResults(String username, Integer page, String sortColumn, Boolean orderDescend,
            String searchTerm) throws FedoraException;

    /**
     * @param resultsToExclude
     * @param username
     * @param page
     * @param sortColumn
     * @param orderDescend
     * @param projIdList
     * @param searchTerm
     * @return
     * @throws FedoraException 
     */
    ResultSearchPagination getPaginatedResultsExcludeResultsSearch(List<String> resultsToExclude, String username,
            Integer page, String sortColumn, Boolean orderDescend, List<String> projIdList, String searchTerm) 
        throws FedoraException;

}
