/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.org.intersect.gda.dto.GdaUserDTO;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.dto.ResultInfoDTO;
import au.org.intersect.gda.dto.TeamDTO;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.service.ProjectService;
import au.org.intersect.gda.service.TeamService;
import au.org.intersect.gda.service.UserManagementService;

/**
 * @version $Rev$
 * 
 */
@Controller
@RequestMapping
public class ProjectController
{
    private static final Logger LOG = Logger.getLogger(ProjectController.class);
    private static final String REDIRECT = "redirect:";
    private static final String LIST_PAGE = "projectList";
    private static final String CREATE_PAGE = "createProject";
    private static final String REDIRECT_TO_LIST = "redirect:listProjects";
    private static final String VIEW_PAGE = "viewProject";
    private static final String EDIT_PAGE = "editProject";

    private static final String PROJECT_RESULT_SELECTED_ATTR = "selectedResult";
    
    private static final String PROJECT_RESULT_SELECTED_LIST_ATTR = "selectedResultList";    
    
    private static final String PROJECT_TEAM_SELECTED_ATTR = "selectedTeam";
    private static final String PROJECT_TEAM_ALL_LIST_ATTR = "allTeamList";
    private static final String PROJECT_TEAM_SELECTED_LIST_ATTR = "selectedTeamList";

    private static final String PROJECT_USER_SELECTED_ATTR = "selectedUser";
    private static final String PROJECT_USER_ALL_LIST_ATTR = "allUserList";
    private static final String PROJECT_USER_SELECTED_LIST_ATTR = "selectedUserList";
    
    private static final String PROJECT_ATTR = "project";
    
    private static final String CANCEL_PARAM = "_cancel";
    
    private static final String REDIRECT_PARAM = "redirect";

    private final ProjectService projectService;

    private final UserManagementService userManagementService;
    
    private final TeamService teamService;

    public ProjectController(ProjectService projectService, UserManagementService userManagementService,
            TeamService teamService)
    {
        super();
        this.projectService = projectService;
        this.userManagementService = userManagementService;
        this.teamService = teamService;
    }

    @RequestMapping("/listProjects")
    public String displayListOfProjects(Model model) throws RepositoryException
    {
        Set<ProjectDTO> users = projectService.getAllProjectsBasicInfoOnly();
        model.addAttribute("projects", users);
        return LIST_PAGE;
    }

    @RequestMapping(value = "/createProject", method = RequestMethod.GET)
    public String showCreateProject(Model model, @ModelAttribute(PROJECT_ATTR) ProjectDTO project)
    {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        setUpUserLists(model, project, currentUser);
        setUpResultLists(model, project);
        setUpTeamLists(model, project);
        return CREATE_PAGE;
    }

    @RequestMapping(value = "/createProject", method = RequestMethod.POST)
    public String saveCreateProject(HttpServletRequest request, Model model,
            @RequestParam(value = PROJECT_USER_SELECTED_ATTR, required = false) List<String> memberList,
            @RequestParam(value = PROJECT_RESULT_SELECTED_ATTR, required = false) List<String> resultIdList,
            @RequestParam(value = PROJECT_TEAM_SELECTED_ATTR, required = false) List<Integer> teamIdList,
            @RequestParam(value = REDIRECT_PARAM, required = false) String redirect,
            @ModelAttribute(PROJECT_ATTR) @Valid ProjectDTO project, BindingResult result)
    {
        if (request.getParameter(CANCEL_PARAM) != null)
        {
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.PROJECT_CREATION_CANCELLED);
            return REDIRECT_TO_LIST;
        }        

        
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (result.hasErrors())
        {
            setUpUserLists(model, project, currentUser);
            setUpResultLists(model, project);
            setUpTeamLists(model, project);
            return CREATE_PAGE;
        } else
        {
            List<String> inputResultList;
            if (resultIdList == null)
            {
                inputResultList = new ArrayList<String>();
            } else
            {
                inputResultList = resultIdList;
            }
            Set<String> members;
            if (memberList == null)
            {
                members = new HashSet<String>();
            } else
            {
                members = new HashSet<String>(memberList);   
            }
             
            
            projectService.createProject(project, members, inputResultList, teamIdList);
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.PROJECT_CREATED);
            
