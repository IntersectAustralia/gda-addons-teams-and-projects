/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;

import au.org.intersect.gda.assembler.ProjectAssembler;
import au.org.intersect.gda.dao.GdaUserDAO;
import au.org.intersect.gda.dao.ProjectDAO;
import au.org.intersect.gda.dao.ProjectNotFoundException;
import au.org.intersect.gda.dao.ResultDAO;
import au.org.intersect.gda.dao.ResultNotFoundException;
import au.org.intersect.gda.dao.TeamDAO;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Result;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.manager.ProjectManager;
import au.org.intersect.gda.manager.ProjectManagerImpl;
import au.org.intersect.gda.oai.ProjectOaiUpdater;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.repository.ResultRepositoryManager;
import au.org.intersect.gda.security.ResultSecurityHelper;
import au.org.intersect.gda.util.MockAuthentication;
import au.org.intersect.gda.util.SystemException;

/**
 * @version $Rev$
 *
 */
public class ProjectServiceImplResultProjectTest
{
    @Mock
    private ProjectDAO projectDAO;
    

    @Mock
    private ResultDAO resultDAO;
    

    @Mock
    private TeamDAO teamDAO;

    @Mock
    private ProjectAssembler assembler;

    @Mock
    private GdaUserDAO userDao;
    
    @Mock
    private ResultRepositoryManager repositoryManager;

    @Mock
    private ProjectOaiUpdater projectOaiUpdater;
    
    @Mock
    private ResultSecurityHelper resultSecurityHelper;
    
    private ProjectServiceImpl service;


    private String currentUser = "user";

    @Before
    public void setUp() throws RepositoryException
    {
        MockitoAnnotations.initMocks(this);
        
        ProjectManager manager = new ProjectManagerImpl(
                projectDAO, 
                assembler, 
                userDao, 
                repositoryManager, 
                resultDAO, 
                teamDAO,
                resultSecurityHelper,
                projectOaiUpdater);
        service = new ProjectServiceImpl(manager);
        SecurityContextHolder.getContext().setAuthentication(new MockAuthentication(currentUser));
        
    }
    
    @Test
    public void testGetPotentialProject() throws ResultNotFoundException
    {
        Integer project1Id = 1;
        Integer project2Id = 2;
        Integer project3Id = 3;
        
        String resultId = "result1";
        
        Set<Project> allPersistedProjects = new HashSet<Project>();
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        allPersistedProjects.add(project1);
        
        Project project2 = new Project();
        project2.setId(project2Id);
        allPersistedProjects.add(project2);
        
        Project project3 = new Project();
        project3.setId(project3Id);
        allPersistedProjects.add(project3);
        
        //dto
        ProjectDTO project2DTO = new ProjectDTO();
        project2DTO.setId(project2Id);
        
        ProjectDTO project3DTO = new ProjectDTO();
        project3DTO.setId(project3Id);
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        
        result.setProjects(existingResultProjects);
        
        when(projectDAO.getAllProjects()).thenReturn(allPersistedProjects);

        when(resultDAO.getResult(resultId)).thenReturn(result);
        
        
        when(assembler.createProjectDTO(project2)).thenReturn(project2DTO);
        when(assembler.createProjectDTO(project3)).thenReturn(project3DTO);
        
        
        List<ProjectDTO> returnedList = service.getPotentialProjects(resultId);
        verify(assembler).createProjectDTO(eq(project2));
        verify(assembler).createProjectDTO(project3);
        
        verify(assembler, never()).createProjectDTO(project1);
        
        assertEquals(2, returnedList.size());
        getProjectDTOFromList(project2Id, returnedList);
        getProjectDTOFromList(project3Id, returnedList);
    }
    
    @Test(expected = SystemException.class)
    public void testGetPotentialProjectException() throws ResultNotFoundException
    {
        Integer project1Id = 1;
        Integer project2Id = 2;
        Integer project3Id = 3;
        
        String resultId = "result1";
        
        Set<Project> allPersistedProjects = new HashSet<Project>();
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        allPersistedProjects.add(project1);
        
        Project project2 = new Project();
        project2.setId(project2Id);
        allPersistedProjects.add(project2);
        
        Project project3 = new Project();
        project3.setId(project3Id);
        allPersistedProjects.add(project3);
        
        //dto
        ProjectDTO project2DTO = new ProjectDTO();
        project2DTO.setId(project2Id);
        
        ProjectDTO project3DTO = new ProjectDTO();
        project3DTO.setId(project3Id);
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        
        result.setProjects(existingResultProjects);
        
        when(projectDAO.getAllProjects()).thenReturn(allPersistedProjects);

        when(resultDAO.getResult(resultId)).thenThrow(new ResultNotFoundException("mock exception"));
        
        
        when(assembler.createProjectDTO(project2)).thenReturn(project2DTO);
        when(assembler.createProjectDTO(project3)).thenReturn(project3DTO);
        
        
        service.getPotentialProjects(resultId);
    }
    
