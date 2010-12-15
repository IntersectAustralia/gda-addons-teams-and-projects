/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;
import au.org.intersect.gda.http.GdaHttpClient;
import au.org.intersect.gda.http.GdaHttpClientException;
import au.org.intersect.gda.http.GdaHttpClientPostResponse;

/**
 * @version $Rev$
 *
 */
public class FedoraRepositoryRISearcherImplTest
{
    
    private FedoraRepositoryRISearcher fedoraRepositoryRISearcher;
    
    private final String fedoraRiUri = "http://localhost:8090/fedora/risearch";
    
    private final String resultId = "GDA:1";
    private final String lang = FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL;
    private final String format = FedoraRepositoryRISearcher.RETURN_FORMAT_CSV;
    private final String type = "tuples";
    private final String flush = "false";
    private final String distinct = "off";
    private final String stream = "off";
    private final String limit = "10";
    
    
    @Mock
    private GdaHttpClient gdaHttpClient;
    
    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        fedoraRepositoryRISearcher = new FedoraRepositoryRISearcherImpl(gdaHttpClient, fedoraRiUri);
    }
    
    @Test
    public void testGetTuplesSuccess() throws FedoraException, GdaHttpClientException
    {
        String query = fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(resultId);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        params.put("flush", flush);
        params.put("lang", lang);
        params.put("format", format);
        params.put("distinct", distinct);
        params.put("stream", stream);
        params.put("query", query);
        
        int expectedReturnCode = HttpStatus.SC_OK;
        String expectedTuple1 = "GDA:1,info:fedora/GDA:2";
        String expectedTuple2 = "GDA:1,info:fedora/GDA:3";
        String[] expectedTuples = {expectedTuple1, expectedTuple2};
        
        GdaHttpClientPostResponse expectedResponse = new GdaHttpClientPostResponse();
        expectedResponse.setReturnCode(expectedReturnCode);
        expectedResponse.setResponse(expectedTuples);
        
        when(gdaHttpClient.post(fedoraRiUri, params)).thenReturn(expectedResponse);
        
        String[] tupleArray = fedoraRepositoryRISearcher.getTuples(lang, format, query);
        
        assertEquals("Return array size:", expectedTuples.length, tupleArray.length);
        assertEquals("Tuple 1:", expectedTuple1, tupleArray[0]);
        assertEquals("Tuple 2:", expectedTuple2, tupleArray[1]);
    }

    @Test
    public void testGetTuplesWithLimitSuccess() throws FedoraException, GdaHttpClientException
    {
        String query = fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(resultId);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        params.put("flush", flush);
        params.put("lang", lang);
        params.put("format", format);
        params.put("limit", limit);
        params.put("distinct", distinct);
        params.put("stream", stream);
        params.put("query", query);
        
        int expectedReturnCode = HttpStatus.SC_OK;
        String expectedTuple1 = "GDA:1,info:fedora/GDA:2";
        String expectedTuple2 = "GDA:1,info:fedora/GDA:3";
        String[] expectedTuples = {expectedTuple1, expectedTuple2};
        
        GdaHttpClientPostResponse expectedResponse = new GdaHttpClientPostResponse();
        expectedResponse.setReturnCode(expectedReturnCode);
        expectedResponse.setResponse(expectedTuples);
        
        when(gdaHttpClient.post(fedoraRiUri, params)).thenReturn(expectedResponse);
        
        String[] tupleArray = 
            fedoraRepositoryRISearcher.getTuples(flush, lang, format, limit, distinct, stream, query);
        
        assertEquals("Return array size:", expectedTuples.length, tupleArray.length);
        assertEquals("Tuple 1:", expectedTuple1, tupleArray[0]);
        assertEquals("Tuple 2:", expectedTuple2, tupleArray[1]);
    }

    @Test
    public void testGetTuplesHttpError() throws FedoraException, GdaHttpClientException
    {
        String query = fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(resultId);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        params.put("flush", flush);
        params.put("lang", lang);
        params.put("format", format);
        params.put("distinct", distinct);
        params.put("stream", stream);
        params.put("query", query);
        
        int expectedReturnCode = HttpStatus.SC_FORBIDDEN;
        String expectedTuple1 = "GDA:1,info:fedora/GDA:2";
        String expectedTuple2 = "GDA:1,info:fedora/GDA:3";
        String[] expectedTuples = {expectedTuple1, expectedTuple2};
        
        GdaHttpClientPostResponse expectedResponse = new GdaHttpClientPostResponse();
        expectedResponse.setReturnCode(expectedReturnCode);
        expectedResponse.setResponse(expectedTuples);
        
        when(gdaHttpClient.post(fedoraRiUri, params)).thenReturn(expectedResponse);
        
        try
        {
            fedoraRepositoryRISearcher.getTuples(lang, format, query);
            fail("did not throw exception on HTTP Error");
        } catch (FedoraException e)
        {
            assertTrue(e.getMessage().endsWith("" + expectedReturnCode));
        }
    }

    @Test
    public void testGetTuplesException() throws FedoraException, GdaHttpClientException
    {
        String query = fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(resultId);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        params.put("flush", flush);
        params.put("lang", lang);
        params.put("format", format);
        params.put("distinct", distinct);
        params.put("stream", stream);
        params.put("query", query);
        
        when(gdaHttpClient.post(fedoraRiUri, params)).thenThrow(
                new GdaHttpClientException("This is a GdaHttpClientException"));
        
        try
        {
            fedoraRepositoryRISearcher.getTuples(lang, format, query);
            fail("Expected exception notthrown.");
        } catch (FedoraException e)
        {
            assertEquals("Exception message: ", "This is a GdaHttpClientException", e.getMessage());
        }
    }

    @Test
    public void testGetCriteriaQueryStringAllValues()
    {
        String expectedQuery = "select $s from <#ri> where $s <dc:title> "
                             + "'The object name' and  ( $s <dc:type> 'The object Type' )  and  ( $s <dc:publisher> "
                             + "'The object owner' )  and  ( $s <dc:creator> 'The object creator' ) "
                             + " and $s <dc:relation> $o";
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();
        String name = "The object name";
        String type = "The object Type";
        String owner = "The object owner";
        String creator = "The object creator";
        boolean hasFile = true;
        
        criteria.setName(name);
        criteria.addType(type);
        criteria.addOwner(owner);
        criteria.addCreator(creator);
        criteria.setHasFile(hasFile);
        
        String query = fedoraRepositoryRISearcher.getCriteriaSearchITQLQuery(criteria);
        
        assertEquals("Query String", expectedQuery, query);
    }

    @Test
    public void testGetCriteriaQueryStringMissingName()
    {
        String expectedQuery = "select $s from <#ri> where ( $s <dc:type> 'The object Type' )  and  "
                             + "( $s <dc:publisher> "
                             + "'The object owner' )  and  ( $s <dc:creator> 'The object creator' ) "
                             + " and $s <dc:relation> $o";
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();
        String name = null;
        String type = "The object Type";
        String owner = "The object owner";
        String creator = "The object creator";
        boolean hasFile = true;
        
        criteria.setName(name);
        criteria.addType(type);
        criteria.addOwner(owner);
        criteria.addCreator(creator);
        criteria.setHasFile(hasFile);
        
        String query = fedoraRepositoryRISearcher.getCriteriaSearchITQLQuery(criteria);
        
        assertEquals("Query String", expectedQuery, query);
    }
    
    @Test
    public void testGetCriteriaQueryStringMissingType()
    {
        String expectedQuery = "select $s from <#ri> where ( $s <dc:publisher> "
                             + "'The object owner' )  and  ( $s <dc:creator> 'The object creator' ) "
                             + " and $s <dc:relation> $o";
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();
        String owner = "The object owner";
        String creator = "The object creator";
        boolean hasFile = true;
        
        criteria.setName(null);
        criteria.addOwner(owner);
        criteria.addCreator(creator);
        criteria.setHasFile(hasFile);
        
        String query = fedoraRepositoryRISearcher.getCriteriaSearchITQLQuery(criteria);
        
        assertEquals("Query String", expectedQuery, query);
    }

    @Test
    public void testGetCriteriaQueryStringMissingOwner()
    {
        String expectedQuery = "select $s from <#ri> where ( $s <dc:creator> 'The object creator' ) "
                             + " and $s <dc:relation> $o";
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();
        String name = null;
        String creator = "The object creator";
        boolean hasFile = true;
        
        criteria.setName(name);
        criteria.addCreator(creator);
        criteria.setHasFile(hasFile);
        
        String query = fedoraRepositoryRISearcher.getCriteriaSearchITQLQuery(criteria);
        
        assertEquals("Query String", expectedQuery, query);
    }
    
    @Test
    public void testGetCriteriaQueryStringMissingCreator()
    {
        String expectedQuery = "select $s from <#ri> where $s <dc:relation> $o";
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();
        boolean hasFile = true;
        
        criteria.setName(null);
        criteria.setHasFile(hasFile);
        
        String query = fedoraRepositoryRISearcher.getCriteriaSearchITQLQuery(criteria);
        
        assertEquals("Query String", expectedQuery, query);
    }
    
    @Test
    public void testGetCriteriaQueryStringMissingHasFile()
    {
        String expectedQuery = "select $s from <#ri> where $s <dc:title> "
                             + "'The object name' and  ( $s <dc:type> 'The object Type' )  and  ( $s <dc:publisher> "
                             + "'The object owner' )  and  ( $s <dc:creator> 'The object creator' ) ";
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();
        String name = "The object name";
        String type = "The object Type";
        String owner = "The object owner";
        String creator = "The object creator";
        boolean hasFile = false;
        
        criteria.setName(name);
        criteria.addType(type);
        criteria.addOwner(owner);
        criteria.addCreator(creator);
        criteria.setHasFile(hasFile);
        
        String query = fedoraRepositoryRISearcher.getCriteriaSearchITQLQuery(criteria);
        
        assertEquals("Query String", expectedQuery, query);
    }
    
    @Test
    public void testGetCriteriaQueryStringNullHasFile()
    {
        String expectedQuery = "select $s from <#ri> where $s <dc:title> "
                             + "'The object name' and  ( $s <dc:type> 'The object Type' )  and  ( $s <dc:publisher> "
                             + "'The object owner' )  and  ( $s <dc:creator> 'The object creator' ) ";
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();
        String name = "The object name";
        String type = "The object Type";
        String owner = "The object owner";
        String creator = "The object creator";
        
        criteria.setName(name);
        criteria.addType(type);
        criteria.addOwner(owner);
        criteria.addCreator(creator);
        criteria.setHasFile(null);
        
        String query = fedoraRepositoryRISearcher.getCriteriaSearchITQLQuery(criteria);
        
        assertEquals("Query String", expectedQuery, query);
    }
    
    @Test
    public void testGetCriteriaQueryStringMultipleType()
    {
        String expectedQuery = "select $s from <#ri> where $s <dc:title> "
                             + "'The object name' and  ( $s <dc:type> 'The object Type1' or "
                             + "$s <dc:type> 'The object Type2' )  and  ( $s <dc:publisher> "
                             + "'The object owner' )  and  ( $s <dc:creator> 'The object creator' )  "
                             + "and $s <dc:relation> $o";
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();
        String name = "The object name";
        String type1 = "The object Type1";
        String type2 = "The object Type2";
        String owner = "The object owner";
        String creator = "The object creator";
        boolean hasFile = true;
        
        criteria.setName(name);
        criteria.addType(type1);
        criteria.addType(type2);
        criteria.addOwner(owner);
        criteria.addCreator(creator);
        criteria.setHasFile(hasFile);
        
        String query = fedoraRepositoryRISearcher.getCriteriaSearchITQLQuery(criteria);
        
        assertEquals("Query String", expectedQuery, query);
    }
}
