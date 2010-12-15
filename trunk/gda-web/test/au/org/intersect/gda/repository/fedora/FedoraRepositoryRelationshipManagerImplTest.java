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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fedora.client.FedoraClient;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.RelationshipTuple;

/**
 * @version $Rev$
 * 
 */
public class FedoraRepositoryRelationshipManagerImplTest
{
    private FedoraRepositoryRelationshipManagerImpl fedoraRepositoryRelationshipManager;
    @Mock
    private FedoraComponentFactory fedoraComponentFactory;
    @Mock
    private FedoraClient fc;
    @Mock
    private FedoraAPIM apim;

    @Before
    public void setUp() throws MalformedURLException, ServiceException, IOException
    {
        MockitoAnnotations.initMocks(this);
        fedoraRepositoryRelationshipManager = new FedoraRepositoryRelationshipManagerImpl(fedoraComponentFactory);
        when(fedoraComponentFactory.buildFedoraClient()).thenReturn(fc);
        when(fc.getAPIM()).thenReturn(apim);
    }

    @Test
    public void testAddRelationship() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        boolean expectedReturnVal = true;

        when(apim.addRelationship(subject, relationship, object, isLiteral, null)).thenReturn(expectedReturnVal);

        boolean returnVal = fedoraRepositoryRelationshipManager.addRelationship(subject, relationship, object,
                isLiteral, null);

        assertEquals("Return", expectedReturnVal, returnVal);
    }

    @Test
    public void testAddRelationshipMUE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        try
        {
            when(fedoraComponentFactory.buildFedoraClient()).thenThrow(
                    new MalformedURLException("Mock MalformedURLException"));

            fedoraRepositoryRelationshipManager.addRelationship(subject, relationship, object, isLiteral, null);
        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(), FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testAddRelationshipSE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        try
        {
            when(fc.getAPIM()).thenThrow(new ServiceException("Mock ServiceException"));

            fedoraRepositoryRelationshipManager.addRelationship(subject, relationship, object, isLiteral, null);
        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(),
                    FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testAddRelationshipIOE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        try
        {
            when(fc.getAPIM()).thenThrow(new IOException("Mock IOException"));

            fedoraRepositoryRelationshipManager.addRelationship(subject, relationship, object, isLiteral, null);
        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(),
                    FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testAddRelationshipRE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        try
        {
            when(apim.addRelationship(subject, relationship, object, isLiteral, null)).thenThrow(
                    new RemoteException("Mock RemoteException"));

            fedoraRepositoryRelationshipManager.addRelationship(subject, relationship, object, isLiteral, null);
        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(), FedoraErrorMessages.ERROR_DURING_REMOTE_METHOD_CALL);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetRelationships() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        RelationshipTuple[] expectedRelationshipArray = new RelationshipTuple[1];
        expectedRelationshipArray[0] = new RelationshipTuple(subject, relationship, object, isLiteral, null);

        when(apim.getRelationships(subject, relationship)).thenReturn(expectedRelationshipArray);

        RelationshipTuple[] relationshipArray = fedoraRepositoryRelationshipManager.getRelationships(subject,
                relationship);

        assertEquals("Return", expectedRelationshipArray.length, relationshipArray.length);
    }

    @Test
    public void testGetRelationshipsMUE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";

        try
        {
            when(fedoraComponentFactory.buildFedoraClient()).thenThrow(
                    new MalformedURLException("Mock MalformedURLException"));

            fedoraRepositoryRelationshipManager.getRelationships(subject, relationship);

        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(), FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetRelationshipsSE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";

        try
        {
            when(fc.getAPIM()).thenThrow(new ServiceException("Mock ServiceException"));

            fedoraRepositoryRelationshipManager.getRelationships(subject, relationship);

        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(),
                    FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetRelationshipsIOE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";

        try
        {
            when(fc.getAPIM()).thenThrow(new IOException("Mock IOException"));

            fedoraRepositoryRelationshipManager.getRelationships(subject, relationship);

        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(),
                    FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetRelationshipsRE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";

        try
        {
            when(apim.getRelationships(subject, relationship)).thenThrow(new RemoteException("Mock RemoteException"));

            fedoraRepositoryRelationshipManager.getRelationships(subject, relationship);

        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(), FedoraErrorMessages.ERROR_DURING_REMOTE_METHOD_CALL);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testPurgeRelationship() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        boolean expectedReturnVal = true;

        when(apim.purgeRelationship(subject, relationship, object, isLiteral, null)).thenReturn(expectedReturnVal);

        boolean returnVal = fedoraRepositoryRelationshipManager.purgeRelationship(subject, relationship, object,
                isLiteral, null);

        assertEquals("Return", expectedReturnVal, returnVal);
    }

    @Test
    public void testPurgeRelationshipMUE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        try
        {
            when(fedoraComponentFactory.buildFedoraClient()).thenThrow(
                    new MalformedURLException("Mock MalformedURLException"));

            fedoraRepositoryRelationshipManager.purgeRelationship(subject, relationship, object, isLiteral, null);
        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(), FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testPurgeRelationshipSE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        try
        {
            when(fc.getAPIM()).thenThrow(new ServiceException("Mock ServiceException"));

            fedoraRepositoryRelationshipManager.purgeRelationship(subject, relationship, object, isLiteral, null);
        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(),
                    FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testPurgeRelationshipIOE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        try
        {
            when(fc.getAPIM()).thenThrow(new IOException("Mock IOException"));

            fedoraRepositoryRelationshipManager.purgeRelationship(subject, relationship, object, isLiteral, null);
        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(),
                    FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

    @Test
    public void testPurgeRelationshipRE() throws FedoraException, RemoteException
    {
        String subject = "gda:1";
        String relationship = "urn:isParentOf";
        String object = "gda:2";
        boolean isLiteral = false;

        try
        {
            when(apim.purgeRelationship(subject, relationship, object, isLiteral, null)).thenThrow(
                    new RemoteException("Mock RemoteException"));

            fedoraRepositoryRelationshipManager.purgeRelationship(subject, relationship, object, isLiteral, null);
        } catch (FedoraException fe)
        {
            assertEquals("Exception message", fe.getMessage(), FedoraErrorMessages.ERROR_DURING_REMOTE_METHOD_CALL);
        } catch (Exception e)
        {
            fail("Wrong exception was thrown: " + e.getMessage());
        }
    }

}
