/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.oai;

/**
 * @version $Rev$
 *
 */
public interface OaiIdGenerator
{
    /**
     * Used to generate a fedora id for the oai object
     * This will return the same id given the same input
     * 
     * @param id
     * @return  
     * 
     */
    String generateId(String id);
    
    /**
     * Used to generate a uuid for use as the RIF-CS identifier
     * 
     * @return
     */
    String generateRifCsId();
}
