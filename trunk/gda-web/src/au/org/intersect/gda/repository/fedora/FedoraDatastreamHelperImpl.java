/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fedora.client.FedoraClient;
import fedora.client.Uploader;
import fedora.server.access.FedoraAPIA;
import fedora.server.errors.ObjectLockedException;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import fedora.server.types.gen.MIMETypedStream;

import au.org.intersect.gda.xml.XMLUtil;

/**
 * @version $Rev$
 * 
 */
public class FedoraDatastreamHelperImpl implements FedoraDatastreamHelper
{

    private static final Logger LOG = Logger.getLogger(FedoraDatastreamHelperImpl.class);

    private final int retryLimit;

    private final FedoraComponentFactory fedoraComponentFactory;
    
    private DocumentBuilderFactory docBuilderFactory;

    public FedoraDatastreamHelperImpl(FedoraComponentFactory fedoraComponentFactory, int retryLimit)
    {
        this.fedoraComponentFactory = fedoraComponentFactory;
        this.retryLimit = retryLimit;
        
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
    }

    @Override
    public boolean dataStreamExists(String pid, String dsId) throws FedoraException
    {
        try
        {
            FedoraClient fc = fedoraComponentFactory.buildFedoraClient();
            FedoraAPIM apim = fc.getAPIM();
            LOG.info("Check if data stream with id " + dsId + " already exists");
            Datastream ds = null;
            try
            {
                ds = apim.getDatastream(pid, dsId, null);
            } catch (RemoteException e)
            {
                LOG.info("Datastream not found");
            }
            if (ds == null)
            {
                return false;
            }
            LOG.info("Datastream exists, created on " + ds.getCreateDate());
            return true;
        } catch (RemoteException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        }
    }

    private String addDataStream(
            FedoraAPIM apim, 
            String pid, 
            String dsId, 
            String label, 
            String mimeType,
            Document content)
        throws IOException, FedoraException
    {
        LOG.info("Prepare to upload: " + dsId + "-" + content);
        Uploader uploader = fedoraComponentFactory.buildFedoraUploader();
        LOG.info("Finish constructing uploader");
        
        String contentString;
        try
        {
            contentString = XMLUtil.documentToString(content);
        } catch (TransformerException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_WRITE_OUTPUT_DATASTREAM_CONTENT, e);            
        }
        
