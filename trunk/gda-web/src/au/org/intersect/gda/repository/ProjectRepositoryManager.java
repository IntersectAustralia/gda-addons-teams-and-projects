/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository;

import au.org.intersect.gda.dto.ProjectOaiDTO;

/**
 * @version $Rev$
 *
 */
public interface ProjectRepositoryManager
{
    /**
     * Creats a project object for purpose of oai export in the repository
     * 
     * @param project
     *      the project's oai details
     *      to be stored in the repository
     * @throws RepositoryException
     */
    String createProjectInRepository(
            Integer projectId,
            ProjectOaiDTO oaiDTO) throws RepositoryException;

    /**
     * Updates the project object for purpose of oai export in the repository
     * 
     * @param project
     *      the project's oai details
     *      to be updated in the repository
     * @throws RepositoryException
     */
    String updateProjectInRepository(
            Integer projectId,
            String projectFedoraId,
            ProjectOaiDTO oaiDTO) throws RepositoryException;

    
    /**
     * Marks the project repository object as deleted
     * 
     * @param project
     *      Marks the project fedora object as deleted
     * @throws RepositoryException
     */
    boolean deleteProjectInRepository(String projectFedoraId) throws RepositoryException;
    
    
}
