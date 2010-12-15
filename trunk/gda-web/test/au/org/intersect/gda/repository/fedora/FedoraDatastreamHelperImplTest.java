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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fedora.client.FedoraClient;
import fedora.client.Uploader;
import fedora.server.access.FedoraAPIA;
import fedora.server.errors.LowlevelStorageException;
import fedora.server.errors.ObjectLockedException;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import fedora.server.types.gen.MIMETypedStream;

import au.org.intersect.gda.util.InputStreamReader;
import au.org.intersect.gda.xml.XMLUtil;

/**
 * @version $Rev$
 *
 */
public class FedoraDatastreamHelperImplTest
{
    private FedoraDatastreamHelperImpl fedoraDsHelper;
    
    @Mock
    private FedoraComponentFactory fedoraComponentFactory;
    @Mock
    private FedoraClient fc;
    @Mock
    private FedoraAPIA apia;
    @Mock
    private FedoraAPIM apim;
    @Mock
    private Uploader uploader;
    
    private int retryLimit = 3;
    
    private Document testDoc;
    
    private String testDocString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><meta>apple</meta>";
    
    private String xmlMime = "text/xml";
    
    @Before
    public void setUp() throws ServiceException, IOException, ParserConfigurationException, SAXException
    {
        MockitoAnnotations.initMocks(this);
        fedoraDsHelper = new FedoraDatastreamHelperImpl(fedoraComponentFactory, retryLimit);
        when(fedoraComponentFactory.buildFedoraClient()).thenReturn(fc);
        when(fedoraComponentFactory.buildFedoraUploader()).thenReturn(uploader);
        when(fc.getAPIA()).thenReturn(apia);
        when(fc.getAPIM()).thenReturn(apim);     
        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        
        docBuilderFactory.setNamespaceAware(true);
        
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        
        testDoc = docBuilder.parse(new InputSource(new StringReader(testDocString)));
    }
    
    @Test
    public void testGetDataStream() throws RemoteException, FedoraException, TransformerException
    {
        String pid = "foo:bar";
        String dsId = "ds1";       
        MIMETypedStream stream = new MIMETypedStream();
        
        stream.setStream(testDocString.getBytes());
        
        ArgumentCaptor<String> pidCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
       
        when(apia.getDatastreamDissemination(pidCaptor.capture(), 
                                             dsCaptor.capture(), 
                                             dateCaptor.capture())).thenReturn(stream);
        
        Document content = fedoraDsHelper.getDataStream(pid, dsId);
        
        verify(apia).getDatastreamDissemination(anyString(), anyString(), anyString());
        
    
        assertEquals(XMLUtil.documentToString(testDoc), XMLUtil.documentToString(content));
        assertEquals(pid, pidCaptor.getValue());
        assertEquals(dsId, dsCaptor.getValue());
        assertEquals(null, dateCaptor.getValue());
    }
    
    @Test
    public void testGetDataStreamException1() throws MalformedURLException
    {
        String pid = "foo:bar";
        String dsId = "ds1";       

        try
        {
            when(fedoraComponentFactory.buildFedoraClient()).thenThrow(new MalformedURLException());
            
            fedoraDsHelper.getDataStream(pid, dsId);
        } catch (FedoraException e)
        {
            assertEquals(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, 
                         e.getMessage());
            return;
        }
        fail("Excpected FedoraException is not thrown");
    }
    
    @Test
    public void testGetDataStreamException2() throws RemoteException
    {
        String pid = "foo:bar";
        String dsId = "ds1";       

        try
        {
            when(apia.getDatastreamDissemination(anyString(), anyString(), anyString())).thenThrow(
                    new RemoteException());
            
            fedoraDsHelper.getDataStream(pid, dsId);
        } catch (FedoraException e)
        {
            assertEquals("Unable to retrieve or save data for foo:bar. Are you sure it has not been deleted?", 
                         e.getMessage());
            return;
        }
        fail("Excpected FedoraException is not thrown");
    }
    
    @Test
    public void testGetDataStreamException3() throws IOException, ServiceException
    {
        String pid = "foo:bar";
        String dsId = "ds1";       
        try
        {
            when(fc.getAPIA()).thenThrow(new IOException());
            
            fedoraDsHelper.getDataStream(pid, dsId);
        } catch (FedoraException e)
        {
            assertEquals(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, 
                         e.getMessage());
            return;
        }
        fail("Excpected FedoraException is not thrown");
    }
    
    @Test
    public void testGetDataStreamException4() throws IOException, ServiceException
    {
        String pid = "foo:bar";
        String dsId = "ds1";       
        try
        {
            when(fc.getAPIA()).thenThrow(new ServiceException());
            
            fedoraDsHelper.getDataStream(pid, dsId);
        } catch (FedoraException e)
        {
            assertEquals(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, 
                         e.getMessage());
            return;
        }
        fail("Excpected FedoraException is not thrown");
    }

