/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.security;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;

import au.org.intersect.gda.dao.TeamDAO;
import au.org.intersect.gda.dao.TeamNotFoundException;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.TeamDTO;

/**
 * @version $Rev$
 * 
 */
public class TeamPermissionChecker implements PermissionChecker
{
    private static final Logger LOG = Logger.getLogger(TeamPermissionChecker.class);
    private final TeamDAO teamDAO;

    public TeamPermissionChecker(TeamDAO teamDAO)
    {
        super();
        this.teamDAO = teamDAO;
    }

    public boolean hasPermission(Authentication authentication, Object objectToCheck, AccessType accessType)
    {
        String allPermission = accessType.toString() + "_ALL_TEAMS";
        String ownPermission = accessType.toString() + "_OWN_TEAMS";
        boolean allow = false;
        String currentUser = authentication.getName();

        if (AuthorityHelper.hasPermission(authentication, allPermission))
        {
            allow = true;
        } else if (AuthorityHelper.hasPermission(authentication, ownPermission))
        {
            try
            {
                allow = isCurrentUserTheOwner(currentUser, objectToCheck);
            } catch (TeamNotFoundException e)
            {           
                return true;
            }
        }
        
        return allow;
    }

    protected boolean isCurrentUserTheOwner(String currentUser, Object objectToCheck) throws TeamNotFoundException
    {
        String owner = getOwner(objectToCheck);

        boolean isSameUser = currentUser.equals(owner);
        if (!isSameUser)
        {
            LOG.info("Denying access to team [" + objectToCheck + "] as current user [" + currentUser
                    + "] does not equal owner [" + owner + "]");
        }
        return isSameUser;
    }

    private String getOwner(Object objectToCheck) throws TeamNotFoundException
    {
        if (objectToCheck instanceof TeamDTO)
        {
            TeamDTO dto = (TeamDTO) objectToCheck;
            return dto.getOwner().getUsername();
        } else if (objectToCheck instanceof Integer)
        {
            Team team = loadTeam(objectToCheck);
            return team.getOwner().getUsername();
        } else
        {
            throw new IllegalArgumentException(
                    "Can only check access to instances of TeamDTO or Integer identifier. Received "
                            + objectToCheck.getClass().getName());
        }
    }

    private Team loadTeam(Object objectToCheck) throws TeamNotFoundException
    {
        return teamDAO.getTeam((Integer) objectToCheck);

    }
    
}
