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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
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
import au.org.intersect.gda.dao.GdaUserNotFoundException;
import au.org.intersect.gda.dao.ProjectDAO;
import au.org.intersect.gda.dao.ProjectNotFoundException;
import au.org.intersect.gda.dao.ResultDAO;
import au.org.intersect.gda.dao.ResultNotFoundException;
import au.org.intersect.gda.dao.TeamDAO;
import au.org.intersect.gda.dao.TeamNotFoundException;
import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Result;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.GdaUserDTO;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.dto.ResultAccessDTO;
import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.dto.ResultInfoDTO;
import au.org.intersect.gda.manager.ProjectManager;
import au.org.intersect.gda.manager.ProjectManagerImpl;
import au.org.intersect.gda.oai.ProjectOaiUpdater;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.repository.ResultRepositoryManager;
import au.org.intersect.gda.repository.fedora.FedoraException;
import au.org.intersect.gda.security.ResultSecurityHelper;
import au.org.intersect.gda.util.MockAuthentication;
import au.org.intersect.gda.util.SystemException;

/**
 * @version $Rev$
 * 
 */
public class ProjectServiceImplTest
{

    @Mock
    private ProjectDAO dao;
    

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

    private GdaUser bob;

    private GdaUser sue;

    private GdaUser fred;
    
    private List<ResultInfoDTO> resultList = new ArrayList<ResultInfoDTO>();
    
    private List<String> resultIdList = new ArrayList<String>();
    private List<Integer> teamIdList = new ArrayList<Integer>();

    private Set<Result> resultSet = new HashSet<Result>();

    
    private String result1Id = "result1";
    private String result2Id = "result2";
    
    private Team team1;
    private Team team2;

    
    private Integer team1Id = 1;
    private Integer team2Id = 2;
    
    private ResultInfoDTO result1;
    private ResultInfoDTO result2;

    private String currentUser = "user";

