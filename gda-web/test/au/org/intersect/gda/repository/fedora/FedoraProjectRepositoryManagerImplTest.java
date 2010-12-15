/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.org.intersect.gda.dto.ProjectOaiDTO;
import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.oai.OaiConfig;
import au.org.intersect.gda.oai.OaiConfigImpl;
import au.org.intersect.gda.oai.OaiIdGenerator;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.xml.XMLUtil;

/**
 * @version $Rev$
 *
 */
public class FedoraProjectRepositoryManagerImplTest
{

    private static final String RELS_EXT_DS_ID = "RELS-EXT";
    
    private static final String PROJECT_DS = "PROJECT";

    private static final String RELS_EXT_MIME_TYPE = "application/rdf+xml";
    
    private static final String RELS_EXT_NODE_NAME = "oai:itemID";
    
    
    @Mock
    private FedoraDatastreamHelper fedoraDsHelper;
    
    @Mock
    private FedoraRepositorySetter<ProjectOaiDTO> fedoraProjectRepositorySetter;
    
    @Mock
    private FedoraXmlTemplateBuilder fedoraXmlTemplateBuilder;
    
    private String xsdTimestampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
    
    @Mock
    private OaiIdGenerator oaiIdGenerator;
    
    private FedoraProjectRepositoryManagerImpl repository;
    
    private Timestamp currentTime = new Timestamp(System.currentTimeMillis());
    
    private Calendar currentCalendar = Calendar.getInstance();
    
    private String relsTemplate;
    
    private String relsMarkedTemplate;
    
    private String objectTemplate;
    
    private DocumentBuilderFactory docBuilderFactory;
    
