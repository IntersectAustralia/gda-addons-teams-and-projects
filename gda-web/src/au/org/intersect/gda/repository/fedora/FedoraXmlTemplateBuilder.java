/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

/**
 * Ingesting brand new objects in Fedora requires an xml template for the data.
 * The object of this class builds such a template, taking in the object id,
 * data result name and the owner of that result.
 * 
 * @version $Rev$
 *
 */
public interface FedoraXmlTemplateBuilder
{
    public String buildXmlTemplate(String pid);    
}
