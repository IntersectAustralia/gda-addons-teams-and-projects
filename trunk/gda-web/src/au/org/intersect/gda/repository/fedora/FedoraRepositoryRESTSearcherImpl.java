/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;
import au.org.intersect.gda.http.GdaHttpClient;
import au.org.intersect.gda.http.GdaHttpClientException;
import au.org.intersect.gda.http.GdaHttpClientGetResponse;
import au.org.intersect.gda.rest.RESTGenericSearchInterpreter;
import au.org.intersect.gda.rest.RESTSearchParamHelper;
import au.org.intersect.gda.rest.RESTSearchQueryUriBuilder;
import au.org.intersect.gda.rest.ResultSearchPageHit;

/**
 * @version $Rev$
 *
 */
public class FedoraRepositoryRESTSearcherImpl implements FedoraRepositoryRESTSearcher
{    
    private static final Logger LOG = Logger.getLogger(FedoraRepositoryRESTSearcherImpl.class);

    private final RESTSearchQueryUriBuilder restSearchQueryUriBuilder;
    
    private final RESTGenericSearchInterpreter restSearchResultInterpreter;
    
    private final GdaHttpClient gdaHttpClient;
    
    private final RESTSearchParamHelper paramHelper;

    
    
    public FedoraRepositoryRESTSearcherImpl(RESTSearchQueryUriBuilder restSearchQueryUriBuilder, 
            RESTGenericSearchInterpreter restSearchResultInterpreter, 
            GdaHttpClient gdaHttpClient,
            RESTSearchParamHelper paramHelper)
    {
        this.restSearchQueryUriBuilder = restSearchQueryUriBuilder;
        this.restSearchResultInterpreter = restSearchResultInterpreter;
        this.gdaHttpClient = gdaHttpClient;
        this.paramHelper = paramHelper;
        
    }
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRESTSearcher#
     * getCriteriaSearchQuery(au.org.intersect.gda.dto.ResultSearchCriteriaDTO)
     */
    @Override
    public String getCriteriaSearchQuery(ResultSearchCriteriaDTO criteria) throws FedoraSearchException
    {
        try
        {
            return restSearchQueryUriBuilder.getUri(criteria);
        } catch (UnsupportedEncodingException e)
        {
            throw new FedoraSearchException(e);
        }
    }
    
    @Override
    public String getRetrieveAllResultQuery(boolean includeResultsInProject) 
    {    
        if (includeResultsInProject)
        {
            return restSearchQueryUriBuilder.getUriForAllResults()
                + restSearchQueryUriBuilder.getDefaultSortParam();
        }
        return restSearchQueryUriBuilder.getUriForAllResultsNotInProjects()
            + restSearchQueryUriBuilder.getDefaultSortParam();
    }
    
    @Override
    public String getRetrieveResultsForUserQuery(String ownerName, boolean includeResultsInProject) 
    {    
        if (includeResultsInProject)
        {
            return restSearchQueryUriBuilder.getUriForAllResults(ownerName)
                + restSearchQueryUriBuilder.getDefaultSortParam();
        } 
        return restSearchQueryUriBuilder.getUriForAllResultsNotInProjects(ownerName)
            + restSearchQueryUriBuilder.getDefaultSortParam();
        
    }
    
    @Override
    public String getRetrievePaginatedResultQuery(
            String ownerName, 
            Integer page, 
            String sortColumn, 
            Boolean order, 
            List<String> projIdList, 
            boolean includeProjectResults) 
    {            
        int hitPage = paramHelper.resolvePageToHitStart(page);
        
        int hitPageSize = paramHelper.getHitPageSize();
        
        String sortField = paramHelper.resolveColumnToField(sortColumn);
        
        String sortOrder = paramHelper.resolveSortOrder(order);
        
        String baseQueryUri;
        if (includeProjectResults)
        {
            baseQueryUri = restSearchQueryUriBuilder.getUriForAllResults(ownerName, projIdList);
        } else
        {
            if (StringUtils.isEmpty(ownerName))
            {
                baseQueryUri = restSearchQueryUriBuilder.getUriForAllResultsNotInProjects();    
            } else
            {
                baseQueryUri = restSearchQueryUriBuilder.getUriForAllResultsNotInProjects(ownerName);    
            }
        }        
        
        String paginationParamUri = restSearchQueryUriBuilder.getPaginationParamUri(
                hitPage, hitPageSize, sortField, sortOrder);
        
        StringBuffer buffer = new StringBuffer(baseQueryUri);
        buffer.append(paginationParamUri);
        return buffer.toString();
    }
    
