/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import au.org.intersect.gda.util.XpathHelper;

/**
 * @version $Rev$
 *
 */
public class FedoraRepositoryRESTSearcherImplTest
{    
    @Mock
    private RESTSearchQueryUriBuilder queryBuilder;
    
    @Mock
    private RESTGenericSearchInterpreter interpreter;
    
    @Mock
    private GdaHttpClient client;
    
    @Mock
    private RESTSearchParamHelper paramHelper;
    
    private FedoraRepositoryRESTSearcherImpl searcher;
    
    private XpathHelper xpathHelper = new XpathHelper();
    
    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        searcher = new FedoraRepositoryRESTSearcherImpl(queryBuilder, interpreter, client, paramHelper);
    }
    
    @Test
    public void testSearchSuccessful() 
        throws GdaHttpClientException, XPathExpressionException, FedoraSearchException
    {
        GdaHttpClientGetResponse response = new GdaHttpClientGetResponse();
        InputStream inStream = new ByteArrayInputStream("<xml/>".getBytes());
        Node rootNode = xpathHelper.getRootNodeFromXmlStream(inStream);
        List<ResultDTO> rlist = new ArrayList<ResultDTO>();
        response.setResponse(rootNode);
        when(client.get(anyString())).thenReturn(response);
        when(interpreter.translate((Node) any())).thenReturn(rlist);
        
        String uri = "http://blah";
        List<ResultDTO> resultList = searcher.search(uri);
        verify(client).get(uri);
        verify(interpreter).translate(rootNode);
        assertEquals("Result list should be equal", rlist, resultList);        
    }
    
    @Test (expected = FedoraSearchException.class)
    public void testSearchGdaHttpClientException()
        throws GdaHttpClientException, XPathExpressionException, FedoraSearchException
    {
        GdaHttpClientGetResponse response = new GdaHttpClientGetResponse();
        response.setResponse(null);
        when(client.get(anyString())).thenThrow(new GdaHttpClientException("Get Method Failed"));

        String uri = "http://blah";
        searcher.search(uri);
    }
    
    @Test (expected = FedoraSearchException.class)
    public void testSearchXPathExpressionException()
        throws GdaHttpClientException, XPathExpressionException, FedoraSearchException
    {
        GdaHttpClientGetResponse response = new GdaHttpClientGetResponse();
        response.setResponse(null);
        when(client.get(anyString())).thenReturn(response);
        when(interpreter.translate((Node) any())).thenThrow(new XPathExpressionException("XPath Failed"));

        String uri = "http://blah";
        searcher.search(uri);
        verify(client).get(uri);
    }
    
    @Test
    public void testGetCriteriaSearchQuery() throws UnsupportedEncodingException, FedoraSearchException
    {
        ResultSearchCriteriaDTO arg = new ResultSearchCriteriaDTO();
        when(queryBuilder.getUri((ResultSearchCriteriaDTO) any())).thenReturn("123");
        String res = searcher.getCriteriaSearchQuery(arg);
        
        verify(queryBuilder).getUri(arg);
        assertEquals("123", res);        
    }
    
    @Test
    public void testGetRetrieveAllResultQuery() throws UnsupportedEncodingException, FedoraSearchException
    {
        searcher.getRetrieveAllResultQuery(true);        
        verify(queryBuilder).getUriForAllResults();    
        
        searcher.getRetrieveAllResultQuery(false);        
        verify(queryBuilder).getUriForAllResultsNotInProjects();  
    }
 
    @Test
    public void testGetRetrieveResultsForUserQuery() throws UnsupportedEncodingException, FedoraSearchException
    {
        String owner = "user1";
        searcher.getRetrieveResultsForUserQuery(owner, true);        
        verify(queryBuilder).getUriForAllResults(owner);    
        
        searcher.getRetrieveResultsForUserQuery(owner, false);        
        verify(queryBuilder).getUriForAllResultsNotInProjects(owner);    
    }
    
    @Test
    public void testGetRetrieveResultsInProjectQuery() throws UnsupportedEncodingException, FedoraSearchException
    {
        String ownerName = "username";
        List<String> projIds = new ArrayList<String>();
        searcher.getRetrieveResultsInProjectQuery(ownerName, projIds);        
        verify(queryBuilder).getUriForAllResults(ownerName, projIds);    
    }
    
    @Test
    public void testGetRetrieveSingleResultQuery() throws UnsupportedEncodingException, FedoraSearchException
    {
        String resId = "1h2";
        searcher.getRetrieveSingleResultQuery(resId);        
        verify(queryBuilder).getUriForSingleResult(resId);    
    } 
    
    
    @Test
    public void testGetPaginatedOwnerAndProjects()
    {
        String ownerName = "owner";
        Integer page = 1;
        String sortColumn = "col1";
        Boolean order = true;
        List<String> projIdList = new ArrayList<String>();
        boolean includeProjectResults = true;
        
        int hitStart = 1;
        int pageSize = 10;
        String colCode = "col code";
        String orderParam = "sort order";
        
        when(paramHelper.resolvePageToHitStart(page)).thenReturn(hitStart);
        when(paramHelper.getHitPageSize()).thenReturn(pageSize);
        when(paramHelper.resolveColumnToField(sortColumn)).thenReturn(colCode);
        when(paramHelper.resolveSortOrder(order)).thenReturn(orderParam);
        
        when(queryBuilder.getUriForAllResults(ownerName, projIdList)).thenReturn("basic uri");
        when(queryBuilder.getPaginationParamUri(hitStart, pageSize, colCode, orderParam)).thenReturn("sort uri");
        
        String combinedUri = 
            searcher.getRetrievePaginatedResultQuery(
                    ownerName, page, sortColumn, order, projIdList, includeProjectResults);
                
        assertEquals("basic uri" + "sort uri", combinedUri);                        
    }
    
    @Test
    public void testGetPaginatedOwnerAndNotProjects()
    {
        String ownerName = "owner";
        Integer page = 1;
        String sortColumn = "col1";
        Boolean order = true;
        List<String> projIdList = null;
        boolean includeProjectResults = false;
        
        int hitStart = 1;
        int pageSize = 10;
        String colCode = "col code";
        String orderParam = "sort order";
        
        when(paramHelper.resolvePageToHitStart(page)).thenReturn(hitStart);
        when(paramHelper.getHitPageSize()).thenReturn(pageSize);
        when(paramHelper.resolveColumnToField(sortColumn)).thenReturn(colCode);
        when(paramHelper.resolveSortOrder(order)).thenReturn(orderParam);
        
        when(queryBuilder.getUriForAllResultsNotInProjects(ownerName)).thenReturn("basic uri");
        when(queryBuilder.getPaginationParamUri(hitStart, pageSize, colCode, orderParam)).thenReturn("sort uri");
        
        String combinedUri = 
            searcher.getRetrievePaginatedResultQuery(
                    ownerName, page, sortColumn, order, projIdList, includeProjectResults);
                
        assertEquals("basic uri" + "sort uri", combinedUri);             
    }
    
    @Test
    public void testGetPaginatedAllAndNotProjects()
    {
        String ownerName = null;
        Integer page = 1;
        String sortColumn = "col1";
        Boolean order = true;
        List<String> projIdList = null;
        boolean includeProjectResults = false;
        
        int hitStart = 1;
        int pageSize = 10;
        String colCode = "col code";
        String orderParam = "sort order";
        
        when(paramHelper.resolvePageToHitStart(page)).thenReturn(hitStart);
        when(paramHelper.getHitPageSize()).thenReturn(pageSize);
        when(paramHelper.resolveColumnToField(sortColumn)).thenReturn(colCode);
        when(paramHelper.resolveSortOrder(order)).thenReturn(orderParam);
        
        when(queryBuilder.getUriForAllResultsNotInProjects()).thenReturn("basic uri");
        when(queryBuilder.getPaginationParamUri(hitStart, pageSize, colCode, orderParam)).thenReturn("sort uri");
        
        String combinedUri = 
            searcher.getRetrievePaginatedResultQuery(
                    ownerName, page, sortColumn, order, projIdList, includeProjectResults);
                
        assertEquals("basic uri" + "sort uri", combinedUri);             
    }
    
    @Test
    public void testGetPaginatedCriteriaSearchQuery() throws FedoraSearchException, UnsupportedEncodingException
    {
        String ownerName = null;
        Integer page = 1;
        String sortColumn = "col1";
        Boolean order = true;
        List<String> projIdList = null;
        
        ResultSearchCriteriaDTO  searchCriteria = new ResultSearchCriteriaDTO();
        
        int hitStart = 1;
        int pageSize = 10;
        String colCode = "col code";
        String orderParam = "sort order";
        
        when(paramHelper.resolvePageToHitStart(page)).thenReturn(hitStart);
        when(paramHelper.getHitPageSize()).thenReturn(pageSize);
        when(paramHelper.resolveColumnToField(sortColumn)).thenReturn(colCode);
        when(paramHelper.resolveSortOrder(order)).thenReturn(orderParam);
        
        when(queryBuilder.getUri(searchCriteria, ownerName, projIdList)).thenReturn("basic uri");
        when(queryBuilder.getPaginationParamUri(hitStart, pageSize, colCode, orderParam)).thenReturn("sort uri");
        
        String combinedUri = 
            searcher.getPaginatedCriteriaSearchQuery(
                    searchCriteria, ownerName, page, sortColumn, order, projIdList);
                
        assertEquals("basic uri" + "sort uri", combinedUri);             
    }
    
    
    @Test
    public void testPaginatedSearch() throws 
        GdaHttpClientException, XPathExpressionException, FedoraException, FedoraSearchException
    {
        String uri = "uri";
        GdaHttpClientGetResponse resp = new GdaHttpClientGetResponse();
        when(client.get(uri)).thenReturn(resp);
        InputStream inStream = new ByteArrayInputStream("<xml/>".getBytes());
        Node rootNode = xpathHelper.getRootNodeFromXmlStream(inStream);
        resp.setResponse(rootNode);
        
        int totalHit = 10;
        int currentHit = 1;
        List<ResultDTO> results = new ArrayList<ResultDTO>();
        
        ResultDTO result1 = new ResultDTO();
        result1.setId("result id");
        results.add(result1);
        
        int currentPage = 2;
        int totalPage = 3;
        
        ResultSearchPageHit pageHit = new ResultSearchPageHit(totalHit, currentHit, results);
        when(interpreter.translatePaginatedResult(rootNode)).thenReturn(pageHit);
        when(paramHelper.convertHitToPage(currentHit)).thenReturn(currentPage);
        when(paramHelper.convertHitToPage(totalHit)).thenReturn(totalPage);
        
        
        ResultSearchPagination paginatedResult = searcher.paginatedSearch(uri);
        
        assertEquals(currentPage, paginatedResult.getCurrentPage());
        assertEquals(totalPage, paginatedResult.getTotalPage());
        assertEquals(totalHit, paginatedResult.getTotalResults());
        
        assertEquals(results, paginatedResult.getResults());
    }
}
