/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.oai;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

import au.org.intersect.gda.dao.ProjectDAO;
import au.org.intersect.gda.dao.ProjectNotFoundException;
import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Result;
import au.org.intersect.gda.dto.ProjectOaiDTO;
import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.repository.ProjectRepositoryManager;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.repository.ResultRepositoryManager;
import au.org.intersect.gda.repository.fedora.FedoraException;

/**
 * @version $Rev$
 *
 */
public class ProjectOaiUpdaterImplTest
{
    private ProjectOaiUpdater projectOaiUpdater;
    
    
    @Mock
    private ProjectDAO projectDAO;
    
    @Mock
    private ProjectRepositoryManager projectRepositoryManager;
    
    @Mock
    private ResultRepositoryManager resultRepositoryManager;
    
    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        
        projectOaiUpdater = new ProjectOaiUpdaterImpl(
                projectDAO, 
                projectRepositoryManager, 
                resultRepositoryManager);
    }
    
    
    @Test
    public void testOnProjectCreate() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        
        String fedoraId = "fedoraId1";
        
        Project project = createProjectWithId(1);
        
        when(projectDAO.getProject(projectId)).thenReturn(project);
        
        ArgumentCaptor<ProjectOaiDTO> oaiDTOCapture = ArgumentCaptor.forClass(ProjectOaiDTO.class);
        
        when(projectRepositoryManager.createProjectInRepository(
                eq(projectId), oaiDTOCapture.capture())).thenReturn(fedoraId);
        
        ArgumentCaptor<Project> projectPersistCapture = ArgumentCaptor.forClass(Project.class);

        projectOaiUpdater.onProjectCreate(projectId);
        
        verify(projectDAO).persist(projectPersistCapture.capture());
        
        ProjectOaiDTO capturedOaiDTO = oaiDTOCapture.getValue();
        
        assertEquals(project.getName(), capturedOaiDTO.getName());
        assertEquals(project.getDescription(), capturedOaiDTO.getDescription());

        Project capturedProjectToPersist = projectPersistCapture.getValue();
        
        assertEquals(fedoraId, capturedProjectToPersist.getFedoraId());
        
        //mark for capture isn't set so should be false
        assertFalse(capturedOaiDTO.isMarkForExport());

    }
    

    @Test
    public void testOnProjectCreateProjectNotFound() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        
        when(projectDAO.getProject(projectId)).thenThrow(new ProjectNotFoundException(projectId));
        
        projectOaiUpdater.onProjectCreate(projectId);
        //verify called and exception suppressed
        verify(projectDAO).getProject(projectId);
    }
    
    @Test
    public void testOnProjectCreateRepositoryException() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        Project project = createProjectWithId(1);
        
        when(projectDAO.getProject(projectId)).thenReturn(project);
        

        when(projectRepositoryManager.createProjectInRepository(
                eq(projectId), (ProjectOaiDTO) any())).thenThrow(new FedoraException("exception"));
        
        
        projectOaiUpdater.onProjectCreate(projectId);
        
        verify(projectDAO).getProject(projectId);
        
        //verify called and exception suppressed
        verify(projectRepositoryManager).createProjectInRepository(eq(projectId), (ProjectOaiDTO) any());
    }
    

    @Test
    public void testOnProjectUpdate() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        
        String fedoraId = "fedoraId1";
        
        Project project = createProjectWithId(1);
        project.setFedoraId(fedoraId);
        project.setMarkedForExport(true);
        
        Result result1 = new Result();
        result1.setResultId("result1");
        Result result2 = new Result();
        result2.setResultId("result2");
        
        Set<Result> projectResults = new HashSet<Result>();
        projectResults.add(result1);
        projectResults.add(result2);
        project.setResults(projectResults);
        
        ResultDTO result1DTO = new ResultDTO();
        result1DTO.setId("result1");
        result1DTO.setOwner("owner");
        result1DTO.setType("some type");
        result1DTO.setTypeDisplayName("pretty display");
        ResultDTO result2DTO = new ResultDTO();
        result2DTO.setId("result2");
        result2DTO.setOwner("owner");
        result2DTO.setType("some type");
        result2DTO.setTypeDisplayName("pretty display");
        
        
        when(resultRepositoryManager.getResult("result1")).thenReturn(result1DTO);
        when(resultRepositoryManager.getResult("result2")).thenReturn(result2DTO);
        when(projectDAO.getProject(projectId)).thenReturn(project);
        
        ArgumentCaptor<ProjectOaiDTO> oaiDTOCapture = ArgumentCaptor.forClass(ProjectOaiDTO.class);
        
        

        projectOaiUpdater.onProjectUpdate(projectId);
        
        verify(projectRepositoryManager).updateProjectInRepository(
                eq(projectId), eq(fedoraId), oaiDTOCapture.capture());

        ProjectOaiDTO capturedOaiDTO = oaiDTOCapture.getValue();
        
        assertEquals(project.getName(), capturedOaiDTO.getName());
        assertEquals(project.getDescription(), capturedOaiDTO.getDescription());
        assertEquals(project.getMarkedForExport(), capturedOaiDTO.isMarkForExport());
        
        
        List<ResultDTO> resultsCaptured = capturedOaiDTO.getResults();
        
        assertTrue("project oai dto passed repo manager should contain result1", resultsCaptured.contains(result1DTO));
        assertTrue("project oai dto passed repo manager should contain result2", resultsCaptured.contains(result2DTO));
    }
    

    @Test
    public void testOnProjectUpdateProjectNotFound() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        
        when(projectDAO.getProject(projectId)).thenThrow(new ProjectNotFoundException(projectId));
        
        projectOaiUpdater.onProjectUpdate(projectId);
        //verify called and exception suppressed
        verify(projectDAO).getProject(projectId);
    }
    
    @Test
    public void testOnProjectUpdateRepositoryException() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        Project project = createProjectWithId(1);
        
        String fedoraId = "id1";
        project.setFedoraId(fedoraId);
        
        when(projectDAO.getProject(projectId)).thenReturn(project);
        

        when(projectRepositoryManager.updateProjectInRepository(
                eq(projectId), eq(fedoraId), (ProjectOaiDTO) any())).thenThrow(new FedoraException("exception"));
        
        
        projectOaiUpdater.onProjectUpdate(projectId);
        
        verify(projectDAO).getProject(projectId);
        
        //verify called and exception suppressed
        verify(projectRepositoryManager).updateProjectInRepository(eq(projectId), eq(fedoraId), (ProjectOaiDTO) any());
    }

    @Test
    public void testOnProjectDelete() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        
        String fedoraId = "fedoraId1";
        
        Project project = createProjectWithId(1);
        project.setFedoraId(fedoraId);
        
        when(projectDAO.getProject(projectId)).thenReturn(project);
        
        projectOaiUpdater.onDeleteProject(projectId);
        
        verify(projectRepositoryManager).deleteProjectInRepository(eq(fedoraId));
    }
    

    @Test
    public void testOnProjectDeleteProjectNotFound() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        
        when(projectDAO.getProject(projectId)).thenThrow(new ProjectNotFoundException(projectId));
        
        projectOaiUpdater.onDeleteProject(projectId);
        //verify called and exception suppressed
        verify(projectDAO).getProject(projectId);
    }
    
    @Test
    public void testOnProjectDeleteRepositoryException() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        Project project = createProjectWithId(1);
        
        String fedoraId = "id1";
        project.setFedoraId(fedoraId);
        
        when(projectDAO.getProject(projectId)).thenReturn(project);
        

        when(projectRepositoryManager.deleteProjectInRepository(eq(fedoraId)))
            .thenThrow(new FedoraException("exception"));
        
        
        projectOaiUpdater.onDeleteProject(projectId);
        
        verify(projectDAO).getProject(projectId);
        
        //verify called and exception suppressed
        verify(projectRepositoryManager).deleteProjectInRepository(eq(fedoraId));
    }
    

    @Test
    public void testOnMultipleProjectUpdate() throws ProjectNotFoundException, RepositoryException
    {
        Integer project1Id = 1;
        
        String fedora1Id = "fedoraId1";
        
        Project project1 = createProjectWithId(1);
        project1.setFedoraId(fedora1Id);
        project1.setMarkedForExport(false);
        
        when(projectDAO.getProject(project1Id)).thenReturn(project1);
        
        Integer project2Id = 2;
        
        String fedora2Id = "fedoraId2";
        
        Project project2 = createProjectWithId(2);
        project2.setFedoraId(fedora2Id);
        project2.setMarkedForExport(true);
        when(projectDAO.getProject(project2Id)).thenReturn(project2);
        
        List<Integer> projectIdList = new ArrayList<Integer>();
        
        projectIdList.add(project1Id);
        projectIdList.add(project2Id);
        

        projectOaiUpdater.onUpdateMultipleProject(projectIdList);
        
        

        ArgumentCaptor<ProjectOaiDTO> oaiDTO1Capture = ArgumentCaptor.forClass(ProjectOaiDTO.class);
        
        verify(projectRepositoryManager).updateProjectInRepository(
                eq(project1Id), eq(fedora1Id), oaiDTO1Capture.capture());

        ProjectOaiDTO capturedOaiDTO1 = oaiDTO1Capture.getValue();
        
        assertEquals(project1.getName(), capturedOaiDTO1.getName());
        assertEquals(project1.getDescription(), capturedOaiDTO1.getDescription());
        assertEquals(project1.getMarkedForExport(), capturedOaiDTO1.isMarkForExport());


        ArgumentCaptor<ProjectOaiDTO> oaiDTO2Capture = ArgumentCaptor.forClass(ProjectOaiDTO.class);
        
        verify(projectRepositoryManager).updateProjectInRepository(
                eq(project2Id), eq(fedora2Id), oaiDTO2Capture.capture());

        ProjectOaiDTO capturedOaiDTO2 = oaiDTO2Capture.getValue();
        
        assertEquals(project2.getName(), capturedOaiDTO2.getName());
        assertEquals(project2.getDescription(), capturedOaiDTO2.getDescription());
        assertEquals(project2.getMarkedForExport(), capturedOaiDTO2.isMarkForExport());

        
    }
    

    @Test
    public void testOnResultNotFound() throws ProjectNotFoundException, RepositoryException
    {
        Integer projectId = 1;
        
        String fedoraId = "fedoraId1";
        
        Project project = createProjectWithId(1);
        project.setFedoraId(fedoraId);
        project.setMarkedForExport(true);
        
        Result result1 = new Result();
        result1.setResultId("result1");
        Result result2 = new Result();
        result2.setResultId("result2");
        
        Set<Result> projectResults = new HashSet<Result>();
        projectResults.add(result1);
        projectResults.add(result2);
        project.setResults(projectResults);
        
        ResultDTO result1DTO = new ResultDTO();
        result1DTO.setId("result1");
        result1DTO.setOwner("owner");
        result1DTO.setType("some type");
        result1DTO.setTypeDisplayName("pretty display");
        ResultDTO result2DTO = new ResultDTO();
        result2DTO.setId("result2");
        result2DTO.setOwner("owner");
        result2DTO.setType("some type");
        result2DTO.setTypeDisplayName("pretty display");
        
        
        //make result1 not found, the updater should then skip result 1 but
        //continue to process the rest of them
        when(resultRepositoryManager.getResult("result1")).thenThrow(new FedoraException("exception"));
        when(resultRepositoryManager.getResult("result2")).thenReturn(result2DTO);
        when(projectDAO.getProject(projectId)).thenReturn(project);
        
        ArgumentCaptor<ProjectOaiDTO> oaiDTOCapture = ArgumentCaptor.forClass(ProjectOaiDTO.class);
        

        projectOaiUpdater.onProjectUpdate(projectId);
        
        verify(projectRepositoryManager).updateProjectInRepository(
                eq(projectId), eq(fedoraId), oaiDTOCapture.capture());

        ProjectOaiDTO capturedOaiDTO = oaiDTOCapture.getValue();
        
        assertEquals(project.getName(), capturedOaiDTO.getName());
        assertEquals(project.getDescription(), capturedOaiDTO.getDescription());
        assertEquals(project.getMarkedForExport(), capturedOaiDTO.isMarkForExport());


        List<ResultDTO> resultsCaptured = capturedOaiDTO.getResults();
        
        assertFalse("project oai dto passed repo manager should not contain result1", 
                resultsCaptured.contains(result1DTO));
        assertTrue("project oai dto passed repo manager should contain result2", resultsCaptured.contains(result2DTO));
    }
    
    private Project createProjectWithId(Integer id)
    {

        Project project = new Project();
        
        project.setId(id);
        
        GdaUser owner = new GdaUser("owner name");
        
        project.setOwner(owner);
        
        
        
        return project;
    }
    
    
    
}
