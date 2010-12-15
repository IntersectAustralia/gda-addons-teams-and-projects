<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:m="http://www.gda.intersect.org"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:x="http://www.w3.org/1999/XSL/TransformAlias"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
exclude-result-prefixes="">

<xsl:output method="xml"/>

<!-- The aliax is needed to generate xsl elements with the xsl namespace
without conflicting/being interpreted in the current one -->
<xsl:namespace-alias stylesheet-prefix="x" result-prefix="xsl"/>

<!-- Match simple type, currently only deals with enumerations which 
translates to a select box -->
<xsl:template match="xs:simpleType"> 
    <xsl:variable name="typeName" select="@name"/>
    <x:template name="processAttributeType-{$typeName}">
    <x:param name="useTextarea"/>   
    <x:param name="attrValue"/>
    <x:param name="attrName"/>
    <x:param name="dataState"/>
    <x:param name="description"/>
    <x:param name="displayNameOverride"/>
     
    <xsl:variable name="plugin">
     <xsl:value-of select=".//xs:annotation//*[local-name() = 'plugin']/@value"/> 
        </xsl:variable>

    <xsl:variable name="defaultDisplayName">
         <xsl:choose>
             <xsl:when test="./xs:annotation//*[local-name() = 'name']">
                 <xsl:value-of select="./xs:annotation//*[local-name() = 'name']/text()"/>
             </xsl:when>
             <xsl:otherwise>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:variable>        
	                    
	<x:variable name="displayName">
        <x:choose>
            <x:when test="boolean(string($displayNameOverride))">
                <x:value-of select="$displayNameOverride"/>
            </x:when>
            <x:otherwise>
                 <xsl:choose>
                    <xsl:when test="boolean(string($defaultDisplayName))">
                        <xsl:value-of select="$defaultDisplayName"/>
                    </xsl:when>
                    <xsl:otherwise>
                          <x:value-of select="$attrName"/>                        
                    </xsl:otherwise>
                </xsl:choose>                
            </x:otherwise>
        </x:choose>
    </x:variable>              
     
    <span class="attribute field_level">
    <x:attribute name="data-xmldisplayname">
        <x:call-template name="trim">
           <x:with-param name="s">
               <x:value-of select="$displayName"/>   
           </x:with-param>
        </x:call-template>                                      
    </x:attribute>  
    <x:attribute name="data-xmlnodename">
        <x:call-template name="trim">
           <x:with-param name="s">
               <x:value-of select="$attrName"/>   
           </x:with-param>
        </x:call-template>                                      
    </x:attribute> 
     
        
        <label class="attribute_label">
           <x:value-of select="$displayName"/>
        </label>
        
        
        <xsl:choose>
        <xsl:when test=".//xs:enumeration">
         <!-- Enumeration, do select -->
            <select class="xmlAttribute">    
                <x:attribute name="name">
                    <x:value-of select="$attrName"/>
                </x:attribute>
                <x:attribute name="data-xmldatastate">
                    <x:value-of select="$dataState"/>
                </x:attribute>
                
                <xsl:if test="$plugin"> 
                   <x:attribute name="data-xmlplugin"><xsl:value-of select="$plugin"/></x:attribute>
                </xsl:if>
               
            
                <!-- Empty select option -->
               <option value="">Please select a value</option>
               
               <xsl:for-each select=".//xs:enumeration">
               <option value="{@value}">
               <x:if test="$attrValue = '{@value}'">
                   <x:attribute name="selected">true</x:attribute>
               </x:if>
               <xsl:value-of select="@value"/></option>      
            
               </xsl:for-each>
            </select>     
          
        </xsl:when>
        <xsl:when test=".//xs:minLength">
            <xsl:variable name="typeRestriction">
                <xsl:choose>
                <!-- determines which type it is -->
                    <xsl:when test=".//xs:restriction[@base = 'xs:string']">string</xsl:when>
                    <xsl:when test=".//xs:restriction[@base = 'xs:decimal']">decimal</xsl:when>
                    <xsl:when test=".//xs:restriction[@base = 'xs:double']">double</xsl:when>
                    <xsl:when test=".//xs:restriction[@base = 'xs:integer']">integer</xsl:when>
                </xsl:choose>
            </xsl:variable>
            
            <x:choose>
            <x:when test="$useTextarea = 'true'">
            
            <textarea type="text" class="xmlAttribute"                
               data-xmlinputrestriction="{$typeRestriction}"               
               >             
               <x:attribute name="name">
                    <x:value-of select="$attrName"/>
                </x:attribute>
                <x:attribute name="data-xmldatastate">
                    <x:value-of select="$dataState"/>
                </x:attribute>
               
               
               <x:attribute name="data-xmlminlength">
                    <x:value-of select="{.//xs:minLength/@value}"/>
                </x:attribute>
                <xsl:if test="$plugin"> 
                   <x:attribute name="data-xmlplugin"><xsl:value-of select="$plugin"/></x:attribute>
                </xsl:if>
                
                             
                   <x:value-of select="$attrValue"/>
                   <x:text><xsl:text> </xsl:text></x:text>
                   <!-- space is forceably added to prevent xsl create <textarea/> which
                   will cause rendering error -->                          
            </textarea>
           
            
            </x:when>
            <x:otherwise>          
            
            
            <input type="text" class="xmlAttribute"                
               data-xmlinputrestriction="{$typeRestriction}"               
               >             
               <x:attribute name="name">
                    <x:value-of select="$attrName"/>
                </x:attribute>
                <x:attribute name="data-xmldatastate">
                    <x:value-of select="$dataState"/>
                </x:attribute>
               
               <x:attribute name="value">              
                   <x:value-of select="$attrValue"/>
               </x:attribute>
               <x:attribute name="data-xmlminlength">
                    <x:value-of select="{.//xs:minLength/@value}"/>
                </x:attribute>     
                <xsl:if test="$plugin"> 
                   <x:attribute name="data-xmlplugin"><xsl:value-of select="$plugin"/></x:attribute>
                </xsl:if>      
            </input>
            
            </x:otherwise>
            </x:choose>
        </xsl:when>      
        <xsl:when test=".//xs:pattern">
            <xsl:variable name="typeRestriction">
                <xsl:choose>
                <!-- determines which type it is -->
                    <xsl:when test=".//xs:restriction[@base = 'xs:string']">string</xsl:when>
                    <xsl:when test=".//xs:restriction[@base = 'xs:decimal']">decimal</xsl:when>
                    <xsl:when test=".//xs:restriction[@base = 'xs:double']">double</xsl:when>
                    <xsl:when test=".//xs:restriction[@base = 'xs:integer']">integer</xsl:when>
                </xsl:choose>
            </xsl:variable>
            
            <x:choose>
            <x:when test="$useTextarea = 'true'">
            
            <textarea type="text" class="xmlAttribute" 
                data-xmlpattern="/{.//xs:pattern/@value}/" 
                data-xmlinputrestriction="{$typeRestriction}"        >             
               <x:attribute name="name">
                    <x:value-of select="$attrName"/>
                </x:attribute>
                <x:attribute name="data-xmldatastate">
                    <x:value-of select="$dataState"/>
                </x:attribute>
                <xsl:if test="$plugin"> 
                   <x:attribute name="data-xmlplugin"><xsl:value-of select="$plugin"/></x:attribute>
                </xsl:if>
               
               
                             
                   <x:value-of select="$attrValue"/>
                   <x:text><xsl:text> </xsl:text></x:text>
                   <!-- space is forceably added to prevent xsl create <textarea/> which
                   will cause rendering error -->                          
            </textarea>
            
            </x:when>
            <x:otherwise>          
            
            
            <input type="text" class="xmlAttribute" 
                data-xmlpattern="/{.//xs:pattern/@value}/"
                data-xmlinputrestriction="{$typeRestriction}">             
               <x:attribute name="name">
                    <x:value-of select="$attrName"/>
                </x:attribute>
                <x:attribute name="data-xmldatastate">
                    <x:value-of select="$dataState"/>
                </x:attribute>
               
               <x:attribute name="value">              
                   <x:value-of select="$attrValue"/>
               </x:attribute>
               <xsl:if test="$plugin"> 
                   <x:attribute name="data-xmlplugin"><xsl:value-of select="$plugin"/></x:attribute>
                </xsl:if>
            </input>
            
            </x:otherwise>
            </x:choose>
        </xsl:when>  
        </xsl:choose>
        <x:if test="boolean(string($description))">
            <span class="meta_description">
                <x:value-of select="$description"/>
            </span>
        </x:if>
    </span>
    </x:template>
