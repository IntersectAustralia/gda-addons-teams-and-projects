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

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.ObjectProfile;

import au.org.intersect.gda.dto.ProjectOaiDTO;
import au.org.intersect.gda.metadata.GdaObjectMetaDataHelper;

/**
 * @version $Rev$
 *
 */
public class FedoraProjectRepositorySetterImpl
    extends AbstractFedoraRepositorySetter<ProjectOaiDTO>
{
    private static final Logger LOG = Logger.getLogger(FedoraProjectRepositorySetterImpl.class);

    private static final NonNegativeInteger NUMBER_OF_PIDS = new NonNegativeInteger("1");

    private FedoraXmlTemplateBuilder fedoraXmlTemplateBuilder;
    private FedoraComponentFactory fedoraComponentFactory;
    private FedoraDatastreamHelper fedoraDsHelper;
    private FedoraConfig fedoraConfig;
    private GdaObjectMetaDataHelper objMetaDataHelper;
    
    public FedoraProjectRepositorySetterImpl(FedoraXmlTemplateBuilder fedoraXmlTemplateBuilder,
            FedoraComponentFactory fedoraComponentFactory, FedoraDatastreamHelper fedoraDsHelper,
            GdaObjectMetaDataHelper objMetaDataHelper, FedoraConfig fedoraConfig)
    {
        super();
        this.fedoraXmlTemplateBuilder = fedoraXmlTemplateBuilder;
        this.fedoraComponentFactory = fedoraComponentFactory;
        this.fedoraDsHelper = fedoraDsHelper;
        this.objMetaDataHelper = objMetaDataHelper;
        this.fedoraConfig = fedoraConfig;
    }
    
    @Override
    public String createObjectInRepository(ProjectOaiDTO project) throws FedoraException
    {    
        try
        {

            FedoraClient fc = fedoraComponentFactory.buildFedoraClient();
            FedoraAPIA apia = fc.getAPIA();
            FedoraAPIM apim = fc.getAPIM();
            String logMessage = "Inserting: " + project.getName();

            String pid = createNewObject(apim, project.getName(), project.getOwner(), logMessage);
                        
            if (LOG.isInfoEnabled())
            {
                ObjectProfile objectProfile = apia.getObjectProfile(pid, null);
                
                StringBuffer buffer = new StringBuffer();
                
                buffer.append("Created project: ");
                buffer.append(pid);
                buffer.append("Created date: ");
                buffer.append(objectProfile.getObjCreateDate());
                buffer.append("Modified date: ");
                buffer.append(objectProfile.getObjLastModDate());
                LOG.info(buffer.toString());
            }

            
            return pid;
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
    
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.AbstractFedoraRepositorySetter#getFedoraComponentFactory()
     */
    @Override
    protected FedoraComponentFactory getFedoraComponentFactory()
    {
        return this.fedoraComponentFactory;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.AbstractFedoraRepositorySetter#getFedoraConfig()
     */
    @Override
    protected FedoraConfig getFedoraConfig()
    {
        return this.fedoraConfig;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.AbstractFedoraRepositorySetter#getFedoraDsHelper()
     */
    @Override
    protected FedoraDatastreamHelper getFedoraDsHelper()
    {
        return this.fedoraDsHelper;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.AbstractFedoraRepositorySetter#getFedoraXmlTemplateBuilder()
     */
    @Override
    protected FedoraXmlTemplateBuilder getFedoraXmlTemplateBuilder()
    {
        return this.fedoraXmlTemplateBuilder;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.AbstractFedoraRepositorySetter#getLogger()
     */
    @Override
    protected Logger getLogger()
    {
        return LOG;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.AbstractFedoraRepositorySetter#getNumberOfPids()
     */
    @Override
    protected NonNegativeInteger getNumberOfPids()
    {
        return NUMBER_OF_PIDS;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.AbstractFedoraRepositorySetter#getObjectMetaDataHelper()
     */
    @Override
    protected GdaObjectMetaDataHelper getObjectMetaDataHelper()
    {
        return this.objMetaDataHelper;
    }


}
