/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import java.util.List;

import au.org.intersect.gda.dto.ResultDTO;

/**
 * @version $Rev$
 *
 */
public class ResultSearchPagination
{
    private final int currentPage;
    private final int totalPage;
    
    private final int totalResults;
    
    private final List<ResultDTO> results;

    public ResultSearchPagination(
            int totalResults,
            int totalPage, 
            int currentPage,             
            List<ResultDTO> results)
    {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.totalResults = totalResults;
        this.results = results;
    }

    public int getCurrentPage()
    {
        return currentPage;
    }

    public int getTotalPage()
    {
        return totalPage;
    }

    public List<ResultDTO> getResults()
    {
        return results;
    }

    public int getTotalResults()
    {
        return totalResults;
    }
    
}
