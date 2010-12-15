/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.oai;

import java.util.UUID;

/**
 * @version $Rev$
 *
 */
public class OaiIdGeneratorImpl implements OaiIdGenerator
{
    private final String oaiIdPrefix;
    
    public OaiIdGeneratorImpl(String oaiIdPrefix)
    {
        this.oaiIdPrefix = oaiIdPrefix;
    }
    
    
    @Override
    public String generateId(String objectId)
    {
        return oaiIdPrefix + objectId;
    }
    
    

    /* (non-Javadoc)
     * @see au.org.intersect.gda.oai.OaiIdGenerator#generateRifCsId()
     */
    @Override
    public String generateRifCsId()
    {
        return UUID.randomUUID().toString();

    }

}
