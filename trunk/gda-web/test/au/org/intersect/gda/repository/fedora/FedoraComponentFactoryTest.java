/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fedora.client.FedoraClient;
import fedora.client.Uploader;

/**
 * @version $Rev$
 * 
 */
public class FedoraComponentFactoryTest
{
    private FedoraComponentFactory componentFactory;
    
    @Mock
    private FedoraConfig fedoraConfig;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        componentFactory = new FedoraComponentFactoryImpl(fedoraConfig);
    }

    @Test
    public void testCreateClientSuccessful() throws MalformedURLException
    {
        when(fedoraConfig.getUsername()).thenReturn("username");
        when(fedoraConfig.getPassword()).thenReturn("pwd");
        when(fedoraConfig.getURL()).thenReturn("http://www.blah.com");
        FedoraClient fc = componentFactory.buildFedoraClient();
        assertNotNull(fc);
    }

    @Test
    public void testBuildFedoraUploaderSuccessful() throws IOException
    {
        when(fedoraConfig.getUsername()).thenReturn("username");
        when(fedoraConfig.getPassword()).thenReturn("pwd");
        when(fedoraConfig.getProtocol()).thenReturn("http");
        when(fedoraConfig.getHost()).thenReturn("host");
        when(fedoraConfig.getPort()).thenReturn(80);
        when(fedoraConfig.getContext()).thenReturn("fed");
        Uploader uploader = componentFactory.buildFedoraUploader();
        assertNotNull(uploader);
    }

}
