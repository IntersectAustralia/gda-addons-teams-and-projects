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
import java.util.List;

import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.GdaUserDTO;
import au.org.intersect.gda.dto.TeamDTO;

/**
 * @version $Rev$
 * 
 */
public class TeamAssemblerImpl implements TeamAssembler
{

    private final GdaUserAssembler userAssembler;
    

    public TeamAssemblerImpl(GdaUserAssembler userAssembler)
    {
        super();
        this.userAssembler = userAssembler;
    }
    
    @Override
    public TeamDTO createTeamDTO(Team team)
    {
        TeamDTO dto = mapCommonFields(team);
        List<GdaUserDTO> members = new ArrayList<GdaUserDTO>();
        for (GdaUser user : team.getMembers())
        {
            GdaUserDTO userDTO = new GdaUserDTO();
            userAssembler.mapUserDTO(userDTO, user);
            members.add(userDTO);
        }
        dto.setMembers(members);

        return dto;
    }
    
    private TeamDTO mapCommonFields(Team team)
    {
        TeamDTO dto = new TeamDTO();
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        dto.setId(team.getId());
        
        GdaUserDTO userDTO = new GdaUserDTO();
        userAssembler.mapUserDTO(userDTO, team.getOwner());
        dto.setOwner(userDTO);
        dto.setLastModifiedDate(team.getLastModifiedDate());
        dto.setCreatedDate(team.getCreatedDate());
        
        return dto;
    }

    @Override
    public Team createTeamFromDTO(TeamDTO dto, GdaUser owner)
    {
        Team team = new Team();
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        team.setOwner(owner);
        return team;
    }

    @Override
    public void updateTeamFromDTO(Team team, TeamDTO dto)
    {
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
    }
}
