/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import fedora.server.types.gen.RelationshipTuple;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultFilesDTO;
import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.repository.ResultRepositoryManager;
import au.org.intersect.gda.rest.FedoraUpdateIndexException;
import au.org.intersect.gda.rest.RESTGenericSearchIndexUpdater;
import au.org.intersect.gda.xml.XmlStringInterchange;

/**
 * @version $Rev$
 * 
 */
public class FedoraResultRepositoryManagerImpl implements ResultRepositoryManager
{
    public static final String DC_IDENTIFIER = "DC";
    public static final String XML_MIME = "text/xml";

    private static final Logger LOG = Logger.getLogger(FedoraResultRepositoryManagerImpl.class);
    private static final String ATTACHMENT_INFO_DS_ID = "ATTACHMENT_INFO";

    private FedoraRepositoryGetter fedoraRepositoryGetter;

    private FedoraRepositorySetter<ResultDTO> fedoraResultRepositorySetter;
    
    private FedoraRepositoryRelationshipManager fedoraRepositoryRelationshipManager;
    
    private FedoraRepositoryRISearcher fedoraRepositoryRISearcher;
    
    private RESTGenericSearchIndexUpdater genericSearchIndexUpdater;
    
    public FedoraResultRepositoryManagerImpl(
            FedoraRepositoryGetter fedoraRepositoryGetter,
            FedoraRepositorySetter<ResultDTO> fedoraResultRepositorySetter,
            FedoraRepositoryRelationshipManager fedoraRepositoryRelationshipManager,
            FedoraRepositoryRISearcher fedoraRepositoryRISearcher,
            RESTGenericSearchIndexUpdater genericSearchIndexUpdater)
    {
        this.fedoraRepositoryGetter = fedoraRepositoryGetter;
        this.fedoraResultRepositorySetter = fedoraResultRepositorySetter;
        this.fedoraRepositoryRelationshipManager = fedoraRepositoryRelationshipManager;
        this.fedoraRepositoryRISearcher = fedoraRepositoryRISearcher;
        this.genericSearchIndexUpdater = genericSearchIndexUpdater;
    }

    @Override
    public ResultDTO createResultInRepository(ResultDTO result) throws FedoraException
    {
        fedoraResultRepositorySetter.createObjectInRepository(result);
        updateChangedIndex(result.getId());
        return result;
    }

    private void updateChangedIndex(String resId) throws FedoraException
    {
        LOG.info("Update changed index, pid: " + resId);
        try
        {
            genericSearchIndexUpdater.updateChangedIndex(resId);
        } catch (FedoraUpdateIndexException e)
        {
            throw new FedoraException("Error updating changed index for result " + resId, e);
        }
    }
    
    private void updateDeletedIndex(String resId) throws FedoraException
    {
        LOG.info("Update deleted index, pid: " + resId);
        try
        {
            genericSearchIndexUpdater.updateDeletedIndex(resId);
        } catch (FedoraUpdateIndexException e)
        {
            throw new FedoraException("Error updating deleted index for result " + resId, e);
        }
    }

    @Override
    public List<ResultDTO> getAllResults(boolean includeProjectResults) throws FedoraException
    {
        LOG.info("Getting all results, include project results? " + includeProjectResults);
        return fedoraRepositoryGetter.getAllResults(includeProjectResults);
    }
    
    @Override
    public List<ResultDTO> getResultsByOwner(String username, boolean includeProjectResults) throws FedoraException
    {
        LOG.info("Getting results by owner, include project results? " + includeProjectResults);
        return fedoraRepositoryGetter.getResultsByOwner(username, includeProjectResults);
    }
    
    @Override
    public List<ResultDTO> getResultsInProjects(String username, List<String> projIdList) 
        throws FedoraException
    {
        LOG.info("Getting all results with project ID list specified");
        return fedoraRepositoryGetter.getResultsInProjects(username, projIdList);
    }

