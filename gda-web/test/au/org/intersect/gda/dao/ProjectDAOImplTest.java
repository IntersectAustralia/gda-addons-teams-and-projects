/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.GdaUserFixture;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Result;
import au.org.intersect.gda.domain.Role;
import au.org.intersect.gda.domain.Team;

/**
 * @version $Rev$
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "test-ProjectDAO-application-context.xml")
@Transactional
public class ProjectDAOImplTest
{
    @Autowired
    private ProjectDAO projectDAO;

    @PersistenceContext
    private EntityManager entityManager;

    private GdaUser fred;
    private GdaUser bob;
    private GdaUser alice;

    @Before
    public void setUp()
    {
        Role role = GdaUserFixture.createSamplePersistedRoles(entityManager);
        fred = GdaUserFixture.createUser("fred", role);
        bob = GdaUserFixture.createUser("bob", role);
        alice = GdaUserFixture.createUser("alice", role);
        entityManager.persist(fred);
        entityManager.persist(bob);
        entityManager.persist(alice);
    }

    @Test
    public void testGetAllProjects()
    {
        Project project1 = createProject("project1", fred);
        projectDAO.persist(project1);
        Project project2 = createProject("project2", bob);
        projectDAO.persist(project2);

        flushAndClear();

        Set<Project> projects = projectDAO.getAllProjects();
        assertEquals(2, projects.size());

        Project reloadedProject1 = getProjectFromList(projects, "project1");
        Project reloadedProject2 = getProjectFromList(projects, "project2");

        assertProject(project1, reloadedProject1);
        assertProject(project2, reloadedProject2);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateProjectWithExistingResult() throws ProjectNotFoundException
    {
        Project project = createProject("project1", bob);
        Set<Result> results = new HashSet<Result>();
        
        Result result1 = new Result("result1");
        Result result2 = new Result("result2");
        
        entityManager.persist(result1);
        entityManager.persist(result2);
        
        flushAndClear();
        

        Result result1New = new Result("result1");
        Result result2New = new Result("result2");
        
        results.add(result1New);
        results.add(result2New);
        
        project.setResults(results);
        
        Project persistedProject = projectDAO.persist(project);
        
        flushAndClear();
        
        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        assertProject(project, reloadedProject);
        assertEquals(2, reloadedProject.getResults().size());
        
        this.getResultFromList(results, result1.getResultId());
        this.getResultFromList(results, result2.getResultId());
        Query query = entityManager.createQuery("select E from " + Result.class.getName() + " as E");
        List<Result> resultList = query.getResultList();
        
        //ensure that the entity manager use existing result instead of creating new ones
        assertEquals("Persisted results in db does not equal 2", 2, resultList.size());
    }
    
    @Test
    public void testCreateProjectWithNewResult() throws ProjectNotFoundException
    {
        Project project = createProject("project1", bob);
        Set<Result> results = new HashSet<Result>();
        
        Result result1 = new Result("result1");
        Result result2 = new Result("result2");
        
        results.add(result1);
        results.add(result2);
        
        project.setResults(results);
        
        Project persistedProject = projectDAO.persist(project);
        
        flushAndClear();
        
        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        assertProject(project, reloadedProject);
        assertEquals(2, reloadedProject.getResults().size());
        
        this.getResultFromList(results, result1.getResultId());
        this.getResultFromList(results, result2.getResultId());
        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateManyProjectWithExistingResult() throws ProjectNotFoundException
    {
        Project project = createProject("project1", bob);
        Set<Result> results = new HashSet<Result>();
        
        Result result1 = new Result("result1");
        Result result2 = new Result("result2");
        
        results.add(result1);
        results.add(result2);
        
        project.setResults(results);
        
        Project persistedProject = projectDAO.persist(project);
        
        flushAndClear();
        

        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        

        assertEquals(2, reloadedProject.getResults().size());
        
        Set<Result> reloadedResult = project.getResults();
        
        this.getResultFromList(reloadedResult, result1.getResultId());
        this.getResultFromList(reloadedResult, result2.getResultId());
        
        
        Set<Result> newResults = new HashSet<Result>();
        
        Result newResult1 = new Result("result1");
        Result newResult3 = new Result("result3");
        
        newResults.add(newResult1);
        newResults.add(newResult3);
        
        reloadedProject.setResults(newResults);
        
        Project updatedProject = projectDAO.persist(reloadedProject);
        
        flushAndClear();
        
        Project reloadedUpdatedProject = projectDAO.getProject(updatedProject.getId());
        
        Set<Result> reloadedUpdatedResult = reloadedUpdatedProject.getResults();
        
        assertEquals(2, reloadedUpdatedProject.getResults().size());
        
        this.getResultFromList(reloadedUpdatedResult, newResult1.getResultId());
        this.getResultFromList(reloadedUpdatedResult, newResult3.getResultId());
        
        
        
        Project project2 = createProject("project2", bob);
        
        
        Set<Result> project2Results = new HashSet<Result>();
        
        Result persistedResult1 = entityManager.find(Result.class, "result1");
        Result persistedResult2 = entityManager.find(Result.class, "result2");
        
        project2Results.add(persistedResult1);
        project2Results.add(persistedResult2);
        
        project2.setResults(project2Results);
        
        projectDAO.persist(project2);
        
        flushAndClear();
        
        
        Project reloadedProject1AfterProject2 = projectDAO.getProject(updatedProject.getId());
        
        Set<Result> reloadedProject1ResultsAfterProject2 = reloadedProject1AfterProject2.getResults();
        
        assertEquals(2, reloadedProject1ResultsAfterProject2.size());

        
        Query query = entityManager.createQuery("select E from " + Result.class.getName() + " as E");
        List<Result> resultList = query.getResultList();
        
        //ensure that the entity manager did not create duplicate or delete old results
        assertEquals("Persisted results in db does not equal 3", 3, resultList.size());
        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateProjectWithExistingResult() throws ProjectNotFoundException
    {
        Project project = createProject("project1", bob);
        Set<Result> results = new HashSet<Result>();
        
        Result result1 = new Result("result1");
        Result result2 = new Result("result2");
        
        results.add(result1);
        results.add(result2);
        
        project.setResults(results);
        
        Project persistedProject = projectDAO.persist(project);
        
        flushAndClear();
        

        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        

        assertEquals(2, reloadedProject.getResults().size());
        
        Set<Result> reloadedResult = project.getResults();
        
        this.getResultFromList(reloadedResult, result1.getResultId());
        this.getResultFromList(reloadedResult, result2.getResultId());
        
        
        Set<Result> newResults = new HashSet<Result>();
        
        Result newResult1 = new Result("result1");
        Result newResult3 = new Result("result3");
        
        newResults.add(newResult1);
        newResults.add(newResult3);
        
        reloadedProject.setResults(newResults);
        
        Project updatedProject = projectDAO.persist(reloadedProject);
        
        flushAndClear();
        
        Project reloadedUpdatedProject = projectDAO.getProject(updatedProject.getId());
        
        Set<Result> reloadedUpdatedResult = reloadedUpdatedProject.getResults();
        
        assertEquals(2, reloadedUpdatedProject.getResults().size());
        
        this.getResultFromList(reloadedUpdatedResult, newResult1.getResultId());
        this.getResultFromList(reloadedUpdatedResult, newResult3.getResultId());
        
        
        Query query = entityManager.createQuery("select E from " + Result.class.getName() + " as E");
        List<Result> resultList = query.getResultList();
        
        //ensure that the entity manager did not create duplicate or delete old results
        assertEquals("Persisted results in db does not equal 3", 3, resultList.size());
    }

    @Test
    public void testCreateAndGetSingleProject() throws ProjectNotFoundException
    {
        Project project = createProject("project1", bob);
        Set<GdaUser> members = new HashSet<GdaUser>();
        members.add(fred);
        members.add(alice);
        project.setMembers(members);
        Project persistedProject = projectDAO.persist(project);
        flushAndClear();
        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        assertProject(persistedProject, reloadedProject);
        assertEquals(2, reloadedProject.getMembers().size());
        assertTrue(reloadedProject.getMembers().contains(fred));
        assertTrue(reloadedProject.getMembers().contains(alice));
    }

    @Test
    public void testDeleteProject() throws ProjectNotFoundException
    {
        Project project = createProject("project1", bob);
        Set<GdaUser> members = new HashSet<GdaUser>();
        members.add(fred);
        members.add(alice);
        project.setMembers(members);
        Project persistedProject = projectDAO.persist(project);
        flushAndClear();
        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        assertProject(persistedProject, reloadedProject);
        projectDAO.delete(reloadedProject);
        flushAndClear();
        try
        {
            projectDAO.getProject(persistedProject.getId());
            fail("should not find deleted project");
        } catch (ProjectNotFoundException gnfe)
        {
            // expected
        }
    }

    @Test
    public void testCreatedDateAndLastModified() throws ProjectNotFoundException, InterruptedException
    {
        Project project = createProject("project1", bob);
        Project persistedProject = projectDAO.persist(project);
        flushAndClear();

        // check that dates are set on save
        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        assertNotNull(reloadedProject.getCreatedDate());
        assertNotNull(reloadedProject.getLastModifiedDate());
        Timestamp lastModified = reloadedProject.getLastModifiedDate();
        
        // modify it
        Thread.sleep(100);
        reloadedProject.setName("newname");
        flushAndClear();

        Project reloadedAgain = projectDAO.getProject(persistedProject.getId());
                
        // check that last modified has updated
        assertTrue(lastModified.before(reloadedAgain.getLastModifiedDate()));
        // but created date has not
        assertEquals(reloadedProject.getCreatedDate(), reloadedAgain.getCreatedDate());
    }

    @Test(expected = ProjectNotFoundException.class)
    public void testGetSingleProjectNotFound() throws ProjectNotFoundException
    {
        Project project = createProject("project1", bob);
        projectDAO.persist(project);
        flushAndClear();
        projectDAO.getProject(5454545);
    }
    
    
    @Test
    public void testCreateProjectWithExistingTeam() throws ProjectNotFoundException
    {
        Team team1 = new Team("team1", bob);
        Team team2 = new Team("team2", bob);
        Team team3 = new Team("team3", bob);
        
        entityManager.persist(team1);
        entityManager.persist(team2);
        entityManager.persist(team3);
        flushAndClear();
        
        Project project = createProject("project1", bob);
        project.addTeam(team1);
        project.addTeam(team2);
        Project persistedProject = projectDAO.persist(project);
        flushAndClear();
        
        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        
        reloadedProject.removeTeam(team1);
        reloadedProject.addTeam(team3);
        
        projectDAO.persist(reloadedProject);
        
        flushAndClear();
        
        Project reloadedUpdatedProject = projectDAO.getProject(persistedProject.getId());
        
        Set<Team> updatedTeams = reloadedUpdatedProject.getTeams();
        
        assertEquals(2, updatedTeams.size());
        

        assertFalse("team1 should have been removed", updatedTeams.contains(team1));
        assertTrue("team2 should not have been removed", updatedTeams.contains(team2));
        assertTrue("team3 should have been added", updatedTeams.contains(team3));
        

        
    }
    
    @Test
    public void testAddResultToProject() throws ProjectNotFoundException, InterruptedException
    {
        Project project = createProject("project1", bob);

        Project persistedProject = projectDAO.persist(project);
        Timestamp lastModified = persistedProject.getLastModifiedDate();
        
        Result result = new Result("result");

        entityManager.persist(result);          
        flushAndClear();
        
        Thread.sleep(1000); //pause to ensure modified time will be different
        
        Project reloadedProject = projectDAO.getProject(persistedProject.getId());
        Result reloadedResult = entityManager.find(Result.class, "result");

        reloadedProject.addResult(reloadedResult);                        
        
        
        flushAndClear();
        
        Project modifiedProject = projectDAO.getProject(persistedProject.getId());
        //assert the date has been changed
        assertTrue(modifiedProject.getLastModifiedDate().compareTo(lastModified) > 0);
    }

    private void assertProject(Project expectedProject, Project actualProject)
    {
        assertEquals(expectedProject.getName(), actualProject.getName());
        assertEquals(expectedProject.getOwner(), actualProject.getOwner());
    }

    private Project getProjectFromList(Set<Project> allProjects, String string)
    {
        for (Project project : allProjects)
        {
            if (project.getName().equals(string))
            {
                return project;
            }
        }
        fail("Did not find project with name [" + string + "] in list of projects");
        return null;
    }
    

    private Result getResultFromList(Set<Result> results, String string)
    {
        for (Result result : results)
        {
            if (result.getResultId().equals(string))
            {
                return result;
            }
        }
        fail("Did not find result with name [" + string + "] in list of results");
        return null;
    }

    private Project createProject(String projectName, GdaUser user)
    {
        Project project = new Project(projectName, user, "some description");
        
        return project;
    }

    protected void flushAndClear()
    {
        entityManager.flush();
        entityManager.clear();
    }

}
