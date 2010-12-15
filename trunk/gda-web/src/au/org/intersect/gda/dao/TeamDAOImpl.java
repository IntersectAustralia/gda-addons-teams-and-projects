/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dao;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import au.org.intersect.gda.domain.Team;

/**
 * @version $Rev$
 * 
 */
public class TeamDAOImpl implements TeamDAO
{
    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @Override
    public Team getTeam(Integer id) throws TeamNotFoundException
    {
        Team team = entityManager.find(Team.class, id);
        if (team == null)
        {
            throw new TeamNotFoundException(id);
        }
        return team;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Team> getAllTeams()
    {
        
        Query query = entityManager.createQuery("select E from " + Team.class.getName() + " as E");
        List<Team> resultList = query.getResultList();
        return new HashSet<Team>(resultList);
    }

    @Override
    public Team persist(Team team)
    {
        team.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
        return entityManager.merge(team);
    }

    @Override
    public void delete(Team team)
    {
        entityManager.remove(team);
    }
}