</xsl:template>


<!-- Match abstract types -->
<xsl:template match="xs:complexType[@abstract='true']">

<xsl:variable name="baseName" select="@name"/>
<xsl:variable name="baseDisplayName">
    <xsl:choose>
        <xsl:when test=".//xs:annotation//*[local-name() = 'name']">
            <xsl:value-of select=".//xs:annotation//*[local-name() = 'name']/text()"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="@name"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<!-- Generate html options template for selecting a type value for this element-->
<x:template name="inputTemplateType-{$baseName}">    
    
    <select name="{$baseName}" class="xmlNode">    
       <xsl:for-each select="//xs:complexType[xs:complexContent/xs:extension[@base=$baseName]]">
       <option value="{@name}">
           <xsl:value-of select="@name"/>
       </option>        
    
       </xsl:for-each>
    </select> 
</x:template>

<!-- Process how to populate this abstract type -->
<x:template name="processElementType-{@name}">    
    <x:param name="noLimit"/>
    <x:param name="atleastOne"/>
    <x:param name="node"/>
    <x:param name="isRecommended"/>
    
    <x:variable name="exist" select="m:{@name}"/>        
    
    
    <div >
        <x:attribute name="data-xmlminoccur">
            <x:value-of  select="$atleastOne"/>
        </x:attribute>
        <x:attribute name="data-xmlmaxoccur">
            <x:value-of select="$noLimit"/> 
        </x:attribute> 
        <x:attribute name="data-xmlrecommended">
            <x:value-of select="$isRecommended"/>
        </x:attribute>
        <x:choose>
	        <x:when test="$atleastOne != '' or $noLimit != '' or $isRecommended != ''">
	            <x:attribute name="class">xmlCardinality</x:attribute>
	        </x:when>
	        <x:otherwise>
	           <x:attribute name="class">xmlIgnoreCardinality</x:attribute>
	        </x:otherwise>
	    </x:choose>
         
    <x:if test="$exist">            
        <!-- Data has element for this abstract type -->
        <x:for-each select="m:{@name}"><!-- Loop through each element -->         
            <x:choose>                                        
             <xsl:for-each select="//xs:complexType[xs:complexContent/xs:extension[@base=$baseName]]"> 
                 <xsl:variable name="typeName" select="@name"/>
                 <x:when test="@xsi:type='{$typeName}'">            
                    <x:call-template name="processElementType-{$typeName}">
                        <x:with-param name="node" select="$node"/>
                        
                    </x:call-template>                          
                 </x:when>                
             </xsl:for-each>           
            </x:choose>         
        </x:for-each>     
    </x:if>                          
    
    <x:if test="not($exist)">
        <!-- Output input template if there can be more than one or the element doesn't exist -->
        <!-- Pass on whether this element is required at this point -->
        <x:call-template name="inputTemplateType-{@name}">
            
        </x:call-template>    
    </x:if>
    
    </div>
    
    
