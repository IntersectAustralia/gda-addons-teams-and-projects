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
import java.text.ParseException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.ObjectProfile;

import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.metadata.GdaObjectMetaDataHelper;
import au.org.intersect.gda.util.DateParser;


/**
 * @version $Rev$
 * 
 */
public class FedoraResultRepositorySetterImpl 
    extends AbstractFedoraRepositorySetter<ResultDTO>
{
    private static final Logger LOG = Logger.getLogger(FedoraResultRepositorySetterImpl.class);

    private static final NonNegativeInteger NUMBER_OF_PIDS = new NonNegativeInteger("1");

    private FedoraXmlTemplateBuilder fedoraXmlTemplateBuilder;
    private FedoraComponentFactory fedoraComponentFactory;
    private FedoraDatastreamHelper fedoraDsHelper;
    private FedoraConfig fedoraConfig;
    private GdaObjectMetaDataHelper objMetaDataHelper;
    
    public FedoraResultRepositorySetterImpl(FedoraXmlTemplateBuilder fedoraXmlTemplateBuilder,
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
    public String createObjectInRepository(ResultDTO result) throws FedoraException
    {    
        try
        {
            LOG.info("Creating result");
            FedoraClient fc = fedoraComponentFactory.buildFedoraClient();
            FedoraAPIA apia = fc.getAPIA();
            FedoraAPIM apim = fc.getAPIM();
            String logMessage = "Inserting: " + result.getName() + " of type: " + result.getType();

            String pid = createNewObject(apim, result.getName(), result.getOwner(), logMessage);
            LOG.info("Created: " + pid);
            saveResultToDatastream(result, pid);
            
            // for the moment, we do not care about versioning, so the
            // version date argument in getObjectProfile can be null, which
            // always returns the most recent version
            ObjectProfile objectProfile = apia.getObjectProfile(pid, null);
            LOG.info("Created date: " + objectProfile.getObjCreateDate());
            LOG.info("Modified date: " + objectProfile.getObjLastModDate());

            result.setId(pid);
            result.setCreatedDate(DateParser.parse(objectProfile.getObjCreateDate()));
            result.setLastModifiedDate(DateParser.parse(objectProfile.getObjLastModDate()));

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
        } catch (ParseException e)
        {
            throw new FedoraException("Unable to parse date", e);
        }
    }

    private void saveResultToDatastream(ResultDTO result, String pid) throws FedoraException
    {
        Document dcDoc = getDcStreamAsDoc(pid);       
        objMetaDataHelper.setProperty(dcDoc, FedoraResultProperties.TYPE, result.getType());
        objMetaDataHelper.setProperty(dcDoc, FedoraResultProperties.NAME, result.getName());
        objMetaDataHelper.setProperty(dcDoc, FedoraResultProperties.OWNER, result.getOwner());
        objMetaDataHelper.setProperty(dcDoc, FedoraResultProperties.CREATOR, result.getCreator());
        writeBackDublinCoreDs(pid, dcDoc);
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
