/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.NonNegativeInteger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fedora.client.FedoraClient;
import fedora.server.access.FedoraAPIA;
import fedora.server.types.gen.FieldSearchQuery;
import fedora.server.types.gen.FieldSearchResult;
import fedora.server.types.gen.ObjectFields;

/**
 * @version $Rev$
 * 
 */
public class FedoraRepositorySearcherImplTest
{
    
    private FedoraRepositorySearcherImpl fedoraRepositorySearcher;

    @Mock
    private FedoraComponentFactory fedoraComponentFactory;

    @Mock
    private FedoraAPIA apia;

    @Mock
    private FedoraClient fc;

    @Before
    public void setUp() throws MalformedURLException, IOException, ServiceException
    {
        MockitoAnnotations.initMocks(this);
        fedoraRepositorySearcher = new FedoraRepositorySearcherImpl(fedoraComponentFactory);
        when(fedoraComponentFactory.buildFedoraClient()).thenReturn(fc);
        when(fc.getAPIA()).thenReturn(apia);
    }

    /**
     * Test method for
     * {@link au.org.intersect.gda.repository.fedora.FedoraRepositorySearcherImpl#findResult(java.lang.String)}.
     */
    @Test
    public void testFindResult() throws FedoraException, RemoteException
    {
        FieldSearchResult fieldSearchResult = mock(FieldSearchResult.class);
        ObjectFields objectFields = mock(ObjectFields.class);
        String pid = "foo:bar";
        when(objectFields.getPid()).thenReturn(pid);
        
        String mDate = "2010-01-07T05:48:46.828Z";
        when(objectFields.getMDate()).thenReturn(mDate);
        String cDate = "2000-03-02T07:38:26.868Z";
        when(objectFields.getCDate()).thenReturn(cDate);
        when(fieldSearchResult.getResultList()).thenReturn(new ObjectFields[] {objectFields});

        when(apia.findObjects((String[]) any(), (NonNegativeInteger) any(), (FieldSearchQuery) any())).thenReturn(
                fieldSearchResult);

        ObjectFields actual = fedoraRepositorySearcher.findResult(pid);
        
        verify(fieldSearchResult, times(0)).getListSession();
        verify(objectFields, times(0)).getLabel();
        verify(objectFields, times(0)).getOwnerId();
        assertEquals(mDate, actual.getMDate());
        assertEquals(cDate, actual.getCDate());
    }

}
