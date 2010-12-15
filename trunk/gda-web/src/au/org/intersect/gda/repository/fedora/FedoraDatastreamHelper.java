/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.util.Map;

import org.w3c.dom.Document;

/**
 * @version $Rev$
 *
 */
public interface FedoraDatastreamHelper
{
    Map<String, String> getAllDataStreams(String objectId) throws FedoraException;
    
    Document getDataStream(String objectId, String dsId) 
        throws FedoraException;
   
    String storeDataStream(String objectId, String dsId, String label, String mimeType, Document content) 
        throws FedoraException;
    
    /**
     * @param objectId
     * @param dsId
     * @param label if null, uses the existing label
     * @param content if null, uses the existing content
     * @throws FedoraException
     */
    void editDataStream(String objectId, String dsId, String label, String mimeType, Document content) 
        throws FedoraException;
    
    void removeDataStream(String objectId, String dsId) throws FedoraException;

    /**
     * @param apim
     * @param pid
     * @param dsId
     * @return
     * @throws FedoraException 
     */
    boolean dataStreamExists(String pid, String dsId) throws FedoraException;
}
