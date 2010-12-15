/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.assembler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.GdaUserDTO;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.dto.ResultInfoDTO;
import au.org.intersect.gda.dto.TeamDTO;

/**
 * @version $Rev$
 * 
 */
public class ProjectAssemblerImpl implements ProjectAssembler
{

    private final GdaUserAssembler userAssembler;
    private final TeamAssembler teamAssembler;
    

    public ProjectAssemblerImpl(GdaUserAssembler userAssembler, TeamAssembler teamAssembler)
    {
        super();
        this.userAssembler = userAssembler;
        this.teamAssembler = teamAssembler;
    }
    
    @Override
    public ProjectDTO createProjectDTO(Project project)
    {
        ProjectDTO dto = mapCommonFields(project);

        return dto;
    }

    @Override
    public ProjectDTO createProjectDTO(Project project, List<ResultInfoDTO> resultList)
    {
        ProjectDTO dto = mapCommonFields(project);

        List<GdaUserDTO> members = new ArrayList<GdaUserDTO>();
        for (GdaUser member : project.getMembers())
        {
            GdaUserDTO userDTO = new GdaUserDTO();
            userDTO = userAssembler.mapUserDTO(userDTO, member);
            members.add(userDTO);
        }
        Collections.sort(resultList);
        dto.setResults(resultList);        
        Collections.sort(members);
        dto.setMembers(members);
        return dto;
    }
    
    private ProjectDTO mapCommonFields(Project project)
    {
        ProjectDTO dto = new ProjectDTO();
        dto.setName(project.getName());
        dto.setId(project.getId());
        dto.setDescription(project.getDescription());
        dto.setNotes(project.getNotes());
        dto.setStatus(project.getStatus());
        GdaUserDTO userDTO = new GdaUserDTO();
        userDTO = userAssembler.mapUserDTO(userDTO, project.getOwner());
        
        
        dto.setOwner(userDTO);
        dto.setLastModifiedDate(project.getLastModifiedDate());
        dto.setCreatedDate(project.getCreatedDate());
        dto.setMarkedForExport(project.getMarkedForExport());
        dto.setFedoraId(project.getFedoraId());
        mapTeam(dto, project);
        
        return dto;
    }
    
    private void mapTeam(ProjectDTO dto, Project project)
    {
        Set<Team> teams = project.getTeams();
        for (Team team : teams)
        {
            TeamDTO teamDTO = teamAssembler.createTeamDTO(team);
            dto.addTeam(teamDTO);
        }
    }

    @Override
    public Project createProjectFromDTO(ProjectDTO dto, GdaUser owner)
    {
        Project project = new Project(dto.getName(), owner, dto.getDescription());
        project.setNotes(dto.getNotes());
        project.setMarkedForExport(dto.getMarkedForExport());
        project.setFedoraId(dto.getFedoraId());
        return project;
    }

    @Override
    public void updateProjectFromDTO(Project project, ProjectDTO dto)
    {
        project.setName(dto.getName());
        project.setMarkedForExport(dto.getMarkedForExport());
        project.setDescription(dto.getDescription());
        project.setNotes(dto.getNotes());
    }
}
