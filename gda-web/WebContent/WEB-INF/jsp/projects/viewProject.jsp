<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring"  uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="gda"  tagdir="/WEB-INF/tags" %>

<sec:authentication var="currentUser" property="principal.username" />

<spring:message var="bulkViewButtonText" code="results.bulk.view.link" text="?Bulk View?"/>
<spring:message var="bulkEditButtonText" code="results.bulk.edit.link" text="?Bulk Edit?"/>


<div class="contentbox_header">
    
	<div class="left">
    	<strong><spring:message code="project.name" text="?name?"/>:</strong> <c:out value="${project.name}"/>
    </div>
	<div class="right">
    	<strong><spring:message code="project.owner" text="?owner?"/>:</strong> <c:out value="${project.owner.username}"/>
    </div>
    <div class="clear_float"></div>
    <div class="left">
        <strong><spring:message code="project.description" text="?description?"/>:</strong><c:out value="${project.description}"/>
    </div>
    <div class="clear_float"></div>
    <div class="left">
        <strong><spring:message code="project.notes" text="?notes?"/>:</strong><c:out value="${project.notes}"/>
    </div>
    <div class="clear_float"></div>
    <div class="left">
        <strong><spring:message code="project.markedForExport" text="?markedForExport?"/>:</strong>
        <c:choose>
        <c:when test="${project.markedForExport}">
            Yes            
         
        </c:when>
        <c:otherwise>
            No
        </c:otherwise>
        </c:choose>
                
    </div>
    <div class="clear_float"></div>
</div>

<div class="contentbox_body">
    <div class="input_group">
        <span class="input_row">
            <spring:message code="project.createdDate" text="?created?"/>
            <fmt:formatDate value="${project.createdDate}" pattern="dd-MM-yyyy HH:mm"/>
        </span>
        <span class="input_row">
            <spring:message code="project.lastModified" text="?modified?"/>
            <fmt:formatDate value="${project.lastModifiedDate}" pattern="dd-MM-yyyy HH:mm"/>
        </span>
    </div>	
    <div id="projectMembersTable" class="search_table_container">
        <h3><spring:message code="projects.editProject.projectUsers" text="?Project Users?"/>:
        
        <input id="search_user_input" class="search_table_input" type="text"/>
        </h3>
        
        <display:table excludedParams="infoNotice" class="results_table search_table_target" name="project.members" sort="list" export="false" uid="projectMembers" requestURI="" defaultsort="1">
        <display:setProperty name="basic.empty.showtable" value="true"/> 
            <display:column titleKey="listTable.user.username" 
                maxLength="60"
                property="username" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>   
            <display:column titleKey="listTable.user.firstName" 
                maxLength="60"
                property="firstName" sortable="true" escapeXml="true" />
            <display:column titleKey="listTable.user.lastName" 
                maxLength="60"
                property="lastName" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
            
            <display:column titleKey="listTable.user.email" 
                maxLength="60"
                property="email" sortable="true" escapeXml="true" />
                                                                                    
        </display:table>       
    </div>
    
    
    
    <div id="projectTeamsTable" class="search_table_container">
        <h3><spring:message code="projects.editProject.projectTeams" text="?Project Teams?"/>:
            <input id="search_team_input" class="search_table_input" type="text"/>
        </h3>
        
        <display:table excludedParams="infoNotice" class="results_table search_table_target" name="project.teams" sort="list" export="false" uid="projectTeams" requestURI="" defaultsort="2">
        <display:setProperty name="basic.empty.showtable" value="true"/> 
            <display:column titleKey="listTable.actions" sortable="false" >
            
                <c:url value="viewTeam" var="viewTeamUrl">
                    <c:param name="id" value="${projectTeams.id}"/>
                </c:url>
                <spring:message var="viewTitle" code="teams.viewTeam.link.title" text="?View Team?"/>               
                <a id="viewTeam${projectTeams.id}" title="${viewTitle}" class="view_button" href="${viewTeamUrl}"></a>
       
            </display:column>
            <display:column titleKey="listTable.team.name" 
                maxLength="60"
                property="name" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator" />
            <display:column titleKey="listTable.team.owner" maxLength="60"
                 property="owner.username" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
            <display:column titleKey="listTable.team.description" maxLength="60"
                property="description" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                                                          
        </display:table>       
    </div>
              

    <div id="projectResultsTable" class="result_search_table_container">
        <h3><spring:message code="projects.editProject.projectResults" text="?Project Results?"/>:
        <input id="search_result_input" class="search_table_input" type="text"/> 
        </h3>
        
        <div class="bulk_action_container">    
            <div class="bulk_select_links">
            <a id="select_all_top" class="select_all_link clickable">Select All</a> / 
            <a id="deselect_all_top" class="deselect_all_link clickable">Deselect All</a>
          </div>    
        <button id="bulk_view_top" class="bulk_view bulk_view_button">${bulkViewButtonText }</button>
        <button id="bulk_edit_top" class="bulk_edit bulk_edit_button">${bulkEditButtonText }</button>
        
        </div>
        <div class="clear_float"></div>
        
      
        
        <display:table excludedParams="infoNotice" class="results_table search_table_target" 
            name="project.results" sort="list" export="false" uid="projectResults" requestURI="" defaultsort="2">
        <display:setProperty name="basic.empty.showtable" value="true"/> 
            <display:column titleKey="listTable.actions">
                
                <gda:resultLink result="${projectResults}"/>
            
            </display:column>
            <display:column titleKey="listTable.result.id" 
                maxLength="60"
                property="id" sortable="true" escapeXml="true" sortProperty="numericId"/>
            <display:column titleKey="listTable.result.name" 
                maxLength="60"
                property="name" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
            <display:column titleKey="listTable.result.type" 
                maxLength="60"
                property="typeDisplayName" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
            <display:column titleKey="listTable.result.modified" property="niceModified" sortable="true" escapeXml="true" sortProperty="lastModifiedDate"/>
            <display:column titleKey="listTable.result.owner" 
                maxLength="60"
                property="owner" sortable="true" escapeXml="true"/>
            
        </display:table>    
        
        <div class="bulk_action_container">   
            <div class="bulk_select_links">
            <a id="select_all" class="select_all_link clickable">Select All</a> / <a id="deselect_all" class="deselect_all_link clickable">Deselect All</a>
          </div>     
        <button id="bulk_view_bottom" class="bulk_view bulk_view_button">${bulkViewButtonText }</button>
        <button id="bulk_edit_bottom" class="bulk_edit bulk_edit_button" >${bulkEditButtonText }</button>
        
        </div>
        <div class="clear_float"></div>
        
        
      
    </div>

