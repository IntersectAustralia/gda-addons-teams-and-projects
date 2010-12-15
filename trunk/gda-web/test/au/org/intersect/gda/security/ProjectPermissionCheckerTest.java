/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.security;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import au.org.intersect.gda.dao.GdaUserDAO;
import au.org.intersect.gda.dao.GdaUserNotFoundException;
import au.org.intersect.gda.dao.ProjectDAO;
import au.org.intersect.gda.dao.ProjectNotFoundException;
import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Team;
import au.org.intersect.gda.dto.GdaUserDTO;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.repository.RepositoryException;

/**
 * @version $Rev$
 * 
 */
public class ProjectPermissionCheckerTest
{
    @Mock
    private ProjectDAO projectDAO;
    
    @Mock
    private GdaUserDAO userDAO;

    @Mock
    private Authentication authentication;

    private ProjectPermissionChecker checker;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        checker = new ProjectPermissionChecker(projectDAO, userDAO);
    }

    @Test
    public void testDeniesIfUserDoesntHaveAnyProjectAccessPermission()
    {
        setupAuthorities();
        assertDeniesAll();

        setupAuthorities("BLAH_ALL_PROJECTS"); // not a match
        assertDeniesAll();

        verifyZeroInteractions(projectDAO);
    }

    @Test
    public void testAlwaysAllowsIfUserHasAllProjectsPermission()
    {
        setupAuthorities("VIEW_ALL_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, new ProjectDTO(), AccessType.VIEW));

        setupAuthorities("EDIT_ALL_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertTrue(checker.hasPermission(authentication, new ProjectDTO(), AccessType.EDIT));

        setupAuthorities("DELETE_ALL_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.DELETE));
        assertTrue(checker.hasPermission(authentication, new ProjectDTO(), AccessType.DELETE));
        verifyZeroInteractions(projectDAO);
    }

    @Test
    public void testAlwaysAllowsIfUserHasAllAndOwnProjectsPermission()
    {
        // even with both "ALL" and "OWN", "ALL" still grants all
        setupAuthorities("VIEW_ALL_PROJECTS", "VIEW_OWN_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, new ProjectDTO(), AccessType.VIEW));

        setupAuthorities("EDIT_ALL_PROJECTS", "EDIT_OWN_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertTrue(checker.hasPermission(authentication, new ProjectDTO(), AccessType.EDIT));

        setupAuthorities("DELETE_ALL_PROJECTS", "DELETE_OWN_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.DELETE));
        assertTrue(checker.hasPermission(authentication, new ProjectDTO(), AccessType.DELETE));
        verifyZeroInteractions(projectDAO);
    }

    @Test
    public void testDeniesAccessIfUserHasOwnProjectsPermissionAndIsNotOwner() throws RepositoryException,
            ProjectNotFoundException
    {
        when(authentication.getName()).thenReturn("currentuser");

        ProjectDTO mockDto = new ProjectDTO();
        mockDto.setOwner(new GdaUserDTO());
        mockDto.getOwner().setUsername("different");

        Project mockProject = new Project();
        mockProject.setOwner(new GdaUser("different"));
        when(projectDAO.getProject(100)).thenReturn(mockProject);

        setupAuthorities("VIEW_OWN_PROJECTS");
        assertFalse(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertFalse(checker.hasPermission(authentication, mockDto, AccessType.VIEW));

        setupAuthorities("EDIT_OWN_PROJECTS");
        assertFalse(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, mockDto, AccessType.EDIT));

        setupAuthorities("DELETE_OWN_PROJECTS");
        assertFalse(checker.hasPermission(authentication, 100, AccessType.DELETE));
        assertFalse(checker.hasPermission(authentication, mockDto, AccessType.DELETE));
    }

    @Test
    public void testAllowsAccessIfUserHasOwnProjectsPermissionAndIsOwner() throws RepositoryException,
            ProjectNotFoundException
    {
        when(authentication.getName()).thenReturn("currentuser");

        ProjectDTO mockDto = new ProjectDTO();
        mockDto.setOwner(new GdaUserDTO());
        mockDto.getOwner().setUsername("currentuser");

        Project mockProject = new Project();
        mockProject.setOwner(new GdaUser("currentuser"));
        when(projectDAO.getProject(100)).thenReturn(mockProject);

        setupAuthorities("VIEW_OWN_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.VIEW));

        setupAuthorities("EDIT_OWN_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.EDIT));

        setupAuthorities("DELETE_OWN_PROJECTS");
        assertTrue(checker.hasPermission(authentication, 100, AccessType.DELETE));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.DELETE));
    }

    @Test
    public void testEditDoesNotImplyViewAccess() throws RepositoryException, ProjectNotFoundException
    {
        when(authentication.getName()).thenReturn("currentuser");

        ProjectDTO mockDto = new ProjectDTO();
        mockDto.setOwner(new GdaUserDTO());
        mockDto.getOwner().setUsername("currentuser");

        Project mockProject = new Project();
        mockProject.setOwner(new GdaUser("currentuser"));
        when(projectDAO.getProject(100)).thenReturn(mockProject);

        setupAuthorities("EDIT_OWN_PROJECTS");
        assertFalse(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.EDIT));
    }

    @Test
    public void testViewButNotEditOnlyAllowsView() throws RepositoryException, ProjectNotFoundException
    {
        when(authentication.getName()).thenReturn("currentuser");

        ProjectDTO mockDto = new ProjectDTO();
        mockDto.setOwner(new GdaUserDTO());
        mockDto.getOwner().setUsername("different");

        Project mockProject = new Project();
        mockProject.setOwner(new GdaUser("different"));
        when(projectDAO.getProject(100)).thenReturn(mockProject);

        setupAuthorities("EDIT_OWN_PROJECTS", "VIEW_ALL_PROJECTS");
        assertFalse(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, mockDto, AccessType.EDIT));
        assertTrue(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.VIEW));
    }

    @Test
    public void testProjectNotFoundException() throws RepositoryException, ProjectNotFoundException
    {
        setupAuthorities("VIEW_OWN_PROJECTS");
        when(authentication.getName()).thenReturn("currentuser");

        when(projectDAO.getProject(100)).thenThrow(new ProjectNotFoundException(100));

        assertTrue(checker.hasPermission(authentication, 100, AccessType.VIEW));
    }
    

    @Test
    public void testProjectsMemberPermission() throws ProjectNotFoundException, GdaUserNotFoundException
    {
        setupAuthorities("VIEW_PROJECT_PROJECTS");        
        when(authentication.getName()).thenReturn("currentuser");
        
        String currentUser = "currentuser";
        //user a member of project
        ProjectDTO mockDto = new ProjectDTO();
        mockDto.setId(100);
        mockDto.setOwner(new GdaUserDTO());
        mockDto.getOwner().setUsername("different");
        

        Project mockProject = new Project();
        mockProject.setOwner(new GdaUser("different"));
        
        Set<GdaUser> members = new HashSet<GdaUser>();

        GdaUser user = new GdaUser(currentUser);
        
        members.add(user);
        
        mockProject.setMembers(members);        
        
           
        Set<Project> userProjects = new HashSet<Project>();
        userProjects.add(mockProject);
        
        user.setProjects(userProjects);
                
        when(projectDAO.getProject(100)).thenReturn(mockProject);
        when(userDAO.getUser(currentUser)).thenReturn(user);


        assertFalse(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, mockDto, AccessType.EDIT));
        assertTrue(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.VIEW));
        
        //user not a member of project
        ProjectDTO mockDto2 = new ProjectDTO();
        mockDto2.setId(200);
        mockDto2.setOwner(new GdaUserDTO());
        mockDto2.getOwner().setUsername("different");
        
        Project mockProject2 = new Project();
        mockProject2.setOwner(new GdaUser("different"));
        
        Set<GdaUser> members2 = new HashSet<GdaUser>();

        GdaUser user2 = new GdaUser("different");
        members2.add(user2);
        
        mockProject2.setMembers(members2);    
        
        when(projectDAO.getProject(200)).thenReturn(mockProject2);
        
        assertFalse(checker.hasPermission(authentication, 200, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, mockDto2, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, 200, AccessType.VIEW));
        assertFalse(checker.hasPermission(authentication, mockDto2, AccessType.VIEW));
        
        //no member in project
        
        ProjectDTO mockDto3 = new ProjectDTO();
        mockDto3.setId(300);
        mockDto3.setOwner(new GdaUserDTO());
        mockDto3.getOwner().setUsername("different");
        
        Project mockProject3 = new Project();
        mockProject3.setOwner(new GdaUser("different"));
        
        Set<GdaUser> members3 = new HashSet<GdaUser>();

        GdaUser user3 = new GdaUser("different");
        members3.add(user3);
        
        mockProject3.setMembers(members3);    
        
        when(projectDAO.getProject(300)).thenReturn(mockProject3);
        
        assertFalse(checker.hasPermission(authentication, 300, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, mockDto3, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, 300, AccessType.VIEW));
        assertFalse(checker.hasPermission(authentication, mockDto3, AccessType.VIEW));

    }
    
    @Test
    public void testProjectsMemberOfTeamPermission() throws ProjectNotFoundException, GdaUserNotFoundException
    {
        setupAuthorities("VIEW_PROJECT_PROJECTS");        
        when(authentication.getName()).thenReturn("currentuser");
        
        String currentUser = "currentuser";
        //user a member of team of the project
        ProjectDTO mockDto = new ProjectDTO();
        mockDto.setId(100);
        mockDto.setOwner(new GdaUserDTO());
        mockDto.getOwner().setUsername("different");
        

        Project mockProject = new Project();
        mockProject.setOwner(new GdaUser("different"));
        
        Set<GdaUser> members = new HashSet<GdaUser>();

        GdaUser user = new GdaUser(currentUser);
        
        members.add(user);
        
        mockProject.setMembers(members);        
        
        Team userTeam = new Team();
        
           
        Set<Project> teamProjects = new HashSet<Project>();
        teamProjects.add(mockProject);
        
        userTeam.setProjects(teamProjects);
        
        Set<Team> userTeams = new HashSet<Team>();
        
        userTeams.add(userTeam);
        
        user.setTeams(userTeams);
                        
        when(projectDAO.getProject(100)).thenReturn(mockProject);
        when(userDAO.getUser(currentUser)).thenReturn(user);


        assertFalse(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, mockDto, AccessType.EDIT));
        assertTrue(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.VIEW));
    }
    
    @Test
    public void testProjectsOwnerPermission() throws ProjectNotFoundException
    {
        setupAuthorities("VIEW_PROJECT_PROJECTS");        
        when(authentication.getName()).thenReturn("currentuser");

        //user a member of project
        ProjectDTO mockDto = new ProjectDTO();
        mockDto.setId(100);
        mockDto.setOwner(new GdaUserDTO());
        //user is owner
        mockDto.getOwner().setUsername("currentuser");
        

        Project mockProject = new Project();
        mockProject.setOwner(new GdaUser("currentuser"));
        
        Set<GdaUser> members = new HashSet<GdaUser>();

        
        //no members in project
        mockProject.setMembers(members);        
        
           
        
        
        when(projectDAO.getProject(100)).thenReturn(mockProject);
        


        assertFalse(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, mockDto, AccessType.EDIT));
        assertTrue(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertTrue(checker.hasPermission(authentication, mockDto, AccessType.VIEW));
      
    }
    
    private void setupAuthorities(String... authorities)
    {
        List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
        for (String auth : authorities)
        {
            auths.add(new GrantedAuthorityImpl(auth));
        }
        when(authentication.getAuthorities()).thenReturn(auths);
    }

    private void assertDeniesAll()
    {
        // try both by id and by object
        assertFalse(checker.hasPermission(authentication, 100, AccessType.VIEW));
        assertFalse(checker.hasPermission(authentication, 100, AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, 100, AccessType.DELETE));
        assertFalse(checker.hasPermission(authentication, new ProjectDTO(), AccessType.VIEW));
        assertFalse(checker.hasPermission(authentication, new ProjectDTO(), AccessType.EDIT));
        assertFalse(checker.hasPermission(authentication, new ProjectDTO(), AccessType.DELETE));
    }
}