    @Test
    public void testGetAllDataStreams() throws RemoteException, FedoraException
    {     
        String objId = "foo:bar";
        String state = "A";
        
        ArgumentCaptor<String> objIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        
        Datastream stream1 = new Datastream();
        stream1.setID("resultType");
        Datastream stream2 = new Datastream();
        stream2.setID("foobar");
        stream2.setLabel("xxx");
        Datastream stream3 = new Datastream();
        stream3.setID("DC");
        
        Datastream[] streams = new Datastream[] {stream1, stream2, stream3};
        
        when(apim.getDatastreams(objIdCaptor.capture(), dateCaptor.capture(),
                                stateCaptor.capture())).thenReturn(streams);
        
        Map<String, String> resMap = fedoraDsHelper.getAllDataStreams(objId);
        
        verify(apim).getDatastreams(anyString(), anyString(), anyString());
        
        assertEquals(2, resMap.size());
        assertEquals("xxx", resMap.get("foobar"));
        assertEquals(objId, objIdCaptor.getValue());
        assertEquals(state, stateCaptor.getValue());
        assertEquals(null, dateCaptor.getValue());
    }
    
    @Test 
    public void testStoreNewDataStream() throws IOException, FedoraException
    {
        String dsLoc = "anyLoc";
        String pid = "foo:bar";
        String dsId = "ds1";
        String label = "label1";
        
        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        
        ArgumentCaptor<String> pidCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dsIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> altIdsCaptor = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<String> labelCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> versionableCaptor = ArgumentCaptor.forClass(boolean.class);
        ArgumentCaptor<String> mimeTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dsLocCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> controlProjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> checkSumAlgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> checkSumCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> logMsgCaptor = ArgumentCaptor.forClass(String.class);
        
        // test the case where getdataStream throws an exception, which means
        // the datastream with the given pid does not exist
        when(apim.getDatastream(anyString(), anyString(), 
                                anyString())).thenThrow(new RemoteException());
        when(uploader.upload(inputStreamCaptor.capture())).thenReturn(dsLoc);
        when(apim.addDatastream(anyString(), anyString(),
                                (String[]) anyObject(),
                                anyString(), anyBoolean(), 
                                anyString(), anyString(), 
                                anyString(), anyString(), 
                                anyString(), anyString(), 
                                anyString(), anyString())).thenReturn(pid);
        
        String resPid = fedoraDsHelper.storeDataStream(pid, dsId, label, xmlMime, testDoc);
        
        verify(apim).getDatastream(pid, dsId, null);
        verify(uploader).upload((InputStream) anyObject());
        verify(apim).addDatastream(pidCaptor.capture(), dsIdCaptor.capture(), 
                            altIdsCaptor.capture(), labelCaptor.capture(), 
                            versionableCaptor.capture(), 
                            mimeTypeCaptor.capture(), uriCaptor.capture(), 
                            dsLocCaptor.capture(), controlProjectCaptor.capture(), 
                            stateCaptor.capture(), 
                            checkSumAlgCaptor.capture(), 
                            checkSumCaptor.capture(), 
                            logMsgCaptor.capture());
        
        assertEquals(pid, resPid);
        
        String resContent = InputStreamReader.readInputStreamIntoString(
                                                inputStreamCaptor.getValue());
        assertEquals(testDocString, resContent);
        
        assertEquals(pid, pidCaptor.getValue());
        assertEquals(dsId, dsIdCaptor.getValue());
        assertEquals(label, labelCaptor.getValue());
        assertEquals(0, altIdsCaptor.getValue().length);
        assertEquals(false, versionableCaptor.getValue());
        assertEquals(xmlMime, mimeTypeCaptor.getValue());
        assertEquals(null, uriCaptor.getValue());
        assertEquals(dsLoc, dsLocCaptor.getValue());
        assertEquals("X", controlProjectCaptor.getValue());
        assertEquals("A", stateCaptor.getValue());
        assertEquals(null, checkSumAlgCaptor.getValue());
        assertEquals(null, checkSumCaptor.getValue());
        assertEquals("Datastream for " + pid, logMsgCaptor.getValue());
    }
    
