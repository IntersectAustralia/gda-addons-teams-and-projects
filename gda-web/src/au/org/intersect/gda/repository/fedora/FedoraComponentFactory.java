/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.io.IOException;
import java.net.MalformedURLException;

import fedora.client.FedoraClient;
import fedora.client.Uploader;

/**
 * @version $Rev$
 * 
 */
public interface FedoraComponentFactory
{

    FedoraClient buildFedoraClient() throws MalformedURLException;

    Uploader buildFedoraUploader() throws IOException;
}
