/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.oai;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import au.org.intersect.gda.dao.ProjectDAO;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.domain.Result;
import au.org.intersect.gda.dto.ProjectOaiDTO;
import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.repository.ProjectRepositoryManager;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.repository.ResultRepositoryManager;

/**
 * @version $Rev$
 *
 */
public class ProjectOaiUpdaterImpl implements ProjectOaiUpdater
{
    private static final Logger LOG = Logger.getLogger(ProjectOaiUpdaterImpl.class);
    
    private static final String FAILED_RESULT_RETRIEVE = "Failed to retrieve result [";
    private static final String FAILED_PROJECT_OAI = "Failed to process oai for project [";

    private static final String CLOSE_SQUARE_BRACKET = "]";

    private final ProjectDAO projectDAO;
    
    private final ProjectRepositoryManager projectRepositoryManager;
    
    private final ResultRepositoryManager resultRepositoryManager;
    
    
    public ProjectOaiUpdaterImpl(ProjectDAO projectDAO, 
            ProjectRepositoryManager projectRepositoryManager,
            ResultRepositoryManager resultRepositoryManager)
    {
        this.projectDAO = projectDAO;
        this.projectRepositoryManager = projectRepositoryManager;
        this.resultRepositoryManager = resultRepositoryManager;
    }
    
    
    @Override
    public void onProjectCreate(Integer projectId)
    {
        if (LOG.isInfoEnabled())
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Performing OAI create for project [");
            buffer.append(projectId);
            buffer.append(CLOSE_SQUARE_BRACKET);
            LOG.info(buffer.toString());            
        }
        
        try
        {
            Project project = projectDAO.getProject(projectId);
            
            ProjectOaiDTO oaiDTO = createOaiDTO(project);

            String fedoraId = projectRepositoryManager.createProjectInRepository(projectId, oaiDTO);
            
            project.setFedoraId(fedoraId);
            
            projectDAO.persist(project);
            
            
        } catch (Exception e)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append(FAILED_PROJECT_OAI);
            buffer.append(projectId);
            buffer.append(CLOSE_SQUARE_BRACKET);
            LOG.error(buffer.toString(), e); 
            //suppress                
        }
    }

    @Override
    public void onProjectUpdate(Integer projectId)
    {
        if (LOG.isInfoEnabled())
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Performing OAI update for project [");
            buffer.append(projectId);
            buffer.append(CLOSE_SQUARE_BRACKET);
            LOG.info(buffer.toString());            
        }
        
        try
        {
            Project project = projectDAO.getProject(projectId);
                        
            ProjectOaiDTO oaiDTO = createOaiDTO(project);
            projectRepositoryManager.updateProjectInRepository(projectId, project.getFedoraId(), oaiDTO);
            
            
        } catch (Exception e)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append(FAILED_PROJECT_OAI);
            buffer.append(projectId);
            buffer.append(CLOSE_SQUARE_BRACKET);
            LOG.error(buffer.toString(), e); 
            //suppress        
        } 
        
    }

    @Override
    public void onUpdateMultipleProject(List<Integer> projectIdList)
    {        
        for (Integer projectId : projectIdList)
        {
            onProjectUpdate(projectId);
        }        
    }

    @Override
    public void onDeleteProject(Integer projectId)
    {
        if (LOG.isInfoEnabled())
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Performing OAI delete for project [");
            buffer.append(projectId);
            buffer.append(CLOSE_SQUARE_BRACKET);
            LOG.info(buffer.toString());            
        }
                
        try
        {
            Project project = projectDAO.getProject(projectId);
            projectRepositoryManager.deleteProjectInRepository(project.getFedoraId());

        } catch (Exception e)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append(FAILED_PROJECT_OAI);
            buffer.append(projectId);
            buffer.append(CLOSE_SQUARE_BRACKET);
            LOG.error(buffer.toString(), e); 
            //suppress
        }
    }
    
    private List<ResultDTO> getResultsForProject(Project project)
    {
        Set<Result> resultSet = project.getResults();
        
        List<ResultDTO> resultList = new ArrayList<ResultDTO>();
        
        for (Result result : resultSet)
        {
            try
            {
                ResultDTO resultDTO = resultRepositoryManager.getResult(result.getResultId());
                
                resultList.add(resultDTO);
            } catch (RepositoryException e)
            {
                StringBuffer buffer = new StringBuffer();
                buffer.append(FAILED_RESULT_RETRIEVE);
                buffer.append(result.getResultId());
                buffer.append("] continuing on with remaining results");
                LOG.error(buffer.toString(), e);
                //suppress error
            }
        }
        
        return resultList;
    }
    
    private ProjectOaiDTO createOaiDTO(Project project)
    {

        ProjectOaiDTO oaiDTO = new ProjectOaiDTO();
        oaiDTO.setName(project.getName());
        oaiDTO.setOwner(project.getOwner().getUsername());
        oaiDTO.setOwnerContact(project.getOwner().getEmail());
        
        if (project.getMarkedForExport() == null || !project.getMarkedForExport())
        {
            oaiDTO.setMarkForExport(false);
        } else
        {
            oaiDTO.setMarkForExport(true);
        }        
        
        oaiDTO.setDescription(project.getDescription());
        oaiDTO.setDateCreated(project.getCreatedDate());
        oaiDTO.setDateModified(project.getLastModifiedDate());  
        
        oaiDTO.setResults(getResultsForProject(project));
        
        return oaiDTO;
    }

}
