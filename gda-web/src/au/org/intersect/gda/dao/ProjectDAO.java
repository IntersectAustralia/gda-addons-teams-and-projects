/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dao;


import java.util.Set;

import au.org.intersect.gda.domain.Project;

/**
 * @version $Rev$
 *
 */

public interface ProjectDAO
{
    public Set<Project> getAllProjects();
    
    public Project getProject(Integer id) throws ProjectNotFoundException;

    public Project persist(Project project);

    public void delete(Project project);
    
}