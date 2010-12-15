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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NonNegativeInteger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.management.FedoraAPIMServiceLocator;
import fedora.server.types.gen.ObjectProfile;

import au.org.intersect.gda.dto.ProjectOaiDTO;
import au.org.intersect.gda.metadata.GdaObjectMetaDataHelper;

/**
 * @version $Rev$
 *
 */
public class FedoraProjectRepositorySetterImplTest
{
    private FedoraProjectRepositorySetterImpl fedoraRepositorySetter;
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
    
    @Before
    public void setUp() throws MalformedURLException, ServiceException, IOException, ParserConfigurationException
    {
        MockitoAnnotations.initMocks(this);
        fedoraRepositorySetter = new FedoraProjectRepositorySetterImpl(fedoraXmlTemplateBuilder, fedoraComponentFactory,
                fedoraDsHelper, objMetaDataHelper, fedoraConfig);
        when(fedoraComponentFactory.buildFedoraClient()).thenReturn(fc);
        when(fc.getAPIA()).thenReturn(apia);
        when(fc.getAPIM()).thenReturn(apim);
        
    }
    

    @Test
    public void testCreateProjectInRepository() throws FedoraException, RemoteException
    {
        ProjectOaiDTO project = new ProjectOaiDTO();
        
        
        ObjectProfile objectProfile = mock(ObjectProfile.class);
        
        project.setName("pname");
        project.setDescription("pdescription");

        String nextPID = "oai:new";
        String someXmlTemplate = "<xml>some xml</xml>";
        String logMessage = "Inserting: " + project.getName();

        when(objectProfile.getObjLabel()).thenReturn(nextPID);
        when(objectProfile.getObjCreateDate()).thenReturn("2000-03-02T07:38:26.868Z");
        when(objectProfile.getObjLastModDate()).thenReturn("2010-01-07T05:48:46.828Z");

        ArgumentCaptor<NonNegativeInteger> numberCaptor = ArgumentCaptor.forClass(NonNegativeInteger.class);
        
        when(apim.getNextPID(numberCaptor.capture(), anyString())).thenReturn(new String[] {nextPID});
        
        when(apia.getObjectProfile("oai:new", null)).thenReturn(objectProfile);
        
        when(fedoraXmlTemplateBuilder.buildXmlTemplate(anyString())).thenReturn(someXmlTemplate);

        String nextId = fedoraRepositorySetter.createObjectInRepository(project);
        
        verify(apim).getNextPID((NonNegativeInteger) anyObject(), anyString());
        verify(apim).ingest(someXmlTemplate.getBytes(), FedoraAPIMServiceLocator.FOXML1_1.toString(), logMessage);
        
        assertEquals(nextPID, nextId);
        assertEquals(new NonNegativeInteger("1"), numberCaptor.getValue());
    }
}