</x:template>


<xsl:for-each select="//xs:complexType[xs:complexContent/xs:extension[@base=$baseName]]">
<xsl:variable name="typeName" select="@name"/>

    <xsl:variable name="defaultDisplayName">
         <xsl:choose>
             <xsl:when test="./xs:annotation//*[local-name() = 'name']">
                 <xsl:value-of select="./xs:annotation//*[local-name() = 'name']/text()"/>
             </xsl:when>
             <xsl:otherwise>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:variable>
     
     <xsl:variable name="defaultDescription">
         <xsl:choose>
             <xsl:when test="./xs:annotation//*[local-name() = 'description']">
                 <xsl:value-of select="./xs:annotation//*[local-name() = 'description']/text()"/>
             </xsl:when>
             <xsl:otherwise>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:variable>
     
    <!-- Template code for complex type element of known concrete type-->              
    
    <x:template name="processElementType-{$typeName}">            
    <x:param name="noLimit"/>
    <x:param name="atleastOne"/>
    <x:param name="node"/>
    <x:param name="nodeName"/>
    <x:param name="isRecommended"/>
    <x:param name="displayNameOverride"/>
    <x:param name="descriptionOverride"/>
        
     <x:variable name="displayName">
        <x:choose>
            <x:when test="boolean(string($displayNameOverride))">
                <x:value-of select="$displayNameOverride"/>
            </x:when>
            <x:otherwise>
                 <xsl:choose>
                    <xsl:when test="boolean(string($defaultDisplayName))">
                        <xsl:value-of select="$defaultDisplayName"/>
                    </xsl:when>
                    <xsl:otherwise>
                          <xsl:value-of select="$typeName"/>                        
                    </xsl:otherwise>
                </xsl:choose>                
            </x:otherwise>
        </x:choose>
    </x:variable>
    
    <x:variable name="description">
        <x:choose>
            <x:when test="boolean(string($descriptionOverride))">
                <x:value-of select="$descriptionOverride"/>
            </x:when>
            <x:otherwise>
                 <xsl:choose>
                    <xsl:when test="boolean(string($defaultDescription))">
                        <xsl:value-of select="$defaultDescription"/>
                    </xsl:when>
                    <xsl:otherwise>
                    </xsl:otherwise>
                </xsl:choose>                
            </x:otherwise>
        </x:choose>
    </x:variable>
    
    <div class="xmlCardinality">
        <x:attribute name="data-xmlminoccur">
            <x:value-of  select="$atleastOne"/>
        </x:attribute>
        <x:attribute name="data-xmlmaxoccur">
            <x:value-of select="$noLimit"/> 
        </x:attribute> 
        <x:attribute name="data-xmlrecommended">
            <x:value-of select="$isRecommended"/> 
        </x:attribute> 
        <x:choose>
            <x:when test="$atleastOne != '' or $noLimit != '' or $isRecommended != ''">
                <x:attribute name="class">xmlCardinality</x:attribute>
            </x:when>
            <x:otherwise>
               <x:attribute name="class">xmlIgnoreCardinality</x:attribute>
            </x:otherwise>
        </x:choose>
        
        <x:variable name="exist" select="$node/*[@xsi:type = '{@name}']"/>

        <x:choose>        
        <x:when test="$exist">    
        <!-- Has existing data -->
            <x:for-each select="$node/*[@xsi:type = '{@name}']">

               <!-- If there's data then it's possible there's multiple of them
               so we must look through them -->
                <div class="section xmlNode field_level" >
                <x:attribute name="data-xmlnodename">
                    <x:value-of select="$nodeName"/>
                </x:attribute>
                <x:attribute name="data-xmltypevalue">
                    <xsl:value-of select="$typeName"/>
                </x:attribute>
                <x:attribute name="data-xmldisplayname">
                    <x:call-template name="trim">
                       <x:with-param name="s">
                           <x:value-of select="$displayName"/>   
                       </x:with-param>
                    </x:call-template>                                      
                </x:attribute>     
                <div class="contentbox_header">
                <strong>
                <x:value-of select="$displayName"/>
                </strong>
                
                
                </div>           
                <div class="contentbox_body"> 
                
                <div class="meta_element_description">
                    <x:value-of select="$description"/>
                    <x:text><xsl:text> </xsl:text></x:text>
                </div>                
                
                <!-- Generate code for element content -->
                <xsl:for-each select=".//xs:sequence/xs:element">               
                    <xsl:call-template name="processElement">
                       <xsl:with-param name="node" select="."/> 
                    </xsl:call-template>           
                </xsl:for-each>                     
                </div>
                </div><!-- end section -->
            </x:for-each>
        </x:when>
        <x:otherwise>
        
               <!-- If there's no data then we just call template once
               to generate a empty template -->
            <div class="section xmlNode field_level">
                <x:attribute name="data-xmlnodename">
                    <x:value-of select="$nodeName"/>
                </x:attribute>
                <x:attribute name="data-xmltypevalue">
                    <xsl:value-of select="$typeName"/>
                </x:attribute>
                <x:attribute name="data-xmldisplayname">
                    <x:call-template name="trim">
                       <x:with-param name="s">
                           <x:value-of select="$displayName"/>   
                       </x:with-param>
                    </x:call-template>       
                </x:attribute>     
                <div class="contentbox_header">
                <strong>
                    <x:value-of select="$displayName"/>
                    </strong>
                </div>            
                <div class="contentbox_body"> 
                <div class="meta_element_description">
                    <x:value-of select="$description"/>
                    <x:text><xsl:text> </xsl:text></x:text>
                </div>  
                <!-- Generate code for element content -->
                <xsl:for-each select=".//xs:sequence/xs:element">               
                    <xsl:call-template name="processElement">
                       <xsl:with-param name="node" select="."/> 
                    </xsl:call-template>           
                </xsl:for-each>                     
                </div>
            </div><!-- End section -->
        </x:otherwise>
        </x:choose>
     </div>   
     </x:template>
