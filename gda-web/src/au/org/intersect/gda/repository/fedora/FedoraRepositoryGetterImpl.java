/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import fedora.server.types.gen.ObjectFields;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;
import au.org.intersect.gda.metadata.GdaObjectMetaDataHelper;
import au.org.intersect.gda.util.DateParser;
import au.org.intersect.gda.xml.ResultSchemaHelper;

/**
 * @version $Rev$
 * 
 */
public class FedoraRepositoryGetterImpl implements FedoraRepositoryGetter
{
    private static final Logger LOG = Logger.getLogger(FedoraRepositoryGetterImpl.class);

    private final FedoraDatastreamHelper fedoraDsHelper;
    
    private final GdaObjectMetaDataHelper objMetaDataHelper;

    private final FedoraRepositoryRESTSearcher fedoraRepositoryRestSearcher;
    
    private final FedoraRepositorySearcher fedoraRepositorySearcher;
    
    private final ResultSchemaHelper resultSchemaHelper;
    
    public FedoraRepositoryGetterImpl(FedoraDatastreamHelper fedoraDsHelper, 
                                      GdaObjectMetaDataHelper objMetaDataHelper,
                                      FedoraRepositoryRESTSearcher fedoraRepositoryRestSearcher,
                                      FedoraRepositorySearcher fedoraRepositorySearcher,
                                      ResultSchemaHelper resultSchemaHelper)
    {
        this.fedoraDsHelper = fedoraDsHelper;
        this.objMetaDataHelper = objMetaDataHelper;
        this.fedoraRepositoryRestSearcher = fedoraRepositoryRestSearcher;
        this.fedoraRepositorySearcher = fedoraRepositorySearcher;
        this.resultSchemaHelper = resultSchemaHelper;
    }
    
    @Override
    public List<ResultDTO> getAllResults(boolean includeProjectResults) 
        throws FedoraException
    {
        LOG.info("Getting all results, include project results? " + includeProjectResults);
        try
        {            
            String uri = fedoraRepositoryRestSearcher.getRetrieveAllResultQuery(includeProjectResults);
            LOG.info(uri);
            List<ResultDTO> results = fedoraRepositoryRestSearcher.search(uri);
            return results;
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search for all results failed", e);
        }
    }
   
