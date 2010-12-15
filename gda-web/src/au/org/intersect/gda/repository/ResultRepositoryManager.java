/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultFilesDTO;
import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;
import au.org.intersect.gda.repository.fedora.FedoraException;
import au.org.intersect.gda.repository.fedora.ResultSearchPagination;


/**
 * @version $Rev$
 * 
 */
public interface ResultRepositoryManager
{
    /** 
     * @praram result
     *            the result to be stored in the repository. It is the repository's duty to set the created date, the
     *            lastModified date and the ID.
     * @throws RepositoryException
     */
    ResultDTO createResultInRepository(ResultDTO result) throws RepositoryException;

    /**
     * @param includeProjectResults
     * @return
     * @throws RepositoryException
     */
    List<ResultDTO> getAllResults(boolean includeProjectResults) throws RepositoryException;
    
    /**
     * @param username 
     * @param includeProjectResults
     * @return
     * @throws RepositoryException
     */
    List<ResultDTO> getResultsByOwner(String username, boolean includeProjectResults) throws RepositoryException;
    
    /**
     * @param username 
     * @param projIdList
     * @return
     * @throws RepositoryException
     */
    List<ResultDTO> getResultsInProjects(String username, List<String> projIdList) throws RepositoryException;

    /**
     * @param id
     *            the unique id associated with a particular result
     * @throws RepositoryException
     */
    ResultDTO getResult(String id) throws RepositoryException;

    /**
     * Get all the properties relating to the result
     * @return Map<key, value>
     * 
     * @param resultId
     * @throws RepositoryException 
     */
    Map<String, String> listAllMetadata(String resultId) throws RepositoryException;
    
    /**
     * Set the type of the result. If the type already exist, then edit the
     * existing type.
     * 
     * @param resultId
     * @throws RepositoryException 
     */
    void setName(String resultId, String name) throws RepositoryException;

    /**
     * Set the type of the result. If the type already exist, then edit the
     * existing type.
     * 
     * @param resultId
     * @throws RepositoryException 
     */
    void setType(String resultId, String type) throws RepositoryException;
    
    /**
     * Get metadata
     * @param resultId
     * @param key
     * @return
     * @throws RepositoryException 
     */
    Document getMetaData(String resultId, String metadataId) throws RepositoryException;

    /**
     * Add some metadata that is related result.
     * 
     * @param resultId
     * @param content
     * @return the metaData id
     * @throws RepositoryException
     */
    String addMetaData(String resultId, String label, Document content) throws RepositoryException;
    
    
    /**
     * Replace the existing metadata with the one specified.
     * 
     * @param resultId
     * @param metaId
     * @param content - the new metadata content
     * @throws RepositoryException
     */
    void editMetaData(String resultId, String metaId, Document content) throws RepositoryException;

    /**
     * Removes the metadata of a result.
     * 
     * @param resultId
     * @param metaId
     * @throws RepositoryException
     */
    void removeMetaData(String resultId, String metaId) throws RepositoryException;

    /**
     * Add an attachment to the result given its file reference (path)
     * @param resultId
     * @throws RepositoryException
     */
    void addAttachmentReference(String resultId, String fileRef) throws RepositoryException;

    /**
     * Retrieve the attachment's reference given its ID
     * 
     * @param resultId
     * @return
     * @throws RepositoryException
     */
    String getAttachmentReference(String resultId) throws RepositoryException; 
    
    /**
     * Remove the attachment's reference given its ID
     * 
     * @param resultId
     * @throws RepositoryException
     */
    void removeAttachmentReference(String resultId) throws RepositoryException;
    
    /**
     * Add a parent-child relationship.
     * 
     * @param childId The child result
     * @param parentId The parent result
     * @return True if the relationship was successfully created
     * @throws RepositoryException
     */
    public boolean addParent(String childId, String parentId) throws RepositoryException;
    
    /**
     * Remove a parent-child relationship
     * 
     * @param childId The child result
     * @param parentId The parent result
     * @return True if the relationship was successfully removed
     * @throws RepositoryException
     */
    public boolean removeParent(String childId, String parentId) throws RepositoryException;

    /**
     * Get a list of all parents for a child result
     * @param childId The child result
     * @return The list of parent results
     * @throws RepositoryException
     */
    public List<String> getAllParentIds(String childId) throws RepositoryException;
    
    /**
     * Get a list of all children for a parent result
     * @param parentId The parent result
     * @return The list of children results
     * @throws RepositoryException
     */
    public List<ResultDTO> getAllChildren(String parentId) throws RepositoryException;
    
    /**
     * Get a list of results matching the specified criteria
     * @param resultSearchCriteriaDTO
     * @return The list of results
     * @throws RepositoryException
     */
    public List<ResultDTO> getResultsByCriteria(ResultSearchCriteriaDTO resultSearchCriteriaDTO) 
        throws RepositoryException;
    
    /**
     * Delete a result
     */
    void deleteResult(String resultId) throws RepositoryException;

    void setOwner(String resultID, String newUser) throws RepositoryException;
    
    void addProjectId(String resultID, String projectId) throws RepositoryException;
    
    /**
     * Remove a project id from a result
     * 
     * @param resultId
     * @param projectId
     * @throws RepositoryException
     */
    void removeProjectId(String resultId, String projectId) throws RepositoryException;

    /**
     * Get/generate the ResultFilesDTO for Result (resultId)
     * @param dto
     * @throws RepositoryException 
     */
    void addAttachmentDetails(ResultFilesDTO dto) throws RepositoryException;

    /**
     * @param resultId
     * @throws RepositoryException 
     */
    ResultFilesDTO getAttachmentDetails(String resultId) throws RepositoryException;

    /**
     * @param username
     * @param page
     * @param sortColumn
     * @param order
     * @param projIdList
     * @param includeProjectResults 
     * @return
     * @throws RepositoryException
     */
    ResultSearchPagination getPaginatedResults(String username, Integer page, String sortColumn, Boolean order,
            List<String> projIdList, boolean includeProjectResults) throws RepositoryException;

    /**
     * @param preparedCriteria
     * @param currentUser 
     * @param page
     * @param sortColumn
     * @param order
     * @param projIdList 
     * @return
     * @throws RepositoryException
     */
    ResultSearchPagination getResultsByCriteria(
            ResultSearchCriteriaDTO preparedCriteria, 
            String currentUser, 
            Integer page,
            String sortColumn, 
            Boolean order, List<String> projIdList) throws RepositoryException;

    /**
     * @param childId
     * @return
     * @throws FedoraException
     */
    List<ResultDTO> getAllParents(String childId) throws FedoraException;

    /**
     * @param parentId
     * @return
     * @throws FedoraException 
     */
    List<String> getAllChildrenIds(String parentId) throws FedoraException;

    /**
     * @param resultsToExclude
     * @param name
     * @param page
     * @param sortColumn
     * @param orderDescend
     * @param projIdList
     * @return
     * @throws FedoraException 
     */
    ResultSearchPagination getPaginatedResultsExcludeResults(List<String> resultsToExclude, String name, Integer page,
            String sortColumn, Boolean orderDescend, List<String> projIdList) throws FedoraException;

    /**
     * @param currentUser
     * @param page
     * @param sortColumn
     * @param orderDescend
     * @param searchTerm
     * @return
     * @throws FedoraException 
     */
    ResultSearchPagination getPaginatedResultsSearch(String currentUser, Integer page, String sortColumn,
            Boolean orderDescend, String searchTerm) throws FedoraException;

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
