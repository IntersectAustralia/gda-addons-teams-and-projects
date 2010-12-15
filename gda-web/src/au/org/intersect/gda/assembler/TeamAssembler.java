/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.assembler;

import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.TeamDTO;

/**
 * @version $Rev$
 *
 */
public interface TeamAssembler
{

    TeamDTO createTeamDTO(Team team);

    Team createTeamFromDTO(TeamDTO dto, GdaUser owner);

    void updateTeamFromDTO(Team team, TeamDTO dto);

}