    @Before
    public void setUp() throws RepositoryException, TeamNotFoundException
    {
        MockitoAnnotations.initMocks(this);
        ProjectManager manager = new ProjectManagerImpl(
                dao, 
                assembler, 
                userDao, 
                repositoryManager, 
                resultDAO, 
                teamDAO,
                resultSecurityHelper,
                projectOaiUpdater);
        service = new ProjectServiceImpl(manager);
        bob = new GdaUser("bob");
        sue = new GdaUser("sue");
        fred = new GdaUser("fred");
        
        resultIdList.add(result1Id);
        resultIdList.add(result2Id);
        
        ResultDTO result1r = new ResultDTO();
        result1 = new ResultInfoDTO(result1r, true);
        result1.setId(result1Id);
        
        ResultDTO result2r = new ResultDTO();
        result2 = new ResultInfoDTO(result2r, true);
        result2.setId(result2Id);
        
        resultList.add(result1);        
        resultList.add(result2);
        
        resultSet.add(new Result(result1Id));
        resultSet.add(new Result(result2Id));
        
        when(repositoryManager.getResult(result1Id)).thenReturn(result1);
        when(repositoryManager.getResult(result2Id)).thenReturn(result2);
        
        
        teamIdList.add(team1Id);
        teamIdList.add(team2Id);
        
        team1 = new Team();
        team1.setId(team1Id);
        
        team2 = new Team();
        team2.setId(team2Id);
        
        

        when(teamDAO.getTeam(team1Id)).thenReturn(team1);
        when(teamDAO.getTeam(team2Id)).thenReturn(team2);
        
        ResultAccessDTO access = new ResultAccessDTO();
        when(resultSecurityHelper.getResultAccessDTOForCurrentUser(result1)).thenReturn(access);
        when(resultSecurityHelper.getResultAccessDTOForCurrentUser(result2)).thenReturn(access);
        
        
        
        SecurityContextHolder.getContext().setAuthentication(new MockAuthentication(currentUser));
        
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAllProjectsForOwnerReturnsCorrectProjects() throws RepositoryException, ProjectNotFoundException
    {
        Set<Project> projects = new HashSet<Project>();
        Project project1 = createProject("g1", "fred");
        project1.setResults(resultSet);
        Project project2 = createProject("g2", "bob");
        project2.setResults(resultSet);
        projects.add(project1);
        projects.add(project2);
        when(dao.getAllProjects()).thenReturn(projects);
        when(dao.getProject(anyInt())).thenReturn(project1);
        
        

        ProjectDTO dto1 = createDto(project1);
        dto1.setResults(resultList);
        ProjectDTO dto2 = createDto(project2);
        dto2.setResults(resultList);
        
        when(assembler.createProjectDTO(eq(project1), (List<ResultInfoDTO>) any())).thenReturn(dto1);
        when(assembler.createProjectDTO(eq(project2), (List<ResultInfoDTO>) any())).thenReturn(dto2);

        SecurityContextHolder.getContext().setAuthentication(new MockAuthentication("fred"));
        Set<ProjectDTO> dtos = service.getAllProjects();
        assertEquals(2, dtos.size());
        assertTrue(dtos.contains(dto1));
        assertTrue(dtos.contains(dto2));
    }

    @Test
    public void testCreateProject() throws GdaUserNotFoundException, TeamNotFoundException
    {
        SecurityContextHolder.getContext().setAuthentication(new MockAuthentication("fred"));

        ProjectDTO input = new ProjectDTO();
        Project project = new Project();
        when(userDao.getUser("fred")).thenReturn(fred);
        when(assembler.createProjectFromDTO(input, fred)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(new GdaUser("bob"));
        when(userDao.getUser("sue")).thenReturn(new GdaUser("sue"));
        
        Project persistedProject = new Project();
        persistedProject.setId(1);
        
        when(dao.persist(project)).thenReturn(persistedProject);
        
        service.createProject(input, members, resultIdList, teamIdList);
        verify(dao).persist(project);
        assertEquals(2, project.getMembers().size());
        
        
        verify(teamDAO).getTeam(eq(team1Id));
        verify(teamDAO).getTeam(eq(team2Id));
    }
    
    @Test
    public void testCreateProjectWithNullResults() throws GdaUserNotFoundException, TeamNotFoundException
    {
        SecurityContextHolder.getContext().setAuthentication(new MockAuthentication("fred"));

        ProjectDTO input = new ProjectDTO();
        Project project = new Project();
        when(userDao.getUser("fred")).thenReturn(fred);
        when(assembler.createProjectFromDTO(input, fred)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(new GdaUser("bob"));
        when(userDao.getUser("sue")).thenReturn(new GdaUser("sue"));

        Project persistedProject = new Project();
        persistedProject.setId(1);
        
        when(dao.persist(project)).thenReturn(persistedProject);
        
        service.createProject(input, members, null, null);
        verify(dao).persist(project);
        assertEquals(2, project.getMembers().size());
        

        verify(teamDAO, never()).getTeam((Integer) any());
    }

    @Test(expected = SystemException.class)
    public void testCreateProjectThrowsSystemExceptionIfUsrNotFound() 
        throws GdaUserNotFoundException
    {
        SecurityContextHolder.getContext().setAuthentication(new MockAuthentication("fred"));

        ProjectDTO input = new ProjectDTO();
        when(userDao.getUser("fred")).thenThrow(new GdaUserNotFoundException("fred"));
        service.createProject(input, new HashSet<String>(), resultIdList, teamIdList);
        
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetProject() throws ProjectNotFoundException, RepositoryException
    {
        Project project = new Project();
        project.setResults(resultSet);
        ProjectDTO dto = new ProjectDTO();
        dto.setResults(resultList);
        when(dao.getProject(100)).thenReturn(project);
        when(assembler.createProjectDTO(eq(project), (List<ResultInfoDTO>) any())).thenReturn(dto);

        ProjectDTO projectDto = service.getProject(100);
        assertSame(dto, projectDto);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testGetProjectNoResult() throws ProjectNotFoundException, RepositoryException
    {
        Project project = new Project();
        ProjectDTO dto = new ProjectDTO();
        when(dao.getProject(100)).thenReturn(project);
        when(assembler.createProjectDTO(eq(project), (List<ResultInfoDTO>) any())).thenReturn(dto);

        ProjectDTO projectDto = service.getProject(100);
        assertSame(dto, projectDto);
    }

    @Test(expected = SystemException.class)
    public void testGetProjectThrowsSystemExceptionIfProjectNotFound() 
        throws ProjectNotFoundException, RepositoryException
    {
        when(dao.getProject(100)).thenThrow(new ProjectNotFoundException(100));
        service.getProject(100);
    }

    @Test
    public void testUpdateProject() throws ProjectNotFoundException, GdaUserNotFoundException, TeamNotFoundException
    {
        Project project = new Project();
        project.setMembers(Collections.singleton(new GdaUser("different")));
        ProjectDTO dto = new ProjectDTO();
        when(dao.getProject(100)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(bob);
        when(userDao.getUser("sue")).thenReturn(sue);

        service.updateProject(100, dto, members, resultIdList, teamIdList);
        verify(assembler).updateProjectFromDTO(project, dto);
        assertEquals(2, project.getMembers().size());
        assertTrue(project.getMembers().contains(bob));
        assertTrue(project.getMembers().contains(sue));
        
        verify(teamDAO).getTeam(eq(team1Id));
        verify(teamDAO).getTeam(eq(team2Id));
    }
    
    @Test
    public void testUpdateProjectWithNullResults() throws ProjectNotFoundException, 
        GdaUserNotFoundException, TeamNotFoundException
    {
        Project project = new Project();
        project.setMembers(Collections.singleton(new GdaUser("different")));
        ProjectDTO dto = new ProjectDTO();
        when(dao.getProject(100)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(bob);
        when(userDao.getUser("sue")).thenReturn(sue);

        service.updateProject(100, dto, members, null, teamIdList);
        verify(assembler).updateProjectFromDTO(project, dto);
        assertEquals(2, project.getMembers().size());
        assertTrue(project.getMembers().contains(bob));
        assertTrue(project.getMembers().contains(sue));
        
        verify(teamDAO).getTeam(eq(team1Id));
        verify(teamDAO).getTeam(eq(team2Id));
    }
    

    @Test
    public void testUpdateProjectWithExistingResults() 
        throws ProjectNotFoundException, GdaUserNotFoundException, 
        ResultNotFoundException, RepositoryException, TeamNotFoundException
    {
        //result1 = owned by user and not removed
        //result2 = owned by user and removed
        //result3 = owned by user and newly added
        //result4 = not owned by user and not in the input, should not be removed
        
        String result3Id = "result3";
        String result4Id = "result4";
        
        Project project = new Project();
        project.setMembers(Collections.singleton(new GdaUser("different")));
        
        Set<Result> existingResults = new HashSet<Result>();
        Result result1 = new Result(result1Id);
        
        existingResults.add(result1);
        Result result2 = new Result(result2Id);
        existingResults.add(result2);
        
        Result result4 = new Result(result4Id);
        existingResults.add(result4);
        project.setResults(existingResults);
        
        ProjectDTO dto = new ProjectDTO();
        when(dao.getProject(100)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(bob);
        when(userDao.getUser("sue")).thenReturn(sue);
        
        Result result3 = new Result("result3");
        when(resultDAO.getResult("result3")).thenReturn(result3);
        
        ResultDTO result2DTO = new ResultDTO();
        result2DTO.setName(result2Id);
        result2DTO.setOwner(currentUser);
        when(repositoryManager.getResult(eq(result2Id))).thenReturn(result2DTO);
        
        
        ResultDTO result4DTO = new ResultDTO();
        result4DTO.setName(result4Id);
        result4DTO.setOwner("other user");
        when(repositoryManager.getResult(result4Id)).thenReturn(result4DTO);
        
        List<String> inputResults = new ArrayList<String>();
        
        inputResults.add(result1Id);
        inputResults.add(result3Id);
        
        service.updateProject(100, dto, members, inputResults, teamIdList);
        
        verify(assembler).updateProjectFromDTO(project, dto);
        
        //as result3 is newly added it'd have to retrieve it from the dao
        verify(resultDAO).getResult("result3");
        
        //as result2,4 are existing it'd have to have checked their owner
        verify(repositoryManager).getResult(eq(result2Id));        
        verify(repositoryManager).getResult(eq(result4Id));

        //as 1 is in the input also it should not need to have its owner checked
        verify(repositoryManager, never()).getResult(eq(result1Id));

        assertEquals(2, project.getMembers().size());
        assertTrue(project.getMembers().contains(bob));
        assertTrue(project.getMembers().contains(sue));
        

        //verify project captured has the right results
        ArgumentCaptor<Project> projectCapture = ArgumentCaptor.forClass(Project.class);

        verify(dao).persist(projectCapture.capture());
        
        Set<Result> capturedResults = projectCapture.getValue().getResults();
        
        assertTrue("Project results sent to persist should contain result", capturedResults.contains(result1));
        assertTrue("Project results sent to persist should contain result", capturedResults.contains(result3));
        assertTrue("Project results sent to persist should contain result", capturedResults.contains(result4));
        
        assertFalse("Project results sent to persist should not contain result", capturedResults.contains(result2));
        
        verify(teamDAO).getTeam(eq(team1Id));
        verify(teamDAO).getTeam(eq(team2Id));
        
    }
    
    @Test(expected = SystemException.class)
    public void testUpdateProjectRepoException() 
        throws ProjectNotFoundException, GdaUserNotFoundException, 
        ResultNotFoundException, RepositoryException, TeamNotFoundException
    {

        Project project = new Project();
        project.setMembers(Collections.singleton(new GdaUser("different")));
        
        Set<Result> existingResults = new HashSet<Result>();
        Result result1 = new Result(result1Id);        
        existingResults.add(result1);
        
        Result result2 = new Result(result2Id);        
        existingResults.add(result2);
      
        project.setResults(existingResults);
        
        ProjectDTO dto = new ProjectDTO();
        when(dao.getProject(100)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(bob);
        when(userDao.getUser("sue")).thenReturn(sue);
        
        when(repositoryManager.getResult(eq(result2Id))).thenThrow(new FedoraException("mock exception"));
        
        

        List<String> inputResults = new ArrayList<String>();
        
        inputResults.add(result1Id);        
        service.updateProject(100, dto, members, inputResults, teamIdList);

        
        verify(teamDAO).getTeam(eq(team1Id));
        verify(teamDAO).getTeam(eq(team2Id));
    }
    

    @Test(expected = SystemException.class)
    public void testUpdateProjectResultNotFoundException() 
        throws ProjectNotFoundException, GdaUserNotFoundException, ResultNotFoundException, RepositoryException
    {

        Project project = new Project();
        project.setMembers(Collections.singleton(new GdaUser("different")));
        
        Set<Result> existingResults = new HashSet<Result>();
        Result result1 = new Result(result1Id);        
        existingResults.add(result1);
        
      
        project.setResults(existingResults);
        
        ProjectDTO dto = new ProjectDTO();
        when(dao.getProject(100)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(bob);
        when(userDao.getUser("sue")).thenReturn(sue);
        
        ResultDTO result2DTO = new ResultDTO();
        result2DTO.setName(result2Id);
        result2DTO.setOwner(currentUser);
        when(repositoryManager.getResult(eq(result2Id))).thenReturn(result2DTO);
        
        when(resultDAO.getResult(result2Id)).thenThrow(new ResultNotFoundException("mock exception"));

        List<String> inputResults = new ArrayList<String>();        
        inputResults.add(result1Id);
        inputResults.add(result2Id);        
        service.updateProject(100, dto, members, inputResults, teamIdList);

    }
    
    @Test
    public void testUpdateProjectUserNotFoundException() 
        throws ProjectNotFoundException, GdaUserNotFoundException, ResultNotFoundException, RepositoryException
    {
        Project project = new Project();
        project.setMembers(Collections.singleton(new GdaUser("different")));
        
        Set<Result> existingResults = new HashSet<Result>();
        Result result1 = new Result(result1Id);        
        existingResults.add(result1);
        
      
        project.setResults(existingResults);
        
        ProjectDTO dto = new ProjectDTO();
        when(dao.getProject(100)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(bob);
        when(userDao.getUser("sue")).thenThrow(new GdaUserNotFoundException("user not found"));
        
        ResultDTO result2DTO = new ResultDTO();
        result2DTO.setName(result2Id);
        result2DTO.setOwner(currentUser);
        when(repositoryManager.getResult(eq(result2Id))).thenReturn(result2DTO);
        

        Result result2 = new Result(result2Id);        
        when(resultDAO.getResult(result2Id)).thenReturn(result2);

        List<String> inputResults = new ArrayList<String>();        
        inputResults.add(result1Id);
        inputResults.add(result2Id);        
        service.updateProject(100, dto, members, inputResults, teamIdList);
        
        //verify a call was made and the exception thrown but is suppressed
        verify(userDao).getUser("sue");

    }


    @Test(expected = SystemException.class)
    public void testUpdateProjectThrowsSystemExceptionIfProjectNotFound() throws ProjectNotFoundException
    {
        when(dao.getProject(100)).thenThrow(new ProjectNotFoundException(100));
        service.updateProject(100, new ProjectDTO(), new HashSet<String>(), resultIdList, teamIdList);
    }

    @Test
    public void testDeleteProject() throws ProjectNotFoundException
    {
        Project project = new Project();
        when(dao.getProject(100)).thenReturn(project);

        service.deleteProject(100);
        verify(dao).delete(project);
    }

    @Test(expected = SystemException.class)
    public void testDeleteProjectThrowsSystemExceptionIfProjectNotFound() throws ProjectNotFoundException
    {
        when(dao.getProject(100)).thenThrow(new ProjectNotFoundException(100));
        service.deleteProject(100);
    }
    
    


    @Test
    public void testUpdateProjectRestrictedWithExistingResults() 
        throws ProjectNotFoundException, GdaUserNotFoundException, ResultNotFoundException, RepositoryException
    {
        //result1 = owned by user and not removed
        //result2 = owned by user and removed
        //result3 = owned by user and newly added
        //result4 = not owned by user and not in the input, should not be removed
        
        String result3Id = "result3";
        String result4Id = "result4";
        
        Project project = new Project();
        project.setMembers(Collections.singleton(new GdaUser("different")));
        
        Set<Result> existingResults = new HashSet<Result>();
        Result result1 = new Result(result1Id);
        
        existingResults.add(result1);
        Result result2 = new Result(result2Id);
        existingResults.add(result2);
        
        Result result4 = new Result(result4Id);
        existingResults.add(result4);
        project.setResults(existingResults);
        
        when(dao.getProject(100)).thenReturn(project);

        Set<String> members = new HashSet<String>();
        members.add("bob");
        members.add("sue");

        when(userDao.getUser("bob")).thenReturn(bob);
        when(userDao.getUser("sue")).thenReturn(sue);
        
        Result result3 = new Result("result3");
        when(resultDAO.getResult("result3")).thenReturn(result3);
        
        ResultDTO result2DTO = new ResultDTO();
        result2DTO.setName(result2Id);
        result2DTO.setOwner(currentUser);
        when(repositoryManager.getResult(eq(result2Id))).thenReturn(result2DTO);
        
        
        ResultDTO result4DTO = new ResultDTO();
        result4DTO.setName(result4Id);
        result4DTO.setOwner("other user");
        when(repositoryManager.getResult(result4Id)).thenReturn(result4DTO);
        
        List<String> inputResults = new ArrayList<String>();
        
        inputResults.add(result1Id);
        inputResults.add(result3Id);
        
        service.updateProjectResultOnly(100, inputResults);
        
        //as result3 is newly added it'd have to retrieve it from the dao
        verify(resultDAO).getResult("result3");
        
        //as result2,4 are existing it'd have to have checked their owner
        verify(repositoryManager).getResult(eq(result2Id));        
        verify(repositoryManager).getResult(eq(result4Id));

        //as 1 is in the input also it should not need to have its owner checked
        verify(repositoryManager, never()).getResult(eq(result1Id));

        //verify project captured has the right results
        ArgumentCaptor<Project> projectCapture = ArgumentCaptor.forClass(Project.class);

        verify(dao).persist(projectCapture.capture());
        
        Set<Result> capturedResults = projectCapture.getValue().getResults();
        
        assertTrue("Project results sent to persist should contain result", capturedResults.contains(result1));
        assertTrue("Project results sent to persist should contain result", capturedResults.contains(result3));
        assertTrue("Project results sent to persist should contain result", capturedResults.contains(result4));
        
        assertFalse("Project results sent to persist should not contain result", capturedResults.contains(result2));
        
        
        
    }
    
    private ProjectDTO createDto(Project project)
    {
        ProjectDTO dto = new ProjectDTO();
        dto.setName(project.getName());
        dto.setOwner(new GdaUserDTO());
        return dto;
    }

    private Project createProject(String name, String ownerId)
    {
        GdaUser owner = new GdaUser(ownerId);
        Project project = new Project(name, owner, "some description");
        return project;
    }
}
