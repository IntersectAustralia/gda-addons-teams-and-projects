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

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.manager.ProjectManager;
import au.org.intersect.gda.repository.RepositoryException;

/**
 * @version $Rev$
 * 
 */
@Transactional(propagation = Propagation.REQUIRED)
public class ProjectServiceImpl implements ProjectService
{
    private final ProjectManager projectManager;
    
    public ProjectServiceImpl(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#addProjectToResult(java.lang.String, java.util.List)
     */
    @Override
    public String addProjectToResult(String resultId, List<Integer> projectIdList)
    {
        return projectManager.addProjectToResult(resultId, projectIdList);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService
     *      #createProject(au.org.intersect.gda.dto.ProjectDTO, java.util.Set, java.util.List, java.util.List)
     */
    @Override
    public Integer createProject(ProjectDTO projectDTO, Set<String> members, List<String> resultIdList,
            List<Integer> teamIdList)
    {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        return projectManager.createProject(projectDTO, members, resultIdList, teamIdList, currentUser);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#deleteProject(java.lang.Integer)
     */
    @Override
    public void deleteProject(Integer projectId)
    {
        projectManager.deleteProject(projectId);        
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#getAllProjects()
     */
    @Override
    public Set<ProjectDTO> getAllProjects() throws RepositoryException
    {
        return projectManager.getAllProjects();
    }
    

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#getAllProjectsBasicInfoOnly()
     */
    @Override
    public Set<ProjectDTO> getAllProjectsBasicInfoOnly() throws RepositoryException
    {
        return projectManager.getAllProjectsBasicInfoOnly();
    }


    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#getPotentialProjects(java.lang.String)
     */
    @Override
    public List<ProjectDTO> getPotentialProjects(String resultId)
    {
        return projectManager.getPotentialProjects(resultId);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#getProject(java.lang.Integer)
     */
    @Override
    public ProjectDTO getProject(Integer projectId) throws RepositoryException
    {
        return projectManager.getProject(projectId);
    }
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#getProjectBasicInfoOnly(java.lang.Integer)
     */
    @Override
    public ProjectDTO getProjectBasicInfoOnly(Integer projectId) throws RepositoryException
    {
        return projectManager.getProjectBasicInfoOnly(projectId);

    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#getProjectsForResult(java.lang.String)
     */
    @Override
    public List<ProjectDTO> getProjectsForResult(String resultId)
    {
        return projectManager.getProjectsForResult(resultId);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#removeProjectFromResult(java.lang.String, java.lang.Integer)
     */
    @Override
    public Integer removeProjectFromResult(String resultId, Integer projectId)
    {
        return projectManager.removeProjectFromResult(resultId, projectId);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService
     *      #updateProject(
     *          java.lang.Integer, 
     *          au.org.intersect.gda.dto.ProjectDTO, java.util.Set, 
     *          java.util.List, java.util.List)
     */
    @Override
    public Integer updateProject(Integer projectId, ProjectDTO projectDTO, 
                                 Set<String> members, List<String> resultIdList,
                                 List<Integer> teamIdList)
    {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        return projectManager.updateProject(projectId, projectDTO, members, resultIdList, teamIdList, currentUser);
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#updateProjectResultOnly(java.lang.Integer, java.util.List)
     */
    @Override
    public Integer updateProjectResultOnly(Integer projectId, List<String> resultIdList)
    {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        return projectManager.updateProjectResultOnlyForUser(projectId, resultIdList, currentUser);
    }

   


}