    @Override
    public ResultDTO getResult(String id) throws FedoraException
    {
        LOG.info("Get result with getter");
        return fedoraRepositoryGetter.getResult(id);
    }

    /**
     * Return all datastreams of a result with the specified id will
     * 
     * @throws FedoraException
     */
    @Override
    public Map<String, String> listAllMetadata(String resultId) throws FedoraException
    {
        Map<String, String> contentMap = fedoraRepositoryGetter.getAllDataStreams(resultId);
        return contentMap;
    }

    @Override
    public void setType(String resultId, String type) throws FedoraException
    {
        fedoraResultRepositorySetter.putProperty(resultId, FedoraResultProperties.TYPE, type);
        updateChangedIndex(resultId);
        LOG.info("Finished setting type");
    }

    @Override
    public void setName(String resultId, String name) throws FedoraException
    {
        fedoraResultRepositorySetter.putProperty(resultId, FedoraResultProperties.NAME, name);
        updateChangedIndex(resultId);
        LOG.info("Finished setting result name");
    }
    
    @Override
    public void setOwner(String resultId, String owner) throws FedoraException
    {
        fedoraResultRepositorySetter.putProperty(resultId, FedoraResultProperties.OWNER, owner);
        updateChangedIndex(resultId);
        LOG.info("Finished setting owner");
    }
    
    @Override
    public void addProjectId(String resultId, String projectId) throws FedoraException
    {
        String projIdStr = fedoraRepositoryGetter.getProperty(resultId, FedoraResultProperties.PROJECT_ID);
        String projIdWithBraces = "prId[" + projectId + "]";
        if (projIdStr != null && !"".equals(projIdStr))
        {
            if (!projIdStr.contains(projIdWithBraces))
            {
                LOG.info("Appending project id " + projIdStr + " to str " + projectId);
                projIdStr += ", " + projIdWithBraces;
                fedoraResultRepositorySetter.putProperty(resultId, FedoraResultProperties.PROJECT_ID, projIdStr);
                updateChangedIndex(resultId);
            }            
        } else
        {       
            LOG.info("Setting project id " + projectId);
            fedoraResultRepositorySetter.putProperty(resultId, 
                 FedoraResultProperties.PROJECT_ID, projIdWithBraces);
            updateChangedIndex(resultId);
        } 
        LOG.info("Finished setting the project id " + projectId + " for result " + resultId);
    }

    @Override
    public void removeProjectId(String resultId, String projectId) throws FedoraException
    {        
        String projIdStr = fedoraRepositoryGetter.getProperty(resultId, FedoraResultProperties.PROJECT_ID);
        if (projIdStr != null && !"".equals(projIdStr))
        {
            String singleComma = ",";
            String projIdWithBraces = "prId[" + projectId + "]";
            if (projIdStr.contains(projIdWithBraces))
            {
                LOG.info("Removing " + projIdWithBraces + " from " + projIdStr);
                projIdStr = projIdStr.replace(projIdWithBraces, "");
                projIdStr = projIdStr.replace(", ,", singleComma);
            }
            LOG.info("After removing project id, dc.source is '" + projIdStr + "'");
            postProcessProjIdString(resultId, projIdStr);
        }   
        LOG.info("Finished removing the project id " + projectId + " for result " + resultId);
    }

    /**
     * Check the project id string, strip leading and trailing spaces and ',',
     * then write back to fedora the result OR if it's empty, remove the
     * property entirely.
     */
    private void postProcessProjIdString(String resultId, String projIdStr) throws FedoraException
    {
        String singleComma = ",";
        if ("".equals(projIdStr.trim()))
        {
            LOG.info("Remove property completely");
            fedoraResultRepositorySetter.removeProperty(resultId, FedoraResultProperties.PROJECT_ID);
        } else
        {
            String processedProjIdStr = projIdStr.trim();
            // strip leading ','
            if (processedProjIdStr.startsWith(singleComma))
            {
                processedProjIdStr = processedProjIdStr.substring(2);
            }
            if (processedProjIdStr.endsWith(singleComma))
            {
                processedProjIdStr = processedProjIdStr.substring(0, processedProjIdStr.length() - 1);
            }
            LOG.info("Writing back property : " + processedProjIdStr);
            fedoraResultRepositorySetter.putProperty(resultId, FedoraResultProperties.PROJECT_ID, processedProjIdStr);
        }
        updateChangedIndex(resultId);
    }
    /**
     * Add a meta-data datastream to a result, with id automatically generated
     */
    @Override
    public String addMetaData(String resultId, String label, Document content) throws FedoraException
    {
        String returnedId = this.fedoraResultRepositorySetter.storeDataStream(resultId, null, label, XML_MIME, content);
        updateChangedIndex(resultId);
        return returnedId;
    }

