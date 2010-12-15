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
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NonNegativeInteger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.management.FedoraAPIMServiceLocator;
import fedora.server.types.gen.ObjectProfile;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.metadata.GdaObjectMetaDataHelper;
import au.org.intersect.gda.metadata.GdaObjectMetaDataHelperException;

/**
 * @version $Rev$
 * 
 */
public class FedoraResultRepositorySetterImplTest
{
    private FedoraResultRepositorySetterImpl fedoraRepositorySetter;
    @Mock
    private FedoraComponentFactory fedoraComponentFactory;
    @Mock
    private FedoraClient fc;
    @Mock
    private FedoraAPIA apia;
    @Mock
    private FedoraAPIM apim;
    @Mock
    private FedoraDatastreamHelper fedoraDsHelper;
    @Mock
    private GdaObjectMetaDataHelper objMetaDataHelper;
    @Mock
    private FedoraXmlTemplateBuilder fedoraXmlTemplateBuilder;
    @Mock
    private FedoraConfig fedoraConfig;
    
    private DocumentBuilder docBuilder;
    private String xmlMime = "text/xml";

    @Before
    public void setUp() throws MalformedURLException, ServiceException, IOException, ParserConfigurationException
    {
        MockitoAnnotations.initMocks(this);
        fedoraRepositorySetter = new FedoraResultRepositorySetterImpl(fedoraXmlTemplateBuilder, fedoraComponentFactory,
                fedoraDsHelper, objMetaDataHelper, fedoraConfig);
        when(fedoraComponentFactory.buildFedoraClient()).thenReturn(fc);
        when(fc.getAPIA()).thenReturn(apia);
        when(fc.getAPIM()).thenReturn(apim);
        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        
        docBuilderFactory.setNamespaceAware(true);
        
        docBuilder = docBuilderFactory.newDocumentBuilder();
        
    }

    @Test
    public void testCreateResultInRepository() throws FedoraException, RemoteException
    {
        ResultDTO result = getResultDTO();
        ObjectProfile objectProfile = mock(ObjectProfile.class);

        String nextPID = "gda:new";
        String someXmlTemplate = "<xml>some xml</xml>";
        String logMessage = "Inserting: " + result.getName() + " of type: " + result.getType();

        when(objectProfile.getObjLabel()).thenReturn(nextPID);
        when(objectProfile.getObjCreateDate()).thenReturn("2000-03-02T07:38:26.868Z");
        when(objectProfile.getObjLastModDate()).thenReturn("2010-01-07T05:48:46.828Z");

        ArgumentCaptor<NonNegativeInteger> numberCaptor = ArgumentCaptor.forClass(NonNegativeInteger.class);
        when(apim.getNextPID(numberCaptor.capture(), anyString())).thenReturn(new String[] {nextPID});
        when(apia.getObjectProfile("gda:new", null)).thenReturn(objectProfile);
        when(fedoraXmlTemplateBuilder.buildXmlTemplate(anyString())).thenReturn(someXmlTemplate);

        fedoraRepositorySetter.createObjectInRepository(result);
        verify(apim).getNextPID((NonNegativeInteger) anyObject(), anyString());
        verify(apim).ingest(someXmlTemplate.getBytes(), FedoraAPIMServiceLocator.FOXML1_1.toString(), logMessage);
        assertEquals(nextPID, result.getId());
        assertEquals(new NonNegativeInteger("1"), numberCaptor.getValue());
    }

    @Test
    public void testStoreDataStream() throws FedoraException, SAXException, IOException
    {
        ArgumentCaptor<String> pidCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dsIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> labelCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mimeCaptor = ArgumentCaptor.forClass(String.class);
        
        ArgumentCaptor<Document> contentCaptor = ArgumentCaptor.forClass(Document.class);

        String pid = "foo:bar";
        String dsId = "ds1";
        String label = "label1";
        Document content = docBuilder.parse(new InputSource(new StringReader("<meta>blahblah</meta>")));

        when(
                fedoraRepositorySetter.storeDataStream(pidCaptor.capture(), dsIdCaptor.capture(),
                        labelCaptor.capture(), mimeCaptor.capture(), contentCaptor.capture())).thenReturn("foo:bar");

        fedoraRepositorySetter.storeDataStream(pid, dsId, label, xmlMime, content);

        verify(fedoraDsHelper).storeDataStream(anyString(), anyString(), anyString(), anyString(), (Document) any());

        assertEquals(pid, pidCaptor.getValue());
        assertEquals(label, labelCaptor.getValue());
        assertEquals(dsId, dsIdCaptor.getValue());
        assertEquals(content, contentCaptor.getValue());
        assertEquals(xmlMime, mimeCaptor.getValue());
    }

    @Test
    public void testEditDataStream() throws FedoraException, SAXException, IOException
    {
        String pid = "foo:bar";
        String dsId = "ds1";
        Document content = docBuilder.parse(new InputSource(new StringReader("<meta>blahblah</meta>")));

        fedoraRepositorySetter.editDataStream(pid, dsId, xmlMime, content);

        verify(fedoraDsHelper).editDataStream(pid, dsId, null, xmlMime, content);
    }

