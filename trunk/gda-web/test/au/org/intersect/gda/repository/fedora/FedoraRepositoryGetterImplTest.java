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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fedora.server.types.gen.ObjectFields;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;
import au.org.intersect.gda.metadata.GdaObjectMetaDataHelper;
import au.org.intersect.gda.metadata.GdaObjectMetaDataHelperException;
import au.org.intersect.gda.util.DateParser;
import au.org.intersect.gda.xml.ResultSchemaHelper;

/**
 * @version $Rev$
 * 
 */
public class FedoraRepositoryGetterImplTest
{
    private FedoraRepositoryGetterImpl fedoraRepositoryGetter;

    @Mock
    private FedoraDatastreamHelper fedoraDsHelper;

    @Mock
    private GdaObjectMetaDataHelper objMetaDataHelper;

    @Mock
    private FedoraRepositoryRESTSearcher fedoraRepositoryRESTSearcher;

    @Mock
    private FedoraRepositorySearcher fedoraRepositorySearcher;

    @Mock
    private ResultSchemaHelper resultSchemaHelper;

    private Document testDoc;
    private String testDocContent = "apple";

    @Before
    public void setUp() throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);

        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        testDoc = docBuilder.parse(new InputSource(new StringReader("<meta>" + testDocContent + "</meta>")));
    }

    @Test
    public void testGetAllResultsSimple() throws FedoraException, FedoraSearchException
    {
        List<ResultDTO> results = new ArrayList<ResultDTO>();
        when(fedoraRepositoryRESTSearcher.getRetrieveAllResultQuery(anyBoolean())).thenReturn("some query");
        when(fedoraRepositoryRESTSearcher.search(anyString())).thenReturn(results);

        List<ResultDTO> actual = fedoraRepositoryGetter.getAllResults(true);
        verify(fedoraRepositoryRESTSearcher).getRetrieveAllResultQuery(true);
        verify(fedoraRepositoryRESTSearcher).search("some query");
        assertEquals(results.size(), actual.size());
    }

    @Test(expected = FedoraException.class)
    public void testGetAllResultsFail() throws FedoraException, FedoraSearchException
    {
        FedoraSearchException exToThrow = new FedoraSearchException();
        when(fedoraRepositoryRESTSearcher.getRetrieveAllResultQuery(anyBoolean())).thenReturn("some query");
        when(fedoraRepositoryRESTSearcher.search(anyString())).thenThrow(exToThrow);

        fedoraRepositoryGetter.getAllResults(true);
    }

    @Test
    public void testGetProperty() throws FedoraException, GdaObjectMetaDataHelperException
    {
        String resultId = "a:b";
        FedoraResultProperties prop = FedoraResultProperties.CREATOR;
        when(fedoraDsHelper.getDataStream(anyString(), anyString())).thenReturn(testDoc);
        when(objMetaDataHelper.getProperty(testDoc, FedoraResultProperties.CREATOR)).thenReturn(testDocContent);

        String actual = fedoraRepositoryGetter.getProperty(resultId, prop);
        assertEquals(testDocContent, actual);
        verify(fedoraDsHelper).getDataStream(resultId, "DC");
    }

    @Test(expected = FedoraException.class)
    public void testGetPropertyFail() throws FedoraException
    {
        FedoraException ex = new FedoraException("fail");
        when(fedoraDsHelper.getDataStream(anyString(), anyString())).thenThrow(ex);
        fedoraRepositoryGetter.getProperty("", FedoraResultProperties.CREATOR);
    }   

    @Test
    public void testGetResult() throws RemoteException, FedoraException, ParseException
    {
        String pid = "abcd";
        ObjectFields objFields = mock(ObjectFields.class);
        ResultDTO expected = new ResultDTO();
        String cDate = "2010-01-27T05:20:57.781Z";
        String mDate = "2010-01-27T06:12:28.546Z";
        String id = "ab:cd";
        String name = "name";
        String type = "type";
        String creator = "creator";
        String owner = "owner";
        when(fedoraDsHelper.getDataStream(id, "DC")).thenReturn(null);
        when(objMetaDataHelper.getProperty(null, FedoraResultProperties.NAME)).thenReturn(name);
        when(objMetaDataHelper.getProperty(null, FedoraResultProperties.TYPE)).thenReturn(type);
        when(objMetaDataHelper.getProperty(null, FedoraResultProperties.CREATOR)).thenReturn(creator);
        when(objMetaDataHelper.getProperty(null, FedoraResultProperties.OWNER)).thenReturn(owner);

        expected.setName(name);
        expected.setType(type);
        expected.setCreator(creator);
        expected.setOwner(owner);

        when(objFields.getPid()).thenReturn(id);
        expected.setId(id);
        when(objFields.getCDate()).thenReturn(cDate);
        expected.setCreatedDate(DateParser.parse(cDate));
        when(objFields.getMDate()).thenReturn(mDate);
        expected.setLastModifiedDate(DateParser.parse(mDate));

        when(fedoraRepositorySearcher.findResult(pid)).thenReturn(objFields);

        ResultDTO actual = fedoraRepositoryGetter.getResult(pid);
        verify(fedoraRepositorySearcher).findResult(pid);

        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getCreator(), actual.getCreator());
    }

    @Test (expected = FedoraException.class)
    public void testGetResultException() throws FedoraException, FedoraSearchException
    {
        String pid = "abc:d";
        when(fedoraRepositorySearcher.findResult(pid)).thenThrow(new FedoraException(""));        
        fedoraRepositoryGetter.getResult(pid);
    }

    @Test (expected = FedoraException.class)
    public void testGetResultException2() throws FedoraException, FedoraSearchException
    {
        String pid = "abc:d";
        ObjectFields objFields = mock(ObjectFields.class);
        when(fedoraRepositorySearcher.findResult(pid)).thenReturn(objFields);
        when(fedoraDsHelper.getDataStream(pid, "DC")).thenThrow(new FedoraException(""));
        fedoraRepositoryGetter.getResult(pid);
    }

    @Test
    public void testGetDataStream() throws FedoraException
    {
        String pid = "foo:bar";
        String dsId = "ds1";

        ArgumentCaptor<String> pidCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dsIdCaptor = ArgumentCaptor.forClass(String.class);

        when(fedoraDsHelper.getDataStream(pidCaptor.capture(), dsIdCaptor.capture())).thenReturn(testDoc);

        Document actualDoc = fedoraRepositoryGetter.getDataStream(pid, dsId);

        verify(fedoraDsHelper).getDataStream(anyString(), anyString());

        assertEquals(testDoc, actualDoc);
        assertEquals(pid, pidCaptor.getValue());
        assertEquals(dsId, dsIdCaptor.getValue());
    }

    @Test
    public void testGetAllDataStreams() throws FedoraException
    {
        String pid = "foo:bar";

        Map<String, String> dsMap = new HashMap<String, String>();
        dsMap.put("foo:bar", "value1");
        ArgumentCaptor<String> pidCaptor = ArgumentCaptor.forClass(String.class);

        when(fedoraDsHelper.getAllDataStreams(pidCaptor.capture())).thenReturn(dsMap);

        Map<String, String> resMap = fedoraRepositoryGetter.getAllDataStreams(pid);

        verify(fedoraDsHelper).getAllDataStreams(anyString());

        assertEquals(dsMap.size(), resMap.size());
        assertEquals(dsMap.get("foo:bar"), dsMap.get("foo:bar"));
        assertEquals(pid, pidCaptor.getValue());
    }

    @Test
    public void testGetResultsByOwner() throws FedoraException, FedoraSearchException
    {
        String owner = "user1";
        when(fedoraRepositoryRESTSearcher.getRetrieveResultsForUserQuery(anyString(), 
                anyBoolean())).thenReturn("some uri");
        List<ResultDTO> results = new ArrayList<ResultDTO>();
        when(fedoraRepositoryRESTSearcher.search(anyString())).thenReturn(results);

        List<ResultDTO> actual = fedoraRepositoryGetter.getResultsByOwner(owner, false);

        verify(fedoraRepositoryRESTSearcher).getRetrieveResultsForUserQuery(owner, false);
        verify(fedoraRepositoryRESTSearcher).search("some uri");
        assertEquals(results.size(), actual.size());
    }

    @Test (expected = FedoraException.class)
    public void testGetResultsByOwnerFailed() throws FedoraException, FedoraSearchException
    {
        String owner = "user1";
        when(fedoraRepositoryRESTSearcher.getRetrieveResultsForUserQuery(anyString(), 
                anyBoolean())).thenReturn("some uri");
        when(fedoraRepositoryRESTSearcher.search(anyString())).thenThrow(new FedoraSearchException());

        fedoraRepositoryGetter.getResultsByOwner(owner, false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetResultsInProjects() throws FedoraException, FedoraSearchException
    {
        String owner = "user1";
        when(fedoraRepositoryRESTSearcher.getRetrieveResultsInProjectQuery(anyString(), 
                anyList())).thenReturn("some uri1");
        List<ResultDTO> results = new ArrayList<ResultDTO>();
        List<String> slist = new ArrayList<String>();
        when(fedoraRepositoryRESTSearcher.search(anyString())).thenReturn(results);

        List<ResultDTO> actual = fedoraRepositoryGetter.getResultsInProjects(owner, slist);

        verify(fedoraRepositoryRESTSearcher).getRetrieveResultsInProjectQuery(owner, slist);
        verify(fedoraRepositoryRESTSearcher).search("some uri1");
        assertEquals(results.size(), actual.size());
    }

    @SuppressWarnings("unchecked")
    @Test (expected = FedoraException.class)
    public void testGetResultsInProjectsFailed() throws FedoraException, FedoraSearchException
    {
        String owner = "user1";
        when(fedoraRepositoryRESTSearcher.getRetrieveResultsInProjectQuery(anyString(), 
                anyList())).thenReturn("some uri1");
        List<String> slist = new ArrayList<String>();
        when(fedoraRepositoryRESTSearcher.search(anyString())).thenThrow(new FedoraSearchException());

        fedoraRepositoryGetter.getResultsInProjects(owner, slist);
    }

    @Test
    public void testHasProperty() throws FedoraException, GdaObjectMetaDataHelperException
    {
        String resultId = "a:b";
        FedoraResultProperties prop = FedoraResultProperties.CREATOR;
        boolean expected = true;
        when(fedoraDsHelper.getDataStream(anyString(), anyString())).thenReturn(testDoc);

        when(objMetaDataHelper.hasProperty(testDoc, FedoraResultProperties.CREATOR)).thenReturn(true);
        boolean actual = fedoraRepositoryGetter.hasProperty(resultId, prop);
        assertEquals(expected, actual);
        verify(fedoraDsHelper).getDataStream(resultId, "DC");
    }

    @Before
    public void setupFedoraComponents() throws ServiceException, IOException
    {
        MockitoAnnotations.initMocks(this);
        fedoraRepositoryGetter = new FedoraRepositoryGetterImpl(fedoraDsHelper, 
                            objMetaDataHelper, fedoraRepositoryRESTSearcher,
                            fedoraRepositorySearcher, resultSchemaHelper);
    }

    @Test
    public void testGetPaginatedResults() throws FedoraException, FedoraSearchException
    {
        String ownerName = null;
        Integer page = 1;
        String sortColumn = "col1";
        Boolean order = true;
        List<String> projIdList = null;
        boolean includeProjectResults = false;

        String uri = "uri";
        when(fedoraRepositoryRESTSearcher.getRetrievePaginatedResultQuery(
                ownerName, page, sortColumn, order, projIdList, includeProjectResults)).thenReturn(uri);

        ResultSearchPagination paginatedResult = 
            new ResultSearchPagination(1, 1, 1, new ArrayList<ResultDTO>());

        when(fedoraRepositoryRESTSearcher.paginatedSearch(uri)).thenReturn(paginatedResult);

        ResultSearchPagination actualReturn = fedoraRepositoryGetter.getPaginatedResults(
                ownerName, page, sortColumn, order, projIdList, includeProjectResults);

        assertEquals(paginatedResult, actualReturn);
    }

    @Test
    public void testGetResultsByCriteria() throws FedoraException, FedoraSearchException
    {
        String ownerName = null;
        Integer page = 1;
        String sortColumn = "col1";
        Boolean order = true;
        List<String> projIdList = null;

        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();

        String uri = "uri";
        when(fedoraRepositoryRESTSearcher.getPaginatedCriteriaSearchQuery(
                criteria, ownerName, page, sortColumn, order, projIdList)).thenReturn(uri);

        ResultSearchPagination paginatedResult = 
            new ResultSearchPagination(1, 1, 1, new ArrayList<ResultDTO>());

        when(fedoraRepositoryRESTSearcher.paginatedSearch(uri)).thenReturn(paginatedResult);

        ResultSearchPagination actualReturn = fedoraRepositoryGetter.getResultsByCriteria(
                criteria, ownerName, page, sortColumn, order, projIdList);

        assertEquals(paginatedResult, actualReturn);
    }
}
