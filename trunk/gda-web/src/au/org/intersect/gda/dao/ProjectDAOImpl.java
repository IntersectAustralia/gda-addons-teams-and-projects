/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import au.org.intersect.gda.domain.Project;

/**
 * @version $Rev$
 * 
 */
public class ProjectDAOImpl implements ProjectDAO
{
    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @Override
    public Project getProject(Integer id) throws ProjectNotFoundException
    {
        Project project = entityManager.find(Project.class, id);
        if (project == null)
        {
            throw new ProjectNotFoundException(id);
        }
        return project;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Project> getAllProjects()
    {
        Query query = entityManager.createQuery("select E from " + Project.class.getName() + " as E");
        List<Project> resultList = query.getResultList();
        return new HashSet<Project>(resultList);
    }

    @Override
    public Project persist(Project project)
    {
        return entityManager.merge(project);
    }

    @Override
    public void delete(Project project)
    {
        entityManager.remove(project);
    }
}