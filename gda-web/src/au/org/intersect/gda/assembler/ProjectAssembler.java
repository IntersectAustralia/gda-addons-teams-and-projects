/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.assembler;

import java.util.List;

import au.org.intersect.gda.domain.GdaUser;
import au.org.intersect.gda.domain.Project;
import au.org.intersect.gda.dto.ProjectDTO;
import au.org.intersect.gda.dto.ResultInfoDTO;

/**
 * @version $Rev$
 *
 */
public interface ProjectAssembler
{


    ProjectDTO createProjectDTO(Project project);
    
    ProjectDTO createProjectDTO(Project project, List<ResultInfoDTO> resultList);

    Project createProjectFromDTO(ProjectDTO dto, GdaUser owner);

    void updateProjectFromDTO(Project project, ProjectDTO dto);


}