</xsl:for-each>
</xsl:template>


<!-- Process element that has child but is not a derived type 
ie. <name>
        <child>
        <child>
    </name>
-->
<xsl:template match="/xs:schema/xs:complexType[not(@abstract) and not(xs:complexContent) and xs:sequence]">

    <xsl:variable name="defaultDisplayName">
         <xsl:choose>
             <xsl:when test="./xs:annotation//*[local-name() = 'name']">
                 <xsl:value-of select="./xs:annotation//*[local-name() = 'name']/text()"/>
             </xsl:when>
             <xsl:otherwise>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:variable>
     
     <xsl:variable name="defaultDescription">
         <xsl:choose>
             <xsl:when test="./xs:annotation//*[local-name() = 'description']">
                 <xsl:value-of select="./xs:annotation//*[local-name() = 'description']/text()"/>
             </xsl:when>
             <xsl:otherwise>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:variable>
            
    <x:template name="processElementType-{@name}">            
    <x:param name="noLimit"/>
    <x:param name="atleastOne"/>
    <x:param name="node"/>
    <x:param name="nodeName"/>
    <x:param name="isRecommended"/>
    <x:param name="displayNameOverride"/>
    <x:param name="descriptionOverride"/>
    
    <x:variable name="displayName">
        <x:choose>
            <x:when test="boolean(string($displayNameOverride))">
                <x:value-of select="$displayNameOverride"/>
            </x:when>
            <x:otherwise>
                 <xsl:choose>
                    <xsl:when test="boolean(string($defaultDisplayName))">
                        <xsl:value-of select="$defaultDisplayName"/>
                    </xsl:when>
                    <xsl:otherwise>
                          <x:value-of select="$nodeName"/>                      
                    </xsl:otherwise>
                </xsl:choose>                
            </x:otherwise>
        </x:choose>
    </x:variable>
    
    <x:variable name="description">
        <x:choose>
            <x:when test="boolean(string($descriptionOverride))">
                <x:value-of select="$descriptionOverride"/>
            </x:when>
            <x:otherwise>
                 <xsl:choose>
                    <xsl:when test="boolean(string($defaultDescription))">
                        <xsl:value-of select="$defaultDescription"/>
                    </xsl:when>
                    <xsl:otherwise>
                    </xsl:otherwise>
                </xsl:choose>                
            </x:otherwise>
        </x:choose>
    </x:variable>
    
    <div class="xmlCardinality">
        <x:attribute name="data-xmlminoccur">
            <x:value-of  select="$atleastOne"/>
        </x:attribute>
        <x:attribute name="data-xmlmaxoccur">
            <x:value-of select="$noLimit"/> 
        </x:attribute> 
        <x:attribute name="data-xmlrecommended">
            <x:value-of select="$isRecommended"/> 
        </x:attribute> 
        <x:choose>
            <x:when test="$atleastOne != '' or $noLimit != '' or $isRecommended != ''">
                <x:attribute name="class">xmlCardinality</x:attribute>
            </x:when>
            <x:otherwise>
               <x:attribute name="class">xmlIgnoreCardinality</x:attribute>
            </x:otherwise>
        </x:choose>
        
        <x:variable name="exist" select="$node/*[name() = $nodeName]"/>

        <x:choose>        
        <x:when test="$exist">    
        <!-- Has existing data -->
            <x:for-each select="$node/*[name() = $nodeName]">

               <!-- If there's data then it's possible there's multiple of them
               so we must look through them -->
                              
                <div class="xmlNode field_level">
                <x:attribute name="data-xmlnodename">
                    <x:value-of select="$nodeName"/>
                </x:attribute>
                <x:attribute name="data-xmldisplayname">
                    <x:call-template name="trim">
                       <x:with-param name="s">
                           <x:value-of select="$displayName"/>   
                       </x:with-param>
                    </x:call-template>       
                </x:attribute>     
                <div class="nzwh-wrapper">
                    <div class="fieldset parameter section">
                    <div class="legend">
                       <x:value-of select="$displayName"/>
                    </div>
                    <div class="content">
                <!-- 
                <fieldset class="parameter section">
                
                <legend>
                    <x:value-of select="$displayName"/>
                </legend>
                 -->
                <div class="meta_element_description">
                    <x:value-of select="$description"/>
                    <x:text><xsl:text> </xsl:text></x:text>
                </div>
                
                    
                 
                <!-- Generate code for element content -->
                <xsl:for-each select=".//xs:sequence/xs:element">               
                    <xsl:call-template name="processElement">
                       <xsl:with-param name="node" select="."/> 
                    </xsl:call-template>           
                </xsl:for-each>                     
                
                <!-- <fieldset> -->      <!-- end section fieldset --> 
                    </div>
                    </div>                    
                    </div><!-- End nzwh wrapper -->        
                </div>
            </x:for-each>
        </x:when>
        <x:otherwise>
        
               <!-- If there's no data then we just call template once
               to generate a empty template -->
            
                
                <div class="xmlNode field_level">
                <x:attribute name="data-xmlnodename">
                    <x:value-of select="$nodeName"/>
                </x:attribute>
                <x:attribute name="data-xmldisplayname">
                    <x:call-template name="trim">
                       <x:with-param name="s">
                           <x:value-of select="$displayName"/>   
                       </x:with-param>
                    </x:call-template>       
                    
                </x:attribute>     
                
                <div class="nzwh-wrapper">
					<div class="fieldset parameter section">
					<div class="legend">
					   <x:value-of select="$displayName"/>
				    </div>
					<div class="content">
                <!-- 
                <fieldset class="parameter section">                
                <legend>
                    <x:value-of select="$displayName"/>
                </legend>        
                 -->
                <div class="meta_element_description">
                    <x:value-of select="$description"/>
                    <x:text><xsl:text> </xsl:text></x:text>
                </div>
                
                <!-- Generate code for element content -->
                <xsl:for-each select=".//xs:sequence/xs:element">               
                    <xsl:call-template name="processElement">
                       <xsl:with-param name="node" select="."/> 
                    </xsl:call-template>           
                </xsl:for-each>   
                <!--                   
                </fieldset>
                 -->
                <!-- end section fieldset -->
	                </div>
	                </div>                    
	                </div><!-- End nzwh wrapper -->                    
                </div>
        </x:otherwise>
        </x:choose>
     </div>   
     </x:template>