</div>

<div class="form_actions">

    <div class="add_variables">
    <sec:authorize access="hasRole('EDIT_ALL_PROJECTS')">
        <c:set var="isAdmin" value="true"/>
    </sec:authorize>

    <button id="edit_project_button"><spring:message code="projects.editProject.link.title" text="?Edit Project?"/></button>

        
    <a href="<c:url value="listProjects"/>"><spring:message code="projects.viewProject.backtoList" text="?Back to list?"/></a>
    </div>
</div>


<c:url var="displayBulkUrl" value="displayResultMeta"/>
<c:url var="editBulkUrl" value="editResultMeta"/>
<form id="bulk_submit_form" class="hidden_field">
    

</form>

<spring:message var="noEditPermissionMessage" code="results.bulk.noEditPermission"/>

<spring:message var="hasChildren" code="result.delete.warnHasChildren"/>
<spring:message var="warningMessage" code="result.delete.warn"/>
<spring:message var="noDeleteMessage" code="notice.info.resultDeleteCancelled" text="?Result Delete Cancelled?"/>

<script type="text/javascript">


    jQuery(document).ready(

            
        function()
        {

            //setup filter for user/team
            var searchTables = jQuery(".search_table_container");

            var setupSearch = function()
            {
                var container = jQuery(this);

                GDA.setupSearch(container);
            };
            
            searchTables.each(setupSearch);

            var resultSearchTable = jQuery(".result_search_table_container");

            var resultLinkControl;
            var afterFilterHide = function(row)
            {
                resultLinkControl.unselectRow(row);
            };

            var resultOpt = 
            {
                    afterHide : afterFilterHide
            };
            
            var searchObject = GDA.setupSearch(resultSearchTable, resultOpt);
            
            
            var editProjectFunc = function()
            {
                window.location = "<c:url value="editProject"><c:param name="id" value="${project.id}"/></c:url>";
            };
            jQuery("#edit_project_button").click(editProjectFunc);


            var viewBulkUrl = "${displayBulkUrl}";
            var editBulkUrl = "${editBulkUrl}";
        
            //do setup table
           var opt = 
           {
                   tableSelector : "#projectResults",
                   hasChildrenMsg : "${hasChildren}",
                   noDeleteMsg : "${noDeleteMessage}",
                   warnDeleteMsg : "${warningMessage}",
                   noEditPermissionMsg : "${noEditPermissionMessage}",
                   viewBulkUrl : viewBulkUrl,
                   editBulkUrl : editBulkUrl,
                   tableSelectOpt : 
                       {
                       tableSelector : "#projectResults"
                       }                                   
           };
           resultLinkControl = new ResultLinkControl(opt);
           
        }
                    
    );
    
</script>