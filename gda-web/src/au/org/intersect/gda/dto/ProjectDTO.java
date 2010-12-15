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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import au.org.intersect.gda.domain.ProjectStatus;

/**
 * @version $Rev$
 * 
 */
public class ProjectDTO implements Comparable<ProjectDTO>, ProjectAndResultDisplay
{
    private static final String DATE_FORMAT_STRING = "dd-MM-yyyy HH:mm";

    private Integer id;
    
    private String fedoraId;

    @Length(max = 50)
    private String name;
    
    @Length(max = 255)
    private String description;
    
    private String notes;
    
    private GdaUserDTO owner;
    private List<GdaUserDTO> members;
    private List<ResultInfoDTO> results;
    private List<TeamDTO> teams;
    private Timestamp createdDate;
    private Timestamp lastModifiedDate;
    
    private Boolean markedForExport;
    
    private ProjectStatus status;
    

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
    

    public void setResults(List<ResultInfoDTO> results)
    {
        this.results = results;
    }

    public List<ResultInfoDTO> getResults()
    {
        return results;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
    
    public boolean addTeam(TeamDTO team)
    {
        if (teams == null)
        {
            teams = new ArrayList<TeamDTO>();
        }        
        return teams.add(team);
    }
    
    public boolean removeTeam(TeamDTO team)
    {
        if (teams == null)
        {
            teams = new ArrayList<TeamDTO>();
        }        
        return teams.remove(team);
    }

    public void setTeams(List<TeamDTO> teams)
    {
        this.teams = teams;
    }

    public List<TeamDTO> getTeams()
    {
        if (teams == null)
        {
            return new ArrayList<TeamDTO>();
        }
        return teams;
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
        if (!otherObj.getClass().equals(ProjectDTO.class))
        {
            return false;
        }
        ProjectDTO other = (ProjectDTO) otherObj;
        // if either are null, we can't compare, so return false
        if (id == null || other.id == null)
        {
            return false;
        }
        return id.equals(other.id);
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

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ProjectDTO other)
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

    /* (non-Javadoc)
     * @see au.org.intersect.gda.dto.ProjectAndResultDisplayInterface#getDisplayType()
     */
    @Override
    public String getDisplayType()
    {
        return "project";
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.dto.ProjectAndResultDisplayInterface#getIdDisplay()
     */
    @Override
    public String getIdDisplay()
    {
        if (id == null)
        {
            return "";
        }
        
        return id.toString();
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.dto.ProjectAndResultDisplayInterface#getLastModifiedDisplay()
     */
    @Override
    public String getLastModifiedDisplay()
    {
        return new SimpleDateFormat(DATE_FORMAT_STRING).format(this.getLastModifiedDate());
    }
    
    @Override
    public Date getLastModifiedDisplayDate()
    {
        return this.getLastModifiedDate();
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.dto.ProjectAndResultDisplayInterface#getNameDisplay()
     */
    @Override
    public String getNameDisplay()
    {
        return this.getName();
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.dto.ProjectAndResultDisplayInterface#getOwnerDisplay()
     */
    @Override
    public String getOwnerDisplay()
    {
        if (owner == null)
        {
            return "";
        }
        return this.owner.getUsername();
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.dto.ProjectAndResultDisplayInterface#getTypeDisplay()
     */
    @Override
    public String getTypeDisplay()
    {
        return "Project";
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.dto.ProjectAndResultDisplay#getIdNumeric()
     */
    @Override
    public Integer getIdNumeric()
    {
  
        return id;

    }

    public void setMarkedForExport(Boolean markedForExport)
    {
        this.markedForExport = markedForExport;
    }

    public Boolean getMarkedForExport()
    {
        return markedForExport;
    }

    public void setFedoraId(String fedoraId)
    {
        this.fedoraId = fedoraId;
    }

    public String getFedoraId()
    {
        return fedoraId;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setStatus(ProjectStatus status)
    {
        this.status = status;
    }

    public ProjectStatus getStatus()
    {
        return status;
    }

}
