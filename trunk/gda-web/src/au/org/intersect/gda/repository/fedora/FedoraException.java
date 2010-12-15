/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import au.org.intersect.gda.repository.RepositoryException;

/**
 * @version $Rev$
 *
 */
public class FedoraException extends RepositoryException
{
    private static final long serialVersionUID = 1L;
    
    public FedoraException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public FedoraException(String message)
    {
        super(message);
    }
    
}