    @Before
    public void setUp() throws SAXException, IOException, ParserConfigurationException
    {
        MockitoAnnotations.initMocks(this);
        
        OaiConfig oaiConfig = 
            new OaiConfigImpl(
                    xsdTimestampFormat,
                    RELS_EXT_DS_ID,
                    PROJECT_DS,
                    "uuid",
                    "uuid",
                    RELS_EXT_MIME_TYPE,
                    "//rdf:Description",
                    RELS_EXT_NODE_NAME,
                    "//" + RELS_EXT_NODE_NAME        
            );
        
        repository = new FedoraProjectRepositoryManagerImpl(
                fedoraDsHelper, 
                fedoraProjectRepositorySetter, 
                fedoraXmlTemplateBuilder,                 
                oaiIdGenerator,
                oaiConfig);
        
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        
        File relsFile = new File("WebContent/WEB-INF/fedoraXmlTemplates/rels-ext.xml");                
        //read in the test xml data as string
        relsTemplate = new Scanner(relsFile).useDelimiter("\\Z").next();    
        

        File relsMarkedFile = new File("WebContent/WEB-INF/fedoraXmlTemplates/rels-ext.xml");                
        //read in the test xml data as string
        relsMarkedTemplate = new Scanner(relsMarkedFile).useDelimiter("\\Z").next();  
        
        relsMarkedTemplate = relsMarkedTemplate.replace("</rdf:Description>", 
                "<oai:itemID>oai:gda:2</oai:itemID></rdf:Description>");
        
        File objFile = new File("WebContent/WEB-INF/fedoraXmlTemplates/template.xml");                
        //read in the test xml data as string
        objectTemplate = new Scanner(objFile).useDelimiter("\\Z").next();  
        

        String formattedObjDoc = String.format(objectTemplate, 1);
        

        String formattedRelsDoc = String.format(relsTemplate, 1);
                
        
        when(fedoraXmlTemplateBuilder.buildXmlTemplate(anyString()))
            .thenReturn(formattedRelsDoc).thenReturn(formattedObjDoc);                
        
    }
    
    
    private Document getRelsTemplateDoc(Integer id) throws SAXException, IOException, ParserConfigurationException
    {

        String formattedRelsDoc = String.format(relsTemplate, id);
                
        Document relsTemplateDoc = 
            docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(formattedRelsDoc)));
        
        return relsTemplateDoc;
    }
    

    private Document getMarkedRelsTemplateDoc(Integer id) throws SAXException, IOException, ParserConfigurationException
    {
        String formattedRelsDoc = String.format(relsMarkedTemplate, id);
                
        
        Document relsTemplateDoc = 
            docBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(formattedRelsDoc)));
        
        return relsTemplateDoc;
    }
    
    @Test
    public void createProjectInRepositoryMarked() 
        throws RepositoryException, TransformerException, SAXException, IOException, ParserConfigurationException
    {
        Integer projectId = 1;
        
        ProjectOaiDTO oaiDTO = createOaiDTO();
        
        String fedoraId = "pid";
        when(fedoraProjectRepositorySetter.createObjectInRepository(oaiDTO)).thenReturn(fedoraId);
        when(fedoraDsHelper.dataStreamExists(fedoraId, RELS_EXT_DS_ID)).thenReturn(false);        
        
        Document blankReturn = getRelsTemplateDoc(1);
        
        Document nextReturn = getRelsTemplateDoc(2);
        
        when(fedoraDsHelper.getDataStream(fedoraId, RELS_EXT_DS_ID)).thenReturn(
                blankReturn).thenReturn(nextReturn);
        
        when(oaiIdGenerator.generateId(String.valueOf(1))).thenReturn("oai:gda:1");
        
        repository.createProjectInRepository(projectId, oaiDTO);
        
        ArgumentCaptor<Document> relsDsCapture = ArgumentCaptor.forClass(Document.class);
        
        verify(fedoraDsHelper, times(1)).storeDataStream(
                eq(fedoraId), 
                eq(RELS_EXT_DS_ID), 
                eq(RELS_EXT_DS_ID), 
                eq(RELS_EXT_MIME_TYPE), 
                relsDsCapture.capture());
        
        
        Document capturedRelsDs = relsDsCapture.getValue();
        
        String relsDsString = XMLUtil.documentToString(capturedRelsDs);
        
        assertTrue("Rels-Ext should have have been tagged for oai polling", relsDsString.contains(RELS_EXT_NODE_NAME));
        
        ArgumentCaptor<Document> projectDsCapture = ArgumentCaptor.forClass(Document.class);        
        
        
        verify(fedoraDsHelper).dataStreamExists(fedoraId, RELS_EXT_DS_ID);
        verify(fedoraDsHelper).storeDataStream(
                eq(fedoraId), 
                eq(PROJECT_DS), 
                eq(PROJECT_DS), 
                eq("text/xml"), 
                projectDsCapture.capture());
        Document capturedProjectDsDs = projectDsCapture.getValue();
        String projectDsString = XMLUtil.documentToString(capturedProjectDsDs);
        assertTrue(projectDsString.contains(oaiDTO.getDescription()));

    }
    
    @Test
    public void createProjectInRepositoryNotMarked() 
        throws RepositoryException, TransformerException, SAXException, IOException, ParserConfigurationException
    {
        Integer projectId = 1;
        
        ProjectOaiDTO oaiDTO = createOaiDTO();
        
        oaiDTO.setMarkForExport(false);
        
        String fedoraId = "pid";
        when(fedoraProjectRepositorySetter.createObjectInRepository(oaiDTO)).thenReturn(fedoraId);
        when(fedoraDsHelper.dataStreamExists(fedoraId, RELS_EXT_DS_ID)).thenReturn(false);        
        
        Document blankReturn = getRelsTemplateDoc(1);
        
        Document nextReturn = getRelsTemplateDoc(2);
        
        when(fedoraDsHelper.getDataStream(fedoraId, RELS_EXT_DS_ID)).thenReturn(
                blankReturn).thenReturn(nextReturn);
        
        when(oaiIdGenerator.generateId(String.valueOf(1))).thenReturn("oai:gda:1");
        
        repository.createProjectInRepository(projectId, oaiDTO);
        
        verify(fedoraProjectRepositorySetter, times(1)).changeObjectState(eq(fedoraId), eq("D"));
       
    }
    
    
    

    @Test
    public void updateProjectInRepositoryMarkedNoRifCsId() 
        throws RepositoryException, TransformerException, SAXException, IOException, ParserConfigurationException
    {
        Integer projectId = 1;
        
        
        
        ProjectOaiDTO oaiDTO = createOaiDTO();
        oaiDTO.setMarkForExport(true);
        
        String fedoraId = "pid1";
        when(fedoraProjectRepositorySetter.createObjectInRepository(oaiDTO)).thenReturn(fedoraId);
        when(fedoraDsHelper.dataStreamExists(fedoraId, RELS_EXT_DS_ID)).thenReturn(true);        
        
        Document markedReturn = getMarkedRelsTemplateDoc(1);
        
        
        when(fedoraDsHelper.getDataStream(fedoraId, RELS_EXT_DS_ID)).thenReturn(
                markedReturn).thenReturn(markedReturn);
        
        when(oaiIdGenerator.generateId(String.valueOf(1))).thenReturn("oai:gda:1");
        
        repository.updateProjectInRepository(projectId, fedoraId, oaiDTO);
        
        //verify it's ignored as it's already marked
        verify(fedoraDsHelper, times(0)).storeDataStream(
                eq(fedoraId), 
                eq(RELS_EXT_DS_ID), 
                eq(RELS_EXT_DS_ID), 
                eq(RELS_EXT_MIME_TYPE), 
                (Document) any());
        
        
        ArgumentCaptor<Document> projectDsCapture = ArgumentCaptor.forClass(Document.class);        
        
        verify(fedoraDsHelper).dataStreamExists(fedoraId, RELS_EXT_DS_ID);
        verify(fedoraDsHelper).storeDataStream(
                eq(fedoraId), 
                eq(PROJECT_DS), 
                eq(PROJECT_DS), 
                eq("text/xml"), 
                projectDsCapture.capture());
        Document capturedProjectDsDs = projectDsCapture.getValue();
        String projectDsString = XMLUtil.documentToString(capturedProjectDsDs);
        assertTrue(projectDsString.contains(oaiDTO.getDescription()));

    }
    

    @Test
    public void updateProjectInRepositoryMarkedHasRifCsId() 
        throws RepositoryException, TransformerException, SAXException, IOException, ParserConfigurationException
    {
        Integer projectId = 1;
                
        ProjectOaiDTO oaiDTO = createOaiDTO();
        oaiDTO.setMarkForExport(true);
        
        String fedoraId = "pid1";
        when(fedoraProjectRepositorySetter.createObjectInRepository(oaiDTO)).thenReturn(fedoraId);
        when(fedoraDsHelper.dataStreamExists(fedoraId, RELS_EXT_DS_ID)).thenReturn(true);        
        when(fedoraDsHelper.dataStreamExists(fedoraId, "uuid")).thenReturn(true);        

        Document markedReturn = getMarkedRelsTemplateDoc(1);
        
        //it should retrive the uuid from fedora
        String uuidXmlStr = "<uuid>abcd</uuid>";
        
        Document uuidXmlDoc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(new InputSource(new StringReader(uuidXmlStr)));
        
        when(fedoraDsHelper.getDataStream(fedoraId, RELS_EXT_DS_ID)).thenReturn(
                markedReturn).thenReturn(markedReturn);        

        when(fedoraDsHelper.getDataStream(fedoraId, "uuid")).thenReturn(uuidXmlDoc);
        
        
        when(oaiIdGenerator.generateId(String.valueOf(1))).thenReturn("oai:gda:1");
        
        repository.updateProjectInRepository(projectId, fedoraId, oaiDTO);
        
        //verify it's ignored as it's already marked
        verify(fedoraDsHelper, times(0)).storeDataStream(
                eq(fedoraId), 
                eq(RELS_EXT_DS_ID), 
                eq(RELS_EXT_DS_ID), 
                eq(RELS_EXT_MIME_TYPE), 
                (Document) any());
        
        
        ArgumentCaptor<Document> projectDsCapture = ArgumentCaptor.forClass(Document.class);        
        
        verify(fedoraDsHelper).getDataStream(eq(fedoraId), eq("uuid"));
        verify(fedoraDsHelper).dataStreamExists(fedoraId, RELS_EXT_DS_ID);
        verify(fedoraDsHelper).storeDataStream(
                eq(fedoraId), 
                eq(PROJECT_DS), 
                eq(PROJECT_DS), 
                eq("text/xml"), 
                projectDsCapture.capture());
        
        
        Document capturedProjectDsDs = projectDsCapture.getValue();
        String projectDsString = XMLUtil.documentToString(capturedProjectDsDs);
        assertTrue(projectDsString.contains(oaiDTO.getDescription()));

    }
    
    @Test
    public void updateProjectInRepositoryNotMarked() 
        throws RepositoryException, TransformerException, SAXException, IOException, ParserConfigurationException
    {
        Integer projectId = 1;
        
        ProjectOaiDTO oaiDTO = createOaiDTO();
        
        oaiDTO.setMarkForExport(false);
        
        String fedoraId = "pid";
        when(fedoraProjectRepositorySetter.createObjectInRepository(oaiDTO)).thenReturn(fedoraId);
        when(fedoraDsHelper.dataStreamExists(fedoraId, RELS_EXT_DS_ID)).thenReturn(true);        
        
        Document markedReturn = getMarkedRelsTemplateDoc(1);
                
        when(fedoraDsHelper.getDataStream(fedoraId, RELS_EXT_DS_ID)).thenReturn(
                markedReturn).thenReturn(markedReturn);
        
        when(oaiIdGenerator.generateId(String.valueOf(1))).thenReturn("oai:gda:1");
        
        repository.updateProjectInRepository(projectId, fedoraId, oaiDTO);
        

        verify(fedoraProjectRepositorySetter, times(1)).changeObjectState(eq(fedoraId), eq("D"));
        
    }
    

    @Test
    public void testDeleteProjectInRepository() 
        throws RepositoryException, TransformerException, SAXException, IOException, ParserConfigurationException
    {
        String fedoraId = "id";
        repository.deleteProjectInRepository(fedoraId);
        
        verify(fedoraProjectRepositorySetter, times(1)).changeObjectState(eq(fedoraId), eq("D"));

    }
    
    
    private ProjectOaiDTO createOaiDTO()
    {
        ProjectOaiDTO oaiDTO = new ProjectOaiDTO();
        
        oaiDTO.setName("project name");
        oaiDTO.setOwner("project owner");
        oaiDTO.setDateCreated(currentTime);
        oaiDTO.setDateModified(currentTime);
        
        oaiDTO.setDescription("project random description");
        
        oaiDTO.setMarkForExport(true);
        
        List<ResultDTO> resultList = new ArrayList<ResultDTO>();
        
        resultList.add(createResultDTO(1));
        resultList.add(createResultDTO(2));
        resultList.add(createResultDTO(3));
        
        oaiDTO.setResults(resultList);
        return oaiDTO;
    }
    
    private ResultDTO createResultDTO(int index)
    {
        ResultDTO resultDTO = new ResultDTO();

        resultDTO.setName("result name " + index);
        
        resultDTO.setCreator("creator");
        resultDTO.setCreatedDate(currentCalendar);
        resultDTO.setLastModifiedDate(currentCalendar);
        
        resultDTO.setOwner("owner");
        
        resultDTO.setType("type" + index);
        
        resultDTO.setId("resultId" + index);
        
        
        return resultDTO;
    }
}
