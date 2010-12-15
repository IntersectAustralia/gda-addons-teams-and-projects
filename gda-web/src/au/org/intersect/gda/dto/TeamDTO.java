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
import java.util.List;

import org.hibernate.validator.constraints.Length;

/**
 * @version $Rev$
 * 
 */
public class TeamDTO implements Comparable<TeamDTO>
{
    private Integer id;

    @Length(max = 50)
    private String name;
    
    @Length(max = 255)
    private String description;
    private GdaUserDTO owner;
    private List<GdaUserDTO> members;
    private Timestamp createdDate;
    private Timestamp lastModifiedDate;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public GdaUserDTO getOwner()
    {
        return owner;
    }

    public void setOwner(GdaUserDTO owner)
    {
        this.owner = owner;
    }

    public List<GdaUserDTO> getMembers()
    {
        return members;
    }

    public void setMembers(List<GdaUserDTO> members)
    {
        this.members = members;
    }

    public Timestamp getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate)
    {
        this.createdDate = createdDate;
    }

    public Timestamp getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Timestamp lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == null)
        {
            return false;
        }
        if (this == otherObj)
        {
            return true;
        }
        if (!otherObj.getClass().equals(TeamDTO.class))
        {
            return false;
        }
        TeamDTO other = (TeamDTO) otherObj;
        // if either are null, we can't compare, so return false
        if (name == null || other.name == null)
        {
            return false;
        }
        return name.equals(other.name);
    }

    @Override
    public int hashCode()
    {
        if (this.id == null)
        {
            return 0;
        }
        return id.hashCode();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TeamDTO other)
    {
        if (other.id == null && this.id == null)
        {
            return 0;
        }
        if (this.id == null)
        {
            return -1;
        }
        if (other.id == null)
        {
            return 1;
        }
        return this.id.compareTo(other.getId());
    }

}
