/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import fedora.server.types.gen.ObjectFields;

/**
 * @version $Rev$
 *
 */
public interface FedoraRepositorySearcher
{
    /**
     * Fetch fedora result by result id.
     * 
     * @param pid
     * @return
     * @throws FedoraException
     */
    ObjectFields findResult(String pid) throws FedoraException;
}
