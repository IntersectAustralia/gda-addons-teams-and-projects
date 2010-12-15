/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dao;

import java.util.Set;

import au.org.intersect.gda.domain.Team;

/**
 * @version $Rev$
 *
 */
public interface TeamDAO
{
    public Set<Team> getAllTeams();
    
    public Team getTeam(Integer id) throws TeamNotFoundException;

    public Team persist(Team team);

    public void delete(Team team);
    
}
