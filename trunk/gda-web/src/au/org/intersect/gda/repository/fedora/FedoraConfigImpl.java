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
public class FedoraConfigImpl implements FedoraConfig
{
    private String password;
    private String username;
    private String protocol;
    private String context;
    private String host;
    private int port;
    // used as a prefix for creating new objects. i.e. the format is ${FEDORA_NAMESPACE}:${result.name}
    private String namespace;

    @Override
    public String getURL()
    {
        return protocol + ":" + "//" + host + ":" + port + context;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public String getContext()
    {
        return context;
    }

    @Override
    public String getHost()
    {
        return host;
    }

    @Override
    public int getPort()
    {
        return port;
    }

    @Override
    public String getProtocol()
    {
        return protocol;
    }

    @Override
    public String getNamespace()
    {
        return namespace;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

}
