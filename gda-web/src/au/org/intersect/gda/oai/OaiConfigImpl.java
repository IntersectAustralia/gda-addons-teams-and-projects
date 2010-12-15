/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.oai;

/**
 * @version $Rev$
 *
 */
public class OaiConfigImpl implements OaiConfig
{
    private final String xsdTimestampFormat;

    private final String relsExtDsId;
    
    private final String projectDsId;
    private final String uuIdDsId;
    
    private final String uuIdNodeName;

    private final String relsExtMime;
    
    
    private final String relsExtAttachToNodePath;
    
    private final String relsExtOaiMarkerNodeName;
    
    private final String relsExtOaiMarkerNodePath;

    public OaiConfigImpl(
            String xsdTimestampFormat,
            String relsExtDsId,
            String projectDsId,
            String uuIdDsId,
            String uuIdNodeName,
            String relsExtMime,
            String relsExtAttachToNodePath,
            String relsExtOaiMarkerNodeName,
            String relsExtOaiMarkerNodePath)
    {
        this.projectDsId = projectDsId;
        this.relsExtAttachToNodePath = relsExtAttachToNodePath;
        this.relsExtDsId = relsExtDsId;
        this.uuIdDsId = uuIdDsId;
        this.uuIdNodeName = uuIdNodeName;
        this.relsExtMime = relsExtMime;
        this.relsExtOaiMarkerNodeName = relsExtOaiMarkerNodeName;
        this.relsExtOaiMarkerNodePath = relsExtOaiMarkerNodePath;
        this.xsdTimestampFormat = xsdTimestampFormat;
        
    }
    
    @Override
    public String getRelsExtDsId()
    {
        return relsExtDsId;
    }

    @Override
    public String getProjectDsId()
    {
        return projectDsId;
    }
    
    @Override    
    public String getRelsExtMime()
    {
        return relsExtMime;
    }

    @Override
    public String getRelsExtAttachToNodePath()
    {
        return relsExtAttachToNodePath;
    }

    @Override
    public String getRelsExtOaiMarkerNodeName()
    {
        return relsExtOaiMarkerNodeName;
    }

    @Override
    public String getRelsExtOaiMarkerNodePath()
    {
        return relsExtOaiMarkerNodePath;
    }

    
    @Override
    public String getXsdTimestampFormat()
    {
        return xsdTimestampFormat;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.oai.OaiConfig#getUuIdDsId()
     */
    @Override
    public String getUuIdDsId()
    {
        return uuIdDsId;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.oai.OaiConfig#getUuIdNodeName()
     */
    @Override
    public String getUuIdNodeName()
    {
        return uuIdNodeName;
    }
    
    
}
