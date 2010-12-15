/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

/**
 * @version $Rev$
 *
 */
public class FedoraObjectNotFoundException extends FedoraException
{
    private static final long serialVersionUID = 1L;

    public FedoraObjectNotFoundException(String resultId)
    {
        super("Fedora Object not found");
    }
}
