/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.gda.assembler.TeamAssembler;
import au.org.intersect.gda.dao.GdaUserDAO;
import au.org.intersect.gda.dao.GdaUserNotFoundException;
import au.org.intersect.gda.dao.TeamDAO;
import au.org.intersect.gda.dao.TeamNotFoundException;
import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.TeamDTO;
import au.org.intersect.gda.util.SystemException;

/**
 * @version $Rev$
 * 
 */
@Transactional(propagation = Propagation.REQUIRED)
public class TeamServiceImpl implements TeamService
{
    private static final Logger LOG = Logger.getLogger(TeamServiceImpl.class);
    private static final String NOT_FOUND_MSG = "Could not find team with id ";
    private static final String DELETED_MSG = ". Are you sure it has not been deleted?";

    private final TeamDAO teamDAO;
    private final TeamAssembler teamAssembler;
    private final GdaUserDAO userDAO;

    public TeamServiceImpl(TeamAssembler teamAssembler, GdaUserDAO userDAO, TeamDAO teamDAO)
    {
        super();
        this.teamDAO = teamDAO;
        this.teamAssembler = teamAssembler;
        this.userDAO = userDAO;
    }

    @Override
    public Set<TeamDTO> getAllTeams()
    {
        Set<Team> teamsByOwner = teamDAO.getAllTeams();
              
        Set<TeamDTO> teamDTOs = new HashSet<TeamDTO>();
        for (Team team : teamsByOwner)
        {
            TeamDTO dto = teamAssembler.createTeamDTO(team);
            
            teamDTOs.add(dto);
        }        
        
        return teamDTOs;
    }

    @Override
    public void createTeam(TeamDTO teamDTO, Set<String> members)
    {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        GdaUser owner;
        try
        {
            owner = userDAO.getUser(currentUser);
            Team team = teamAssembler.createTeamFromDTO(teamDTO, owner);
            members.add(owner.getUsername());
            setMembers(team, members);
            teamDAO.persist(team);
        } catch (GdaUserNotFoundException e)
        {
            throw new SystemException("Could not find user object for logged in user " + currentUser, e);
        }
    }

    @Override
    public TeamDTO getTeam(Integer teamId)
    {
        Team team;
        try
        {
            team = teamDAO.getTeam(teamId);
        } catch (TeamNotFoundException e)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append(NOT_FOUND_MSG);
            buffer.append(teamId);
            buffer.append(DELETED_MSG);
            throw new SystemException(buffer.toString(), e);
        }
        
        return teamAssembler.createTeamDTO(team);
    }

    @Override
    public void updateTeam(Integer teamId, TeamDTO teamDTO, Set<String> members)
    {
        try
        {
            Team team = teamDAO.getTeam(teamId);
            teamAssembler.updateTeamFromDTO(team, teamDTO);
            members.add(team.getOwner().getUsername());
            updateMembers(members, team);
            teamDAO.persist(team);
        } catch (TeamNotFoundException e)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append(NOT_FOUND_MSG);
            buffer.append(teamId);
            buffer.append(DELETED_MSG);
            throw new SystemException(buffer.toString(), e);
        }
    }

    private void updateMembers(Set<String> members, Team team)
    {
        Set<GdaUser> originalMembers = team.getMembers();
        setMembers(team, members);
        addOriginalInactiveMembers(team, originalMembers);
    }

    // add inactive users into the team (they were not selected in the UI)
    private void addOriginalInactiveMembers(Team team, Set<GdaUser> originalMembers)
    {
        for (GdaUser user : originalMembers)
        {
            if (!user.isActive())
            {
                team.getMembers().add(user);
            }
        }
    }

    @Override
    public void deleteTeam(Integer teamId)
    {
        Team team;
        try
        {
            team = teamDAO.getTeam(teamId);
            teamDAO.delete(team);
        } catch (TeamNotFoundException e)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append(NOT_FOUND_MSG);
            buffer.append(teamId);
            buffer.append(DELETED_MSG);
            throw new SystemException(buffer.toString(), e);
        }
    }
    
    private void setMembers(Team team, Set<String> members)
    {
        Set<GdaUser> memberObjects = new HashSet<GdaUser>();
        for (String username : members)
        {
            try
            {
                memberObjects.add(userDAO.getUser(username));
            } catch (GdaUserNotFoundException e)
            {
                LOG.error("Could not find user with username " + username
                        + "Will ignore and continue with other group members but this may indicate a problem");
            }
        }
        team.setMembers(memberObjects);
    }

}
