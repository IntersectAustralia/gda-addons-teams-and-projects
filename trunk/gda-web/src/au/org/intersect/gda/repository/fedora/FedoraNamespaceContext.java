/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

/**
 * @version $Rev$
 *
 */
public class FedoraNamespaceContext implements NamespaceContext
{
    public static final String FEDORA_NAMESPACE_PREFIX = "fns";
    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    @Override
    public String getNamespaceURI(String prefix)
    {
        if (FEDORA_NAMESPACE_PREFIX.equals(prefix))
        {
            return "http://www.fedora.info/definitions/1/0/types/";
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    @Override
    public String getPrefix(String namespaceURI)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator getPrefixes(String namespaceURI)
    {
        return null;
    }
}
