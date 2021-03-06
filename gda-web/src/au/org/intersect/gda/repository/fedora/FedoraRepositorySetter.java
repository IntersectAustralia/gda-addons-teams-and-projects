/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import org.w3c.dom.Document;

/**
 * @version $Rev$
 *
 */
public interface FedoraRepositorySetter<T> 
{

    /**
     * @param object
     * @throws FedoraException 
     */
    String createObjectInRepository(T object) throws FedoraException;
    
    /**
     * @param objectId
     * @param dsId - if null, auto-generated by fedora
     * @param content
     * @return the datastream id
     * @throws FedoraException 
     */
    String storeDataStream(
            String objectId, 
            String dsId, 
            String label, 
            String mimeType, 
            Document content) throws FedoraException;

    
    /**
     * Replaces the current existing result datastream content with another.
     * 
     * @param objectId
     * @param dsId
     * @param content
     * @return
     * @throws FedoraException 
     */
    void editDataStream(String objectId, String dsId, String mimeType, Document content) throws FedoraException;

    /**
     * Removes a datastream of a result.
     * 
     * @param ojbectId
     * @param dsId
     * @throws FedoraException
     */
    void removeDataStream(String ojbectId, String dsId) throws FedoraException;
    
    /**
     * @param resultId
     * @param prop
     * @param name
     * @throws FedoraException 
     */
    void putProperty(String ojbectId, FedoraResultProperties prop, String name) throws FedoraException;

    /**
     * @param resultId
     * @param name
     */
    void removeProperty(String ojbectId, FedoraResultProperties prop) throws FedoraException;
    
    /**
     * @param resultId
     * @throws FedoraException 
     */
    void deleteObject(String objectId) throws FedoraException;
    
    /**
     * Changes the specified object's state in fedora. 
     * 
     * 
     * @param objectId
     * @param state
     *      Can be A,I,D for Active, Inactive, Delete 
     * @throws FedoraException
     */
    void changeObjectState(String objectId, String state) throws FedoraException;
}