</xsl:template>


<!-- Manage non-nested types. those that are not abstract nor have complex content
ie <name attr="" attr=""/>
These are metadata inputs
-->
<xsl:template match="/xs:schema/xs:complexType[not(@abstract) and not(xs:complexContent) and not(xs:sequence)]">

    <xsl:variable name="defaultDisplayName">
         <xsl:choose>
             <xsl:when test="./xs:annotation//*[local-name() = 'name']">
                 <xsl:value-of select="./xs:annotation//*[local-name() = 'name']/text()"/>
             </xsl:when>
             <xsl:otherwise>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:variable>
     
     <xsl:variable name="defaultDescription">
         <xsl:choose>
             <xsl:when test="./xs:annotation//*[local-name() = 'description']">
                 <xsl:value-of select="./xs:annotation//*[local-name() = 'description']/text()"/>
             </xsl:when>
             <xsl:otherwise>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:variable>

    <x:template name="inputTemplateType-{@name}">
            
        <x:call-template name="processElementType-{@name}">
            
        </x:call-template>                
    </x:template>
        
    <x:template name="processElementType-{@name}">    
    
    
    <x:param name="noLimit"/>
    <x:param name="atleastOne"/>
    <x:param name="node"/>
    <x:param name="nodeName"/>
    <x:param name="isRecommended"/>
    <x:param name="displayNameOverride"/>
    <x:param name="descriptionOverride"/>
    
    <x:variable name="displayName">
        <x:choose>
            <x:when test="boolean(string($displayNameOverride))">
                <x:value-of select="$displayNameOverride"/>
            </x:when>
            <x:otherwise>
                 <xsl:choose>
                    <xsl:when test="boolean(string($defaultDisplayName))">
                        <xsl:value-of select="$defaultDisplayName"/>
                    </xsl:when>
                    <xsl:otherwise>
                          <x:value-of select="$nodeName"/>                      
                    </xsl:otherwise>
                </xsl:choose>                
            </x:otherwise>
        </x:choose>
    </x:variable>
    
    <x:variable name="description">
        <x:choose>
            <x:when test="boolean(string($descriptionOverride))">
                <x:value-of select="$descriptionOverride"/>
            </x:when>
            <x:otherwise>
                 <xsl:choose>
                    <xsl:when test="boolean(string($defaultDescription))">
                        <xsl:value-of select="$defaultDescription"/>
                    </xsl:when>
                    <xsl:otherwise>
                    </xsl:otherwise>
                </xsl:choose>                
            </x:otherwise>
        </x:choose>
    </x:variable>
    
    <x:variable name="exist" select="m:{@name}"/>        
    
    <div class="xmlCardinality">
        <x:attribute name="data-xmlminoccur">
            <x:value-of  select="$atleastOne"/>
        </x:attribute>
        <x:attribute name="data-xmlmaxoccur">
            <x:value-of select="$noLimit"/> 
        </x:attribute> 
        <x:attribute name="data-xmlrecommended">
            <x:value-of select="$isRecommended"/> 
        </x:attribute> 
        <x:choose>
            <x:when test="$atleastOne != '' or $noLimit != '' or $isRecommended != ''">
                <x:attribute name="class">xmlCardinality</x:attribute>
            </x:when>
            <x:otherwise>
               <x:attribute name="class">xmlIgnoreCardinality</x:attribute>
            </x:otherwise>
        </x:choose>
     
       <!-- Locate each attribute this element needs -->
            
       <!-- Generate code for attribute content --> 
        <x:variable name="exist" select="m:*[local-name() = $nodeName]"/>      
        <x:choose>
            <x:when test="$exist">          
           <!-- at least one of this simple type node exist -->
           <!-- print a form for each of them -->
           
           <!-- This select the node by the specified node name -->
           <x:for-each select="m:*[local-name() = $nodeName]">
           
           <div class="section xmlNode field_level">
            <x:attribute name="data-xmlnodename">
                <x:value-of select="$nodeName"/>
            </x:attribute>   
            <x:attribute name="data-xmldisplayname">
                <x:call-template name="trim">
                       <x:with-param name="s">
                           <x:value-of select="$displayName"/>   
                       </x:with-param>
                    </x:call-template>       
            </x:attribute>           
                <label>
                    
                        <x:value-of select="$displayName"/>
                    
                </label><!-- end label -->     
                
                <div class="meta_element_description">
                    <x:value-of select="$description"/>
                    <x:text><xsl:text> </xsl:text></x:text>
                </div>
                
                <xsl:variable name="hasMultipleAttribute">
                    <xsl:value-of select="count(xs:attribute) > 1"/>
                </xsl:variable>
                
                <div class="multiple_attribute_{$hasMultipleAttribute}">
                
                  
                
               <xsl:for-each select="xs:attribute">    
                   <xsl:call-template name="processAttribute">
                      <xsl:with-param name="node" select="."/> 
                      
                   </xsl:call-template>                      
               </xsl:for-each>
               
                </div>
                

           </div><!-- end section -->
                  
           </x:for-each>
           </x:when>
           <x:otherwise>
            <div class="section xmlNode field_level">
            <x:attribute name="data-xmlnodename">
                <x:value-of select="$nodeName"/>
            </x:attribute>
            <x:attribute name="data-xmldisplayname">
                <x:call-template name="trim">
                       <x:with-param name="s">
                           <x:value-of select="$displayName"/>   
                       </x:with-param>
                    </x:call-template>       
            </x:attribute>     
                <label>
                    
                        <x:value-of select="$displayName"/>
                    
                </label>          
                <div class="meta_element_description">
                    <x:value-of select="$description"/>
                    <x:text><xsl:text> </xsl:text></x:text>
                </div>
                <xsl:variable name="hasMultipleAttribute">
                    <xsl:value-of select="count(xs:attribute) > 1"/>
                </xsl:variable>
                
                <div class="multiple_attribute_{$hasMultipleAttribute}">
                                           
                <!-- No such node, for each of its allowed attribute output an input form for it -->
               <xsl:for-each select="xs:attribute">    
                   <xsl:call-template name="processAttribute">
                      <xsl:with-param name="node" select="."/> 
                   </xsl:call-template>                      
               </xsl:for-each>       
               </div>
           </div><!-- end section -->         
           </x:otherwise>
       
       </x:choose>
        
        
    </div>
    </x:template>

