/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import au.org.intersect.gda.util.InputStreamReader;

/**
 * @version $Rev$
 *
 */
public class FedoraXmlTemplateBuilderImpl implements FedoraXmlTemplateBuilder, 
    InitializingBean, ResourceLoaderAware
{
    private static final Logger LOG = Logger.getLogger(FedoraXmlTemplateBuilderImpl.class);
    
    private final String pathToObjectTemplate;
        
    private ResourceLoader resourceLoader;
    
    private String foxmlTemplate;
        
    public FedoraXmlTemplateBuilderImpl(String templateName)
    {
        pathToObjectTemplate = 
            "WEB-INF" + File.separator + "fedoraXmlTemplates" + File.separator
            + templateName;
        
        
    }
    
    @Override
    public String buildXmlTemplate(String pid)
    {
        return substituteIntoTemplate(foxmlTemplate, pid);
    }
    

    private String substituteIntoTemplate(String template, String pid)
    {
        String toReplace = "null";
        if (pid != null)
        {
            toReplace = pid;
        }
        return template.replaceAll("%s", toReplace);
    }

    @Override
    public void afterPropertiesSet() throws IOException
    {
        LOG.info(pathToObjectTemplate);
        Resource objectTemplateResource = resourceLoader.getResource(pathToObjectTemplate);
        foxmlTemplate = InputStreamReader.readInputStreamIntoString(objectTemplateResource.getInputStream());        
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }


    
}
