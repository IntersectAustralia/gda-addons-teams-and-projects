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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

/**
 * @version $Rev$
 * 
 */
@Entity
@Table(name = "gda_team")
public class Team
{
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private GdaUser owner;
    

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "team_member", 
            joinColumns = @JoinColumn(name = "team_id"), 
            inverseJoinColumns = @JoinColumn(name = "username"))
    private Set<GdaUser> members = new HashSet<GdaUser>();
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "project_team", 
            joinColumns = @JoinColumn(name = "team_id"), 
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    private Set<Project> projects;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "last_modified", nullable = false)
    private Timestamp lastModifiedDate;

    public Team(String name, GdaUser owner)
    {
        super();
        this.name = name;
        this.owner = owner;
    }

    public Team()
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

    public void setMembers(Set<GdaUser> members)
    {
        this.members = members;
    }

    public Set<GdaUser> getMembers()
    {
        
        return members;
    }
    

    public void setProjects(Set<Project> projects)
    {
        this.projects = projects;
    }

    public Set<Project> getProjects()
    {
        if (projects == null)
        {
            projects = new HashSet<Project>();
        }
        return projects;
    }

    public Timestamp getCreatedDate()
    {
        return createdDate;
    }
    
    public void setLastModifiedDate(Timestamp lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
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
        if (!otherObj.getClass().equals(Team.class))
        {
            return false;
        }
        Team other = (Team) otherObj;
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
        buffer.append("Description=");
        buffer.append(this.description);
        return buffer.toString();
    }

}
