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
import au.org.intersect.gda.dto.TeamDTO;
import au.org.intersect.gda.service.TeamService;
import au.org.intersect.gda.service.UserManagementService;

/**
 * @version $Rev$
 * 
 */
@Controller
@RequestMapping
public class TeamController
{
    private static final Logger LOG = Logger.getLogger(TeamController.class);
    private static final String LIST_PAGE = "teamList";
    private static final String CREATE_PAGE = "createTeam";
    private static final String REDIRECT_TO_LIST = "redirect:listTeams";
    private static final String VIEW_PAGE = "viewTeam";
    private static final String EDIT_PAGE = "editTeam";    
    private static final String TEAM_ATTR = "team";
    private static final String CANCEL_PARAM = "_cancel";

    private final TeamService teamService;

    private final UserManagementService userManagementService;

    public TeamController(TeamService teamService,
            UserManagementService userManagementService)
    {
        super();
        this.teamService = teamService;
        this.userManagementService = userManagementService;
    }

    @RequestMapping("/listTeams")
    public String displayListOfTeams(Model model)
    {
        Set<TeamDTO> teams = teamService.getAllTeams();
        model.addAttribute("teams", teams);
        return LIST_PAGE;
    }

    @RequestMapping(value = "/createTeam", method = RequestMethod.GET)
    public String showCreateTeam(Model model, @ModelAttribute(TEAM_ATTR) TeamDTO team)
    {
        LOG.info("create team");
        LOG.info(team);
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        setUpUserLists(model, team, currentUser);
        return CREATE_PAGE;
    }

    @RequestMapping(value = "/createTeam", method = RequestMethod.POST)
    public String saveCreateTeam(HttpServletRequest request, Model model,
            @ModelAttribute(TEAM_ATTR) @Valid TeamDTO teamDTO, BindingResult result)
    {
        
        if (request.getParameter(CANCEL_PARAM) != null)
        {
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.TEAM_CREATION_CANCELLED);
            return REDIRECT_TO_LIST;
        }
        
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        Set<String> members = getMembers(request);
        if (result.hasErrors())
        {
            setUpUserLists(model, teamDTO, currentUser);
            return CREATE_PAGE;
        } else
        {
            teamService.createTeam(teamDTO, members);
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.TEAM_CREATED);
            return REDIRECT_TO_LIST;
        }
    }

    @RequestMapping(value = "/viewTeam")
    public String viewTeam(Model model, @RequestParam("id") Integer teamId)
    {
        TeamDTO team = teamService.getTeam(teamId);
        List<GdaUserDTO> allUsers = team.getMembers();
        List<GdaUserDTO> activeUsers = filterOutInactiveUsers(allUsers);
        team.setMembers(activeUsers);
        model.addAttribute(TEAM_ATTR, team);
        return VIEW_PAGE;
    }

    @RequestMapping(value = "/editTeam")
    public String showEditTeam(Model model, @RequestParam("id") Integer teamId)
    {
        TeamDTO team = teamService.getTeam(teamId);
        model.addAttribute(TEAM_ATTR, team);
        
        setUpUserLists(model, team, team.getOwner().getUsername());
        return EDIT_PAGE;
    }

    @RequestMapping(value = "/editTeam", method = RequestMethod.POST)
    public String saveEditTeam(HttpServletRequest request, Model model,
            @ModelAttribute(TEAM_ATTR) @Valid TeamDTO team, BindingResult result)
    {
        if (request.getParameter(CANCEL_PARAM) != null)
        {
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.TEAM_UPDATE_CANCELLED);
            return REDIRECT_TO_LIST;
        }

        Set<String> members = getMembers(request);
        TeamDTO reloadedTeam = teamService.getTeam(team.getId());
        
        if (result.hasErrors())
        {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            setUpUserLists(model, reloadedTeam, currentUser);
            return EDIT_PAGE;
        } else
        { 
            teamService.updateTeam(team.getId(), team, members);
            model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.TEAM_UPDATED);
            return REDIRECT_TO_LIST;
        }
    }
    
    @RequestMapping(value = "/deleteTeam")
    public String deleteTeam(Model model, @RequestParam("id") Integer teamId)
    {
        teamService.deleteTeam(teamId);
        model.addAttribute(Notice.INFO_NOTICE_ATTR, Notice.TEAM_DELETED);
        return REDIRECT_TO_LIST;
    }

    private Set<String> getMembers(HttpServletRequest request)
    {
        String[] parameterValues = request.getParameterValues("member");
        Set<String> members = new HashSet<String>();
        if (parameterValues == null || parameterValues.length == 0)
        {
            return members;
        }
        for (String str : parameterValues)
        {
            members.add(str);
        }
        return members;
    }


    private void setUpUserLists(Model model, TeamDTO team, String ownerToExclude)
    {
        List<GdaUserDTO> allUsers = userManagementService.getAllUsers();
        List<GdaUserDTO> selectedUsers = team.getMembers();
        
        removeUserFromList(ownerToExclude, allUsers);
        removeUserFromList(ownerToExclude, selectedUsers);
        
        model.addAttribute("selectedUsers", selectedUsers);
        model.addAttribute("allUsers", allUsers);
    }

    private List<GdaUserDTO> filterOutInactiveUsers(List<GdaUserDTO> users)
    {
        List<GdaUserDTO> activeUsers = new ArrayList<GdaUserDTO>();
        if (users != null)
        {
            for (GdaUserDTO user : users)
            {
                if (user != null && user.isActive())
                {
                    activeUsers.add(user);
                }
            }
        }
        return activeUsers;
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