    @Test
    public void testGetProjectsForResult() throws ResultNotFoundException
    {
        Integer project1Id = 1;
        Integer project2Id = 2;
        
        String resultId = "result1";
        
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        
        Project project2 = new Project();
        project2.setId(project2Id);
        
        
        //dto
        ProjectDTO project1DTO = new ProjectDTO();
        project1DTO.setId(project1Id);
        
        ProjectDTO project2DTO = new ProjectDTO();
        project2DTO.setId(project2Id);
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        existingResultProjects.add(project2);
        
        result.setProjects(existingResultProjects);
        

        when(resultDAO.getResult(resultId)).thenReturn(result);
        
        when(assembler.createProjectDTO(project1)).thenReturn(project1DTO);
        when(assembler.createProjectDTO(project2)).thenReturn(project2DTO);
        
        
        
        List<ProjectDTO> returnedList = service.getProjectsForResult(resultId);
        verify(assembler).createProjectDTO(project1);
        verify(assembler).createProjectDTO(project2);        
        
        assertEquals(2, returnedList.size());
        getProjectDTOFromList(project1Id, returnedList);
        getProjectDTOFromList(project2Id, returnedList);        
    }
    
    @Test(expected = SystemException.class)
    public void testGetProjectsForResultNotFound() throws ResultNotFoundException
    {
        Integer project1Id = 1;
        Integer project2Id = 2;
        
        String resultId = "result1";
        
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        
        Project project2 = new Project();
        project2.setId(project2Id);
        
        
        //dto
        ProjectDTO project1DTO = new ProjectDTO();
        project1DTO.setId(project1Id);
        
        ProjectDTO project2DTO = new ProjectDTO();
        project2DTO.setId(project2Id);
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        existingResultProjects.add(project2);
        
        result.setProjects(existingResultProjects);
        

        when(resultDAO.getResult(resultId)).thenThrow(new ResultNotFoundException("mock exception"));

        
        service.getProjectsForResult(resultId);
       
    }
    
    @Test
    public void testAddProjectToResult() throws ResultNotFoundException, ProjectNotFoundException
    {
        Integer project1Id = 1;
        Integer project2Id = 2;
        Integer project3Id = 3;
        
        String resultId = "result1";
        
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        
        Project project2 = new Project();
        project2.setId(project2Id);
        
        Project project3 = new Project();
        project3.setId(project3Id);
        
        //dto
        ProjectDTO project2DTO = new ProjectDTO();
        project2DTO.setId(project2Id);
        
        ProjectDTO project3DTO = new ProjectDTO();
        project3DTO.setId(project3Id);
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        
        result.setProjects(existingResultProjects);
        

        when(resultDAO.getResult(resultId)).thenReturn(result);
        
        
        when(projectDAO.getProject(project2Id)).thenReturn(project2);
        when(projectDAO.getProject(project3Id)).thenReturn(project3);
        
        List<Integer> projectIdList = new ArrayList<Integer>();
        projectIdList.add(project2Id);
        projectIdList.add(project3Id);
        
        service.addProjectToResult(resultId, projectIdList);
        
        verify(projectDAO).getProject(project2Id);
        verify(projectDAO).getProject(project3Id);
        
        verify(projectDAO, never()).getProject(project1Id);
        

        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(resultDAO).persist(resultCaptor.capture());
        
        Set<Project> projectsSentToPersist = resultCaptor.getValue().getProjects();
        
        assertEquals(3, projectsSentToPersist.size());
        getProjectFromCollection(project1Id, projectsSentToPersist);
        getProjectFromCollection(project2Id, projectsSentToPersist);
        getProjectFromCollection(project3Id, projectsSentToPersist);
    }
    
    @Test(expected = SystemException.class)
    public void testAddProjectToResultProjectException() throws ResultNotFoundException, ProjectNotFoundException
    {
        Integer project1Id = 1;
        Integer project2Id = 2;
        Integer project3Id = 3;
        
        String resultId = "result1";
        
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        
        Project project2 = new Project();
        project2.setId(project2Id);
        
        Project project3 = new Project();
        project3.setId(project3Id);
        
        //dto
        ProjectDTO project2DTO = new ProjectDTO();
        project2DTO.setId(project2Id);
        
        ProjectDTO project3DTO = new ProjectDTO();
        project3DTO.setId(project3Id);
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        
        result.setProjects(existingResultProjects);
        

        when(resultDAO.getResult(resultId)).thenReturn(result);
        
        
        when(projectDAO.getProject(project2Id)).thenThrow(new ProjectNotFoundException(project2Id));
        
        
        List<Integer> projectIdList = new ArrayList<Integer>();
        projectIdList.add(project2Id);
        projectIdList.add(project3Id);
        
        service.addProjectToResult(resultId, projectIdList);
        
    }
    
