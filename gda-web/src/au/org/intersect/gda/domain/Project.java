/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.domain;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 * @version $Rev$
 * 
 */
@Entity
@Table(name = "gda_project")
public class Project
{
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "fedora_id")
    private String fedoraId;
    
    

    @Column(name = "name")
    private String name;
    

    @Column(name = "description")
    private String description;
    
    @Column(name = "notes")
    @Lob
    private String notes;

    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private GdaUser owner;
    

    @ManyToMany
    @JoinTable(name = "project_member", 
            joinColumns = @JoinColumn(name = "project_id"), 
            inverseJoinColumns = @JoinColumn(name = "username"))
    private Set<GdaUser> members;
    
    @ManyToMany
    @JoinTable(name = "project_team", 
            joinColumns = @JoinColumn(name = "project_id"), 
            inverseJoinColumns = @JoinColumn(name = "team_id"))
    private Set<Team> teams;
    
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(name = "project_result", 
            joinColumns = @JoinColumn(name = "project_id"), 
            inverseJoinColumns = @JoinColumn(name = "result_id"))
    private Set<Result> results;
    

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "last_modified", nullable = false)
    private Timestamp lastModifiedDate;
    
    @Column(name = "marked_for_export")
    private Boolean markedForExport;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProjectStatus status;
    

    public Project(String name, GdaUser owner, String description)
    {
        super();
        this.name = name;
        this.owner = owner;
        this.description = description;
    }

    public Project()
    {
        super();
        // for JPA
    }

    @PrePersist
    public void prePersist()
    {
        Timestamp current = new Timestamp(System.currentTimeMillis());
        createdDate = current;
        lastModifiedDate = current;
    }

    @PreUpdate
    public void preUpdate()
    {
        lastModifiedDate = new Timestamp(System.currentTimeMillis());
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public GdaUser getOwner()
    {
        return owner;
    }

    public void setOwner(GdaUser owner)
    {
        this.owner = owner;
    }
    
    //convenience method
    public boolean addMember(GdaUser member)
    {
        if (members == null)
        {
            members = new HashSet<GdaUser>();
        }
        return members.add(member);
    }
    
    public boolean removeMember(GdaUser member)
    {
        if (members == null)
        {
            members = new HashSet<GdaUser>();
        }
        return members.remove(member);
    }

    public void setMembers(Set<GdaUser> members)
    {
        this.members = members;
    }

    public Set<GdaUser> getMembers()
    {
        if (members == null)
        {
            return new HashSet<GdaUser>();
        }
        return members;
    }

    public Timestamp getCreatedDate()
    {
        return createdDate;
    }

    public Timestamp getLastModifiedDate()
    {
        return lastModifiedDate;
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
        if (!otherObj.getClass().equals(Project.class))
        {
            return false;
        }
        Project other = (Project) otherObj;
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

    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID=");
        buffer.append(this.id);
        buffer.append("Name=");
        buffer.append(this.name);
        return buffer.toString();
    }
    
    //convenience method
    public boolean addResult(Result result)
    {
        if (results == null)
        {
            results = new HashSet<Result>();
        }
        this.preUpdate();
        return results.add(result);
    }
    
    public boolean removeResult(Result result)
    {
        if (results == null)
        {
            results = new HashSet<Result>();
        }
        return results.remove(result);
    }

    public void setResults(Set<Result> results)
    {
        this.results = results;
    }

    public Set<Result> getResults()
    {
        if (results == null)
        {
            return new HashSet<Result>();
        }
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
    
    public boolean addTeam(Team team)
    {
        if (teams == null)
        {
            teams = new HashSet<Team>();
        }
        return teams.add(team);
    }
    
    public boolean removeTeam(Team team)
    {
        if (teams == null)
        {
            teams = new HashSet<Team>();
        }
        return teams.remove(team);
    }

    public void setTeams(Set<Team> teams)
    {
        this.teams = teams;
    }

    public Set<Team> getTeams()
    {
        if (teams == null)
        {
            return new HashSet<Team>();
        }
        return teams;
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
