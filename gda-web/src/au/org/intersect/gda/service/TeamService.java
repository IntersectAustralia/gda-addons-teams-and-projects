/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.service;

import java.util.Set;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import au.org.intersect.gda.dto.TeamDTO;

/**
 * @version $Rev$
 * 
 */
public interface TeamService
{
    @PostFilter("hasPermission(filterObject, 'VIEW')")
    Set<TeamDTO> getAllTeams();

    @PreAuthorize("hasAnyRole('EDIT_OWN_TEAMS','EDIT_ALL_TEAMS')")
    void createTeam(TeamDTO teamDTO, Set<String> members);

    @PreAuthorize("hasPermission(#teamId, 'team', 'VIEW')")
    TeamDTO getTeam(Integer teamId);

    @PreAuthorize("hasPermission(#teamId, 'team', 'EDIT')")
    void updateTeam(Integer id, TeamDTO teamDTO, Set<String> members);
    
    @PreAuthorize("hasPermission(#teamId, 'team', 'DELETE')")
    void deleteTeam(Integer teamId);
    
}
