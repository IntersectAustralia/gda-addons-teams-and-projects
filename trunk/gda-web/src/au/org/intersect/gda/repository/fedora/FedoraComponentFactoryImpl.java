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
public class FedoraComponentFactoryImpl implements FedoraComponentFactory
{

    private FedoraConfig fedoraConfig;

    public FedoraComponentFactoryImpl(FedoraConfig fedoraConfig)
    {
        super();
        this.fedoraConfig = fedoraConfig;
    }

    @Override
    public FedoraClient buildFedoraClient() throws MalformedURLException
    {
        return new FedoraClient(fedoraConfig.getURL(), fedoraConfig.getUsername(), fedoraConfig.getPassword());
    }

    @Override
    public Uploader buildFedoraUploader() throws IOException
    {
        return new Uploader(fedoraConfig.getProtocol(), fedoraConfig.getHost(), fedoraConfig.getPort(), fedoraConfig
                .getContext(), fedoraConfig.getUsername(), fedoraConfig.getPassword());
    }

}