    @Override
    public String getRetrieveResultsInProjectQuery(String ownerName, List<String> projIdList) 
    {    
        return restSearchQueryUriBuilder.getUriForAllResults(ownerName, projIdList)
            + restSearchQueryUriBuilder.getDefaultSortParam();
    }
    
    @Override
    public String getRetrieveSingleResultQuery(String resultId) throws FedoraSearchException
    {
        try
        {
            return restSearchQueryUriBuilder.getUriForSingleResult(resultId);
        } catch (UnsupportedEncodingException e)
        {
            throw new FedoraSearchException(e);
        }   
    }
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRESTSearcher#
     * search(java.lang.String)
     */
    @Override
    public List<ResultDTO> search(String uri) throws FedoraSearchException
    {        
        try
        {
            GdaHttpClientGetResponse getResponse = gdaHttpClient.get(uri);
            Node root = getResponse.getResponse();
            return restSearchResultInterpreter.translate(root);
        } catch (GdaHttpClientException e1)
        {
            throw new FedoraSearchException(e1);
        } catch (XPathExpressionException e2)
        {
            throw new FedoraSearchException(e2);
        }
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRESTSearcher#paginatedSearch(java.lang.String)
     */
    @Override
    public ResultSearchPagination paginatedSearch(String uri) throws FedoraSearchException, FedoraException
    {
        if (LOG.isInfoEnabled())
        {
            StringBuffer buffer = new StringBuffer("Executing paginated query with uri: ");
            buffer.append(uri);
            LOG.info(buffer.toString());
        }
        try
        {
            GdaHttpClientGetResponse getResponse = gdaHttpClient.get(uri);
            Node root = getResponse.getResponse();
            ResultSearchPageHit pageHitResult = restSearchResultInterpreter.translatePaginatedResult(root);
            
            int currentPage = paramHelper.convertHitToPage(pageHitResult.getCurrentHit());
            
            int totalPage = paramHelper.convertHitToPage(pageHitResult.getTotalHit());
            
            ResultSearchPagination paginatedResult = 
                new ResultSearchPagination(
                        pageHitResult.getTotalHit(),
                        totalPage, 
                        currentPage, 
                        pageHitResult.getResults());            
            return paginatedResult;
        } catch (GdaHttpClientException e1)
        {
            throw new FedoraSearchException(e1);
        } catch (XPathExpressionException e2)
        {
            throw new FedoraSearchException(e2);
        }
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRESTSearcher
     *      #getPaginatedCriteriaSearchQuery(
     *          au.org.intersect.gda.dto.ResultSearchCriteriaDTO, 
     *          java.lang.Integer, 
     *          java.lang.String, 
     *          java.lang.Boolean)
     */
    @Override
    public String getPaginatedCriteriaSearchQuery(ResultSearchCriteriaDTO resultSearchCriteriaDTO, 
            String ownerName,
            Integer page,
            String sortColumn, Boolean order,
            List<String> projIdList) throws FedoraSearchException
    {
        int hitPage = paramHelper.resolvePageToHitStart(page);
        
        int hitPageSize = paramHelper.getHitPageSize();
        
        String sortField = paramHelper.resolveColumnToField(sortColumn);
        
        String sortOrder = paramHelper.resolveSortOrder(order);
        
        try
        {
            String baseQueryUri =  
                restSearchQueryUriBuilder.getUri(resultSearchCriteriaDTO, ownerName, projIdList);
            String paginationParamUri = restSearchQueryUriBuilder.getPaginationParamUri(
                    hitPage, hitPageSize, sortField, sortOrder);
            
            StringBuffer buffer = new StringBuffer(baseQueryUri);
            buffer.append(paginationParamUri);
            return buffer.toString();
        } catch (UnsupportedEncodingException e)
        {
            throw new FedoraSearchException(e);
        }
    }


    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRESTSearcher
     *      #getRetrievePaginatedResultExcludeResultsQuery(
     *      java.util.List, java.lang.String, java.lang.Integer, java.lang.String, java.lang.Boolean, java.util.List)
     */
    @Override
    public String getRetrievePaginatedResultExcludeResultsQuery(List<String> resultsToExclude, String username,
            Integer page, String sortColumn, Boolean orderDescend, List<String> projIdList)
    {
        int hitPage = paramHelper.resolvePageToHitStart(page);
        
        int hitPageSize = paramHelper.getHitPageSize();
        
        String sortField = paramHelper.resolveColumnToField(sortColumn);
        
        String sortOrder = paramHelper.resolveSortOrder(orderDescend);
        
        String baseQueryUri =  
            restSearchQueryUriBuilder.getUriForAllResults(
                    username, projIdList, resultsToExclude);
        String paginationParamUri = restSearchQueryUriBuilder.getPaginationParamUri(
                hitPage, hitPageSize, sortField, sortOrder);
        
        StringBuffer buffer = new StringBuffer(baseQueryUri);
        buffer.append(paginationParamUri);
        return buffer.toString();
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRESTSearcher
     *      #getRetrievePaginatedResultSearchTerm(
     *          java.lang.String, java.lang.Integer, java.lang.String, java.lang.Boolean, java.lang.String)
     */
    @Override
    public String getRetrievePaginatedResultSearchTerm(String username, Integer page, String sortColumn,
            Boolean orderDescend, String searchTerm) throws FedoraSearchException
    {
        int hitPage = paramHelper.resolvePageToHitStart(page);
        
        int hitPageSize = paramHelper.getHitPageSize();
        
        String sortField = paramHelper.resolveColumnToField(sortColumn);
        
        String sortOrder = paramHelper.resolveSortOrder(orderDescend);
        
        String baseQueryUri;
        try
        {
            baseQueryUri = restSearchQueryUriBuilder.getUriForAllResultMatchingTerm(username, null, searchTerm);
        } catch (UnsupportedEncodingException e)
        {
            throw new FedoraSearchException(e);
        }
        String paginationParamUri = restSearchQueryUriBuilder.getPaginationParamUri(
                hitPage, hitPageSize, sortField, sortOrder);
        
        StringBuffer buffer = new StringBuffer(baseQueryUri);
        buffer.append(paginationParamUri);
        return buffer.toString();
        
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRESTSearcher
     *      #getRetrievePaginatedResultExcludeResultsQuery(
     *          java.util.List, java.lang.String, java.lang.Integer, 
     *          java.lang.String, java.lang.Boolean, java.util.List, java.lang.String)
     */
    @Override
    public String getRetrievePaginatedResultExcludeResultsQuery(List<String> resultsToExclude, String username,
            Integer page, String sortColumn, Boolean orderDescend, List<String> projIdList, String searchTerm) 
        throws FedoraSearchException
    {
        int hitPage = paramHelper.resolvePageToHitStart(page);
        
        int hitPageSize = paramHelper.getHitPageSize();
        
        String sortField = paramHelper.resolveColumnToField(sortColumn);
        
        String sortOrder = paramHelper.resolveSortOrder(orderDescend);
        
        String baseQueryUri;
        try
        {
            baseQueryUri = restSearchQueryUriBuilder.getUriForAllResultMatchingTerm(
                    resultsToExclude, username, projIdList, searchTerm);
        } catch (UnsupportedEncodingException e)
        {
            throw new FedoraSearchException(e);
        }
        String paginationParamUri = restSearchQueryUriBuilder.getPaginationParamUri(
                hitPage, hitPageSize, sortField, sortOrder);
        
        StringBuffer buffer = new StringBuffer(baseQueryUri);
        buffer.append(paginationParamUri);
        return buffer.toString();
        
    }

}
