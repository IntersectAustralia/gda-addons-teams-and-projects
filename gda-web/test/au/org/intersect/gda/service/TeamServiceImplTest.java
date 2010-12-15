/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.service;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import au.org.intersect.gda.assembler.TeamAssembler;
import au.org.intersect.gda.dao.GdaUserDAO;
import au.org.intersect.gda.dao.GdaUserNotFoundException;
import au.org.intersect.gda.dao.TeamDAO;
import au.org.intersect.gda.dao.TeamNotFoundException;
import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.TeamDTO;
import au.org.intersect.gda.util.MockAuthentication;
import au.org.intersect.gda.util.SystemException;

/**
 * @version $Rev$
 *
 */
public class TeamServiceImplTest
{
    @Mock
    private TeamAssembler teamAssembler;
    @Mock
    private GdaUserDAO userDAO;
    @Mock
    private TeamDAO teamDAO;
    
    private TeamServiceImpl teamService;
    
    private Set<Team> teams;
    private Team team0;
    private Team team1;
    
    private TeamDTO dto0;
    private TeamDTO dto1;
    
    private GdaUser user0;
    private GdaUser user1;
    
    private final String username = "user";
    private GdaUser user = new GdaUser(username);


    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        teamService = new TeamServiceImpl(teamAssembler, userDAO, teamDAO);
        
        
        this.user0 = new GdaUser(username);
        
        this.teams = new HashSet<Team>();
        
        this.team0 = new Team("team", user0);
        team0.setId(23);
        this.teams.add(team0);
        dto0 = new TeamDTO();
        dto0.setId(23);
        dto0.setName("team");
        
        this.team1 = new Team("team1", user1);
        team1.setId(1);
        this.teams.add(team1);
        dto1 = new TeamDTO();
        dto1.setId(1);
        dto1.setName("team1");
        
        when(teamDAO.getAllTeams()).thenReturn(this.teams);
        
        when(teamAssembler.createTeamDTO(team0)).thenReturn(dto0);
        when(teamAssembler.createTeamDTO(team1)).thenReturn(dto1);
        
        SecurityContextHolder.getContext().setAuthentication(new MockAuthentication(username));
        when(userDAO.getUser(username)).thenReturn(user);
        
        when(teamDAO.getTeam(1)).thenReturn(team1);
        when(teamDAO.getTeam(23)).thenReturn(team0);
    }


    /**
     * Test method for {@link au.org.intersect.gda.service.TeamServiceImpl#getAllTeams()}.
     */
    @Test
    public void testGetAllTeams()
    {
        Set<TeamDTO> teams = teamService.getAllTeams();
        
        assertNotNull(teams);
        assertEquals(2, teams.size());
        
        assertTrue(teams.contains(dto0));
        assertTrue(teams.contains(dto1));
        
        verify(teamAssembler).createTeamDTO(team0);
        verify(teamAssembler).createTeamDTO(team1);
    }

    @Test
    public void testCreateTeam()
    {
        TeamDTO dto = new TeamDTO();
        Set<String> members = new HashSet<String>();
        Team team = new Team();
        when(teamAssembler.createTeamFromDTO(dto, user)).thenReturn(team);
        
        teamService.createTeam(dto, members);
        verify(teamDAO).persist(team);
        assertTrue(team.getMembers().contains(user));
        
    }

    @Test
    public void testGetTeam()
    {
        TeamDTO dto = teamService.getTeam(1);
        assertSame(dto, dto1);
    }

    @Test
    public void testUpdateTeam()
    {
        TeamDTO newDto = new TeamDTO();
        newDto.setName("updated");
        team1.setOwner(user);
        Set<String> members = new HashSet<String>();
        members.add(username);
        teamService.updateTeam(1, newDto, members);
        verify(teamAssembler).updateTeamFromDTO(team1, newDto); 
        assertTrue(team1.getMembers().contains(user));
    }

    @Test
    public void testDeleteTeam() throws TeamNotFoundException
    {
        Team team = new Team();
        team.setId(1010);
        when(teamDAO.getTeam(1010)).thenReturn(team);
        teamService.deleteTeam(1010);
        verify(teamDAO).delete(team);
    }
    
    @Test
    public void testCreateException() throws GdaUserNotFoundException
    {
        GdaUserNotFoundException ex = new GdaUserNotFoundException("abcd");
        when(userDAO.getUser(anyString())).thenThrow(ex);
        try
        {
            teamService.createTeam(dto0, Collections.singleton(""));
            fail("exception not thrown");
        } catch (SystemException e)
        {
            assertSame(ex, e.getCause());
        }
    }
    
    @Test
    public void testGetException() throws TeamNotFoundException
    {
        TeamNotFoundException ex = new TeamNotFoundException(4253);
        when(teamDAO.getTeam(anyInt())).thenThrow(ex);
        try
        {
            teamService.getTeam(4253);
            fail("exception not thrown");
        } catch (SystemException e)
        {
            assertSame(ex, e.getCause());

        }
    }
    
    @Test
    public void testUpdateException() throws TeamNotFoundException
    {
        TeamNotFoundException ex = new TeamNotFoundException(4253);
        when(teamDAO.getTeam(anyInt())).thenThrow(ex);
        try
        {
            teamService.updateTeam(4253, null, null);
            fail("exception not thrown");
        } catch (SystemException e)
        {
            assertSame(ex, e.getCause());

        }
    }
    
    @Test
    public void testDeleteException() throws TeamNotFoundException
    {
        TeamNotFoundException ex = new TeamNotFoundException(4253);
        when(teamDAO.getTeam(anyInt())).thenThrow(ex);
        try
        {
            teamService.deleteTeam(4253);
            fail("exception not thrown");
        } catch (SystemException e)
        {
            assertSame(ex, e.getCause());
        }
    }

}
