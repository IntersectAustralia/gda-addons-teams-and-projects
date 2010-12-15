/**
 * Copyright (C) Intersect 2010.
 * 
 * This module contains Proprietary Information of Intersect,
 * and should be treated as Confidential.
 *
 * $Id$
 */
package au.org.intersect.gda.repository.fedora;

import fedora.server.types.gen.RelationshipTuple;

/**
 * @version $Rev$
 */
public interface FedoraRepositoryRelationshipManager
{

    // Relationship to define the normal case parent-child relationship 
    // where the child inherits its parents' ancestors
    String IS_DERIVED_FROM  = "GDA:IsDerivedFrom";
    
    // Relationship to define the special case parent-child relationship
    // where a child does not inherit its parents' ancestors
    String IS_AGGREGATED_BY = "GDA:IsAggregatedBy";
    
    /**
     * 
     * @param subject The pid of the object owning the relationship to be created
     * @param relationship The relationship between the subject & the object
     * @param object The target of the relationship
     * @param isLiteral Whether the object is a literal
     * @param dataType The datatype of the object if literal
     * @return True if the relationship is created
     * @throws FedoraException
     * 
     */
    public boolean addRelationship(String subject, 
                                   String relationship, 
                                   String object,
                                   boolean isLiteral,
                                   String dataType) throws FedoraException;
    
    /**
     * 
     * @param subject The pid of the object owning the relationship to be purged
     * @param relationship The relationship between the subject & the object
     * @param object The target of the relationship
     * @param isLiteral Whether the object is a literal
     * @param dataType The datatype of the object if literal
     * @return True if the relationship is created
     * @throws FedoraException
     * 
     */
    public boolean purgeRelationship(String subject, 
                                     String relationship, 
                                     String object,
                                     boolean isLiteral,
                                     String dataType) throws FedoraException;
    
    /**
     * 
     * @param subject The pid of the object owning the relationship
     * @param relationship The relationship
     * @return Array of relationships that match the incoming parameters
     * @throws FedoraException
     * 
     */
    public RelationshipTuple[] getRelationships(String subject,
                                                String relationship) throws FedoraException;
                                        
    
}
