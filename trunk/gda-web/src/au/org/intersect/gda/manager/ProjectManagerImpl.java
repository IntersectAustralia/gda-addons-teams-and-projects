/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.gda.assembler.ProjectAssembler;
import au.org.intersect.gda.dao.GdaUserDAO;
import au.org.intersect.gda.dao.GdaUserNotFoundException;
import au.org.intersect.gda.dao.ProjectDAO;
import au.org.intersect.gda.dao.ProjectNotFoundException;
import au.org.intersect.gda.dao.ResultDAO;
import au.org.intersect.gda.dao.ResultNotFoundException;
import au.org.intersect.gda.dao.TeamDAO;
import au.org.intersect.gda.dao.TeamNotFoundException;
import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.ProjectStatus;
import au.org.intersect.gda.domain.Result;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultInfoDTO;
import au.org.intersect.gda.oai.ProjectOaiUpdater;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.repository.ResultRepositoryManager;
import au.org.intersect.gda.security.ResultSecurityHelper;
import au.org.intersect.gda.util.SystemException;

/**
 * @version $Rev$
 *
 */
@Transactional
public class ProjectManagerImpl implements ProjectManager
{
    private static final Logger LOG = Logger.getLogger(ProjectManagerImpl.class);

    private static final String RESULT_NOT_FOUND_MSG = "Could not find result with id ";

    private final ProjectDAO projectDAO;
    private final ResultDAO resultDAO;
    private final TeamDAO teamDAO;
    private final ProjectAssembler projectAssembler;
    private final GdaUserDAO userDAO;
    private final ResultRepositoryManager resultRepositoryManager;
    private final ProjectOaiUpdater projectOaiUpdater;
    private final ResultSecurityHelper resultSecurityHelper;

    public ProjectManagerImpl(ProjectDAO projectDAO, 
            ProjectAssembler projectAssembler, 
            GdaUserDAO userDAO, 
            ResultRepositoryManager resultRepositoryManager,
            ResultDAO resultDAO,
            TeamDAO teamDAO,
            ResultSecurityHelper resultSecurityHelper,
            ProjectOaiUpdater projectOaiUpdaterHelper)
    {
        super();
        this.projectDAO = projectDAO;
        this.projectAssembler = projectAssembler;
        this.userDAO = userDAO;
        this.resultRepositoryManager = resultRepositoryManager;
        this.resultDAO = resultDAO;
        this.teamDAO = teamDAO;
        this.projectOaiUpdater = projectOaiUpdaterHelper;
        this.resultSecurityHelper = resultSecurityHelper;
    }

    @Override
    public Set<ProjectDTO> getAllProjects() throws RepositoryException
    {
        Set<Project> projectsByOwner = projectDAO.getAllProjects();
        
              
        Set<ProjectDTO> projectDTOs = new HashSet<ProjectDTO>();
        for (Project project : projectsByOwner)
        {
            List<ResultInfoDTO> resultList = getResultDtoForProject(project);
                        
            ProjectDTO dto = projectAssembler.createProjectDTO(project, resultList);
            
            
            projectDTOs.add(dto);
        }        
        
        return projectDTOs;
    }
    

    /* (non-Javadoc)
     * @see au.org.intersect.gda.manager.ProjectManager#getAllProjectsBasicInfoOnly()
     */
    @Override
    public Set<ProjectDTO> getAllProjectsBasicInfoOnly()
    {
        Set<Project> projectsByOwner = projectDAO.getAllProjects();
        
        
        Set<ProjectDTO> projectDTOs = new HashSet<ProjectDTO>();
        for (Project project : projectsByOwner)
        {                        
            ProjectDTO dto = projectAssembler.createProjectDTO(project, new ArrayList<ResultInfoDTO>());
            
            projectDTOs.add(dto);
        }        
        
        return projectDTOs;
    }

