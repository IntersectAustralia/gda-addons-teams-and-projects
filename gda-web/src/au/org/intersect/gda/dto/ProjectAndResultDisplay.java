/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dto;

import java.util.Date;


/**
 * @version $Rev$
 *
 */
public interface ProjectAndResultDisplay
{
    
    public String getDisplayType();
    public String getNameDisplay();    
    public String getIdDisplay();
    
    public Integer getIdNumeric();
    
    public String getLastModifiedDisplay();
    
    public Date getLastModifiedDisplayDate();
    
    public String getOwnerDisplay();
    
    public String getTypeDisplay();
    
    
    
}