    @Test
    public void testStoreExistingDataStream() throws IOException, FedoraException
    {
        String pid = "foo:bar";
        String dsId = "ds1";
        String label = "label1";
        Datastream ds = new Datastream();
        
        ArgumentCaptor<String> pidCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dsIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> altIdsCaptor = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<String> labelCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mimeTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String> checkSumAlgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> checkSumCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> logMsgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> forcedCaptor = ArgumentCaptor.forClass(boolean.class);
        
        // test the case where getdataStream throws an exception, which means
        // the datastream with the given pid does not exist
        when(apim.getDatastream(anyString(), anyString(), 
                                anyString())).thenReturn(ds);
        when(apim.modifyDatastreamByValue(anyString(), anyString(),
                                (String[]) anyObject(),
                                anyString(), 
                                anyString(), anyString(), 
                                (byte[]) anyObject(), 
                                anyString(), anyString(), 
                                anyString(), anyBoolean())).thenReturn(pid);
        
        String resPid = fedoraDsHelper.storeDataStream(pid, dsId, label, xmlMime, testDoc);
        
        verify(apim).getDatastream(pid, dsId, null);
        verify(apim).modifyDatastreamByValue(
                            pidCaptor.capture(), dsIdCaptor.capture(), 
                            altIdsCaptor.capture(), labelCaptor.capture(), 
                            mimeTypeCaptor.capture(), uriCaptor.capture(), 
                            contentCaptor.capture(), 
                            checkSumAlgCaptor.capture(), 
                            checkSumCaptor.capture(), 
                            logMsgCaptor.capture(),
                            forcedCaptor.capture());        
        
        assertEquals(pid, resPid);
        
        assertEquals(pid, pidCaptor.getValue());
        assertEquals(dsId, dsIdCaptor.getValue());
        assertEquals(label, labelCaptor.getValue());
        assertEquals(0, altIdsCaptor.getValue().length);
        assertEquals(xmlMime, mimeTypeCaptor.getValue());
        
        InputStream is = new ByteArrayInputStream(contentCaptor.getValue());
        String resContent = InputStreamReader.readInputStreamIntoString(is);

        assertEquals(testDocString, resContent);
        assertEquals("DEFAULT", checkSumAlgCaptor.getValue());
        assertEquals(null, checkSumCaptor.getValue());
        assertEquals("Datastream for " + pid, logMsgCaptor.getValue());
        assertEquals(true, forcedCaptor.getValue());
    }
    
    @Test (expected = FedoraException.class)
    public void testEditNonExistentDataStream() throws RemoteException, FedoraException
    {
        String pid = "foo:bar";
        String dsId = "ds1";
        String label = "label";
        
        when(apim.getDatastream(anyString(), anyString(), 
                anyString())).thenThrow(new RemoteException());
     
        fedoraDsHelper.editDataStream(pid, dsId, label, xmlMime, testDoc);
        
        verify(apim).getDatastream(pid, dsId, null);
    }
    
    @Test
    public void testEditDataStream() throws RemoteException, FedoraException
    {
        String pid = "foo:bar";
        String dsId = "ds1";
        String label = "label1";
        Datastream ds = new Datastream();
        ds.setID(dsId);
        ds.setLabel(label);
        
        when(apim.getDatastream(anyString(), anyString(), anyString())).thenReturn(ds);
        
        ArgumentCaptor<String> pidCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dsIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> altIdsCaptor = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<String> labelCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mimeTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String> checkSumAlgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> checkSumCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> logMsgCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> forcedCaptor = ArgumentCaptor.forClass(boolean.class);
        
        fedoraDsHelper.editDataStream(pid, dsId, null, xmlMime, testDoc);
        
        verify(apim, times(2)).getDatastream(pid, dsId, null);
        verify(apim).modifyDatastreamByValue(
                            pidCaptor.capture(), dsIdCaptor.capture(), 
                            altIdsCaptor.capture(), labelCaptor.capture(), 
                            mimeTypeCaptor.capture(), uriCaptor.capture(), 
                            contentCaptor.capture(), 
                            checkSumAlgCaptor.capture(), 
                            checkSumCaptor.capture(), 
                            logMsgCaptor.capture(),
                            forcedCaptor.capture());        
        
        assertEquals(pid, pidCaptor.getValue());
        assertEquals(dsId, dsIdCaptor.getValue());
        assertEquals(label, labelCaptor.getValue());
        assertEquals(0, altIdsCaptor.getValue().length);
        assertEquals(xmlMime, mimeTypeCaptor.getValue());
        
        InputStream is = new ByteArrayInputStream(contentCaptor.getValue());
        String resContent = InputStreamReader.readInputStreamIntoString(is);

        assertEquals(testDocString, resContent);
        assertEquals("DEFAULT", checkSumAlgCaptor.getValue());
        assertEquals(null, checkSumCaptor.getValue());
        assertEquals("Datastream for " + pid, logMsgCaptor.getValue());
        assertEquals(true, forcedCaptor.getValue());       
    }
    
