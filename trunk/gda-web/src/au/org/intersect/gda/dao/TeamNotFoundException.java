/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.dao;

/**
 * @version $Rev$
 *
 */
public class TeamNotFoundException extends Exception
{

    private static final long serialVersionUID = 1L;

    public TeamNotFoundException(Integer teamId)
    {
        super("Team with id [" + teamId + "] does not exist");
    }
}