    @Override
    public List<ResultDTO> getResultsByOwner(String ownerName, boolean includeProjectResults) 
        throws FedoraException
    {
        LOG.info("Getting results own by " + ownerName + " include project results? " 
                 + includeProjectResults);
        try
        {            
            String uri = fedoraRepositoryRestSearcher.getRetrieveResultsForUserQuery(ownerName, 
                            includeProjectResults);
            LOG.info(uri);
            List<ResultDTO> results = fedoraRepositoryRestSearcher.search(uri);
            return results;
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search for results by owner failed", e);
        }
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryGetter#
     * getAllResults(java.lang.String, java.util.List)
     */
    @Override
    public List<ResultDTO> getResultsInProjects(String ownerName, List<String> projIdList) throws FedoraException
    {
        LOG.info("Getting all results in projects for " + ownerName);
        try
        {            
            String uri = fedoraRepositoryRestSearcher.getRetrieveResultsInProjectQuery(ownerName, projIdList);
            LOG.info(uri);
            List<ResultDTO> results = fedoraRepositoryRestSearcher.search(uri);
            return results;
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search for results in project failed", e);
        }
    }   
    /**
     * Get the property from the result in fedora - this will retrieved from the
     * Dublin Core datastream
     * 
     * @throws FedoraException
     */
    @Override
    public String getProperty(String resultId, FedoraResultProperties prop) 
        throws FedoraException
    {
        Document dcDoc = getDcStreamAsDoc(resultId);
        return objMetaDataHelper.getProperty(dcDoc, prop);
    }
    
    @Override
    public ResultDTO getResult(String id) throws FedoraException
    {
        ResultDTO result = makeResultDTO(id, fedoraRepositorySearcher.findResult(id));
        return result;
    }

    @Override
    public Map<String, String> getAllDataStreams(String objectId) throws FedoraException
    {
        return fedoraDsHelper.getAllDataStreams(objectId);
    }

    @Override
    public Document getDataStream(String objectId, String dsId) throws FedoraException
    {
        return fedoraDsHelper.getDataStream(objectId, dsId);
    }

    @Override
    public List<ResultDTO> getResultsByCriteria(ResultSearchCriteriaDTO criteria) throws FedoraException
    {
        try
        {            
            String uri = fedoraRepositoryRestSearcher.getCriteriaSearchQuery(criteria);        
            LOG.info("Uri:" + uri);
            return fedoraRepositoryRestSearcher.search(uri);
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search by criteria failed", e);
        }
    }
    
    @Override
    public boolean hasProperty(String resultId, FedoraResultProperties prop) throws FedoraException
    {
        Document dcDoc = getDcStreamAsDoc(resultId);
        return objMetaDataHelper.hasProperty(dcDoc, prop);
    }

    private Document getDcStreamAsDoc(String resultId) 
        throws FedoraException
    {        

        Document dcDoc = fedoraDsHelper.getDataStream(resultId, 
                FedoraResultRepositoryManagerImpl.DC_IDENTIFIER);
        return dcDoc;
    }
    
    private ResultDTO makeResultDTO(String id, ObjectFields o) throws FedoraException
    {
        ResultDTO result = new ResultDTO();
        String createdDate = o.getCDate();
        String lastModifiedDate = o.getMDate();
        setPropertiesInResult(result, id, createdDate, lastModifiedDate);
        return result;
    }
   
    private void setPropertiesInResult(ResultDTO result, String id, String createdDate, String lastModifiedDate)
        throws FedoraException
    {
        Document dcDoc = null;
        dcDoc = getDcStreamAsDoc(id);
        String name = objMetaDataHelper.getProperty(dcDoc, FedoraResultProperties.NAME);
        String owner = objMetaDataHelper.getProperty(dcDoc, FedoraResultProperties.OWNER);
        String type = objMetaDataHelper.getProperty(dcDoc, FedoraResultProperties.TYPE);
        String creator = objMetaDataHelper.getProperty(dcDoc, FedoraResultProperties.CREATOR);
        boolean hasAttachment = objMetaDataHelper.hasProperty(dcDoc, FedoraResultProperties.ATTACHMENT);
        
        // now set all properties in result
        result.setId(id);
        setDatesInResult(result, createdDate, lastModifiedDate);
        result.setName(name);
        result.setOwner(owner);
        result.setType(type);
        result.setTypeDisplayName(this.resultSchemaHelper.getTypeDisplayName(type));
        result.setCreator(creator);
        result.setHasAttachment(hasAttachment);
    }

    private void setDatesInResult(ResultDTO result, String createdDate, String lastModifiedDate) throws FedoraException
    {
        try
        {
            result.setCreatedDate(DateParser.parse(createdDate));
            result.setLastModifiedDate(DateParser.parse(lastModifiedDate));
        } catch (ParseException e)
        {
            throw new FedoraException("Unable to parse date", e);
        }
    }

   
    @Override
    public ResultSearchPagination getPaginatedResults(String username, Integer page, String sortColumn, Boolean order,
            List<String> projIdList, boolean includeProjectResults) throws FedoraException
    {
        String uri = 
            fedoraRepositoryRestSearcher.getRetrievePaginatedResultQuery(
                    username, page, sortColumn, order, projIdList, includeProjectResults);
        LOG.info(uri);
        
        try
        {
            return fedoraRepositoryRestSearcher.paginatedSearch(uri);
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search for all paginated results failed", e);
        }
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryGetter
     *      #getResultsByCriteria(
     *          au.org.intersect.gda.dto.ResultSearchCriteriaDTO, 
     *          java.lang.Integer, 
     *          java.lang.String, 
     *          java.lang.Boolean)
     */
    @Override
    public ResultSearchPagination getResultsByCriteria(
            ResultSearchCriteriaDTO resultSearchCriteriaDTO, 
            String ownerName,
            Integer page,
            String sortColumn, 
            Boolean order,
            List<String> projIdList) throws FedoraException
    {
        try
        {            
            String uri = fedoraRepositoryRestSearcher.getPaginatedCriteriaSearchQuery(
                    resultSearchCriteriaDTO,
                    ownerName,
                    page,
                    sortColumn,
                    order,
                    projIdList);        
            LOG.info("Uri:" + uri);
            return fedoraRepositoryRestSearcher.paginatedSearch(uri);
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search by criteria failed", e);
        }
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryGetter
     *      #getPaginatedResultsExcludeResults(
     *      java.util.List, java.lang.String, java.lang.Integer, java.lang.String, java.lang.Boolean, java.util.List)
     */
    @Override
    public ResultSearchPagination getPaginatedResultsExcludeResults(List<String> resultsToExclude, String username,
            Integer page, String sortColumn, Boolean orderDescend, List<String> projIdList) throws FedoraException
    {
        String uri = 
            fedoraRepositoryRestSearcher.getRetrievePaginatedResultExcludeResultsQuery(
                    resultsToExclude, username, page, sortColumn, orderDescend, projIdList);
        LOG.info(uri);
        
        try
        {
            return fedoraRepositoryRestSearcher.paginatedSearch(uri);
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search for exclude results paginated results failed", e);
        }
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryGetter#getPaginatedResults(
     *      java.lang.String, java.lang.Integer, java.lang.String, java.lang.Boolean, java.lang.String)
     */
    @Override
    public ResultSearchPagination getPaginatedResults(String username, Integer page, String sortColumn,
            Boolean orderDescend, String searchTerm) throws FedoraException
    {
        try
        { 
            String uri = 
                fedoraRepositoryRestSearcher.getRetrievePaginatedResultSearchTerm(
                    username, page, sortColumn, orderDescend, searchTerm);
        
            return fedoraRepositoryRestSearcher.paginatedSearch(uri);
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search for search term paginated results failed", e);
        }
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryGetter
     *      #getPaginatedResultsExcludeResultsSearch(
     *          java.util.List, java.lang.String, java.lang.Integer, java.lang.String, 
     *          java.lang.Boolean, java.util.List, java.lang.String)
     */
    @Override
    public ResultSearchPagination getPaginatedResultsExcludeResultsSearch(List<String> resultsToExclude,
            String username, Integer page, String sortColumn, Boolean orderDescend, List<String> projIdList,
            String searchTerm) throws FedoraException
    {
       
        try
        { 
            String uri = 
                fedoraRepositoryRestSearcher.getRetrievePaginatedResultExcludeResultsQuery(
                    resultsToExclude, username, page, sortColumn, orderDescend, projIdList, searchTerm);
        
            return fedoraRepositoryRestSearcher.paginatedSearch(uri);
        } catch (FedoraSearchException e)
        {
            throw new FedoraException("Search for exclude results paginated results failed", e);
        }
    }   


}