    @Test
    public void testRemoveDataStream() throws FedoraException
    {
        String pid = "foo:bar";
        String dsId = "ds1";

        fedoraRepositorySetter.removeDataStream(pid, dsId);

        verify(fedoraDsHelper).removeDataStream(pid, dsId);
    }

    @Test
    public void testPutProperty() throws FedoraException, GdaObjectMetaDataHelperException, SAXException, IOException
    {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        String input = "~!@#$%^&*()_+";
        String resId = "a:b";
        
        Document dcContent = docBuilder.parse(new InputSource(new StringReader("<xml>somexml</xml>")));

        
        when(fedoraDsHelper.getDataStream(anyString(), anyString())).thenReturn(dcContent);
        fedoraRepositorySetter.putProperty(resId, FedoraResultProperties.CREATOR, input);

        verify(objMetaDataHelper).setProperty((Document) any(), (FedoraResultProperties) any(), captor.capture());
        verify(fedoraDsHelper).storeDataStream(resId, "DC", null, xmlMime, dcContent);
        
        String expected = input;
        assertEquals(expected, captor.getValue());
    }
    
    @Test (expected = FedoraException.class)
    public void testPutPropertyFail1() throws FedoraException, GdaObjectMetaDataHelperException
    {
        FedoraException ex = new FedoraException("fail");
        String input = "~!@#$%^&*()_+";
        when(fedoraDsHelper.getDataStream(anyString(), anyString())).thenThrow(ex);
        fedoraRepositorySetter.putProperty("a:b", FedoraResultProperties.CREATOR, input);
    }
    

    @Test
    public void testDeleteResult() throws FedoraException, ServiceException, IOException
    {
        String resultId = "GDA:222";

        fedoraRepositorySetter.deleteObject(resultId);

        verify(fedoraComponentFactory).buildFedoraClient();
        verify(fc).getAPIM();
        verify(apim).purgeObject(eq(resultId), anyString(), eq(false));
    }

    @Test
    public void testDeleteResultMalformed() throws FedoraException, ServiceException, IOException
    {
        String resultId = "GDA:222";
        MalformedURLException ex = new MalformedURLException();
        when(fedoraComponentFactory.buildFedoraClient()).thenThrow(ex);

        try
        {
            fedoraRepositorySetter.deleteObject(resultId);
        } catch (FedoraException e)
        {
            assertSame(ex, e.getCause());
        }
    }

    @Test
    public void testDeleteResultService() throws FedoraException, ServiceException, IOException
    {
        String resultId = "GDA:222";
        ServiceException ex = new ServiceException();
        when(fc.getAPIM()).thenThrow(ex);

        try
        {
            fedoraRepositorySetter.deleteObject(resultId);
        } catch (FedoraException e)
        {
            assertSame(ex, e.getCause());
        }
    }

    @Test
    public void testDeleteResultRemote() throws FedoraException, ServiceException, IOException
    {
        String resultId = "GDA:222";
        RemoteException ex = new RemoteException();
        when(apim.purgeObject(anyString(), anyString(), eq(false))).thenThrow(ex);

        try
        {
            fedoraRepositorySetter.deleteObject(resultId);
        } catch (FedoraException e)
        {
            assertSame(ex, e.getCause());
        }
    }

    @Test
    public void testDeleteResultIO() throws FedoraException, ServiceException, IOException
    {
        String resultId = "GDA:222";
        IOException ex = new IOException();
        when(fc.getAPIM()).thenThrow(ex);

        try
        {
            fedoraRepositorySetter.deleteObject(resultId);
        } catch (FedoraException e)
        {
            assertSame(ex, e.getCause());
        }
    }
    
    @Test
    public void testRemoveProperty() throws FedoraException, GdaObjectMetaDataHelperException, SAXException, IOException
    {
        String resId = "gda:123";
        
        Document dcContent = docBuilder.parse(new InputSource(new StringReader("<meta>blahblah</meta>")));

        
        when(fedoraDsHelper.getDataStream(anyString(), anyString())).thenReturn(dcContent);
        
        fedoraRepositorySetter.removeProperty(resId, FedoraResultProperties.ATTACHMENT);
        verify(objMetaDataHelper).removeProperty(dcContent, FedoraResultProperties.ATTACHMENT);
        verify(fedoraDsHelper).storeDataStream(resId, "DC", null, xmlMime, dcContent);
    }

    @Test (expected = FedoraException.class)
    public void testRemovePropertyFail1() throws FedoraException, GdaObjectMetaDataHelperException
    {
        FedoraException ex = new FedoraException("fail");
        when(fedoraDsHelper.getDataStream(anyString(), anyString())).thenThrow(ex);
        fedoraRepositorySetter.removeProperty("a:b", FedoraResultProperties.CREATOR);
    }    
    
    
    private ResultDTO getResultDTO()
    {
        ResultDTO result = new ResultDTO();
        result.setName("name");
        result.setType("type");
        result.setOwner("owner");
        return result;
    }
}
