/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.log4j.Logger;

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.types.gen.ComparisonOperator;
import fedora.server.types.gen.Condition;
import fedora.server.types.gen.FieldSearchQuery;
import fedora.server.types.gen.FieldSearchResult;
import fedora.server.types.gen.ObjectFields;

/**
 * @version $Rev$
 *
 */
public class FedoraRepositorySearcherImpl implements FedoraRepositorySearcher
{
    private static final Logger LOG = Logger.getLogger(FedoraRepositorySearcherImpl.class);

    private static final String PID = "pid";
    // The PID is essential for the query to work - this has to be part of the
    // condition of the query.
    private static final String[] FIELDS_TO_FETCH = {PID, "mDate", "cDate"};
    private final FedoraComponentFactory fedoraComponentFactory;

    public FedoraRepositorySearcherImpl(FedoraComponentFactory fedoraComponentFactory)
    {
        super();
        this.fedoraComponentFactory = fedoraComponentFactory;
    }
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositorySearcher#
     * findResult(java.lang.String)
     */
    @Override
    public ObjectFields findResult(String pid) throws FedoraException
    {
        LOG.info("Finding result with id " + pid);
        ObjectFields result = null;
        try
        {
            FedoraClient client = fedoraComponentFactory.buildFedoraClient();
            FedoraAPIA apia = client.getAPIA();

            FieldSearchQuery query = new FieldSearchQuery();
            Condition condition = new Condition();
            condition.setOperator(ComparisonOperator.eq);
            condition.setValue(pid);
            condition.setProperty(PID);
            query.setConditions(new Condition[] {condition});
            
            FieldSearchResult results = apia.findObjects(FIELDS_TO_FETCH, new NonNegativeInteger("1"), query);
            ObjectFields[] eachData = results.getResultList();
            LOG.info("Results found: " + eachData.length);
            if (eachData.length < 1)
            {
                throw new FedoraObjectNotFoundException(pid);
            }
            ObjectFields o = eachData[0];
            result = o;

        } catch (MalformedURLException e)
        {
            throw new FedoraException(FedoraErrorMessages.URL_TO_FEDORA_REPOSITORY_IS_INVALID, e);
        } catch (IOException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_APIA_OR_FEDORA_APIM, e);
        } catch (ServiceException e)
        {
            throw new FedoraException(FedoraErrorMessages.UNABLE_TO_INSTANTIATE_FEDORA_SERVICE_COMPONENTS, e);
        }
        return result;
    }

}
