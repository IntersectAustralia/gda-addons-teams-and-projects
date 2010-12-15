/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.security;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.gda.dao.GdaUserDAO;
import au.org.intersect.gda.dao.GdaUserNotFoundException;
import au.org.intersect.gda.dao.ProjectDAO;
import au.org.intersect.gda.dao.ProjectNotFoundException;
import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.ProjectDTO;

/**
 * @version $Rev$
 * 
 */
@Transactional(propagation = Propagation.REQUIRED)
public class ProjectPermissionChecker implements PermissionChecker
{
    private static final Logger LOG = Logger.getLogger(ResultPermissionChecker.class);
    private final ProjectDAO projectDAO;
    private final GdaUserDAO userDAO;

    public ProjectPermissionChecker(ProjectDAO projectDAO, GdaUserDAO userDAO)
    {
        super();
        this.projectDAO = projectDAO;
        this.userDAO = userDAO;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object objectToCheck, AccessType accessType)
    {
        String allPermission = accessType.toString() + "_ALL_PROJECTS";
        String ownPermission = accessType.toString() + "_OWN_PROJECTS";
        String projectPermission = accessType.toString() + "_PROJECT_PROJECTS";
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
            } catch (ProjectNotFoundException e)
            {           
                return true;
            }
        }

        if (!allow)
        {
            if (AuthorityHelper.hasPermission(authentication, projectPermission))
            {
                try
                {
                    allow = isCurrentUserMemberOfProject(currentUser, objectToCheck);
                } catch (ProjectNotFoundException e)
                {                  
                    return true;
                }
            }
        }
        
        return allow;
    }

    protected boolean isCurrentUserTheOwner(String currentUser, Object objectToCheck) throws ProjectNotFoundException
    {
        String owner = getOwner(objectToCheck);

        boolean isSameUser = currentUser.equals(owner);
        if (!isSameUser)
        {
            LOG.info("Denying access to project [" + objectToCheck + "] as current user [" + currentUser
                    + "] does not equal owner [" + owner + "]");
        }
        return isSameUser;
    }

    private String getOwner(Object objectToCheck) throws ProjectNotFoundException
    {
        if (objectToCheck instanceof ProjectDTO)
        {
            ProjectDTO dto = (ProjectDTO) objectToCheck;
            return dto.getOwner().getUsername();
        } else if (objectToCheck instanceof Integer)
        {
            Project project = loadProject(objectToCheck);
            return project.getOwner().getUsername();
        } else
        {
            throw new IllegalArgumentException(
                    "Can only check access to instances of ProjectDTO or Integer identifier. Received "
                            + objectToCheck.getClass().getName());
        }
    }
    
    private Project getProject(Object objectToCheck) throws ProjectNotFoundException
    {
        if (objectToCheck instanceof ProjectDTO)
        {
            ProjectDTO dto = (ProjectDTO) objectToCheck;
            return loadProject(dto.getId());
        } else if (objectToCheck instanceof Integer)
        {
            Project project = loadProject(objectToCheck);
            return project;
        } else
        {
            throw new IllegalArgumentException(
                    "Can only check access to instances of ProjectDTO or Integer identifier. Received "
                            + objectToCheck.getClass().getName());
        }
    }

    private Project loadProject(Object objectToCheck) throws ProjectNotFoundException
    {
  
        return projectDAO.getProject((Integer) objectToCheck);

    }
    


    private boolean isCurrentUserMemberOfProject(String currentUser, Object objectToCheck) 
        throws ProjectNotFoundException
    {
   
        Project project = getProject(objectToCheck);
        
        //check owner
        if (currentUser.equals(project.getOwner().getUsername()))
        {
            return true;
        }
        
        Set<Project> userProjects;
        try
        {
            userProjects = getUserProjects(currentUser);

            if (userProjects.contains(project))
            {
                return true;
            } else
            {
                if (LOG.isInfoEnabled())
                {
                    LOG.info(buildDenyAccessMessage(
                            project.getId(), 
                            currentUser, 
                            "is not a project member"));                
                }
                return false;
            }
        } catch (GdaUserNotFoundException e)
        {
            if (LOG.isInfoEnabled())
            {
                LOG.info(buildDenyAccessMessage(
                        project.getId(), 
                        currentUser, 
                        "cannot be found"));                
            }
            return false;
        }        
    }
    
    private Set<Project> getUserProjects(String username) throws GdaUserNotFoundException
    {
        Set<Project> userProjects = new HashSet<Project>();

        GdaUser user;
      
        user = this.userDAO.getUser(username);

        userProjects.addAll(user.getProjects());
        
        Set<Team> userTeams = user.getTeams();
        
        for (Team team : userTeams)
        {
            userProjects.addAll(team.getProjects());
        }
        return userProjects;
    }
    
    
    private String buildDenyAccessMessage(Integer projectId, String currentUser, String reason)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Denying access to project [");
        buffer.append(projectId);
        buffer.append("] as current user [");
        buffer.append(currentUser);
        buffer.append("] ");
        buffer.append(reason);
        
        return buffer.toString();
    }
    

}
