/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

import fedora.client.FedoraClient;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.RelationshipTuple;

/**
 * @version $Rev$
 *
 */
public class FedoraRepositoryRelationshipManagerImpl implements FedoraRepositoryRelationshipManager
{

    private static final Logger LOG = Logger.getLogger(FedoraRepositoryRelationshipManagerImpl.class);
    
    private FedoraComponentFactory fedoraComponentFactory;
    
    
    public FedoraRepositoryRelationshipManagerImpl(FedoraComponentFactory fedoraComponentFactory)
    {
        this.fedoraComponentFactory = fedoraComponentFactory;
    }
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.
     * FedoraRepositoryRelationshipManager#addRelationship(java.lang.String, 
     * java.lang.String, java.lang.String, boolean, java.lang.String)
     */
    @Override
    public boolean addRelationship(String subject, 
                                   String relationship, 
                                   String object, 
                                   boolean isLiteral,
                                   String dataType) throws FedoraException
    {
        LOG.info("Adding relationship");
       
        boolean returnVal = false;
        try
        {
            returnVal = getFedoraAPIM().addRelationship(subject, relationship, object, isLiteral, dataType);
        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (RemoteException e)
        {
            throw new FedoraException(FedoraErrorMessages.ERROR_DURING_REMOTE_METHOD_CALL, e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM, e);
        } 
        
        
        return returnVal;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.
     * FedoraRepositoryRelationshipManager#getRelationships(java.lang.String, java.lang.String)
     */
    @Override
    public RelationshipTuple[] getRelationships(String subject, String relationship) throws FedoraException
    {
        LOG.info("Getting relationships of type " + relationship);
        
        RelationshipTuple[] relationshipArray = null;
        try
        {
            relationshipArray = getFedoraAPIM().getRelationships(subject, relationship);
        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (RemoteException e)
        {
            throw new FedoraException(FedoraErrorMessages.ERROR_DURING_REMOTE_METHOD_CALL, e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM, e);
        }
        
        return relationshipArray;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.
     * FedoraRepositoryRelationshipManager#purgeRelationship
     * (java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String)
     */
    @Override
    public boolean purgeRelationship(String subject, 
                                     String relationship, 
                                     String object,
                                     boolean isLiteral,
                                     String dataType) throws FedoraException
    {
        LOG.info("Purging relationship");
        
        boolean returnVal = false;
        try
        {
            returnVal = getFedoraAPIM().purgeRelationship(subject, relationship, object, isLiteral, dataType);
        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (RemoteException e)
        {
            throw new FedoraException(FedoraErrorMessages.ERROR_DURING_REMOTE_METHOD_CALL, e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM, e);
        }
        
        return returnVal;
    }
    
    private FedoraAPIM getFedoraAPIM() throws MalformedURLException, IOException, ServiceException
    {
        FedoraClient fc = fedoraComponentFactory.buildFedoraClient();
        return fc.getAPIM();
    }
}
