/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dao;

import static org.junit.Assert.*;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.GdaUserFixture;
import au.org.intersect.gda.domain.Role;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.domain.TeamFixture;

/**
 * @version $Rev$
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "test-TeamDAO-application-context.xml")
@Transactional
public class TeamDAOImplTest
{
    @Autowired
    private TeamDAO teamDAO;
    
    private GdaUser trudy;
    private GdaUser bob;
    private GdaUser alice;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Before
    public void setUp() throws Exception
    {
        Role role = GdaUserFixture.createSamplePersistedRoles(entityManager);
        trudy = GdaUserFixture.createUser("trudy", role);
        bob = GdaUserFixture.createUser("bob", role);
        alice = GdaUserFixture.createUser("alice", role);
        entityManager.persist(trudy);
        entityManager.persist(bob);
        entityManager.persist(alice);
    }
    
    @Test
    public void testNoSuchTeam()
    {
        try
        {
            teamDAO.getTeam(-1);
            fail("did not throw exception");
        } catch (TeamNotFoundException e)
        {
            assertNotNull(e);
        }
    }
    
    /**
     * Test method for {@link au.org.intersect.gda.dao.TeamDAOImpl#getAllTeams()}.
     */
    @Test
    public void testGetAllTeams()
    {
        Team team0 = TeamFixture.createTeam("team0", bob);
        teamDAO.persist(team0);
        Team team1 = TeamFixture.createTeam("team1", alice);
        teamDAO.persist(team1);
        flushAndClear();
        
        Set<Team> teams = teamDAO.getAllTeams();
        assertEquals(2, teams.size());
        Team loaded0 = findTeam(teams, "team0");
        assertNotNull(loaded0);
        Team loaded1 = findTeam(teams, "team1");
        assertNotNull(loaded1);
        
        assertEqualTeams(team0, loaded0);
        assertEqualTeams(team1, loaded1);
    }
    
    @Test
    public void testPersistLoad() throws TeamNotFoundException
    {
        Team team = TeamFixture.createTeam("team", alice);
        Team persistedTeam = teamDAO.persist(team);
        flushAndClear();
        Team loadedTeam = teamDAO.getTeam(persistedTeam.getId());
        assertEquals(persistedTeam.toString(), loadedTeam.toString());
    }


    @Test
    public void testDelete()
    {
        Team team0 = TeamFixture.createTeam("team0", trudy);
        Team team1 = TeamFixture.createTeam("team1", bob);
        Team team2 = TeamFixture.createTeam("team2", bob);
        Team team3 = TeamFixture.createTeam("team3", bob);
        teamDAO.persist(team0);
        teamDAO.persist(team1);
        teamDAO.persist(team2);
        teamDAO.persist(team3);
        flushAndClear();
        Set<Team> teams = teamDAO.getAllTeams();
        
        int expectedNumTeams = 4;
        assertEquals(expectedNumTeams, teams.size());
        while (!teams.isEmpty())
        {
            Team team = teams.iterator().next();
            teamDAO.delete(team);
            flushAndClear();
            teams = teamDAO.getAllTeams();
            assertEquals(--expectedNumTeams, teams.size());
        }
    }
    
    private void flushAndClear()
    {
        entityManager.flush();
        entityManager.clear();
    }
    
    private Team findTeam(Set<Team> teams, String teamName)
    {
        Team team = null;
        for (Team t : teams)
        {
            if (teamName.equals(t.getName()))
            {
                team = t;
                break;
            }
        }
        return team;
    }
    
    private void assertEqualTeams(Team team0, Team team1)
    {
        assertEquals(team0.getName(), team1.getName());
        assertEquals(team0.getDescription(), team1.getDescription());
        assertEquals(team0.getOwner(), team1.getOwner());
    }


}