            if (StringUtils.isNotEmpty(redirect))
            {
                if (LOG.isInfoEnabled())
                {
                    LOG.info("Saving create then redirecting to " + redirect);
                }
                return REDIRECT + redirect;
            }
            
            return REDIRECT_TO_LIST;
        }
    }

    @RequestMapping(value = "/viewProject")
    public String viewProject(Model model, @RequestParam("id") Integer projectId) throws RepositoryException
    {
        ProjectDTO project = projectService.getProject(projectId);
        List<GdaUserDTO> allUsers = project.getMembers();
        List<GdaUserDTO> activeUsers = filterOutInactiveUsers(allUsers);
        project.setMembers(activeUsers);
        model.addAttribute(PROJECT_ATTR, project);
        return VIEW_PAGE;
    }

    @RequestMapping(value = "/editProject")
    public String showEditProject(Model model, @RequestParam("id") Integer projectId) throws RepositoryException
    {
        ProjectDTO project = projectService.getProject(projectId);
        model.addAttribute(PROJECT_ATTR, project);
        setUpUserLists(model, project, project.getOwner().getUsername());
        setUpResultLists(model, project);
        setUpTeamLists(model, project);
        return EDIT_PAGE;
    }

    @RequestMapping(value = "/editProject", method = RequestMethod.POST)
    public String saveEditProject(HttpServletRequest request, Model model,
            @RequestParam(value = PROJECT_USER_SELECTED_ATTR, required = false) List<String> memberList,
            @RequestParam(value = PROJECT_RESULT_SELECTED_ATTR, required = false) List<String> resultIdList,
            @RequestParam(value = PROJECT_TEAM_SELECTED_ATTR, required = false) List<Integer> teamIdList,
            @RequestParam(value = REDIRECT_PARAM, required = false) String redirect,
            @ModelAttribute(PROJECT_ATTR) @Valid ProjectDTO project, BindingResult result) throws RepositoryException
    {
        if (request.getParameter(CANCEL_PARAM) != null)
        {
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.PROJECT_UPDATE_CANCELLED);
            return REDIRECT_TO_LIST;
        }

        
        ProjectDTO reloadedProject = projectService.getProject(project.getId());
        
        if (result.hasErrors())
        {
            
            setUpUserLists(model, reloadedProject, reloadedProject.getOwner().getUsername());
            setUpResultLists(model, reloadedProject);
            setUpTeamLists(model, reloadedProject);
            return EDIT_PAGE;
        } else
        {
            List<String> inputResultList;
            if (resultIdList == null)
            {
                inputResultList = new ArrayList<String>();
            } else
            {
                inputResultList = resultIdList;
            }
            Set<String> members;
            if (memberList == null)
            {
                members = new HashSet<String>(0);
            } else
            {
                members = new HashSet<String>(memberList);
            }
            
            projectService.updateProject(project.getId(), project, members, inputResultList, teamIdList);
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.PROJECT_UPDATED);
            
            if (StringUtils.isNotEmpty(redirect))
            {
                if (LOG.isInfoEnabled())
                {
                    LOG.info("Saving edit then redirecting to " + redirect);
                }
                return REDIRECT + redirect;
            }
            
            return REDIRECT_TO_LIST;
        }
    }
    

    @RequestMapping(value = "/editProjectRestricted", method = RequestMethod.POST)
    public String saveEditProjectRestricted(HttpServletRequest request, Model model,
            @RequestParam(value = PROJECT_RESULT_SELECTED_ATTR, required = false) List<String> resultIdList, 
            @RequestParam(value = REDIRECT_PARAM, required = false) String redirect,
            @ModelAttribute(PROJECT_ATTR)ProjectDTO project, BindingResult result) throws RepositoryException
    {
        if (request.getParameter(CANCEL_PARAM) != null)
        {
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.PROJECT_UPDATE_CANCELLED);
            return REDIRECT_TO_LIST;
        }

        if (result.hasErrors())
        {
            ProjectDTO projectReloaded = projectService.getProject(project.getId());
            model.addAttribute(PROJECT_ATTR, projectReloaded);
            setUpResultLists(model, projectReloaded);
            return EDIT_PAGE;
        } else
        {
            List<String> inputResultList;
            if (resultIdList == null)
            {
                inputResultList = new ArrayList<String>();
            } else
            {
                inputResultList = resultIdList;
            }
            projectService.updateProjectResultOnly(project.getId(), inputResultList);
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.PROJECT_UPDATED);
            
            if (StringUtils.isNotEmpty(redirect))
            {
                if (LOG.isInfoEnabled())
                {
                    LOG.info("Saving then redirecting to " + redirect);
                }
                return REDIRECT + redirect;
            }
            
            return REDIRECT_TO_LIST;
        }
    }

    @RequestMapping(value = "/deleteProject")
    public String deleteProject(Model model, @RequestParam("id") Integer projectId,
            @RequestParam(value = "referrer", required = false) String referrer)
    {
        projectService.deleteProject(projectId);
        model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.PROJECT_DELETED);
        
        if (referrer != null)
        {
            return REDIRECT + referrer;    
        }        
        return REDIRECT_TO_LIST;
    }
    

    private void setUpUserLists(Model model, ProjectDTO project, String projectOwner)
    {   
        List<GdaUserDTO> allUsers = userManagementService.getAllUsers();
        
        allUsers = filterOutInactiveUsers(allUsers);
        

        List<GdaUserDTO> members = project.getMembers();
        
        removeUserFromList(projectOwner, allUsers);
        removeUserFromList(projectOwner, members);
        

        model.addAttribute(PROJECT_USER_SELECTED_LIST_ATTR, members);
        model.addAttribute(PROJECT_USER_ALL_LIST_ATTR, allUsers);
    }


    private List<GdaUserDTO> filterOutInactiveUsers(List<GdaUserDTO> users)
    {
        List<GdaUserDTO> activeUsers = new ArrayList<GdaUserDTO>();
        for (GdaUserDTO user : users)
        {
            if (user.isActive())
            {
                activeUsers.add(user);
            }
        }
        return activeUsers;
    }

    
    private void setUpTeamLists(Model model, ProjectDTO project)
    {
        
        model.addAttribute(PROJECT_TEAM_SELECTED_LIST_ATTR, project.getTeams());
        
        Set<TeamDTO> teams = teamService.getAllTeams();
        
        List<TeamDTO> teamList = new ArrayList<TeamDTO>(teams);
                
        
        model.addAttribute(PROJECT_TEAM_ALL_LIST_ATTR, teamList);
        
    }


    private void setUpResultLists(Model model, ProjectDTO project)
    {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ResultInfoDTO> existingResults = project.getResults();            
        
        List<ResultInfoDTO> resultOwnedByUser = new ArrayList<ResultInfoDTO>();
        
        if (existingResults != null)
        {
            
            for (ResultInfoDTO result : existingResults)
            {
                if (currentUser.equals(result.getOwner()))
                {
                    resultOwnedByUser.add(result);
                }
            }
        }
        model.addAttribute(PROJECT_RESULT_SELECTED_LIST_ATTR, resultOwnedByUser);
    }

    private GdaUserDTO removeUserFromList(String username, List<GdaUserDTO> users)
    {
        GdaUserDTO dtoToRemove = null;
        if (users == null)
        {
            return null;        
        }
        for (GdaUserDTO dto : users)
        {
            if (dto.getUsername().equals(username))
            {
                dtoToRemove = dto;
            }
        }
        if (dtoToRemove != null)
        {
            users.remove(dtoToRemove);
        }
        return dtoToRemove;
    }
    
}