    @Test(expected = SystemException.class)
    public void testAddProjectToResultResultException() throws ResultNotFoundException, ProjectNotFoundException
    {
        Integer project1Id = 1;
        Integer project2Id = 2;
        Integer project3Id = 3;
        
        String resultId = "result1";
        
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        
        Project project2 = new Project();
        project2.setId(project2Id);
        
        Project project3 = new Project();
        project3.setId(project3Id);
        
        //dto
        ProjectDTO project2DTO = new ProjectDTO();
        project2DTO.setId(project2Id);
        
        ProjectDTO project3DTO = new ProjectDTO();
        project3DTO.setId(project3Id);
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        
        result.setProjects(existingResultProjects);
        

        when(resultDAO.getResult(resultId)).thenThrow(new ResultNotFoundException(resultId));
        
        
        List<Integer> projectIdList = new ArrayList<Integer>();
        projectIdList.add(project2Id);
        projectIdList.add(project3Id);
        
        service.addProjectToResult(resultId, projectIdList);
        
    }
    
    @Test
    public void testRemoveProjectFromResult() throws ResultNotFoundException, ProjectNotFoundException
    {
        //test having 1,2,3 in initial result and remove 2
        Integer project1Id = 1;
        Integer project2Id = 2;
        Integer project3Id = 3;
        
        String resultId = "result1";
        
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        
        Project project2 = new Project();
        project2.setId(project2Id);
        
        Project project3 = new Project();
        project3.setId(project3Id);
        
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        existingResultProjects.add(project2);
        existingResultProjects.add(project3);
        
        result.setProjects(existingResultProjects);
        

        when(resultDAO.getResult(resultId)).thenReturn(result);
        
        
        service.removeProjectFromResult(resultId, project2Id);
        
        ArgumentCaptor<Result> resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(resultDAO).persist(resultCaptor.capture());
        
        Set<Project> projectsSentToPersist = resultCaptor.getValue().getProjects();
        
        assertEquals(2, projectsSentToPersist.size());
        getProjectFromCollection(project1Id, projectsSentToPersist);
        getProjectFromCollection(project3Id, projectsSentToPersist);
    }
    
    @Test(expected = SystemException.class)
    public void testRemoveProjectFromResultResultException() throws ResultNotFoundException, ProjectNotFoundException
    {
        //test having 1,2,3 in initial result and remove 2
        Integer project1Id = 1;
        Integer project2Id = 2;
        Integer project3Id = 3;
        
        String resultId = "result1";
        
        
        //entity
        Project project1 = new Project();
        project1.setId(project1Id);        
        
        Project project2 = new Project();
        project2.setId(project2Id);
        
        Project project3 = new Project();
        project3.setId(project3Id);
        
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        existingResultProjects.add(project1);
        existingResultProjects.add(project2);
        existingResultProjects.add(project3);
        
        result.setProjects(existingResultProjects);
        

        when(resultDAO.getResult(resultId)).thenThrow(new ResultNotFoundException("mock exception"));
        
        
        service.removeProjectFromResult(resultId, project2Id);
        
    }
    
    @Test(expected = SystemException.class)
    public void testRemoveProjectFromResultNoExistingResults() throws ResultNotFoundException, ProjectNotFoundException
    {
      //test having 1,2,3 in initial result and remove 2
        Integer project2Id = 2;
        
        String resultId = "result1";
        
        
        
        Result result = new Result();
        Set<Project> existingResultProjects = new HashSet<Project>();
        
        result.setProjects(existingResultProjects);
        
        when(resultDAO.getResult(resultId)).thenReturn(result);
                
        service.removeProjectFromResult(resultId, project2Id);
    }
    
    private ProjectDTO getProjectDTOFromList(Integer id, List<ProjectDTO> list)
    {
        for (ProjectDTO item : list)
        {
            if (id.equals(item.getId()))
            {
                return item;
            }
        }
        fail("Project with id " + id + " was not found");
        return null;
    }
    
    private Project getProjectFromCollection(Integer id, Set<Project> set)
    {
        for (Project item : set)
        {
            if (id.equals(item.getId()))
            {
                return item;
            }
        }
        fail("Project with id " + id + " was not found");
        return null;
    }
    
    
}
