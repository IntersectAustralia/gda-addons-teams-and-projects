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

import org.apache.axis.types.NonNegativeInteger;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import fedora.client.FedoraClient;
import fedora.server.management.FedoraAPIM;
import fedora.server.management.FedoraAPIMServiceLocator;

import au.org.intersect.gda.metadata.GdaObjectMetaDataHelper;

/**
 * @version $Rev$
 *
 */
public abstract class AbstractFedoraRepositorySetter<T> implements FedoraRepositorySetter<T> 
{        
    public abstract String createObjectInRepository(T object) throws FedoraException;
    
    protected abstract NonNegativeInteger getNumberOfPids();
    
    protected abstract FedoraConfig getFedoraConfig();

    protected abstract Logger getLogger();
    
    protected abstract FedoraXmlTemplateBuilder getFedoraXmlTemplateBuilder();
    
    protected abstract FedoraDatastreamHelper getFedoraDsHelper();
    
    protected abstract GdaObjectMetaDataHelper getObjectMetaDataHelper();
    
    protected abstract FedoraComponentFactory getFedoraComponentFactory();
    

    /**
     * creates a new object with name as the label
     * 
     * @return the auto-generated PID assigned to this new object
     */
    protected String createNewObject(FedoraAPIM apim, String resultName, String ownerName, String logMessage)
        throws FedoraException
    {
        String pid = "";
        try
        {
            NonNegativeInteger pidNumber = getNumberOfPids();
            
            pid = apim.getNextPID(pidNumber, getFedoraConfig().getNamespace())[0];
            byte[] bytes = createObjectXML(pid, resultName, ownerName);
            apim.ingest(bytes, FedoraAPIMServiceLocator.FOXML1_1.toString(), logMessage);
        } catch (RemoteException e)
        {
            getLogger().info("error occurred when attempting to create a new fedora obj");
            throw new FedoraException("Error occured when attempting to create a new Fedora object.", e);
        }
        getLogger().info("successfuly created object: " + pid);
        return pid;
    }

    private byte[] createObjectXML(String pid, String resultName, String ownerName)
    {
        String formattedString = getFedoraXmlTemplateBuilder().buildXmlTemplate(pid);
        return formattedString.getBytes();
    }

    @Override
    public String storeDataStream(String objectId, String dsId, String label, String mimeType, Document content) 
        throws FedoraException
    {
        return getFedoraDsHelper().storeDataStream(objectId, dsId, label, mimeType, content);
    }    

    @Override
    public void editDataStream(String objectId, String dsId, String mimeType, Document content) throws FedoraException
    {
        getFedoraDsHelper().editDataStream(objectId, dsId, null, mimeType, content);
    }
    
    @Override
    public void removeDataStream(String objectId, String dsId) throws FedoraException
    {
        getFedoraDsHelper().removeDataStream(objectId, dsId);       
    }
    
    /**
     * Add a property into the Dublin Core datastream
     */
    @Override
    public void putProperty(String resultId, FedoraResultProperties prop, String value) 
        throws FedoraException
    {  
        Document dcDoc = getDcStreamAsDoc(resultId);
        getObjectMetaDataHelper().setProperty(dcDoc, prop, value);
        writeBackDublinCoreDs(resultId, dcDoc);       
    }

    @Override
    public void removeProperty(String resultId, FedoraResultProperties prop) throws FedoraException
    {
        Document dcDoc = getDcStreamAsDoc(resultId);
        getObjectMetaDataHelper().removeProperty(dcDoc, prop);
        writeBackDublinCoreDs(resultId, dcDoc);          
    }

    
    @Override
    public void deleteObject(String objectId) throws FedoraException
    {
        try
        {
            FedoraClient client = getFedoraComponentFactory().buildFedoraClient();
            FedoraAPIM apim = client.getAPIM();
            apim.purgeObject(objectId, "Deleting", false);
        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (RemoteException e)
        {
            throw new FedoraException("Unable to retrieve the object profile", e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM, e);
        }
    }
    
    @Override
    public void changeObjectState(String objectId, String state) throws FedoraException
    {
        try
        {
            FedoraClient client = getFedoraComponentFactory().buildFedoraClient();
            FedoraAPIM apim = client.getAPIM();
            apim.modifyObject(
                    objectId, state, null, null, "Updating object state " + state);
            
        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (RemoteException e)
        {
            throw new FedoraException("Unable to retrieve the object profile", e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM, e);
        }
    }

    protected Document getDcStreamAsDoc(String resultId) 
        throws FedoraException
    { 
  
        Document dcDoc = getFedoraDsHelper().getDataStream(resultId, 
                             FedoraResultRepositoryManagerImpl.DC_IDENTIFIER);
        return dcDoc;     
      
    }
    
    protected void writeBackDublinCoreDs(String resultId, Document dcDoc) 
        throws FedoraException
    {

        getFedoraDsHelper().storeDataStream(resultId, 
                    FedoraResultRepositoryManagerImpl.DC_IDENTIFIER, null, "text/xml", dcDoc);
 
    }
}