</xsl:template>




<!-- Attributes corresponds to input type -->
<!-- Attributes takes on the cardinality of their node, as all attributes are 
    assume to be required for an attribute node -->
<xsl:template name="processAttribute">
    <xsl:param name="node"/>
      
    
    <xsl:variable name="allowedType" select="$node/@type"/>
    <xsl:variable name="useTextarea">
    <xsl:choose>
        <xsl:when test="$node//xs:annotation//*[local-name() = 'textarea']">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>        
    </xsl:choose>
    </xsl:variable>        
    
    <xsl:variable name="displayName">
        <xsl:choose>
            <xsl:when test="$node//xs:annotation//*[local-name() = 'name']">
                <xsl:value-of select="$node//xs:annotation//*[local-name() = 'name']/text()"/>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    
    <xsl:variable name="description">
    <xsl:choose>
        <xsl:when test="$node//xs:annotation//*[local-name() = 'description']">
            <xsl:value-of select="$node//xs:annotation//*[local-name() = 'description']/text()"/>
        </xsl:when>
        <xsl:otherwise></xsl:otherwise>        
    </xsl:choose>
    </xsl:variable>        
    
    
    <!-- Determine type -->
    <xsl:variable name="typeRestriction">
        <xsl:choose>
        <!-- determines which type it is -->
        <xsl:when test="$allowedType = 'xs:string'">string</xsl:when>
        <xsl:when test="$allowedType = 'xs:integer'">integer</xsl:when>
        <xsl:when test="$allowedType = 'xs:double'">double</xsl:when>
        <xsl:when test="$allowedType = 'xs:decimal'">decimal</xsl:when>
        <xsl:when test="$allowedType = 'xs:date'">date</xsl:when>                                          
        </xsl:choose>
    </xsl:variable>
    
    <xsl:choose>
        <xsl:when test="boolean(string($typeRestriction))">
            
		    <xsl:variable name="displayNameToApply">
		        <xsl:choose>
		            <xsl:when test="boolean(string($displayName))">
		                <xsl:value-of select="$displayName"/>
		            </xsl:when>
		            <xsl:otherwise>
		                 <xsl:value-of select="$node/@name"/>
		            </xsl:otherwise>
		        </xsl:choose>
		    </xsl:variable>
		    
            <span class="attribute field_level" data-xmldisplayname="{$displayNameToApply}" data-xmlnodename="{$node/@name}">
            
            <label class="attribute_label">
                <xsl:value-of select="$displayNameToApply"/>
            </label>
            
            <xsl:choose>
            <xsl:when test="$useTextarea = 'true'">
                                   
            <!-- Apply a textarea -->
            <textarea type="text" class="xmlAttribute" 
               name="{$node/@name}"
               data-xmlinputrestriction="{$typeRestriction}"
               
               >                                     
               <x:attribute name="data-xmldatastate">              
                   <x:choose><x:when test="not(@{$node/@name})">no_value</x:when><x:when test="boolean(string(@{$node/@name}))">has_value</x:when><x:otherwise>empty_value</x:otherwise></x:choose>
               </x:attribute>
                             
               <x:value-of select="@{$node/@name}"/>    
               
               <x:text><xsl:text> </xsl:text></x:text>
                </textarea>
               
            </xsl:when>
            <xsl:otherwise>       
             <input type="text" class="xmlAttribute" 
               name="{$node/@name}"
               data-xmlinputrestriction="{$typeRestriction}"           
               >             
               
               <x:attribute name="value">              
                   <x:value-of select="@{$node/@name}"/>
               </x:attribute>        
               <x:attribute name="data-xmldatastate">              
                   <x:choose><x:when test="not(@{$node/@name})">no_value</x:when><x:when test="boolean(string(@{$node/@name}))">has_value</x:when><x:otherwise>empty_value</x:otherwise></x:choose>
               </x:attribute>          
            </input>
            
            </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="boolean(string($description))">
                <span class="meta_description">
                    <xsl:value-of select="$description"/>
                </span>
            </xsl:if>
            
        </span>
        </xsl:when>
        <xsl:otherwise>
             <!-- Custom type -->
            
            <!-- 
            eg.
            <xs:simpleType name="valueAFormat">
                <xs:restriction base="xs:string">
                    <xs:enumeration value="valueA-1"/>
                    <xs:enumeration value="valueA-2"/>
                    <xs:enumeration value="valueA-3"/>
                </xs:restriction>
            </xs:simpleType>
                
                
            <xs:complexType name="fileFormat">
               <xs:attribute name="value" type="valueAFormat"/>           
            </xs:complexType>           
             -->
            
                <x:call-template name="processAttributeType-{$allowedType}">
                    
                    <x:with-param name="attrName">
                        <xsl:value-of select="$node/@name"/>
                    </x:with-param>
                    <x:with-param name="useTextarea">
                        <xsl:value-of select="$useTextarea"/>
                    </x:with-param>            
                    <x:with-param name="attrValue" select="@{$node/@name}"/>
                    <xsl:if test="boolean(string($description))">
                      <x:with-param name="description">
                          <xsl:value-of select="$description"/>
                      </x:with-param>
                    </xsl:if>
                    <x:with-param name="dataState">
                    <x:choose><x:when test="not(@{$node/@name})">no_value</x:when><x:when test="boolean(string(@{$node/@name}))">has_value</x:when><x:otherwise>empty_value</x:otherwise></x:choose>
                    </x:with-param>           
                    <x:with-param name="displayNameOverride">
                        <xsl:value-of select="$displayName"/>
                    </x:with-param> 
                </x:call-template>
        </xsl:otherwise>    
    </xsl:choose>        
    
    
