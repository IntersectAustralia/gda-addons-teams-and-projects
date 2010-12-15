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
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.time.DateFormatUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.org.intersect.gda.dto.ProjectOaiDTO;
import au.org.intersect.gda.dto.ResultDTO;
import au.org.intersect.gda.oai.OaiConfig;
import au.org.intersect.gda.oai.OaiIdGenerator;
import au.org.intersect.gda.repository.ProjectRepositoryManager;
import au.org.intersect.gda.repository.RepositoryException;
import au.org.intersect.gda.xml.GenericNamespaceResolver;

/**
 * @version $Rev$
 *
 */
public class FedoraProjectRepositoryManagerImpl implements ProjectRepositoryManager
{
    public static final String DC_IDENTIFIER = "DC";
        
    private final FedoraDatastreamHelper fedoraDsHelper;
    
    private final FedoraXmlTemplateBuilder fedoraRelxExtTemplateBuilder;    
    
    private final FedoraRepositorySetter<ProjectOaiDTO> fedoraProjectRepositorySetter;
    
    private final OaiIdGenerator oaiIdGenerator;
    
    private final OaiConfig oaiConfig;
    
    private XPathFactory xpathFactory;
    
    private DocumentBuilderFactory docBuilderFactory;
    
    
    
    public FedoraProjectRepositoryManagerImpl(
            FedoraDatastreamHelper fedoraDsHelper,
            FedoraRepositorySetter<ProjectOaiDTO> fedoraProjectRepositorySetter,
            FedoraXmlTemplateBuilder fedoraRelxExtTemplateBuilder,   
            OaiIdGenerator oaiIdGenerator,
            OaiConfig oaiConfig)
    {
        this.fedoraDsHelper = fedoraDsHelper;
        this.fedoraProjectRepositorySetter = fedoraProjectRepositorySetter;
        this.fedoraRelxExtTemplateBuilder = fedoraRelxExtTemplateBuilder;
        
        this.oaiConfig = oaiConfig;
        
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        
        this.oaiIdGenerator = oaiIdGenerator;
        
        xpathFactory = XPathFactory.newInstance();
    }

    
    @Override
    public String createProjectInRepository(
            Integer projectId,
            ProjectOaiDTO oaiDTO) throws RepositoryException
    {
    
        String fedoraId = fedoraProjectRepositorySetter.createObjectInRepository(oaiDTO);
        
        String uuId = createRifCsIdentifier();
        
        storeOaiIdentifier(fedoraId, uuId);
        
        if (oaiDTO.isMarkForExport())
        {
            markProjectForExport(projectId, fedoraId);    
        } else
        {
            unmarkProjectExportForExport(fedoraId);
        }
        

        buildProjectInfoDs(projectId, fedoraId, oaiDTO);

        
        return fedoraId;
    }

    
    @Override
    public String updateProjectInRepository(
            Integer projectId,
            String projectFedoraId, 
            ProjectOaiDTO oaiDTO) throws RepositoryException
    {

        if (oaiDTO.isMarkForExport())
        {
            markProjectForExport(projectId, projectFedoraId);    
        } else
        {
            unmarkProjectExportForExport(projectFedoraId);
        }
        buildProjectInfoDs(projectId, projectFedoraId, oaiDTO);

        
        return projectFedoraId;
    }


    /* (non-Javadoc)
     * @see au.org.intersect.gda.metadata.ProjectRepositoryManager#deleteProjectInRepository(java.lang.String)
     */
    @Override
    public boolean deleteProjectInRepository(String projectFedoraId) throws RepositoryException
    {

        this.fedoraProjectRepositorySetter.changeObjectState(projectFedoraId, "D");
    
        return true;
    }
    

