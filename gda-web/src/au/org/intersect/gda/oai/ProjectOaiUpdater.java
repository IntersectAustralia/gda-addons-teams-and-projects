/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.oai;

import java.util.List;

/**
 * 
 * The methods should be called regardless of whether
 * the projects in question need to be exported
 * The Updater will determine the exact behaviour of them
 * 
 * 
 * @version $Rev$
 *
 */
public interface ProjectOaiUpdater
{

    /**
     * 
     * Called when a project is created to
     * generate an oai feed for it
     * This should be called after persistence change
     * 
     * @param projectId
     * 
     */
    void onProjectCreate(Integer projectId);

    /**
     * Called to update a project's oai feed
     * This should be called after persistence change
     *      
     * @param projectId
     */
    void onProjectUpdate(Integer projectId);

    /**
     * Called to update each project's oai feed
     * This should be called after persistence change
     * 
     * @param projectIdList
     */
    void onUpdateMultipleProject(List<Integer> projectIdList);

    /**
     * 
     * Called to mark a project as deleted in the oai feed
     * Note that this method should be called prior to deletion 
     * in the persistence
     * 
     * @param projectId
     */
    void onDeleteProject(Integer projectId);
   
}
