/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.oai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Rev$
 *
 */
public class OaiIdGeneratorImplTest
{
    private OaiIdGeneratorImpl oaiIdGenerator;
    
    private String idPrefix = "oai:gda:";
    
    @Before
    public void setUp()
    {
        oaiIdGenerator = new OaiIdGeneratorImpl(idPrefix);
    }
    
    
    @Test
    public void testCreateId()
    {
        String rawId1 = "obj1";
        String id1 = oaiIdGenerator.generateId(rawId1);
        
        assertEquals(idPrefix + rawId1, id1);
        
        String rawId2 = "obj2";
        String id2 = oaiIdGenerator.generateId(rawId2);
        
        assertEquals(idPrefix + rawId2, id2);
    }
    

    @Test
    public void testCreateRifCsId()
    {
        String id1 = oaiIdGenerator.generateRifCsId();
        
        assertNotNull(id1);
    }
}
