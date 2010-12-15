/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.service;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;

import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.repository.RepositoryException;

/**
 * @version $Rev$
 * 
 */
public interface ProjectService
{
    @PostFilter("hasPermission(filterObject, 'VIEW')")
    Set<ProjectDTO> getAllProjects() throws RepositoryException;

    @PostFilter("hasPermission(filterObject, 'VIEW')")
    Set<ProjectDTO> getAllProjectsBasicInfoOnly() throws RepositoryException;

    @PreAuthorize("hasAnyRole('EDIT_OWN_PROJECTS','EDIT_ALL_PROJECTS')")
    @PreFilter(value = "hasPermission(filterObject, 'result', 'EDIT')", 
            filterTarget = "resultIdList")
    Integer createProject(ProjectDTO projectDTO, 
            Set<String> members, 
            List<String> resultIdList,
            List<Integer> teamIdList);

    @PreAuthorize("hasPermission(#projectId, 'project', 'VIEW')")
    ProjectDTO getProject(Integer projectId) throws RepositoryException;
    
    @PreAuthorize("hasPermission(#projectId, 'project', 'VIEW')")
    ProjectDTO getProjectBasicInfoOnly(Integer projectId) throws RepositoryException;

    @PreAuthorize("hasPermission(#projectId, 'project', 'EDIT')")
    @PreFilter(value = "hasPermission(filterObject, 'result', 'EDIT')", 
            filterTarget = "resultIdList")
    Integer updateProject(Integer projectId, ProjectDTO projectDTO, 
            Set<String> members, 
            List<String> resultIdList,
            List<Integer> teamIdList);

    @PreAuthorize("hasPermission(#projectId, 'project', 'VIEW')")
    @PreFilter(value = "hasPermission(filterObject, 'result', 'EDIT')", 
            filterTarget = "resultIdList")
    Integer updateProjectResultOnly(Integer projectId, List<String> resultIdList);
    
    @PreAuthorize("hasPermission(#projectId, 'project', 'DELETE')")
    void deleteProject(Integer projectId);
    
    @PreAuthorize("hasPermission(#resultId, 'result', 'VIEW')")
    List<ProjectDTO> getProjectsForResult(String resultId);    
    
    @PreAuthorize("hasPermission(#resultId, 'result', 'VIEW')")
    @PostFilter(value = "hasPermission(filterObject, 'VIEW')")   
    List<ProjectDTO> getPotentialProjects(String resultId);
    
    @PreAuthorize("hasPermission(#resultId, 'result', 'EDIT')")
    @PreFilter(value = "hasPermission(filterObject, 'project', 'VIEW')", 
            filterTarget = "projectIdList")            
    String addProjectToResult(String resultId, List<Integer> projectIdList);
    
    @PreAuthorize("hasPermission(#resultId, 'result', 'EDIT')")
    Integer removeProjectFromResult(String resultId, Integer projectId);
    
}
