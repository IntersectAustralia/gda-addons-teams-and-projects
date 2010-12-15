/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.domain;

/**
 * @version $Rev$
 *
 */
public final class TeamFixture
{
    private TeamFixture()
    {
        // helper class
    }
    
    public static Team createTeam(String name, GdaUser owner)
    {
        return new Team(name, owner);
    }
    
    

}