        String dsLoc = uploader.upload(new ByteArrayInputStream(contentString.getBytes()));
        LOG.info("Prepare to add datastream");
        return apim.addDatastream(pid, dsId, // datastream id, can be null
                new String[] {}, // alternative ids
                label, // datastream label
                false, // versionable
                mimeType, // MIME type, assume xml
                null, // format URI of datastream, can be null
                dsLoc, // data location, if not xml
                "X", // control project, X for inline xml
                "A", // state, A for active
                null, // checksum algorithm
                null, // checksum, can be null
                "Datastream for " + pid); // log message
    }

    private String modifyDataStream(FedoraAPIM apim, 
            String pid, 
            String dsId, 
            String label, 
            String mimeType,
            Document content, 
            int retries)
        throws RemoteException, FedoraException
    {
        LOG.info("Prepare to change: " + dsId + "-" + content);
        try
        {
            String contentString = XMLUtil.documentToString(content);
            return apim.modifyDatastreamByValue(pid, dsId, // datastream id, can be null
                    new String[] {}, // alternative ids
                    label, // datastream label
                    mimeType, // MIME type, assume xml
                    null, // format URI of datastream
                    contentString.getBytes(), "DEFAULT", // checksum algorithm
                    null, // checksum, can be null
                    "Datastream for " + pid, // log message
                    true);
        } catch (RemoteException e)
        {
            if (e.getMessage().contains(ObjectLockedException.class.getName()))
            {
                int currentTry = retries + 1;
                if (currentTry >= retryLimit)
                {
                    // retry limit reached, stop

                    throw e;
                }
                // object locked, retry
                LOG.info("ObjectLockedException, retrying attempt " + currentTry);
                return this.modifyDataStream(apim, pid, dsId, label, mimeType, content, currentTry);
            } else
            {
                throw e;
            }
        } catch (TransformerException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_WRITE_OUTPUT_DATASTREAM_CONTENT, e);

        }
    }

    private String modifyDataStream(
            FedoraAPIM apim, 
            String pid, 
            String dsId, 
            String label, 
            String mimeType, 
            Document content)
        throws RemoteException, FedoraException
    {
        return this.modifyDataStream(apim, pid, dsId, label, mimeType, content, 0);
    }

    @Override
    public Map<String, String> getAllDataStreams(String objectId) throws FedoraException
    {
        Map<String, String> dsMap = new HashMap<String, String>();
        try
        {
            FedoraClient fc = fedoraComponentFactory.buildFedoraClient();

            FedoraAPIM apim = fc.getAPIM();
            Datastream[] streams = apim.getDatastreams(objectId, null, "A");
            for (Datastream ds : streams)
            {
                String dsId = ds.getID();
                if (FedoraResultRepositoryManagerImpl.DC_IDENTIFIER.equals(dsId))
                {
                    continue;
                }
                // otherwise it is a metadata id
                dsMap.put(dsId, ds.getLabel());
            }
            return dsMap;
        } catch (RemoteException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        }
    }

    @Override
    public Document getDataStream(String objectId, String dsId) throws FedoraException
    {
        try
        {
            FedoraClient fc = fedoraComponentFactory.buildFedoraClient();

            FedoraAPIA apia = fc.getAPIA();
            MIMETypedStream stream = apia.getDatastreamDissemination(objectId, dsId, null);
            // null for date because we don't have versioning yet

            byte[] bytes = stream.getStream();
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            
            Document content = docBuilder.parse(is);
            
            LOG.info("Retrieved datastream for " + objectId + ", datastream id: " + dsId);
            LOG.debug("Content:\n" + content);
            return content;
        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (RemoteException e)
        {  
            throw new FedoraException("Unable to retrieve or save data for " + objectId 
                                      + ". Are you sure it has not been deleted?", e);
        } catch (IOException e)
        {  
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (SAXException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_PARSE_FEDORA_DATASTREAM, e);

        } catch (ParserConfigurationException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_PARSE_FEDORA_DATASTREAM, e);
        }
    }

    @Override
    public String storeDataStream(
            String objectId, 
            String dsId, 
            String label, 
            String mimeType,
            Document content) throws FedoraException
    {
        try
        {
            FedoraClient fc = fedoraComponentFactory.buildFedoraClient();

            FedoraAPIM apim = fc.getAPIM();

            if (dataStreamExists(objectId, dsId))
            {
                return modifyDataStream(apim, objectId, dsId, label, mimeType, content);
            }
            LOG.info("Datastream does not exists, add new");
            return addDataStream(apim, objectId, dsId, label, mimeType, content);

        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (IOException e)
        {
            LOG.info("IO Exception is " + e);
            throw new FedoraException(FedoraErrorMessages.ERROR_WHEN_MANIPULATING_DATASTREAM, e);
        }
    }

    @Override
    public void editDataStream(
            String objectId, 
            String dsId, 
            String label, 
            String mimeType,
            Document content) throws FedoraException
    {
        try
        {
            FedoraClient fc = fedoraComponentFactory.buildFedoraClient();
            FedoraAPIM apim = fc.getAPIM();
            if (!dataStreamExists(objectId, dsId))
            {
                throw new FedoraException("Datastream with id " + dsId + " does not exist");
            }
            Datastream ds = apim.getDatastream(objectId, dsId, null);

            String newLabel = label == null ? ds.getLabel() : label;
            Document newContent = content == null ? getDataStream(objectId, dsId) : content;

            modifyDataStream(apim, objectId, dsId, newLabel, mimeType, newContent);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.ERROR_WHEN_MANIPULATING_DATASTREAM, e);
        }
    }

    @Override
    public void removeDataStream(String objectId, String dsId) throws FedoraException
    {
        try
        {
            FedoraClient fc = fedoraComponentFactory.buildFedoraClient();
            FedoraAPIM apim = fc.getAPIM();
            if (!dataStreamExists(objectId, dsId))
            {
                throw new FedoraException("Datastream with id " + dsId + " does not exist");
            }
            apim.purgeDatastream(objectId, dsId, null, null, "Purging Datastream " + dsId, false);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.ERROR_WHEN_MANIPULATING_DATASTREAM, e);
        }
    }

}