    @Test
    public void testRemoveDataStream() throws RemoteException, FedoraException
    {
        String pid = "foo:bar";
        String dsId = "ds1";
        Datastream ds = new Datastream();
        ds.setID(dsId);

        // log should show "Datastream exists, created on 22-01-1999
        ds.setCreateDate("22-01-1999"); 
        
        when(apim.getDatastream(anyString(), anyString(), anyString())).thenReturn(ds);
        when(apim.purgeDatastream(anyString(), anyString(), anyString(), 
                      anyString(), anyString(), anyBoolean())).thenReturn(null);
        
        fedoraDsHelper.removeDataStream(pid, dsId);
        
        verify(apim).getDatastream(pid, dsId, null);
        verify(apim).purgeDatastream(pid, dsId, null, null, 
                                     "Purging Datastream " + dsId, false);      
    }
    
    @Test (expected = FedoraException.class)
    public void testRemoveNonExistentDataStream() throws RemoteException, FedoraException
    {
        String pid = "foo:bar";
        String dsId = "ds1";
        
        when(apim.getDatastream(anyString(), anyString(), 
                anyString())).thenThrow(new RemoteException());
     
        fedoraDsHelper.removeDataStream(pid, dsId);
        
        verify(apim).getDatastream(pid, dsId, null);
    }
    
    @Test
    public void testRetry() throws RemoteException, FedoraException
    {
        RemoteException objectLockedException = new RemoteException(ObjectLockedException.class.getName());
                
        String dsId = "dsId";
        Datastream dataStream = new Datastream();
        dataStream.setID(dsId);

        // log should show "Datastream exists, created on 22-01-1999
        dataStream.setCreateDate("22-01-1999"); 
        
        when(apim.getDatastream(anyString(), anyString(), 
                anyString())).thenReturn(dataStream);
     
        when(apim.modifyDatastreamByValue(
                anyString(), 
                anyString(), 
                (String[]) anyObject(), 
                anyString(), 
                anyString(), 
                anyString(), 
                (byte[]) anyObject(), 
                anyString(), 
                anyString(), 
                anyString(), 
                anyBoolean())).thenThrow(objectLockedException);
        
        try
        {
            fedoraDsHelper.editDataStream("randomId", dsId, "randomLabel", xmlMime, testDoc);    
            fail("Fedora Exception expected");
        } catch (FedoraException e)
        {
            //expected to be thrown eventually
        }
        
        
        
        verify(apim, times(retryLimit)).modifyDatastreamByValue(
                anyString(), 
                anyString(), 
                (String[]) anyObject(), 
                anyString(), 
                anyString(), 
                anyString(), 
                (byte[]) anyObject(), 
                anyString(), 
                anyString(), 
                anyString(), 
                anyBoolean());
        
    }
    

    @Test
    public void testNoRetry() throws RemoteException, FedoraException
    {
        RemoteException lowlevelStorageException = new RemoteException(LowlevelStorageException.class.getName());
                
        String dsId = "dsId";
        Datastream dataStream = new Datastream();
        dataStream.setID(dsId);

        // log should show "Datastream exists, created on 22-01-1999
        dataStream.setCreateDate("22-01-1999"); 
        
        when(apim.getDatastream(anyString(), anyString(), 
                anyString())).thenReturn(dataStream);
     
        when(apim.modifyDatastreamByValue(
                anyString(), 
                anyString(), 
                (String[]) anyObject(), 
                anyString(), 
                anyString(), 
                anyString(), 
                (byte[]) anyObject(), 
                anyString(), 
                anyString(), 
                anyString(), 
                anyBoolean())).thenThrow(lowlevelStorageException);
        
        try
        {
            fedoraDsHelper.editDataStream("randomId", dsId, "randomLabel", xmlMime, testDoc);    
            fail("Fedora Exception expected");
        } catch (FedoraException e)
        {
            //expected
        }                
        
        verify(apim, times(1)).modifyDatastreamByValue(
                anyString(), 
                anyString(), 
                (String[]) anyObject(), 
                anyString(), 
                anyString(), 
                anyString(), 
                (byte[]) anyObject(), 
                anyString(), 
                anyString(), 
                anyString(), 
                anyBoolean());
        
    }
    
    @Test
    public void testDatastreamExistsExisting() throws RemoteException, FedoraException
    {
        String pid = "GDA:123";
        String dsId = "foo";
        
        Datastream ds = mock(Datastream.class);
        
        when(apim.getDatastream(pid, dsId, null)).thenReturn(ds);
        
        assertTrue(fedoraDsHelper.dataStreamExists(pid, dsId));
        
        verify(apim).getDatastream(pid, dsId, null);
    }
    
    @Test
    public void testDatastreamExistsNonExisting() throws RemoteException, FedoraException
    {
        String pid = "GDA:123";
        String dsId = "foo";
        
        assertFalse(fedoraDsHelper.dataStreamExists(pid, dsId));

        
        verify(apim).getDatastream(pid, dsId, null);
    }
}