    private void unmarkProjectExportForExport(String projectFedoraId) throws FedoraException
    {        
        this.fedoraProjectRepositorySetter.changeObjectState(projectFedoraId, "D");    
      
    }
    
    
    private void markProjectForExport(Integer projectId, String projectFedoraId) throws FedoraException
    {
        this.fedoraProjectRepositorySetter.changeObjectState(projectFedoraId, "A");
        try
        {
            Document relsDoc;
            if (!fedoraDsHelper.dataStreamExists(projectFedoraId, oaiConfig.getRelsExtDsId()))
            {                
                relsDoc = createRelsExt(projectFedoraId);
            } else
            {
                relsDoc = fedoraDsHelper.getDataStream(projectFedoraId, oaiConfig.getRelsExtDsId());
                
                //only need to check existing rels data
                if (getOaiMarked(relsDoc) != null)
                {                
                    //the project is marked currently, leave as it is                       
                    return;
                }
            }             

            
            XPath xpath = xpathFactory.newXPath();
            GenericNamespaceResolver namespaceResolver = new GenericNamespaceResolver(relsDoc);        
            xpath.setNamespaceContext(namespaceResolver);  
            
            String rdfDescriptionPath = oaiConfig.getRelsExtAttachToNodePath();
            XPathExpression xpathExp = xpath.compile(rdfDescriptionPath);
                  
            
            NodeList rdfDescriptionList = (NodeList) xpathExp.evaluate(relsDoc, XPathConstants.NODESET);
            
            Node rdfDescription = rdfDescriptionList.item(0);
            
            Node oaiNode = relsDoc.createElement(oaiConfig.getRelsExtOaiMarkerNodeName());
            
            rdfDescription.appendChild(oaiNode); 
                        
            String oaiIdentifier = this.createOaiIdentifier(projectId);
            Node oaiIdentifierText = relsDoc.createTextNode(oaiIdentifier);

            rdfDescription.appendChild(oaiNode); 
            
            oaiNode.appendChild(oaiIdentifierText);
   
            
            storeRelsExtDs(projectFedoraId, relsDoc);

            
        } catch (XPathExpressionException e)
        {
            throw new FedoraException("Failed to perform xpath on rels-ext document", e);
        }                
    }
    
    private void storeRelsExtDs(String fedoraId, Document relsDoc) throws FedoraException
    {            
        fedoraDsHelper.storeDataStream(
                fedoraId, 
                oaiConfig.getRelsExtDsId(), 
                oaiConfig.getRelsExtDsId(), 
                oaiConfig.getRelsExtMime(), 
                relsDoc);
    }
    
    private Document createRelsExt(String pid) throws FedoraException
    {        
        try
        {            
            String relsExtXmlString = fedoraRelxExtTemplateBuilder.buildXmlTemplate(pid);
            
            Document relsDoc = docBuilderFactory.newDocumentBuilder().parse(
                    new InputSource(new StringReader(relsExtXmlString))); 
            
            return relsDoc;
        } catch (ParserConfigurationException e)
        {
            throw new FedoraException("Failed to create rels-ext document", e);
        } catch (SAXException e)
        {
            throw new FedoraException("Failed to parse rels-ext template", e);

        } catch (IOException e)
        {
            throw new FedoraException("Failed to load rels-ext template source", e);
        }
    }
    
    
    private Node getOaiMarked(Document relsDoc) throws XPathExpressionException
    {
        XPath xpath = xpathFactory.newXPath();

        GenericNamespaceResolver namespaceResolver = new GenericNamespaceResolver(relsDoc);        
        xpath.setNamespaceContext(namespaceResolver);       
        
        String oaiPath = oaiConfig.getRelsExtOaiMarkerNodePath();
        
        XPathExpression xpathExp = xpath.compile(oaiPath);
         
        NodeList nodeList = (NodeList) xpathExp.evaluate(relsDoc, XPathConstants.NODESET);                       
        
        return nodeList.item(0);         
    }
    
