/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$
 *
 */
public class ProjectOaiDTO
{

    private String name;
    private String description;
    private Timestamp dateCreated;
    private Timestamp dateModified;    
    private String owner;
    private String ownerContact;
    
    private boolean markForExport;

    private List<ResultDTO> results;    
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Timestamp getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public Timestamp getDateModified()
    {
        return dateModified;
    }

    public void setDateModified(Timestamp dateModified)
    {
        this.dateModified = dateModified;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public List<ResultDTO> getResults()
    {
        if (results == null)
        {
            results = new ArrayList<ResultDTO>();
        }
        return results;
    }

    public void setResults(List<ResultDTO> results)
    {
        this.results = results;
    }

    public void setMarkForExport(boolean markForExport)
    {
        this.markForExport = markForExport;
    }

    public boolean isMarkForExport()
    {
        return markForExport;
    }

    public void setOwnerContact(String ownerContact)
    {
        this.ownerContact = ownerContact;
    }

    public String getOwnerContact()
    {
        return ownerContact;
    }
    
}
