/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.GdaUserDTO;
import au.org.intersect.gda.dto.TeamDTO;

/**
 * @version $Rev$
 *
 */
public class TeamAssemblerImplTest
{
    @Mock
    private GdaUserAssembler userAssembler;

    private TeamAssembler assembler;
    
    private GdaUser fly;
    private GdaUser taj;
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.assembler = new TeamAssemblerImpl(userAssembler);
        this.fly = new GdaUser("fly");
        this.taj = new GdaUser("taj");
    }

    @Test
    public void testCreateTeamDTO()
    {
        Team team = new Team("team", fly);
        team.setId(1);
        team.setDescription("description");
        
        team.getMembers().add(fly);
        
        TeamDTO dto = assembler.createTeamDTO(team);
        assertEquals(new Integer(1), dto.getId());
        assertTrue(team.getMembers().contains(fly));
        assertEquals("description", team.getDescription());
        assertEquals("team", team.getName());        
    }

    @Test
    public void testCreateTeamFromDTO()
    {
        TeamDTO dto = new TeamDTO();
        final Timestamp createdDate = new Timestamp(0);
        final Timestamp modifiedDate = new Timestamp(120);
        dto.setCreatedDate(createdDate);
        dto.setLastModifiedDate(modifiedDate);
        List<GdaUserDTO> members = new ArrayList<GdaUserDTO>();
        dto.setMembers(members);
        
        Team team = assembler.createTeamFromDTO(dto, taj);
        
        assertEquals(dto.getName(), team.getName());
        assertEquals(dto.getDescription(), team.getDescription());
        for (GdaUserDTO user : members)
        {
            assertTrue(team.getMembers().contains(user));
        }
    }

    @Test
    public void testUpdateTeamFromDTO()
    {
        TeamDTO dto = new TeamDTO();
        dto.setName("new");
        Team team = new Team();
        team.setName("initial");
        assembler.updateTeamFromDTO(team, dto);
        assertEquals("new", team.getName());
    }

}


