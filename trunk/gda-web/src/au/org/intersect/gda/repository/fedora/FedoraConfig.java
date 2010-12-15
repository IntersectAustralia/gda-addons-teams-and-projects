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
 * @version $Rev$
 *
 */
public interface FedoraConfig
{

    String getURL();

    String getUsername();

    String getPassword();

    String getProtocol();

    String getHost();

    int getPort();

    String getContext();

    String getNamespace();

}
