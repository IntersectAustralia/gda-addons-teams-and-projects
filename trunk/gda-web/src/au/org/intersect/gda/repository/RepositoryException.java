/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository;

/**
 * This is the parent class to any Repository related exceptions.
 * 
 * @version $Rev$
 *
 */
public abstract class RepositoryException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public RepositoryException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RepositoryException(String message)
    {
        super(message);
    }
}
