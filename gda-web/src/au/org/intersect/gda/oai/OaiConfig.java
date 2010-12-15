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
public interface OaiConfig
{

    /**
     * @return
     */
    String getXsdTimestampFormat();

    /**
     * @return
     */
    String getRelsExtOaiMarkerNodePath();

    /**
     * @return
     */
    String getRelsExtOaiMarkerNodeName();

    /**
     * @return
     */
    String getRelsExtAttachToNodePath();

    /**
     * @return
     */
    String getRelsExtMime();

    /**
     * @return
     */
    String getProjectDsId();
    
    String getUuIdDsId();
    
    String getUuIdNodeName();

    /**
     * @return
     */
    String getRelsExtDsId();

}
