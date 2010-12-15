/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;

import au.org.intersect.gda.dto.ResultSearchCriteriaDTO;
import au.org.intersect.gda.http.GdaHttpClient;
import au.org.intersect.gda.http.GdaHttpClientException;
import au.org.intersect.gda.http.GdaHttpClientPostResponse;

/**
 * @version $Rev$
 *
 */
public class FedoraRepositoryRISearcherImpl implements FedoraRepositoryRISearcher
{

    private final GdaHttpClient gdaHttpClient;
    private final String fedoraRIUri;

    /**
     * Constructor
     * @param gdaHttpClient The HTTP Client interface to the Fedora Resource Index search
     * @param fedoraResourceIndexUri The URI of the fedora resource index
     */
    public FedoraRepositoryRISearcherImpl(GdaHttpClient gdaHttpClient,
                                          String fedoraRIUri)
    {
        this.gdaHttpClient = gdaHttpClient;
        this.fedoraRIUri = fedoraRIUri;
    }


    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRISearcher#getTuples(java.lang.String,
     *  java.lang.String, java.lang.String)
     */
    @Override
    public String[] getTuples(String lang, String format, String query) throws FedoraException
    {
        return getTuples("false", lang, format, null, "off", "off", query);
    }
    
    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRISearcher#getTuples(java.lang.String, 
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String[] getTuples(String flush, 
                              String lang, 
                              String format, 
                              String limit, 
                              String distinct, 
                              String stream,
                              String query) throws FedoraException
    {
        String[] tuples = new String[0];
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "tuples");
        params.put("flush", flush);
        params.put("lang", lang);
        params.put("format", format);
        if (limit != null) 
        {
            params.put("limit", limit);
        }
        params.put("distinct", distinct);
        params.put("stream", stream);
        params.put("query", query);
        
        try
        {
            GdaHttpClientPostResponse response = gdaHttpClient.post(fedoraRIUri, params);
            
            if (response.getReturnCode() == HttpStatus.SC_OK)
            {
                tuples = response.getResponse();
            } else
            {
                throw new FedoraException("Fedora Resource Index query error, http status = "
                        + response.getReturnCode());
            }
            
        } catch (GdaHttpClientException e)
        {
            throw new FedoraException(e.getMessage(), e);
        }
        
        return tuples;
    }


    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRISearcher#
     * getChildrenSearchITQLQuery(java.lang.String)
     */
    @Override
    public String getChildrenSearchITQLQuery(String parentId)
    {
        return "select <" + parentId + "> $child "
               + "from <#ri> "
               + "where $child <" + FedoraRepositoryRelationshipManager.IS_DERIVED_FROM + "> <" + parentId + ">";
    }


    /* (non-Javadoc)
     * @see au.org.intersect.gda.repository.fedora.FedoraRepositoryRISearcher#getCriteriaSearchITQLQuery(
     * au.org.intersect.gda.dto.ResultSearchCriteriaDTO)
     */
    @Override
    public String getCriteriaSearchITQLQuery(ResultSearchCriteriaDTO criteria)
    {
        String query = "select $s from <#ri> where";
        
        boolean isFirst = true;
        String operator = "and";
        
        // Object Name
        String nameLine = addItemToQuery(criteria.getName(), operator, "<dc:title>", isFirst);
        isFirst = "".equals(nameLine);
        query += nameLine;
        
        // Object Type
        
        if (criteria.getTypes().size() > 0)
        {
            String typeLine = addListToQuery(criteria.getTypes(), "<dc:type>", isFirst);
            isFirst = isFirst ? "".equals(typeLine) : false;
            query += typeLine;
        }
        
        // Object Owner
        if (criteria.getOwnerList().size() > 0)
        {
            String ownerLine = addListToQuery(criteria.getOwnerList(), "<dc:publisher>", isFirst);
            isFirst = isFirst ? "".equals(ownerLine) : false;
            query += ownerLine;
        }

        // Object Creator
        if (criteria.getCreatorList().size() > 0)
        {
            String creatorLine = addListToQuery(criteria.getCreatorList(), "<dc:creator>", isFirst);
            isFirst = isFirst ? "".equals(creatorLine) : false;
            query += creatorLine;
        }

        // TODO: Created Date From & To
        
        // TODO: Modified Date From & To
        
        // Object has attachment
        if ((criteria.getHasFile() != null) && criteria.getHasFile())
        {
            if (!isFirst)
            {
                query += " and";
            }
            query += " $s <dc:relation> $o";
            isFirst = false;
        }
        
        return query;
    }

    private String addItemToQuery(String item, String operator, String relation, boolean isFirst)
    {
        String queryLine = "";
        
        if (item != null)
        {
            if (!isFirst)
            {
                queryLine += " " + operator + " ";
            }
            queryLine += " $s " + relation + " '" + item + "'";
        }
        
        return queryLine;
    }
    
    private String addListToQuery(List<String> itemList, String relation, boolean isFirst)
    {
    
        String queryLine = "";
        if (!isFirst)
        {
            queryLine += " and ";
        }
        queryLine += " ( ";
        
        for (int i = 0; i < itemList.size(); ++i)
        {
            String item = itemList.get(i);
            if (i > 0)
            {
                queryLine += " or ";
            }
            queryLine += "$s " + relation + " '" + item + "'";
        }
        queryLine += " ) ";
        
        return queryLine; 
    }
    
}
