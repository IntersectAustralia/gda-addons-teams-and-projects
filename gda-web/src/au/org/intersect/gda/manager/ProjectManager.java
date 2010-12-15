/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.manager;

import java.util.List;
import java.util.Set;

import au.org.intersect.gda.domain.ProjectStatus;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.repository.RepositoryException;

/**
 * @version $Rev$
 * 
 */
public interface ProjectManager
{
    Set<ProjectDTO> getAllProjects() throws RepositoryException;

    Integer createProject(ProjectDTO projectDTO, Set<String> members, List<String> resultIdList,
            List<Integer> teamIdList, String username);

    ProjectDTO getProject(Integer projectId) throws RepositoryException;

    Integer updateProject(Integer id, ProjectDTO projectDTO, Set<String> members, List<String> resultIdList,
            List<Integer> teamIdList, String username);

    Integer updateProjectResultOnlyForUser(Integer id, List<String> resultIdList, String username);

    void deleteProject(Integer projectId);

    List<ProjectDTO> getProjectsForResult(String resultId);

    List<ProjectDTO> getPotentialProjects(String resultId);

    String addProjectToResult(String resultId, List<Integer> projectIdList);

    Integer removeProjectFromResult(String resultId, Integer projectId);

    /**
     * @return
     */
    Set<ProjectDTO> getAllProjectsBasicInfoOnly();

    /**
     * @param projectId
     * @return
     */
    ProjectDTO getProjectBasicInfoOnly(Integer projectId);

    /**
     * @param projectDto
     * @param emptyMembers
     * @param emptyResults
     * @param emptyTeams
     * @param approvedBy
     * @param status
     * @return
     */
    Integer createProject(ProjectDTO projectDto, Set<String> emptyMembers, List<String> emptyResults,
            List<Integer> emptyTeams, String approvedBy, ProjectStatus status);

    /**
     * @param projectId
     * @param processed
     */
    void updateProjectStatus(Integer projectId, ProjectStatus status);

}