    @Override
    public Integer createProject(ProjectDTO projectDTO, 
            Set<String> members, 
            List<String> resultIdList,
            List<Integer> teamIdList, 
            String username)
    {
        return this.createProjectCommon(
                projectDTO, members, resultIdList, teamIdList, username, ProjectStatus.PROCESSED);
    }
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.manager.ProjectManager#createProject(
     *      au.org.intersect.gda.dto.ProjectDTO, 
     *      java.util.Set, 
     *      java.util.List, 
     *      java.util.List, 
     *      java.lang.String, au.org.intersect.gda.domain.ProjectStatus)
     */
    @Override
    public Integer createProject(ProjectDTO projectDto, Set<String> members, List<String> resultIdList,
            List<Integer> teamIdList, String username, ProjectStatus status)
    {
        return this.createProjectCommon(projectDto, members, resultIdList, teamIdList, username, status);
    }
    
    private Integer createProjectCommon(ProjectDTO projectDto, Set<String> members, List<String> resultIdList,
            List<Integer> teamIdList, String username, ProjectStatus status)
    {
        GdaUser owner;
        try
        {
            owner = userDAO.getUser(username);
            Project project = projectAssembler.createProjectFromDTO(projectDto, owner);
            setMembers(project, members);
            setResults(project, resultIdList, username);
            setTeams(project, teamIdList);
            project.setStatus(status);
            Project persistedProject = projectDAO.persist(project);   
            updateResultsInFedora(resultIdList, persistedProject.getId().toString());
            projectOaiUpdater.onProjectCreate(persistedProject.getId());            
            return persistedProject.getId();
        } catch (GdaUserNotFoundException e)
        {
            throw new SystemException("Could not find user object for logged in user " + username);
        }
    }


    @Override
    public ProjectDTO getProject(Integer projectId) throws RepositoryException
    {
        Project project;
        List<ResultInfoDTO> resultList = null;
        try
        {
            project = projectDAO.getProject(projectId);
            resultList = getResultDtoForProject(project);
        } catch (ProjectNotFoundException e)
        {
            throw new SystemException(buildNotFoundMessage(projectId));
        }
        
        return projectAssembler.createProjectDTO(project, resultList);
    }
    

    /* (non-Javadoc)
     * @see au.org.intersect.gda.manager.ProjectManager#getProjectBasicInfoOnly(java.lang.Integer)
     */
    @Override
    public ProjectDTO getProjectBasicInfoOnly(Integer projectId)
    {
        Project project;
        List<ResultInfoDTO> resultList = new ArrayList<ResultInfoDTO>();
        try
        {
            project = projectDAO.getProject(projectId);
        } catch (ProjectNotFoundException e)
        {
            throw new SystemException(buildNotFoundMessage(projectId));
        }
        
        return projectAssembler.createProjectDTO(project, resultList);
    }
    

    @Override
    public Integer updateProject(Integer projectId, 
            ProjectDTO projectDTO, 
            Set<String> members, 
            List<String> resultIdList,
            List<Integer> teamIdList,
            String username)
    {       
        try
        {
            Project project = projectDAO.getProject(projectId);
            
            this.enforceProjectNotInProcess(project);
            
            updateResultNotInList(resultIdList, project, username);
            projectAssembler.updateProjectFromDTO(project, projectDTO);
            updateMembers(members, project);
            setResults(project, resultIdList, username);
            setTeams(project, teamIdList);
            project.preUpdate();
            projectDAO.persist(project);
            updateResultsInFedora(resultIdList, projectId.toString());
            projectOaiUpdater.onProjectUpdate(project.getId());  
            return project.getId();
        } catch (ProjectNotFoundException e)
        {
            throw new SystemException(buildNotFoundMessage(projectId));
        }
    }
    

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#updateProjectResultOnly(java.lang.Integer, java.util.List)
     */
    @Override
    public Integer updateProjectResultOnlyForUser(Integer projectId, List<String> resultIdList, String username)
    {       
        try
        {            
            Project project = projectDAO.getProject(projectId);
            
            enforceProjectNotInProcess(project);
            
            updateResultNotInList(resultIdList, project, username);    
            setResults(project, resultIdList, username);
            project.preUpdate();
            projectDAO.persist(project);
            updateResultsInFedora(resultIdList, projectId.toString());
            projectOaiUpdater.onProjectUpdate(project.getId());           
            return project.getId();
        } catch (ProjectNotFoundException e)
        {
            throw new SystemException(buildNotFoundMessage(projectId));
        }        
    }
    
    private void updateResultsInFedora(List<String> resultIdList, String projectId)
    {
        LOG.info("Updating project id " + projectId + "for results in fedora");
        if (resultIdList == null)
        {
            LOG.info("Result id list is null");
            return;
        }            
        for (int i = 0; i < resultIdList.size(); i++)
        {
            String resId = resultIdList.get(i);
            updateResultsInFedora(resId, projectId);
        }
    }
    
    private void updateResultsInFedora(String resultId, String projectId)
    {  
        LOG.info("Updating result in fedora for " + resultId);
        try
        {
            resultRepositoryManager.addProjectId(resultId, projectId);
        } catch (RepositoryException e)
        {
            throw new SystemException("Cannot update project id " + projectId + " for result " + resultId);
        }
    }

    private void updateResultNotInList(List<String> resultIdList, Project project, String currentUser)
    {
        // Get a list of results belongs to this project in db
        try
        {
            Set<Result> results = project.getResults();
            if (results == null || results.isEmpty())
            {
                LOG.info("No previous results to update");
                return;
            }
            LOG.info("Results has " + results.size());
            removeProjIdFromOrphanedResult(resultIdList, project, results, currentUser);
            
        } catch (RepositoryException e)
        {
            throw new SystemException("Cannot update project id in fedora");
        }
    }

    private void removeProjIdFromOrphanedResult(List<String> resultIdList,
        Project project, Set<Result> results, String currentUser) 
        throws RepositoryException 
    {
        Iterator<Result> itr = results.iterator();
        while (itr.hasNext())
        {
            Result res = itr.next();
            String resId = res.getResultId();
            LOG.info("Checking result " + resId + " now");
            Integer projectId = project.getId();
            if (!resultIdList.contains(resId) && projectId != null)
            {
                // if there are any in the db list and not in resultIdList
                // (for current user only) then for those results, 
                // delete the project ID
                removeIfBelongsToCurrentUser(currentUser, resId, projectId);               
            }
        }
    }

    private void removeIfBelongsToCurrentUser(String currentUser, String resId, Integer projectId)
        throws RepositoryException
    {
        ResultDTO resDTO = resultRepositoryManager.getResult(resId);
        if (resDTO.getOwner().equals(currentUser))
        {
            LOG.info("Removing project id " + projectId + " from result " + resId);
            resultRepositoryManager.removeProjectId(resId, projectId.toString());
        }
    }   
    
    private void updateMembers(Set<String> members, Project project)
    {
        Set<GdaUser> originalMembers = project.getMembers();
        setMembers(project, members);
        addOriginalInactiveMembers(project, originalMembers);
    }
    
    private void setTeams(Project project, List<Integer> teamIdList)
    {
        Set<Team> newTeams = new HashSet<Team>();
        if (teamIdList != null)
        {
            for (Integer teamId : teamIdList)
            {
                try
                {
                    Team team = teamDAO.getTeam(teamId);
                    newTeams.add(team);
                } catch (TeamNotFoundException e)
                {
                    throw new SystemException("Could not retrieve team " + teamId);
                }
            }
        }
        project.setTeams(newTeams);
    }

    // add inactive users into the project (they were not selected in the UI)
    private void addOriginalInactiveMembers(Project project, Set<GdaUser> originalMembers)
    {
        for (GdaUser user : originalMembers)
        {
            if (!user.isActive())
            {
                project.getMembers().add(user);
            }
        }
    }

    @Override
    public void deleteProject(Integer projectId)
    {
        Project project;
        try
        {
            project = projectDAO.getProject(projectId);
            
            this.enforceProjectNotInProcess(project);
            
            projectOaiUpdater.onDeleteProject(projectId);
            
            updateDeletedProjectInFedora(project.getResults(), projectId);
            projectDAO.delete(project);
                        
        } catch (ProjectNotFoundException e)
        {
            throw new SystemException(buildNotFoundMessage(projectId));
        }
    }
    
    private void updateDeletedProjectInFedora(Set<Result> results, Integer projectId)
    {
        if (results == null)
        {
            return;
        }
        Iterator<Result> itr = results.iterator();
        while (itr.hasNext())
        {
            Result result = itr.next();
            String resId = result.getResultId();
            updateDeletedProjectInFedora(resId, projectId);        
        }        
    }
    
    private void updateDeletedProjectInFedora(String resId, Integer projectId)
    {
        try
        {
            resultRepositoryManager.removeProjectId(resId, projectId.toString());
        } catch (RepositoryException e)
        {
            throw new SystemException("Cannot delete project id from result " + resId);
        } 
    }
    
    private void setMembers(Project project, Set<String> members)
    {
        Set<GdaUser> memberObjects = new HashSet<GdaUser>();
        for (String username : members)
        {
            try
            {
                memberObjects.add(userDAO.getUser(username));
            } catch (GdaUserNotFoundException e)
            {
                LOG.error("Could not find user with username " + username
                        + "Will ignore and continue with other project members but this may indicate a problem");
            }
        }
        project.setMembers(memberObjects);
    }
    

    private void setResults(Project project, List<String> resultIdList, String username)
    {                      
        Set<Result> existingResults = project.getResults();
        Set<Result> remainingResults;
      
        remainingResults = new HashSet<Result>(existingResults);
        
        List<String> inputResults;
        List<String> remainingInputResults;
        if (resultIdList == null)
        {
            inputResults = new ArrayList<String>();
            remainingInputResults = new ArrayList<String>();
        } else
        {
            inputResults = resultIdList;
            remainingInputResults = new ArrayList<String>(inputResults);
        } 
        removeUnselectedOwnerResults(existingResults, inputResults, remainingResults, remainingInputResults, username);
        
        Set<Result> resultsTopersist = addSelectedNewResults(remainingInputResults, remainingResults);
        
        project.setResults(resultsTopersist);
    }
    
    private Set<Result> addSelectedNewResults(List<String> newResults, Set<Result> resultsToPersist)
    {
        //for each remaining input result, ie, new results, add it to the set to persist
        for (String resultId : newResults)
        {           
            try
            {
                Result resultToAdd = resultDAO.getResult(resultId);
                resultsToPersist.add(resultToAdd);
            } catch (ResultNotFoundException e)
            {
                throw new SystemException(RESULT_NOT_FOUND_MSG + resultId);
            }                
        }           
        return resultsToPersist;
    }
    
    private void removeUnselectedOwnerResults(
            Set<Result> existingResults, List<String> inputResults,
            Set<Result> remainingResults, List<String> remainingInputResults, String username)
    {

        //remove all existing results that belongs to the user
        //and is not in the input list
        for (Result result : existingResults)
        {            
            
            String resultId = result.getResultId();
            try
            {
                
                if (inputResults.contains(resultId))
                {
                    //the input list contains this, retain it
                    remainingInputResults.remove(resultId);
                    continue;
                }
                ResultDTO resultDTO = resultRepositoryManager.getResult(resultId);

                //result is not in the input list
                //if it belongs to the current owner then we
                //remove it from the existing list
                //if it belongs to other users we keep it
                if (username.equals(resultDTO.getOwner()))
                {
                    remainingResults.remove(result);
                }
            } catch (RepositoryException e)
            {
                
                throw new SystemException(RESULT_NOT_FOUND_MSG + resultId, e);
            }
        }
    }
    
    private List<ResultInfoDTO> getResultDtoForProject(Project project) throws RepositoryException
    {   
        if (LOG.isInfoEnabled())
        {
            LOG.info("Retrieving results for project " + project.getId());            
        }
        
        List<ResultInfoDTO> resultList = new ArrayList<ResultInfoDTO>();
        
                                
        for (Result result : project.getResults())
        {
            ResultDTO resultDto = resultRepositoryManager.getResult(result.getResultId());
            
            ResultInfoDTO resultInfo = getOwnershipInfoFromResult(resultDto);
            resultList.add(resultInfo);
        }
    
        
        return resultList;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#getProjectsForResult(java.lang.String)
     */
    @Override
    public List<ProjectDTO> getProjectsForResult(String resultId)
    {
        try
        {
            Result result = resultDAO.getResult(resultId);
            
            Set<Project> projectList = result.getProjects();
            
            List<ProjectDTO> projectDTOList = new ArrayList<ProjectDTO>();
                
            for (Project project : projectList)
            {
                ProjectDTO projectDTO = projectAssembler.createProjectDTO(project);
                projectDTOList.add(projectDTO);
            }      
            
            return projectDTOList;
            
        } catch (ResultNotFoundException e)
        {
            throw new SystemException(RESULT_NOT_FOUND_MSG + resultId);
        }
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#getPotentialProjects(java.lang.String)
     */
    @Override
    public List<ProjectDTO> getPotentialProjects(String resultId)
    {
        Set<Project> allProjectEntities = projectDAO.getAllProjects();
        
        Result result;
        try
        {
            result = resultDAO.getResult(resultId);
        } catch (ResultNotFoundException e)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Unable to retrieve result [");
            buffer.append(resultId);
            buffer.append("]");
            throw new SystemException(buffer.toString(), e);
        }
        
        Set<Project> existingProjects = result.getProjects();
        
        allProjectEntities.removeAll(existingProjects);
        
        List<ProjectDTO> projectDTOList = new ArrayList<ProjectDTO>();
        
        for (Project project : allProjectEntities)
        {
            
            ProjectDTO dto = projectAssembler.createProjectDTO(project);
            
            projectDTOList.add(dto);
        }        
                
        
        return projectDTOList;
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#addProject(java.lang.String, java.util.List)
     */
    @Override
    public String addProjectToResult(String resultId, List<Integer> projectIdList)
    {
        try
        {
            Result result = resultDAO.getResult(resultId);
            
            Set<Project> existingProjectList = result.getProjects();
                        
            for (Integer projectId : projectIdList)
            {
                Project project;
                try
                {
                    project = projectDAO.getProject(projectId);
                    existingProjectList.add(project);
                    project.addResult(result);
                    updateResultsInFedora(resultId, projectId.toString());
                } catch (ProjectNotFoundException e)
                {                                
                    throw new SystemException(buildNotFoundMessage(projectId));                 
                }
                
            }
            
            result.setProjects(existingProjectList);
            
            resultDAO.persist(result);            
            
            projectOaiUpdater.onUpdateMultipleProject(projectIdList);

            return result.getResultId();
            
        } catch (ResultNotFoundException e)
        {
            throw new SystemException(RESULT_NOT_FOUND_MSG + resultId);
        } 
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.service.ProjectService#removeProject(java.lang.String, java.lang.Integer)
     */
    @Override
    public Integer removeProjectFromResult(String resultId, Integer projectId)
    {
        try
        {
            Result result = resultDAO.getResult(resultId);
            
            Set<Project> existingProjectList = result.getProjects();
                        
            Project projectToRemove = null;
            for (Project project : existingProjectList)
            {                
                if (project.getId().equals(projectId))
                {
                    projectToRemove = project;
                    project.removeResult(result);
                    updateDeletedProjectInFedora(result.getResultId(), projectId);
                    break;
                }
            }
            
            if (projectToRemove == null)
            {
                throw new SystemException(buildNotFoundMessage(projectId));
            }
            
            existingProjectList.remove(projectToRemove);
            
            result.setProjects(existingProjectList);
                        
            resultDAO.persist(result);
            
            projectOaiUpdater.onProjectUpdate(projectId);
            return projectId;
        } catch (ResultNotFoundException e)
        {
            throw new SystemException(RESULT_NOT_FOUND_MSG + resultId);
        }
    }

    private String buildNotFoundMessage(Integer projectId)
    {
        String notFound = "Could not find project with id ";
        String deleted = ". Are you sure it has not been deleted?";
        
        StringBuffer buffer = new StringBuffer();
        buffer.append(notFound);
        buffer.append(projectId);
        buffer.append(deleted);
        return buffer.toString();
    }

    /* (non-Javadoc)
     * @see au.org.intersect.gda.manager.ProjectManager#updateProjectStatus(
     *      java.lang.Integer, au.org.intersect.gda.domain.ProjectStatus)
     */
    @Override
    public void updateProjectStatus(Integer projectId, ProjectStatus status)
    {
        try
        {
            Project project = projectDAO.getProject(projectId);
            
            project.setStatus(status);
            projectDAO.persist(project);
            
            projectOaiUpdater.onProjectUpdate(projectId);
            
        } catch (ProjectNotFoundException e)
        {
            throw new SystemException(buildNotFoundMessage(projectId));
        }
    }

    private void enforceProjectNotInProcess(Project project)
    {
        if (project.getStatus() == ProjectStatus.PROCESSING)
        {
            throw new SystemException("Modifying project in process is now allowed");
        }
    }

    private ResultInfoDTO getOwnershipInfoFromResult(ResultDTO result)
    {
        
        Boolean canChangeOwner = resultSecurityHelper.getResultAccessDTOForCurrentUser(result).isCanChangeOwner();
        ResultInfoDTO resultInfo = new ResultInfoDTO(result, canChangeOwner);
    
        return resultInfo;
    }
}
