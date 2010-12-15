/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @version $Rev$
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "gda-test-servlet.xml" })
public class FedoraXmlTemplateBuilderImplTest
{
    private  String lineEnd = System.getProperty("line.separator");
    
    @Autowired
    @Qualifier("fedoraXmlTemplateBuilder")
    private FedoraXmlTemplateBuilderImpl fedoraXmlTemplateBuilder;
    
    @Autowired
    @Qualifier("fedoraOaiTemplateBuilder")
    private FedoraXmlTemplateBuilderImpl fedoraOaiTemplateBuilder;
    
    @Autowired
    @Qualifier("fedoraRelsExtTemplateBuilder")
    private FedoraXmlTemplateBuilderImpl fedoraRelsExtTemplateBuilder;
    
    @Test
    public void testBuildXmlTemplate()
    {
        String pid = "foo:bar";

        String substitutedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineEnd 
                                + "<foxml:digitalObject VERSION=\"1.1\" PID=\"" + pid + "\"" + lineEnd 
                                + "xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"" + lineEnd
                                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineEnd
                                + "xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# "
                                + "http://www.fedora.info/definitions/1/0/foxml1-1.xsd\">" + lineEnd
                                + "<foxml:objectProperties>" + lineEnd
                                + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" "
                                + "VALUE=\"Active\"/>" + lineEnd
                                + "</foxml:objectProperties>" + lineEnd
                                + "</foxml:digitalObject>";
       
        String foxml = fedoraXmlTemplateBuilder.buildXmlTemplate(pid);
        assertEquals(substitutedXml, foxml);  
    }
    
    @Test
    public void testNullValues()
    {
        String substitutedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineEnd
                              + "<foxml:digitalObject VERSION=\"1.1\" PID=\"" + null + "\"" + lineEnd 
                              + "xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"" + lineEnd 
                              + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineEnd 
                              + "xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# "
                              + "http://www.fedora.info/definitions/1/0/foxml1-1.xsd\">" + lineEnd 
                              + "<foxml:objectProperties>" + lineEnd 
                              + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" "
                              + "VALUE=\"Active\"/>" + lineEnd 
                              + "</foxml:objectProperties>" + lineEnd 
                              + "</foxml:digitalObject>";
       
        String foxml = fedoraXmlTemplateBuilder.buildXmlTemplate(null);
        assertEquals(substitutedXml, foxml);  
    }
    
    @Test
    public void testBuildOai()
    {
        //assert oai template is parsable
        fedoraOaiTemplateBuilder.buildXmlTemplate("oai:1");        
    }
    
    @Test
    public void testBuildRels()
    {
        //assert oai template is parsable
        fedoraRelsExtTemplateBuilder.buildXmlTemplate("oai:1");        
    }
    
}