    @Override
    public void editMetaData(String resultId, String metaId, Document content) throws FedoraException
    {
        this.fedoraResultRepositorySetter.editDataStream(resultId, metaId, XML_MIME, content);
        updateChangedIndex(resultId);
    }

    @Override
    public void removeMetaData(String resultId, String metaId) throws FedoraException
    {
        this.fedoraResultRepositorySetter.removeDataStream(resultId, metaId);
        updateChangedIndex(resultId);
    }

    @Override
    public Document getMetaData(String resultId, String metadataId) throws FedoraException
    {
        return fedoraRepositoryGetter.getDataStream(resultId, metadataId);
    }

    @Override
    public void addAttachmentReference(String resultId, String fileRef) throws FedoraException
    {
        fedoraResultRepositorySetter.putProperty(resultId, FedoraResultProperties.ATTACHMENT, fileRef);
        updateChangedIndex(resultId);
    }

    @Override
    public String getAttachmentReference(String resultId) throws FedoraException
    {
        return fedoraRepositoryGetter.getProperty(resultId, FedoraResultProperties.ATTACHMENT);
    }
    
    @Override
    public void removeAttachmentReference(String resultId) throws FedoraException
    {
        fedoraResultRepositorySetter.removeProperty(resultId, FedoraResultProperties.ATTACHMENT);
        updateChangedIndex(resultId);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.RepositoryManager#addParent(java.lang.String, java.lang.String)
     */
    @Override
    public boolean addParent(String childId, String parentId) throws FedoraException
    {
        return fedoraRepositoryRelationshipManager.addRelationship(childId, 
                                                                   FedoraRepositoryRelationshipManager.IS_DERIVED_FROM, 
                                                                   parentId, 
                                                                   false, 
                                                                   null);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.RepositoryManager#getAllParents(java.lang.String)
     */
    @Override
    public List<ResultDTO> getAllParents(String childId) throws FedoraException
    {
        List<ResultDTO> parentList = new ArrayList<ResultDTO>();
        
        RelationshipTuple[] relationshipArray = 
            fedoraRepositoryRelationshipManager.getRelationships(childId, 
                                                                 FedoraRepositoryRelationshipManager.IS_DERIVED_FROM);
        
        if (relationshipArray != null)
        {
            for (RelationshipTuple tuple : relationshipArray)
            {
                String parentId = tuple.getObject();
                try
                {                    
                    ResultDTO parent = getResult(parentId);
                    parentList.add(parent);    
                } catch (FedoraException e)
                {
                    StringBuffer buffer = new StringBuffer("Failed to retrieve parent with id [");
                    buffer.append(parentId);
                    buffer.append("] from repository for result with id [");
                    buffer.append(childId);
                    buffer.append("] suppress and continuing");
                    LOG.warn(buffer.toString());
                }
                
            }
        }
        
        return parentList;
    }
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.ResultRepositoryManager#getAllParentIds(java.lang.String)
     */
    @Override
    public List<String> getAllParentIds(String childId) throws RepositoryException
    {
        List<String> parentList = new ArrayList<String>();
        
        RelationshipTuple[] relationshipArray = 
            fedoraRepositoryRelationshipManager.getRelationships(childId, 
                                                                 FedoraRepositoryRelationshipManager.IS_DERIVED_FROM);
        
        if (relationshipArray != null)
        {
            for (RelationshipTuple tuple : relationshipArray)
            {
                String parentId = tuple.getObject();
                parentList.add(parentId);
            }
        }
        
        return parentList;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.RepositoryManager#removeParent(java.lang.String, java.lang.String)
     */
    @Override
    public boolean removeParent(String childId, String parentId) throws FedoraException
    {
        return fedoraRepositoryRelationshipManager.purgeRelationship(
                                                      childId,
                                                      FedoraRepositoryRelationshipManager.IS_DERIVED_FROM, 
                                                      parentId, 
                                                      false, 
                                                      null);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.RepositoryManager#getAllChildren(java.lang.String)
     */
    @Override
    public List<ResultDTO> getAllChildren(String parentId) throws FedoraException
    {
        List<ResultDTO> childList = new ArrayList<ResultDTO>();
        
        // forcing the index to update for this search
        // It is necessary for this search to be accurate due to the semantics of GDA.
        // If a parent has children, it should be "undeletable" or inconsistencies will arise
        String[] tupleArray = fedoraRepositoryRISearcher.getTuples("true",
                FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL, FedoraRepositoryRISearcher.RETURN_FORMAT_CSV, null,
                "off", "off", fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(parentId));
        
        List<String> childIdList = getResultIdListFromTupleArray(tupleArray,
                                                FedoraRepositoryRISearcher.CHILDREN_SEARCH_QUERY_PATTERN_REGEX,
                                                FedoraRepositoryRISearcher.CHILDREN_SEARCH_QUERY_ID_GROUP);
        
        for (String childId : childIdList)
        {
            ResultDTO result = this.getResult(childId);
            childList.add(result);
        }
        
        return childList;
    }
    

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.ResultRepositoryManager#getAllChildrenIds(java.lang.String)
     */
    @Override
    public List<String> getAllChildrenIds(String parentId) throws FedoraException
    {
        List<String> childList = null;
        
        String[] tupleArray = 
            fedoraRepositoryRISearcher.getTuples(FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL, 
                                                 FedoraRepositoryRISearcher.RETURN_FORMAT_CSV,
                                                 fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(parentId));
        
        childList = getResultIdListFromTupleArray(tupleArray,
                                                FedoraRepositoryRISearcher.CHILDREN_SEARCH_QUERY_PATTERN_REGEX,
                                                FedoraRepositoryRISearcher.CHILDREN_SEARCH_QUERY_ID_GROUP);
        
        return childList;
    }

   

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.RepositoryManager#getResultsByCriteria(
     * au.org.intersect.gda.dto.ResultSearchCriteriaDTO)
     */
    @Override
    public List<ResultDTO> getResultsByCriteria(ResultSearchCriteriaDTO resultSearchCriteriaDTO)
        throws FedoraException
    {
        LOG.info("getting results by criteria");
        return fedoraRepositoryGetter.getResultsByCriteria(resultSearchCriteriaDTO);
    }

    @Override
    public void deleteResult(String resultId) throws FedoraException
    {
        fedoraResultRepositorySetter.deleteObject(resultId);       
        updateDeletedIndex(resultId);
    }
    
    /*
     * Takes the results from an ri search & parses out the ids of the 
     * results we're looking for using a reg exp
     */
    private List<String> getResultIdListFromTupleArray(String[] tupleArray,
                                                        String regexp,
                                                        int idProject) throws FedoraException
    {
        List<String> resultList = new ArrayList<String>(0);
        
        if (tupleArray != null)
        {            
            Pattern idPattern = Pattern.compile(regexp);
            
            for (String tuple : tupleArray)
            {
                Matcher matcher = idPattern.matcher(tuple);
                if (matcher.matches())
                {
                    String resultId = matcher.group(idProject);
                    resultList.add(resultId);
                }
            }
        }
        
        return resultList;
    }

    @Override
    public void addAttachmentDetails(ResultFilesDTO dto) throws FedoraException
    {
        this.addAttachmentReference(dto.getResultId(), dto.getAttachmentReference());

        Document doc = XmlStringInterchange.parseString(dto.getXmlTree());

        fedoraResultRepositorySetter.storeDataStream(dto.getResultId(), ATTACHMENT_INFO_DS_ID,
                "attached files and details", XML_MIME, doc);
    }

    @Override
    public ResultFilesDTO getAttachmentDetails(String resultId) throws FedoraException
    {
        ResultFilesDTO dto = new ResultFilesDTO();
        dto.setResultId(resultId);

        dto.setAttachmentReference(this.getAttachmentReference(resultId));
        Document xml = fedoraRepositoryGetter.getDataStream(resultId, ATTACHMENT_INFO_DS_ID);
        String asString = XmlStringInterchange.toString(xml);
        dto.setXmlTree(asString);
        return dto;
    }

  

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.ResultRepositoryManager
     *      #getAllResults(java.lang.String, java.lang.Integer, java.lang.String, java.lang.Boolean, java.util.List)
     */
    @Override
    public ResultSearchPagination getPaginatedResults(String username, Integer page, String sortColumn, Boolean order,
            List<String> projIdList, boolean includeProjectResults) throws FedoraException
    {
        return fedoraRepositoryGetter.getPaginatedResults(
                username, page, sortColumn, order, projIdList, includeProjectResults);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.ResultRepositoryManager
     *      #getResultsByCriteria(
     *          au.org.intersect.gda.dto.ResultSearchCriteriaDTO, 
     *          java.lang.Integer, 
     *          java.lang.String, 
     *          java.lang.Boolean)
     */
    @Override
    public ResultSearchPagination getResultsByCriteria(ResultSearchCriteriaDTO resultSearchCriteriaDTO, 
            String ownerName,
            Integer page,
            String sortColumn, 
            Boolean order,
            List<String> projIdList) throws FedoraException
    {
        return fedoraRepositoryGetter.getResultsByCriteria(
                resultSearchCriteriaDTO, 
                ownerName, 
                page, 
                sortColumn, 
                order,
                projIdList);

    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.ResultRepositoryManager
     *      #getPaginatedResultsExcludeResults(
     *      java.util.List, java.lang.String, java.lang.Integer, java.lang.String, java.lang.Boolean, java.util.List)
     */
    @Override
    public ResultSearchPagination getPaginatedResultsExcludeResults(
            List<String> resultsToExclude, String username,
            Integer page, String sortColumn, Boolean orderDescend, List<String> projIdList) throws FedoraException
    {
        return fedoraRepositoryGetter.getPaginatedResultsExcludeResults(
                resultsToExclude, username, page, sortColumn, orderDescend, projIdList);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.ResultRepositoryManager#getPaginatedResults(
     *      java.lang.String, java.lang.Integer, java.lang.String, java.lang.Boolean, java.lang.String)
     */
    @Override
    public ResultSearchPagination getPaginatedResultsSearch(String username, Integer page, String sortColumn,
            Boolean orderDescend, String searchTerm) throws FedoraException
    {
        return fedoraRepositoryGetter.getPaginatedResults(
                username, page, sortColumn, orderDescend, searchTerm);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.ResultRepositoryManager
     *      #getPaginatedResultsExcludeResultsSearch(
     *          java.util.List, java.lang.String, java.lang.Integer, 
     *          java.lang.String, java.lang.Boolean, java.util.List, 
     *          java.lang.String)
     */
    @Override
    public ResultSearchPagination getPaginatedResultsExcludeResultsSearch(List<String> resultsToExclude,
            String username, Integer page, String sortColumn, Boolean orderDescend, List<String> projIdList,
            String searchTerm) throws FedoraException
    {
        return fedoraRepositoryGetter.getPaginatedResultsExcludeResultsSearch(
                resultsToExclude, username, page, sortColumn, orderDescend, projIdList, searchTerm);
    }


}
