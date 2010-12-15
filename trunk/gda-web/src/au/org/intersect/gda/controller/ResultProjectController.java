/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.service.ProjectService;
import au.org.intersect.gda.util.TrimStringPropertyEditor;

/**
 * @version $Rev$
 *
 */
@Controller
public class ResultProjectController
{
    private static final Logger LOG = Logger.getLogger(ResultProjectController.class);
    
    private static final String PROJECT_ADDED_ATTR = "projectsAdded";
    
    private final ProjectService projectService;
    
    public ResultProjectController(ProjectService projectService)
    {
        this.projectService = projectService;
    }
    
    @InitBinder
    public void initBinder(ServletRequestDataBinder binder)
    {              
        binder.registerCustomEditor(String.class, new TrimStringPropertyEditor());
    }

    @RequestMapping("/ajax/getPotentialProjectList")
    public String getPotentialProjectList(@RequestParam("resultId") String resultId, Model model) 
        throws RepositoryException 
    {
        List<ProjectDTO> potentialProjects = projectService.getPotentialProjects(resultId);
        
        model.addAttribute("projectList", potentialProjects);
        
        model.addAttribute("resultId", resultId);
        
        return "ajaxPotentialProjectList";
    }
    

    @RequestMapping("/ajax/addProjects")
    public String addProjects(@RequestParam("resultId") String resultId, 
            @RequestParam(value = "projectId", required = false) List<Integer> projectIdList, Model model) 
        throws RepositoryException 
    {
        LOG.info("Adding projects of ids " + projectIdList);

        
        if (projectIdList != null)
        {
            projectService.addProjectToResult(resultId, projectIdList);
            model.addAttribute(PROJECT_ADDED_ATTR, true);
        } else
        {
            model.addAttribute(PROJECT_ADDED_ATTR, false);
        }
        
        
        return this.viewAjaxProjectListSetup(resultId, model);
    }

    @RequestMapping("/ajax/removeProject")
    public String removeProject(@RequestParam("resultId") String resultId, 
            @RequestParam("projectId") Integer projectId, Model model) 
        throws RepositoryException 
    {
        projectService.removeProjectFromResult(resultId, projectId);
    
        return this.viewAjaxProjectListSetup(resultId, model);
    }
    
    private String viewAjaxProjectListSetup(String resultId, Model model) 
        throws RepositoryException
    {
        List<ProjectDTO> allProjects = projectService.getProjectsForResult(resultId);
        
        model.addAttribute("resultProjectList", allProjects);
        model.addAttribute("resultId", resultId);
        
        return "ajaxProjectList";
    }
    
}
