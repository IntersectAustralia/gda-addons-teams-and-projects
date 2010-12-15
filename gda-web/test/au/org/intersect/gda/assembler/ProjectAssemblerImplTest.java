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
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.GdaUserDTO;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.dto.ResultInfoDTO;
import au.org.intersect.gda.dto.TeamDTO;
import au.org.intersect.gda.util.ObjectCreationHelper;

/**
 * @version $Rev$
 * 
 */
public class ProjectAssemblerImplTest
{

    @Mock
    private GdaUserAssembler userAssembler;
    
    @Mock
    private TeamAssembler teamAssembler;

    private ProjectAssemblerImpl assembler;

    private GdaUser fred;
    private GdaUser bob;
    private GdaUser sue;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        assembler = new ProjectAssemblerImpl(userAssembler, teamAssembler);
        fred = new GdaUser("fred");
        bob = new GdaUser("bob");
        sue = new GdaUser("sue");
    }
    
    @Test
    public void testCreateProjectDTONoResultDTO()
    {
        Team team1 = new Team("team1", fred);
        team1.setId(1);
        Team team2 = new Team("team2", fred);
        team2.setId(2);
        Timestamp modified = new Timestamp(System.currentTimeMillis());
        Timestamp created = new Timestamp(System.currentTimeMillis() - 1000 * 60 * 60);
        Project project = createProject("name", fred, 100, created, modified);
        project.setNotes("some notes");
        project.getMembers().add(sue);
        project.getMembers().add(bob);
        
        project.addTeam(team1);
        project.addTeam(team2);

        GdaUserDTO fredDto = createUserDto("fred");

        
        when(userAssembler.mapUserDTO((GdaUserDTO) any(), eq(fred))).thenReturn(fredDto);

        TeamDTO team1DTO = new TeamDTO();
        team1DTO.setName("team1");
        TeamDTO team2DTO = new TeamDTO();
        team2DTO.setName("team2");
        
        when(teamAssembler.createTeamDTO(team1)).thenReturn(team1DTO);
        when(teamAssembler.createTeamDTO(team2)).thenReturn(team2DTO);

        ProjectDTO dto = assembler.createProjectDTO(project);
        
        assertEquals("name", dto.getName());
        assertEquals("some notes", dto.getNotes());
        assertEquals(new Integer(100), dto.getId());
        assertSame(fredDto, dto.getOwner());
        assertEquals(modified, dto.getLastModifiedDate());
        assertEquals(created, dto.getCreatedDate());


        assertEquals(2, dto.getTeams().size());
        assertSame(team1DTO, dto.getTeams().get(0));
        assertSame(team2DTO, dto.getTeams().get(1));
    }

    @Test
    public void testCreateDTOFromDomain()
    {
        Team team1 = new Team("team1", fred);
        team1.setId(1);
        Team team2 = new Team("team2", fred);
        team2.setId(2);
        
        Timestamp modified = new Timestamp(System.currentTimeMillis());
        Timestamp created = new Timestamp(System.currentTimeMillis() - 1000 * 60 * 60);
        Project project = createProject("name", fred, 100, created, modified);
        project.getMembers().add(sue);
        project.getMembers().add(bob);
        
        project.addTeam(team1);
        project.addTeam(team2);

        GdaUserDTO fredDto = createUserDto("fred");
        GdaUserDTO bobDto = createUserDto("bob");
        GdaUserDTO sueDto = createUserDto("sue");
        when(userAssembler.mapUserDTO((GdaUserDTO) any(), eq(fred))).thenReturn(fredDto);
        when(userAssembler.mapUserDTO((GdaUserDTO) any(), eq(bob))).thenReturn(bobDto);
        when(userAssembler.mapUserDTO((GdaUserDTO) any(), eq(sue))).thenReturn(sueDto);

        TeamDTO team1DTO = new TeamDTO();
        team1DTO.setName("team1");
        TeamDTO team2DTO = new TeamDTO();
        team2DTO.setName("team2");
        when(teamAssembler.createTeamDTO(team1)).thenReturn(team1DTO);
        when(teamAssembler.createTeamDTO(team2)).thenReturn(team2DTO);
        
        ProjectDTO dto = assembler.createProjectDTO(project, new ArrayList<ResultInfoDTO>());
        assertEquals("name", dto.getName());
        assertEquals(new Integer(100), dto.getId());
        assertSame(fredDto, dto.getOwner());
        assertEquals(modified, dto.getLastModifiedDate());
        assertEquals(created, dto.getCreatedDate());

        assertEquals(2, dto.getMembers().size());
        assertSame(bobDto, dto.getMembers().get(0));
        assertSame(sueDto, dto.getMembers().get(1));
        


        assertEquals(2, dto.getTeams().size());
        assertSame(team1DTO, dto.getTeams().get(0));
        assertSame(team2DTO, dto.getTeams().get(1));
    }

    @Test
    public void testCreateDomainFromDTO()
    {
        GdaUser owner = new GdaUser("fred");
        ProjectDTO dto = new ProjectDTO();
        dto.setName("name");
        dto.setNotes("some notes");

        Project project = assembler.createProjectFromDTO(dto, owner);
        assertEquals("name", project.getName());
        assertEquals("some notes", project.getNotes());
        assertSame(owner, project.getOwner());
    }

    @Test
    public void testUpdatedDomainFromDTO()
    {
        ProjectDTO dto = new ProjectDTO();
        dto.setName("name");
        dto.setNotes("some notes");

        Project project = new Project();
        project.setName("another");

        assembler.updateProjectFromDTO(project, dto);
        assertEquals("name", project.getName());
        assertEquals("some notes", project.getNotes());
    }

    private GdaUserDTO createUserDto(String username)
    {
        GdaUserDTO dto = new GdaUserDTO();
        dto.setUsername(username);
        return dto;
    }

    private Project createProject(String name, GdaUser owner, int id, Timestamp created, Timestamp lastModified)
    {
        Project project = new Project();
        project.setName(name);
        project.setOwner(owner);
        project.setId(id);
        project.setDescription("some description");
        project.setMarkedForExport(true);
        project.setMembers(new HashSet<GdaUser>());
        ObjectCreationHelper.setPrivateField(project, created, "createdDate");
        ObjectCreationHelper.setPrivateField(project, lastModified, "lastModifiedDate");
        return project;
    }

}