    private void buildProjectInfoDs(Integer projectId, String fedoraId, ProjectOaiDTO project) 
        throws FedoraException
    {
        Document doc;
        try
        {
            doc = docBuilderFactory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e)
        {
            throw new FedoraException("Failed to create xml document", e);
        }
        
        Node projectNode = doc.createElement("Project");
        
        doc.appendChild(projectNode);


        String oaiIdentifier = this.getRifCsIdentifier(fedoraId);
        
        createAndAttachTextNode(doc, projectNode, "OaiId", oaiIdentifier);

        
        createAndAttachTextNode(doc, projectNode, "Name", project.getName());
        
        createAndAttachTextNode(doc, projectNode, "Description", project.getDescription());
        
        createAndAttachTextNode(doc, projectNode, "OwnerName", project.getOwner());

        createAndAttachTextNode(doc, projectNode, "OwnerContact", project.getOwnerContact());

        String createDateString = 
            DateFormatUtils.format(project.getDateCreated(), oaiConfig.getXsdTimestampFormat());        
        createAndAttachTextNode(doc, projectNode, "DateCreated", createDateString);
        
        String modifiedDateString = 
            DateFormatUtils.format(project.getDateModified(), oaiConfig.getXsdTimestampFormat());        
        createAndAttachTextNode(doc, projectNode, "DateModified", modifiedDateString);
                
        for (ResultDTO result : project.getResults())
        {
            Node resultNode = buildResultInfoNode(doc, result);
            projectNode.appendChild(resultNode);
        }

        fedoraDsHelper.storeDataStream(
                fedoraId, oaiConfig.getProjectDsId(), oaiConfig.getProjectDsId(), "text/xml", doc);
    }
    
    private void createAndAttachTextNode(Document doc, Node parentNode, String elementName, String text)
    {
        Node nodeWithText = doc.createElement(elementName);       
        
        String textValue;
        if (text != null)
        {
            textValue = text;
        } else
        {
            textValue = "";
        }
        
        Node textNode = doc.createTextNode(textValue);
        nodeWithText.appendChild(textNode);     
        
        parentNode.appendChild(nodeWithText);     
    }
    
    private Node buildResultInfoNode(Document doc, ResultDTO result)
    {
        Node resultNode = doc.createElement("Result");
        
    
        
        createAndAttachTextNode(doc, resultNode, "Name", result.getName());

        createAndAttachTextNode(doc, resultNode, "Type", result.getTypeDisplayName());
        
        createAndAttachTextNode(doc, resultNode, "Id", result.getId());
        
        createAndAttachTextNode(doc, resultNode, "CreatedDate", result.getNiceCreated());

        
        return resultNode;        
    }
    
    
    private void storeOaiIdentifier(String fedoraId, String uuId) throws FedoraException
    {
        
        Document uuIdDoc;
                
        try
        {
            uuIdDoc = docBuilderFactory.newDocumentBuilder().newDocument();
            
            
            createAndAttachTextNode(uuIdDoc, uuIdDoc, oaiConfig.getUuIdNodeName(), uuId);
            
            fedoraProjectRepositorySetter.storeDataStream(
                    fedoraId, oaiConfig.getUuIdDsId(), "uuid", "text/xml", uuIdDoc);
            
        } catch (ParserConfigurationException e)
        {
            throw new FedoraException("Failed to create uuId datastream");
        } 
    }
    
    private String createOaiIdentifier(Integer projectId)
    {
        return oaiIdGenerator.generateId(projectId.toString());
    }
    

    private String createRifCsIdentifier()
    {
        return oaiIdGenerator.generateRifCsId();
    }
    
    
    private String getRifCsIdentifier(String fedoraId) throws FedoraException
    {
        if (!fedoraDsHelper.dataStreamExists(fedoraId, oaiConfig.getUuIdDsId()))
        {                
            String uuId = this.createRifCsIdentifier();
            
            storeOaiIdentifier(fedoraId, uuId);
            
            return uuId;
            
        } else
        {
            Document uuIdDoc = fedoraDsHelper.getDataStream(fedoraId, oaiConfig.getUuIdDsId());
            
            return getUuIdFromDs(uuIdDoc);            
        }             
    }
    
    
    private String getUuIdFromDs(Document uuIdDs) throws FedoraException
    {
        XPath xpath = xpathFactory.newXPath();

        GenericNamespaceResolver namespaceResolver = new GenericNamespaceResolver(uuIdDs);        
        xpath.setNamespaceContext(namespaceResolver);       
        
        String oaiPath = "/" + oaiConfig.getUuIdNodeName() + "/text()";
        
        XPathExpression xpathExp;
        try
        {
            xpathExp = xpath.compile(oaiPath);                
            return (String) xpathExp.evaluate(uuIdDs, XPathConstants.STRING);
        } catch (XPathExpressionException e)
        {
            throw new FedoraException("Failed to extract uuId from ds");
        }
                 
    }

}