</xsl:template>

    
<xsl:template name="processElement">
    <xsl:param name="node"/> 
    
    <xsl:variable name="elementName" select="$node/@name"/>
    <xsl:variable name="noLimit" select="$node/@maxOccurs"/>
    <xsl:variable name="atleastOne" select="$node/@minOccurs"/>
    <xsl:variable name="allowedType" select="$node/@type"/>
    
    <xsl:variable name="displayName">
        <xsl:choose>
            <xsl:when test="$node/xs:annotation//*[local-name() = 'name']">
                <xsl:value-of select="$node//xs:annotation//*[local-name() = 'name']/text()"/>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="description">
        <xsl:choose>
            <xsl:when test="$node/xs:annotation//*[local-name() = 'description']">
                <xsl:value-of select="$node//xs:annotation//*[local-name() = 'description']/text()"/>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    
    <xsl:variable name="isRecommended"><xsl:choose>
        <xsl:when test="$node//xs:annotation//*[local-name() = 'recommended']">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose></xsl:variable>            
    
    
    
    <xsl:choose>
    <xsl:when test="//xs:simpleType[@name=$allowedType]">
    <!-- A simple type element -->
        <x:call-template name="processElementType-{$allowedType}">
            <x:with-param name="noLimit">
                <xsl:value-of select="$noLimit"/>
            </x:with-param>
            <x:with-param name="atleastOne">
                <xsl:value-of select="$atleastOne"/>
            </x:with-param>
            <x:with-param name="elementName">
               <xsl:value-of select="$elementName"/>
            </x:with-param>
            <x:with-param name="isRecommended">
               <xsl:value-of select="$isRecommended"/>
            </x:with-param>
            <x:with-param name="displayNameOverride">
               <xsl:value-of select="$displayName"/>
            </x:with-param>
            <x:with-param name="descriptionOverride">
               <xsl:value-of select="$description"/>
            </x:with-param>
        </x:call-template>
    </xsl:when>
    <xsl:otherwise>

    <x:call-template name="processElementType-{$allowedType}">
        <x:with-param name="node" select="."/>
        <x:with-param name="nodeName"><xsl:value-of select="$elementName"/></x:with-param>
        <x:with-param name="noLimit"><xsl:value-of select="$noLimit"/></x:with-param>
        <x:with-param name="atleastOne">
            <xsl:value-of select="$atleastOne"/>
        </x:with-param>
        <x:with-param name="isRecommended">
           <xsl:value-of select="$isRecommended"/>
        </x:with-param>        
        <x:with-param name="displayNameOverride">
               <xsl:value-of select="$displayName"/>
        </x:with-param>
        <x:with-param name="descriptionOverride">
               <xsl:value-of select="$description"/>
        </x:with-param>
    </x:call-template>          
    
    </xsl:otherwise>    
    </xsl:choose>
    
