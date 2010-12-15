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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ServiceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import fedora.server.types.gen.RelationshipTuple;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.rest.FedoraUpdateIndexException;
import au.org.intersect.gda.rest.RESTGenericSearchIndexUpdater;

/**
 * @version $Rev$
 * 
 */
public class FedoraResultRepositoryManagerImplTest
{
    private static final String PROJ_ID_PREFIX = "prId";
    
    private FedoraResultRepositoryManagerImpl fedoraRepositoryManager;

    @Mock
    private FedoraRepositoryGetterImpl fedoraRepositoryGetter;
    
    @Mock
    private FedoraResultRepositorySetterImpl fedoraRepositorySetter;
    
    @Mock 
    private FedoraRepositoryRelationshipManager fedoraRepositoryRelationshipManager;
    
    @Mock
    private FedoraRepositoryRISearcher fedoraRepositoryRISearcher;

    @Mock
    private RESTGenericSearchIndexUpdater genericSearchIndexUpdater;
    
    private Document testDoc;
    
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        fedoraRepositoryManager = new FedoraResultRepositoryManagerImpl(fedoraRepositoryGetter,
                                                                  fedoraRepositorySetter,
                                                                  fedoraRepositoryRelationshipManager,
                                                                  fedoraRepositoryRISearcher,
                                                                  genericSearchIndexUpdater);
        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        
        docBuilderFactory.setNamespaceAware(true);
        
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        
        testDoc = docBuilder.parse(new InputSource(new StringReader("<meta>blahblah</meta>")));
    }

    @Test
    public void testCreateResultInRepository() 
        throws FedoraException, IOException, ServiceException, FedoraUpdateIndexException
    {
        ResultDTO result = getResultDTO();
        ArgumentCaptor<ResultDTO> captor = ArgumentCaptor.forClass(ResultDTO.class);

        fedoraRepositoryManager.createResultInRepository(result);

        verify(fedoraRepositorySetter).createObjectInRepository(captor.capture());
        verify(genericSearchIndexUpdater).updateChangedIndex(result.getId());
        assertEquals(result, captor.getValue());
        verifyZeroInteractions(fedoraRepositoryGetter);
    }

    @Test(expected = FedoraException.class)
    public void testCreateResultInRepositoryFail() throws FedoraException, IOException, ServiceException
    {
        ResultDTO result = getResultDTO();
        doThrow(new FedoraException("a")).when(fedoraRepositorySetter)
                .createObjectInRepository((ResultDTO) anyObject());
        fedoraRepositoryManager.createResultInRepository(result);
    }

    @Test
    public void testGetAllResults() throws FedoraException, IOException, ServiceException
    {
        List<ResultDTO> toReturn = new ArrayList<ResultDTO>();
        ResultDTO result = getResultDTO();
        toReturn.add(result);
        when(fedoraRepositoryGetter.getAllResults(anyBoolean())).thenReturn(toReturn);

        List<ResultDTO> results = fedoraRepositoryManager.getAllResults(false);

        verify(fedoraRepositoryGetter).getAllResults(false);
        verifyZeroInteractions(fedoraRepositorySetter);
        assertNotNull(results);
        assertEquals(results, toReturn);
        assertTrue(results.contains(result));
        assertEquals(results.size(), 1);
    }

    @Test(expected = FedoraException.class)
    public void testGetAllResultsFail() throws FedoraException, IOException, ServiceException
    {
        when(fedoraRepositoryGetter.getAllResults(anyBoolean())).thenThrow(new FedoraException("a"));

        fedoraRepositoryManager.getAllResults(true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testGetResultsInProjects() 
        throws IOException, ServiceException, RepositoryException
    {
        List<ResultDTO> toReturn = new ArrayList<ResultDTO>();
        ResultDTO result = getResultDTO();
        toReturn.add(result);
        when(fedoraRepositoryGetter.getResultsInProjects(anyString(), (List<String>) any())).thenReturn(toReturn);

        List<String> projIdList = new ArrayList<String>();
        List<ResultDTO> results = fedoraRepositoryManager.getResultsInProjects("user1", projIdList);

        verify(fedoraRepositoryGetter).getResultsInProjects("user1", projIdList);
        verifyZeroInteractions(fedoraRepositorySetter);
        assertNotNull(results);
        assertEquals(results, toReturn);
        assertTrue(results.contains(result));
        assertEquals(results.size(), 1);
    }

    @Test
    public void testGetResult() throws FedoraException
    {
        String id = "foo:bar";
        ResultDTO expected = getResultDTO();
        when(fedoraRepositoryGetter.getResult(id)).thenReturn(expected);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        ResultDTO actual = fedoraRepositoryManager.getResult(id);

        verify(fedoraRepositoryGetter).getResult(captor.capture());
        verifyZeroInteractions(fedoraRepositorySetter);
        assertNotNull(captor.getValue());
        assertEquals(id, captor.getValue());
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test(expected = FedoraException.class)
    public void testGetResultFail() throws FedoraException
    {
        String id = "foo:bar";
        when(fedoraRepositoryGetter.getResult(anyString())).thenThrow(new FedoraException("a"));

        fedoraRepositoryManager.getResult(id);
    }

    @Test
    public void testGetDatastreamContent() throws FedoraException
    {
        String objectID = "foo:bar";
        String dsID = "bar:foo";

        Document expected = testDoc;

        
        when(fedoraRepositoryGetter.getDataStream(objectID, dsID)).thenReturn(expected);

        Document actual = fedoraRepositoryManager.getMetaData(objectID, dsID);

        assertNotNull(actual);
        verify(fedoraRepositoryGetter).getDataStream(objectID, dsID);
        verifyZeroInteractions(fedoraRepositorySetter);
        assertEquals(expected, actual);
    }

    @Test
    public void testListAllDatastreams() throws FedoraException
    {
        String objectID = "foo:bar";
        Map<String, String> expected = new HashMap<String, String>();
        String key = "bar:foo";
        String value = "sadfsadfcvb";
        expected.put(key, value);

        when(fedoraRepositoryGetter.getAllDataStreams(objectID)).thenReturn(expected);

        Map<String, String> actual = fedoraRepositoryManager.listAllMetadata(objectID);

        assertNotNull(actual);
        verifyZeroInteractions(fedoraRepositorySetter);
        verify(fedoraRepositoryGetter).getAllDataStreams(objectID);
        assertTrue(actual.containsKey(key));
        assertTrue(actual.containsValue(value));
        assertTrue(actual.get(key).equals(value));
        assertTrue(actual.size() == expected.size());
        assertEquals(expected, actual);
    }

    @Test
    public void testAddMetadata() throws FedoraException, FedoraUpdateIndexException
    {

        String objectID = "foo:bar";
        
        String label = "This is a label.";

        String expectedId = "unique id";

        when(fedoraRepositorySetter.storeDataStream(
                anyString(), anyString(), anyString(), anyString(), (Document) any())).thenReturn(
                        expectedId);

        String actual = fedoraRepositoryManager.addMetaData(objectID, label, testDoc);

        verifyZeroInteractions(fedoraRepositoryGetter);
        verify(fedoraRepositorySetter).storeDataStream(objectID, null, label, "text/xml", testDoc);
        verify(genericSearchIndexUpdater).updateChangedIndex(objectID);
        assertNotNull(actual);
        assertEquals(expectedId, actual);
    }

    @Test
    public void testSetType() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:45435";
        String type = "type a+";
        FedoraResultProperties prop = FedoraResultProperties.TYPE;

        
        fedoraRepositoryManager.setType(resultId, type);
        verify(fedoraRepositorySetter).putProperty(resultId, prop, type); 
        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);    
    }
    
    
    @Test
    public void testEditMetaData() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "GDA:432";
        String metaId = "xcbdfsadf";
        Document content = testDoc;
        
        fedoraRepositoryManager.editMetaData(resultId, metaId, content);
        
        verify(fedoraRepositorySetter).editDataStream(resultId, metaId, "text/xml", content);
        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
        verifyZeroInteractions(fedoraRepositoryGetter);
    }
    
    @Test
    public void testRemoveMetaData() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "b:d";
        String metaId = "asfdvcxbr";
        
        fedoraRepositoryManager.removeMetaData(resultId, metaId);
        
        verify(fedoraRepositorySetter).removeDataStream(resultId, metaId);
        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
        verifyZeroInteractions(fedoraRepositoryGetter);
    }
    
    @Test
    public void testSetName() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:45435";
        String name = "type a+";
        FedoraResultProperties prop = FedoraResultProperties.NAME;

        fedoraRepositoryManager.setName(resultId, name);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, name);
        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }
    
    @Test
    public void testSetOwner() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:45435";
        String owner = "type a+";
        FedoraResultProperties prop = FedoraResultProperties.OWNER;

        fedoraRepositoryManager.setOwner(resultId, owner);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, owner);
        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
        
    }
    
    @Test
    public void testSetProjectId() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:111";
        String projectId = "p123";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        String previousProjId = PROJ_ID_PREFIX + "[xyz]";
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn(previousProjId);
        
        fedoraRepositoryManager.addProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, previousProjId 
                + ", " + PROJ_ID_PREFIX + "[" + projectId + "]");

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }
 
    @Test
    public void testSetExistingProjectId() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:111";
        String projectId = "p133";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        String previousProjId = PROJ_ID_PREFIX + "[" + projectId  + "]";
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn(previousProjId);
        
        fedoraRepositoryManager.addProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter, never()).putProperty(resultId, prop, previousProjId 
                + ", " + PROJ_ID_PREFIX + "[" + projectId + "]");

        verify(genericSearchIndexUpdater, never()).updateChangedIndex(resultId);
    }
    
    @Test
    public void testSetProjectIdNull() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:121";
        String projectId = "px";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn(null);
        
        fedoraRepositoryManager.addProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, 
                PROJ_ID_PREFIX + "[" + projectId + "]");

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }
    
    @Test
    public void testSetProjectIdEmpty() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:112";
        String projectId = "px2";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn("");
        
        fedoraRepositoryManager.addProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, PROJ_ID_PREFIX 
                + "[" + projectId + "]");

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }  
 
    @Test
    public void testRemoveProjectId() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "abs";
        String projectId = "1234";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn("prId[2], prId[1234], prId[1]");
        
        fedoraRepositoryManager.removeProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, "prId[2], prId[1]"); 

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }
    
    @Test
    public void testRemoveProjectIdBeginning() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "abs";
        String projectId = "234";
        String existingProjId = PROJ_ID_PREFIX + "[10]";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn("prId[234], prId[10]");
        
        fedoraRepositoryManager.removeProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, existingProjId); 

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }
    
    @Test
    public void testRemoveProjectIdEnd() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "as";
        String projectId = "34";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn("prId[19], prId[34]");
        
        fedoraRepositoryManager.removeProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, PROJ_ID_PREFIX + "[19]"); 

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }
    
    @Test
    public void testRemoveProjectIdOnly() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "a";
        String projectId = "3";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn("prId[3]");
        
        fedoraRepositoryManager.removeProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter).removeProperty(resultId, prop); 

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }
    
    @Test
    public void testRemoveProjectIdNull() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "ab";
        String projectId = "123";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn(null);
        
        fedoraRepositoryManager.removeProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter, never()).removeProperty(resultId, prop);     

        verify(genericSearchIndexUpdater, never()).updateChangedIndex(resultId);
    }
 
    @Test
    public void testRemoveProjectIdEmpty() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "abs";
        String projectId = "1234";
        FedoraResultProperties prop = FedoraResultProperties.PROJECT_ID;
        when(fedoraRepositoryGetter.getProperty(resultId, prop)).thenReturn("");
        
        fedoraRepositoryManager.removeProjectId(resultId, projectId);
        
        verify(fedoraRepositorySetter, never()).removeProperty(resultId, prop);   

        verify(genericSearchIndexUpdater, never()).updateChangedIndex(resultId);
    }
    
    @Test
    public void testGetResultsByOwner() throws FedoraException
    {
        
        String owner = "owner";
        List<ResultDTO> expected = new ArrayList<ResultDTO>();
        
        when(fedoraRepositoryGetter.getResultsByOwner(anyString(), anyBoolean())).thenReturn(expected);
        
        List<ResultDTO> actual = fedoraRepositoryManager.getResultsByOwner(owner, true);
        
        assertEquals(expected, actual);
        
        verify(fedoraRepositoryGetter).getResultsByOwner(owner, true);
    }

    @Test
    public void testAddAttachmentRef() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:45435";
        String attachmentPath = "asd/fgh";
        FedoraResultProperties prop = FedoraResultProperties.ATTACHMENT;

        fedoraRepositoryManager.addAttachmentReference(resultId, attachmentPath);
        
        verify(fedoraRepositorySetter).putProperty(resultId, prop, attachmentPath);

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }

    @Test
    public void testGetAttachmentRef() throws FedoraException
    {
        String resultId = "gda:45435";
        String attachmentPath = "asd/fgh";
        FedoraResultProperties prop = FedoraResultProperties.ATTACHMENT;

        when(fedoraRepositoryGetter.getDataStream(eq(resultId), eq(prop.toString()))).thenReturn(
               testDoc);
        when(fedoraRepositoryGetter.getProperty(eq(resultId), eq(prop))).thenReturn(attachmentPath);
        
        String content = fedoraRepositoryManager.getAttachmentReference(resultId);
        
        verify(fedoraRepositoryGetter).getProperty(resultId, prop);    
        assertEquals(attachmentPath, content);
    }
    
    @Test
    public void testAddParentSuccess() throws FedoraException
    {
        String childId = "GDA:123";
        String parentId = "GDA:456";
        
        when(fedoraRepositoryRelationshipManager.addRelationship(childId, 
                                                                 FedoraRepositoryRelationshipManager.IS_DERIVED_FROM, 
                                                                 parentId, 
                                                                 false, 
                                                                 null)).thenReturn(true);

        boolean returnVal = fedoraRepositoryManager.addParent(childId, parentId);
        
        assertEquals("Relationship created", true, returnVal);
    }

    @Test
    public void testAddParentFail() throws FedoraException
    {
        String childId = "GDA:123";
        String parentId = "GDA:456";
        
        when(fedoraRepositoryRelationshipManager.addRelationship(childId, 
                                                                 FedoraRepositoryRelationshipManager.IS_DERIVED_FROM, 
                                                                 parentId, 
                                                                 false, 
                                                                 null)).thenReturn(false);
        
        boolean returnVal = fedoraRepositoryManager.addParent(childId, parentId);
        
        assertEquals("Relationship not created", false, returnVal);
    }

    @Test
    public void testRemoveParentSuccess() throws FedoraException
    {
        String childId = "GDA:123";
        String parentId = "GDA:456";
        
        when(fedoraRepositoryRelationshipManager.purgeRelationship(childId, 
                                                                   FedoraRepositoryRelationshipManager.IS_DERIVED_FROM, 
                                                                   parentId, 
                                                                   false, 
                                                                   null)).thenReturn(true);

        boolean returnVal = fedoraRepositoryManager.removeParent(childId, parentId);
        
        assertEquals("Relationship removed", true, returnVal);
    }

    @Test
    public void testRemoveParentFail() throws FedoraException
    {
        String childId = "GDA:123";
        String parentId = "GDA:456";
        
        when(fedoraRepositoryRelationshipManager.purgeRelationship(childId, 
                                                                   FedoraRepositoryRelationshipManager.IS_DERIVED_FROM, 
                                                                   parentId, 
                                                                   false, 
                                                                   null)).thenReturn(false);
        
        boolean returnVal = fedoraRepositoryManager.removeParent(childId, parentId);
        
        assertEquals("Relationship not removed", false, returnVal);
    }

    @Test
    public void testGetParents() throws FedoraException
    {
        String childId = "GDA:123";
        String parentId1 = "GDA:456";
        String parentId2 = "GDA:789";
        
        RelationshipTuple parentTuple1 = new RelationshipTuple();
        parentTuple1.setSubject(childId);
        parentTuple1.setPredicate(FedoraRepositoryRelationshipManager.IS_DERIVED_FROM);
        parentTuple1.setObject(parentId1);
        parentTuple1.setIsLiteral(false);
        parentTuple1.setDatatype(null);
        
        RelationshipTuple parentTuple2 = new RelationshipTuple();
        parentTuple2.setSubject(childId);
        parentTuple2.setPredicate(FedoraRepositoryRelationshipManager.IS_DERIVED_FROM);
        parentTuple2.setObject(parentId2);
        parentTuple2.setIsLiteral(false);
        parentTuple2.setDatatype(null);
        
        RelationshipTuple[] relationArray = new RelationshipTuple[2];
        relationArray[0] = parentTuple1;
        relationArray[1] = parentTuple2;
        
        ResultDTO parentResult1 = new ResultDTO();
        parentResult1.setId(parentId1);
        parentResult1.setType("type");
        parentResult1.setName("Name 1");

        ResultDTO parentResult2 = new ResultDTO();
        parentResult2.setId(parentId2);
        parentResult2.setType("type");
        parentResult2.setName("Name 2");
        
        List<ResultDTO> parentList = new ArrayList<ResultDTO>(0);
        parentList.add(parentResult1);
        parentList.add(parentResult2);
        
        when(fedoraRepositoryRelationshipManager.getRelationships(
                                        childId, 
                                        FedoraRepositoryRelationshipManager.IS_DERIVED_FROM)).thenReturn(relationArray);
        
        when(fedoraRepositoryGetter.getResult(parentId1)).thenReturn(parentResult1);
        when(fedoraRepositoryGetter.getResult(parentId2)).thenReturn(parentResult2);
        
        List<ResultDTO> actual = fedoraRepositoryManager.getAllParents(childId);
        
        assertEquals("Parent List size", parentList.size(), actual.size());
        assertEquals("First parent id", parentId1, actual.get(0).getId());
        assertEquals("Second parent id", parentId2, actual.get(1).getId());
    }

    @Test
    public void testGetParentsEmptyList() throws FedoraException
    {
        String childId = "GDA:123";
        
        when(fedoraRepositoryRelationshipManager.getRelationships(
                                        childId, 
                                        FedoraRepositoryRelationshipManager.IS_DERIVED_FROM)).thenReturn(null);
        
        List<ResultDTO> actual = fedoraRepositoryManager.getAllParents(childId);
        
        assertEquals("Parent List is empty", 0, actual.size());
    }
    
    @Test
    public void testGetAllChildrenSuccess() throws FedoraException
    {
        String parentId = "GDA:1";
        
        String childId1 = "GDA:2";
        String childId2 = "GDA:3";
        
        String expectedTuple1 = parentId + ",info:fedora/" + childId1;
        String expectedTuple2 = parentId + ",info:fedora/" + childId2;
        String[] expectedTuples = {expectedTuple1, expectedTuple2};
        
        ResultDTO childResult1 = new ResultDTO();
        childResult1.setId(childId1);
        childResult1.setType("type");
        childResult1.setName("Name 1");

        ResultDTO childResult2 = new ResultDTO();
        childResult2.setId(childId2);
        childResult2.setType("type");
        childResult2.setName("Name 2");
        
        List<ResultDTO> expectedChildren = new ArrayList<ResultDTO>(0);
        expectedChildren.add(childResult1);
        expectedChildren.add(childResult2);
        
        String query = fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(parentId);
        
        when(
                fedoraRepositoryRISearcher.getTuples("true", FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL,
                        FedoraRepositoryRISearcherImpl.RETURN_FORMAT_CSV, null, "off", "off",
                        query)).thenReturn(expectedTuples);
        
        when(fedoraRepositoryGetter.getResult(childId1)).thenReturn(childResult1);
        when(fedoraRepositoryGetter.getResult(childId2)).thenReturn(childResult2);
        
        List<ResultDTO> children = fedoraRepositoryManager.getAllChildren(parentId);
        
        assertEquals("Number of children:", expectedChildren.size(), children.size());
        
        verify(fedoraRepositoryRISearcher).getTuples("true", FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL,
                FedoraRepositoryRISearcher.RETURN_FORMAT_CSV, null, "off", "off", query);
    }

    @Test
    public void testGetAllChildrenNoChildren() throws FedoraException
    {
        String parentId = "GDA:1";
        
        String expectedTuple1 = "parentId $p $o";
        String[] expectedTuples = {expectedTuple1};
        
        List<ResultDTO> expectedChildren = new ArrayList<ResultDTO>(0);
        
        String query = fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(parentId);

                
        when(fedoraRepositoryRISearcher.getTuples(FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL, 
                                                 FedoraRepositoryRISearcher.RETURN_FORMAT_CSV,
                                                 query)).thenReturn(expectedTuples);
        
        List<ResultDTO> children = fedoraRepositoryManager.getAllChildren(parentId);
        
        assertEquals("Number of children:", expectedChildren.size(), children.size());
        
        verify(fedoraRepositoryRISearcher).getTuples("true", FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL,
                FedoraRepositoryRISearcher.RETURN_FORMAT_CSV, null, "off", "off", query);
    }

    @Test
    public void testGetAllChildrenNullResponse() throws FedoraException
    {
        String parentId = "GDA:1";
        
        List<ResultDTO> expectedChildren = new ArrayList<ResultDTO>();
        
        String query = fedoraRepositoryRISearcher.getChildrenSearchITQLQuery(parentId);
                
        when(fedoraRepositoryRISearcher.getTuples(FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL, 
                                                 FedoraRepositoryRISearcher.RETURN_FORMAT_CSV,
                                                 query)).thenReturn(null);
        
        List<ResultDTO> children = fedoraRepositoryManager.getAllChildren(parentId);
        
        assertEquals("Number of children:", expectedChildren, children);
        
        verify(fedoraRepositoryRISearcher).getTuples("true", FedoraRepositoryRISearcher.QUERY_LANGUAGE_ITQL,
                FedoraRepositoryRISearcher.RETURN_FORMAT_CSV, null, "off", "off", query);
    }
    
    @Test
    public void testCriteriaSearchSuccess() throws FedoraException
    {
        List<ResultDTO> res = new ArrayList<ResultDTO>();
        when(fedoraRepositoryGetter.getResultsByCriteria((ResultSearchCriteriaDTO) any())).thenReturn(res);
       
        List<ResultDTO> results = fedoraRepositoryManager.getResultsByCriteria(new ResultSearchCriteriaDTO());
        
        assertEquals("Number of children:", res.size(), results.size());
        
    }
    
    @Test
    public void testDeleteResult() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "GDA:3434";
        
        fedoraRepositoryManager.deleteResult(resultId);
        verify(fedoraRepositorySetter).deleteObject(resultId);

        verify(genericSearchIndexUpdater).updateDeletedIndex(resultId);
    }
    
    @Test
    public void testDeleteResultException() throws FedoraException
    {
        String resultId = "GDA:342433";
        FedoraException ex = new FedoraException("called");
        doThrow(ex).when(fedoraRepositorySetter).deleteObject(resultId);
        try
        {
            fedoraRepositoryManager.deleteResult(resultId);
        } catch (FedoraException e)
        {
            assertSame(ex, e);
        }
    }
    
    @Test
    public void testRemoveAttachmentReference() throws FedoraException, FedoraUpdateIndexException
    {
        String resultId = "gda:123";
        fedoraRepositoryManager.removeAttachmentReference(resultId);
        verify(fedoraRepositorySetter).removeProperty(resultId, FedoraResultProperties.ATTACHMENT);

        verify(genericSearchIndexUpdater).updateChangedIndex(resultId);
    }
    
    
    @Test
    public void testGetPaginatedResults() throws FedoraException
    {
        String ownerName = null;
        Integer page = 1;
        String sortColumn = "col1";
        Boolean order = true;
        List<String> projIdList = null;
        boolean includeProjectResults = false;
        ResultSearchPagination paginatedResult = 
            new ResultSearchPagination(1, 1, 1, new ArrayList<ResultDTO>());
        
        when(fedoraRepositoryManager.getPaginatedResults(
                ownerName, page, sortColumn, order, projIdList, includeProjectResults)).thenReturn(paginatedResult);
        
        ResultSearchPagination actualReturn = fedoraRepositoryManager.getPaginatedResults(
                ownerName, page, sortColumn, order, projIdList, includeProjectResults);        
        
        assertEquals(paginatedResult, actualReturn);

    }
    
    @Test
    public void testGetResultsByCriteria() throws FedoraException
    {
        String ownerName = null;
        Integer page = 1;
        String sortColumn = "col1";
        Boolean order = true;
        List<String> projIdList = null;
        
        ResultSearchCriteriaDTO criteria = new ResultSearchCriteriaDTO();

        
        ResultSearchPagination paginatedResult = 
            new ResultSearchPagination(1, 1, 1, new ArrayList<ResultDTO>());
        
        when(fedoraRepositoryManager.getResultsByCriteria(
                criteria, ownerName, page, sortColumn, order, projIdList)).thenReturn(paginatedResult);
        
        ResultSearchPagination actualReturn = fedoraRepositoryManager.getResultsByCriteria(
                criteria, ownerName, page, sortColumn, order, projIdList);        
        
        assertEquals(paginatedResult, actualReturn);

    }

    private ResultDTO getResultDTO()
    {
        ResultDTO result = new ResultDTO();
        result.setName("name");
        result.setType("type");
        return result;
    }
}