</xsl:template>


<!-- locate data root -->
<xsl:template match="xs:schema/xs:element">
    <x:template match="/">    
        
        <x:variable name="node" select="."/>
        
        <xsl:choose>
        <xsl:when test="@type">
        <!-- If root element has a type then process this as a node-->
        <!-- 
        This is if the root element has a type reference
        ie.        
         <complexType type="typeA>
                <element/>
                <element/>
            </complexType>
        <element type="typeA"/>
           

         -->
         
            <div class="root xmlRoot"> 
            <xsl:call-template name="processElement">
                <xsl:with-param name="node" select="."/> 
            </xsl:call-template>      
            </div>
        </xsl:when>
        <xsl:otherwise>
        
        <!-- Locate elements defined in the childs -->
        <!-- 
        This is if the root element is an inline type def
        ie.
        <element>
            <complexType type="typeA>
                <element/>
                <element/>
            </complexType>
        </element>
         -->
        
        <div class="root xmlRoot">
        <xsl:attribute name="data-xmlnodename"><xsl:value-of select="@name"/></xsl:attribute>
        <span><xsl:value-of select="@name"/></span>
        
        <xsl:for-each select=".//xs:element">
            
            <xsl:call-template name="processElement">
                <xsl:with-param name="node" select="."/> 
            </xsl:call-template>
        
        </xsl:for-each>
        </div>     
        </xsl:otherwise>   
        </xsl:choose>
    </x:template>
</xsl:template>

<!-- 
XSL Root
The generated XSl starts from here
-->
<xsl:template match="/xs:schema" >  
    <!-- The output does not need the schema namespaces so we tell the xsl
    to not include them, functionally this
    has no effect, only cleanliness of result output -->
    <x:stylesheet exclude-result-prefixes="xs m">        
    <xsl:attribute name="version">2.0</xsl:attribute>    
<x:template name="left-trim">
  <x:param name="s" />
  <x:choose>
    <x:when test="substring($s, 1, 1) = ''">
      <x:value-of select="$s"/>
    </x:when>
    <x:when test="normalize-space(substring($s, 1, 1)) = ''">
      <x:call-template name="left-trim">
        <x:with-param name="s" select="substring($s, 2)" />
      </x:call-template>
    </x:when>
    <x:otherwise>
      <x:value-of select="$s" />
    </x:otherwise>
  </x:choose>
</x:template>

<x:template name="right-trim">
  <x:param name="s" />
  <x:choose>
    <x:when test="substring($s, 1, 1) = ''">
      <x:value-of select="$s"/>
    </x:when>
    <x:when test="normalize-space(substring($s, string-length($s))) = ''">
      <x:call-template name="right-trim">
        <x:with-param name="s" select="substring($s, 1, string-length($s) - 1)" />
      </x:call-template>
    </x:when>
    <x:otherwise>
      <x:value-of select="$s" />
    </x:otherwise>
  </x:choose>
</x:template>

<x:template name="trim">
  <x:param name="s" />
  <x:call-template name="right-trim">
    <x:with-param name="s">
      <x:call-template name="left-trim">
        <x:with-param name="s" select="$s" />
      </x:call-template>
    </x:with-param>
  </x:call-template>
</x:template>

        
        
        <xsl:apply-templates/>                        
    </x:stylesheet>
</xsl:template>

</xsl:stylesheet>