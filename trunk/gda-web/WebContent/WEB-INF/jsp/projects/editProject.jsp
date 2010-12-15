<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring"  uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib prefix="gda"  tagdir="/WEB-INF/tags" %>


<tiles:importAttribute name="pageMode" scope="request"/>

<sec:authentication var="currentUser" property="principal.username" />

<spring:message var="bulkViewButtonText" code="results.bulk.view.link" text="?Bulk View?"/>
<spring:message var="bulkEditButtonText" code="results.bulk.edit.link" text="?Bulk Edit?"/>

<c:set var="doFullEdit" value="false"/>
<c:choose>
	<c:when test="${pageMode == 'create'}">
		<c:set var="formAction">createProject</c:set>
        <c:set var="doFullEdit" value="true"/>                
	</c:when>
	<c:otherwise>		
        <sec:authentication var="currentUser" property="principal.username" />
         <sec:authorize access="hasRole('EDIT_ALL_PROJECTS')">
            <c:set var="isAdmin" value="true"/>
        </sec:authorize>        
        <c:choose>
        <c:when test="${currentUser==project.owner.username || isAdmin}">
            <c:set var="doFullEdit" value="true"/>
            <c:set var="formAction">editProject</c:set>       
        </c:when>
        <c:otherwise>
            <c:set var="formAction">editProjectRestricted</c:set>   
        </c:otherwise>
        </c:choose>                        
	</c:otherwise>
</c:choose>

<form:form modelAttribute="project" method="post" action="${formAction}" autocomplete="off">
	<form:hidden path="id"/>
<div class="contentbox_header"><strong>Project Information</strong></div>
<div class="contentbox_body" style="">
	    <div class="nzwh-wrapper">
            <div class="fieldset parameter section">
           
            <div class="content">
		    
		    <form:errors path="*" cssClass="notification" element="p"/>
		    
		    <div class="input_group">
		        <form:label path="name">
		            <spring:message code="project.name" text="?name?"/>
		        </form:label>
                
                <c:choose>
                <c:when test="${doFullEdit }">
    		        <form:input path="name" cssErrorClass="input_error" maxlength="50"/> 
                    
                    <span class="hint">
    		        <spring:message code="projects.editProject.nameHint" text="?Give a name?"/>
                    </span>
                </c:when>
                <c:otherwise>
                    <c:out value="${project.name}"/>
                    <form:hidden path="name"/>
                </c:otherwise>
                </c:choose>
		    </div>
            <div class="input_group">
                <form:label path="description">
                    <spring:message code="project.description" text="?description?"/>
                </form:label>
                
                <c:choose>
                <c:when test="${doFullEdit }">
                    <form:input path="description" cssErrorClass="input_error" maxlength="200"/> 
                    
                    <span class="hint">
                    <spring:message code="projects.editProject.descriptionHint" text="?Give description?"/>
                    </span>
                </c:when>
                <c:otherwise>
                    <span>
                        <c:out value="${project.description}"/>
                    </span>
                    <form:hidden path="description"/>
                </c:otherwise>
                </c:choose>
            </div>
            <div class="input_group">
                <form:label path="notes">
                    <spring:message code="project.notes" text="?notes?"/>
                </form:label>
                
                <c:choose>
                <c:when test="${doFullEdit }">
                    <form:textarea path="notes" cssErrorClass="input_error" />                   
                </c:when>
                <c:otherwise>
                    <c:out value="${project.notes}"/>
                    <form:hidden path="notes"/>
                </c:otherwise>
                </c:choose>
            </div>
             <div class="input_group">
                <form:label path="markedForExport">
                    <spring:message code="project.markedForExport" text="?markedForExport?"/>
                </form:label>
                
                <c:choose>
                <c:when test="${doFullEdit }">
                    <form:checkbox id="markedForExport" path="markedForExport" cssErrorClass="input_error" />
                  
                </c:when>
                <c:otherwise>
                    <c:out value="${project.markedForExport}"/>
                    <form:hidden path="markedForExport"/>
                    
                </c:otherwise>
                </c:choose>
            </div>
	</div>
    </div>
    </div><!-- end faux fieldset -->
</div>	
	
    
    <c:choose>
    <c:when test="${doFullEdit}">
    <div class="contentbox_header">
        <strong><spring:message code="projects.editProject.sectionTitle"/></strong>
    </div>
	<div id="user_check_table_container" class="contentbox_body check_table_wrap">
    

		<div id="projectUsersTable" class="clear_float">
            <h4><spring:message code="projects.editProject.projectUsers" text="?Project Users?"/>:
            <input id="project_user_search_input" class="search_table_input" type="text"/>
            </h4>
            <div class="scroll_table_outer">
            <div class="scroll_table_inner">           
            <div class="bulk_action_container"> 
                <div class="bulk_select_links">
                <a id="select_all_user_top" class="select_all_user_link clickable">Select All</a> / 
                <a id="deselect_all_user_top" class="deselect_all_user_link clickable">Deselect All</a>
              </div>        
                <button id="add_more_user" class="add_more_user">
                <spring:message 
                        code="projects.edit.addUsers" 
                        text="?Add Users?"/>        
                </button>
                <spring:message var="bulkRemoveUserMsg" 
                    code="projects.edit.bulkRemoveUser.link.title" 
                    text="?Remove Selected?"/>
                                
                <button id="bulk_remove_user_top" class="remove_user_button"
                    >
                    ${bulkRemoveUserMsg }
                </button>
            </div>
            <div class="clear_float"></div>
            
            <display:table class="results_table fixed_table check_table_to search_table_target" 
                name="selectedUserList" sort="list" export="false" uid="selectedUserTable" requestURI="" defaultsort="2">
            <display:setProperty name="basic.empty.showtable" value="true"/> 
                <display:setProperty name="basic.msg.empty_list" value=""/>
                <display:setProperty name="basic.msg.empty_list_row" value=""/>  
                <display:column>
                    <input type="hidden" name="selectedUser" value="${selectedUserTable.username}"/>                        
                    <span class="row_identifier hidden_field" data-rowid="${selectedUserTable.username}">select_user_${selectedUserTable.username}</span>              
                </display:column>
                
                <display:column titleKey="listTable.user.username" 
                    maxLength="60"
                    property="username" sortable="false" escapeXml="true"/>   
                <display:column titleKey="listTable.user.firstName"
                    maxLength="60" 
                    property="firstName" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                <display:column titleKey="listTable.user.lastName"
                    maxLength="60" 
                    property="lastName" sortable="false"decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>                
                <display:column titleKey="listTable.user.email"
                    maxLength="60" 
                    property="email" sortable="false" escapeXml="true" sortProperty="lastModifiedDate"/>
                                                                                        
            </display:table>    
            
            <div class="bulk_action_container">
                <div class="bulk_select_links">
                <a id="select_all_user_bottom" class="select_all_user_link clickable">Select All</a> / 
                <a id="deselect_all_user_bottom" class="deselect_all_user_link clickable">Deselect All</a>
              </div>        
                <button id="add_more_user" class="add_more_user">
                <spring:message 
                        code="projects.edit.addUsers" 
                        text="?Add Users?"/>        
                </button>
                <spring:message var="bulkRemoveUserMsg" 
                    code="projects.edit.bulkRemoveUser.link.title" 
                    text="?Remove Selected?"/>
                                
                <button id="bulk_remove_user_bottom" class="remove_user_button"
                    >
                    ${bulkRemoveUserMsg }
                </button>
            </div>
            <div class="clear_float"></div>
            
            </div>
            </div><!-- end scroll outer -->
	    </div>
	    
		<div class="clear_float"></div>
	</div>
    </c:when>
    <c:otherwise>
    <div class="contentbox_header">
        <strong><spring:message code="projects.editProject.viewUserTitle"/></strong>
    </div>
    <div id="projectUsersTable" class="search_table_container contentbox_body">
        <h3><spring:message code="projects.editProject.projectUsers" text="?Project Users?"/>:
        <input id="project_user_search_input" class="search_table_input" type="text"/>
        </h3>
        <div class="scroll_table_outer">
        <div class="scroll_table_inner">       
        <display:table excludedParams="infoNotice" class="results_table fixed_table search_table_target" 
            name="project.members" sort="list" export="false" uid="selectedUserTable" 
            requestURI="" defaultsort="2">
        <display:setProperty name="basic.empty.showtable" value="true"/> 
            <display:column titleKey="listTable.user.username" 
                maxLength="60"
                property="username" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>   
            <display:column titleKey="listTable.user.firstName" 
                maxLength="60"
                property="firstName" sortable="false" escapeXml="true" />
            <display:column titleKey="listTable.user.lastName" 
                maxLength="60"
                property="lastName" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
            
            <display:column titleKey="listTable.user.email" 
                maxLength="60"
                property="email" sortable="false" escapeXml="true" sortProperty="lastModifiedDate"/>
                                                                                    
        </display:table>       
        </div>
        </div><!-- end scroll outer -->
    </div>
       
    </c:otherwise>
    </c:choose>
    
    
    <!-- Team Select -->

    
   
    
    <c:choose>
    <c:when test="${doFullEdit}">
     <div class="contentbox_header">
        <strong><spring:message code="projects.editProject.editTeamTitle" text="?Edit Team?"/></strong>
        
    </div>

    <div id="team_check_table_container" class="contentbox_body check_table_wrap">
                
        
        
        <div id="projectTeamsTable" class="clear_float">
            <h4><spring:message code="projects.editProject.projectTeams" text="?Project Teams?"/>:
            <input id="project_team_search_input" class="search_table_input" type="text"/>
            </h4>
        <div class="scroll_table_outer">
        <div class="scroll_table_inner">   
        <div class="bulk_action_container">
            <div class="bulk_select_links">
                <a id="select_all_team_top" class="select_all_team_link clickable">Select All</a> / 
                <a id="deselect_all_team_top" class="deselect_all_team_link clickable">Deselect All</a>
              </div>    
            <button id="add_more_team" class="add_more_team">
            <spring:message 
                    code="projects.edit.addTeams" 
                    text="?Add Teams?"/>        
            </button>
            <spring:message var="bulkRemoveTeamMsg" 
                code="projects.edit.bulkRemoveTeam.link.title" 
                text="?Remove Selected?"/>
                        
            <button id="bulk_remove_team_top" class="remove_team_button"
                >
                ${bulkRemoveTeamMsg }
            </button>
            </div>
            <div class="clear_float"></div>
            
               
            <display:table class="results_table fixed_table check_table_to search_table_target" 
                name="selectedTeamList" sort="list" export="false" uid="selectedTeamTable" requestURI="" defaultsort="2">
            <display:setProperty name="basic.empty.showtable" value="true"/> 
                <display:setProperty name="basic.msg.empty_list" value=""/>
                <display:setProperty name="basic.msg.empty_list_row" value=""/>  
                <display:column class="action_column" headerClass="action_column">                
                    <input type="hidden" name="selectedTeam" value="${selectedTeamTable.id}"/>                        
                    <span class="row_identifier hidden_field" data-rowid="${selectedTeamTable.id}">select_team_${selectedTeamTable.id}</span>   
                    
                    
                    <c:url value="editTeam" var="editUrl">
                        <c:param name="id" value="${selectedTeamTable.id}"/>
                    </c:url>
                    <c:url value="viewTeam" var="viewUrl">
                        <c:param name="id" value="${selectedTeamTable.id}"/>
                    </c:url>
                    <c:url value="deleteTeam" var="deleteUrl">
                        <c:param name="id" value="${selectedTeamTable.id}"/>
                    </c:url>
                    
                    <spring:message var="editTitle" code="teams.editTeam.link.title" text="?Edit Team?"/>
            
                    <c:if test="${currentUser==selectedTeamTable.owner.username || canEditAll}">
                    <a id="editTeam${selectedTeamTable.id}" title="${editTitle}" class="action_button edit_button" href="${editUrl}"></a>
                    </c:if>
                    
                    <c:if test="${currentUser==selectedTeamTable.owner.username || canDeleteAll}">            
                        <spring:message var="deleteTitle" code="teams.deleteTeam.link.title" text="?Delete Team?"/>            
                        <a id="deleteTeam${selectedTeamTable.id}" title="${deleteTitle}" class="action_button delete_icon delete_team" href="${deleteUrl}"></a>
                    </c:if>
                    <spring:message var="viewTitle" code="teams.viewTeam.link.title" text="?View Team?"/>
                    <a id="viewTeam${selectedTeamTable.id}" title="${viewTitle}" class="action_button view_button" href="${viewUrl}"></a>
                           
                </display:column>
                                                     
                <display:column  class="wider" headerClass="wider"
                    titleKey="listTable.team.name" maxLength="60"
                    property="name" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator" />
                <display:column  class="wider" headerClass="wider"
                    titleKey="listTable.team.owner" property="owner.username" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                <display:column  class="wider" headerClass="wider"
                    titleKey="listTable.team.description" maxLength="60"
                    property="description" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>                                                                                                    
            </display:table>   
            
            
            <div class="bulk_action_container">
                <div class="bulk_select_links">
                <a id="select_all_team_bottom" class="select_all_team_link clickable">Select All</a> / 
                <a id="deselect_all_team_bottom" class="deselect_all_team_link clickable">Deselect All</a>
              </div>        
                <button id="add_more_team" class="add_more_team">
                <spring:message 
                        code="projects.edit.addTeams" 
                        text="?Add Teams?"/>        
                </button>
                <spring:message var="bulkRemoveTeamMsg" 
                    code="projects.edit.bulkRemoveTeam.link.title" 
                    text="?Remove Selected?"/>
                            
                <button id="bulk_remove_team_bottom" class="remove_team_button"
                    >
                    ${bulkRemoveTeamMsg }
                    
                </button>
            </div>
            <div class="clear_float"></div>
             
        </div>
        </div><!-- end scroll outer -->
        </div>
        
        <div class="clear_float"></div>
    </div>
    </c:when>
    <c:otherwise>
    <div class="contentbox_header">
        <strong><spring:message code="projects.editProject.viewTeamTitle" text="?View Team?"/></strong>
        
    </div>
    <div id="projectTeamsTable" class="search_table_container contentbox_body" >
        <h3><spring:message code="projects.editProject.projectTeams" text="?Project Teams?"/>:
        <input id="project_team_search_input" class="search_table_input" type="text"/>
        </h3>
        <div class="scroll_table_outer">
        <div class="scroll_table_inner">           
        
        <c:set var="selectedTeamList" value="${project.teams}"/>   
          
            <display:table excludedParams="infoNotice" 
                class="results_table fixed_table search_table_target" 
                name="selectedTeamList" sort="list" export="false" 
                uid="selectedTeamTable" requestURI="" defaultsort="1">
            <display:setProperty name="basic.empty.showtable" value="true"/> 
        
                <display:column  class="wider" headerClass="wider"
                    titleKey="listTable.team.name" maxLength="60"
                    property="name" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator" />
                <display:column  class="wider" headerClass="wider"
                    titleKey="listTable.team.owner" property="owner.username" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                <display:column  class="wider" headerClass="wider"
                    titleKey="listTable.team.description" maxLength="60"
                    property="description" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                                                              
            </display:table>   
            
        
        </div>
        </div><!-- end scroll outer -->
        
    </div>
       
    </c:otherwise>
    </c:choose>
    
    <!-- Result Select -->
    
    
    <div class="contentbox_header">
        <strong><spring:message code="projects.editProject.editResultTitle" text="?Edit Result?"/></strong>
        <span class="hint">You may only add/remove results which you own</span>
    </div>
    
    <div id="result_check_table_container" class="contentbox_body check_table_wrap">
        
        <div id="projectResultsTable" class="clear_float">
            <h4><spring:message code="projects.editProject.projectResults" text="?Project Results?"/>:
            <input id="project_result_search_input" class="search_table_input" type="text"/>
            </h4>
            <div class="scroll_table_outer">
            <div class="scroll_table_inner">      
            <div class="bulk_action_container">
                
                    <div class="bulk_select_links">
                        <a id="select_all_result" class="select_all_result_link clickable">Select All</a> / 
                        <a id="deselect_all_result" class="deselect_all_result_link clickable">Deselect All</a>
                      </div>
                      
                    <button id="add_more_result" class="add_more_result">
                    <spring:message 
                            code="projects.edit.addResults" 
                            text="?Add Results?"/>        
                    </button>
                    <spring:message var="bulkRemoveResultMsg" code="projects.edit.bulkRemoveResult.link.title" 
                        text="?Remove Selected?"/>
                    <button id="bulk_remove_result_top" class="remove_result_button" 
                        >
                        ${bulkRemoveResultMsg }
                    </button>
                    
                    <button id="bulk_view_top" class="bulk_view bulk_view_button">${bulkViewButtonText }</button>
                    <button id="bulk_edit_top" class="bulk_edit bulk_edit_button">${bulkEditButtonText }</button>
                    
                    
                    </div>
                    <div class="clear_float"></div>
                     
                              
                <display:table class="results_table fixed_table  check_table_to search_table_target" 
                    name="selectedResultList" sort="list" export="false" 
                    uid="selectedResultTable" requestURI="" defaultsort="2">
                <display:setProperty name="basic.empty.showtable" value="true"/> 
                <display:setProperty name="basic.msg.empty_list" value=""/>
                <display:setProperty name="basic.msg.empty_list_row" value=""/>
                    <display:column class="action_column" headerClass="action_column">
                        
                        <input type="hidden" name="selectedResult" value="${selectedResultTable.id}"/>                        
                        
                        <gda:resultLink result="${selectedResultTable}"/>
                    </display:column>
                    <display:column class="wider" headerClass="wider" 
                        titleKey="listTable.result.id" property="id" sortable="false" escapeXml="true" sortProperty="numericId"/>
                    <display:column class="wider" headerClass="wider" 
                    titleKey="listTable.result.name" property="name" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                    <display:column class="wider" headerClass="wider" 
                    titleKey="listTable.result.type" property="typeDisplayName" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                    <display:column class="wider" headerClass="wider" 
                    titleKey="listTable.result.modified" property="niceModified" sortable="false" escapeXml="true" sortProperty="lastModifiedDate"/>
                    <display:column class="wider" headerClass="wider"
                    titleKey="listTable.result.owner" property="owner" sortable="false" escapeXml="true"/>
                </display:table>    
                
                <div class="bulk_action_container">
                    <div class="bulk_select_links">
                    <a id="select_all_result" class="select_all_result_link clickable">Select All</a> / 
                    <a id="deselect_all_result" class="deselect_all_result_link clickable">Deselect All</a>
                  </div>
                  
                    <button id="add_more_result" class="add_more_result">
                    <spring:message 
                            code="projects.edit.addResults" 
                            text="?Add Results?"/>        
                    </button>
                    <spring:message var="bulkRemoveResultMsg" code="projects.edit.bulkRemoveResult.link.title" 
                        text="?Remove Selected?"/>
                    <button id="bulk_remove_result_bottom" class="remove_result_button">
                        ${bulkRemoveResultMsg }
                    </button>
                    <button id="bulk_view_bottom" class="bulk_view bulk_view_button" >
                        ${bulkViewButtonText}
                    </button>
                    <button id="bulk_edit_bottom" class="bulk_edit bulk_edit_button" >
                        ${bulkEditButtonText}
                    </button>
                    
                    
                    </div>
                    <div class="clear_float"></div>
                    
                    
                   
                  
            </div>
            </div>
        </div>
        
        <div class="clear_float"></div>
    </div>

	
	<div class="form_actions">
    <div class="add_variables">
	    <input type="button" class="submit_button" name="_submit" value="Save Project" id="submit_button" />
	    
        
        <input id="form_hidden_submit" type="hidden" name="" value="" />
        <input id="form_hidden_redirect" type="hidden" name="redirect" value="" />
        <a id="cancel_submit" class="clickable">Cancel</a>
    </div>        
	</div>
</form:form>

<c:if test="${doFullEdit }">
<div class="hidden_field">
    <!-- Hidden teams -->
<div id="lightbox_all_teams" class="lightbox_target_container search_potential_parent_container lightbox_tall">
<div class="lightbox_target_inner_wrap">
    <div class="lightbox_target_header">
    
    </div>
    <div class="lightbox_target_content">
    
    <div id="allTeamsTable" class="">
        <h4><spring:message code="projects.editProject.allTeams" text="?All Teams?"/>:
            <input id="all_team_search_input" class="search_table_input" type="text"/>
        </h4>
        
        <display:table excludedParams="infoNotice" class="results_table check_table_from search_table_target" 
            name="allTeamList" sort="list" export="false" 
            uid="allTeamTable" requestURI="" defaultsort="2">
        <display:setProperty name="basic.empty.showtable" value="true"/> 
            <display:setProperty name="basic.msg.empty_list" value=""/>
            <display:setProperty name="basic.msg.empty_list_row" value=""/>  
            <display:column class="action_column" headerClass="action_column">
                <input type="hidden" name="selectedTeam" value="${allTeamTable.id}"/>                        
                <span class="row_identifier hidden_field" data-rowid="${allTeamTable.id}">select_team_${allTeamTable.id}</span>    
                
                <c:url value="editTeam" var="editUrl">
                    <c:param name="id" value="${allTeamTable.id}"/>
                </c:url>
                <c:url value="viewTeam" var="viewUrl">
                    <c:param name="id" value="${allTeamTable.id}"/>
                </c:url>
                <c:url value="deleteTeam" var="deleteUrl">
                    <c:param name="id" value="${allTeamTable.id}"/>
                </c:url>
                
                <spring:message var="editTitle" code="teams.editTeam.link.title" text="?Edit Team?"/>
        
                <c:if test="${currentUser==allTeamTable.owner.username || canEditAll}">
                <a id="editTeam${allTeamTable.id}" title="${editTitle}" class="action_button edit_button" href="${editUrl}"></a>
                </c:if>
                
                <c:if test="${currentUser==allTeamTable.owner.username || canDeleteAll}">            
                    <spring:message var="deleteTitle" code="teams.deleteTeam.link.title" text="?Delete Team?"/>            
                    <a id="deleteTeam${allTeamTable.id}" title="${deleteTitle}" class="action_button delete_icon delete_team" href="${deleteUrl}"></a>
                </c:if>
                <spring:message var="viewTitle" code="teams.viewTeam.link.title" text="?View Team?"/>
                <a id="viewTeam${allTeamTable.id}" title="${viewTitle}" class="action_button view_button" href="${viewUrl}"></a>
                      
            </display:column>                   
            <display:column titleKey="listTable.team.name" 
                property="name" maxLength="60"
                sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator" />
            <display:column titleKey="listTable.team.owner" 
                maxLength="60"
                property="owner.username" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
            <display:column titleKey="listTable.team.description" 
                maxLength="60"
                property="description" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>                                                                                                        
        </display:table>               
    </div>
    </div>
        <div class="lightbox_target_footer">
        </div>
    </div>
    </div>
    
    
<div id="lightbox_all_users" class="lightbox_target_container search_potential_parent_container lightbox_tall">
<div class="lightbox_target_inner_wrap">
    <div class="lightbox_target_header">
    
    </div>
    <div class="lightbox_target_content">    
    <div id="allUsersTable" class="">
            <h4><spring:message code="projects.editProject.allUsers" text="?All Users?"/>:
            <input id="all_user_search_input" class="search_table_input" type="text"/>
            </h4>
            
            <display:table excludedParams="infoNotice" 
                class="results_table check_table_from search_table_target" 
                name="allUserList" sort="list" export="false" uid="allUserTable" requestURI="" defaultsort="2">
            <display:setProperty name="basic.empty.showtable" value="true"/> 
                <display:setProperty name="basic.msg.empty_list" value=""/>
                <display:setProperty name="basic.msg.empty_list_row" value=""/>  
                <display:column class="action_column" headerClass="action_column">
                    <input type="hidden" name="selectedUser" value="${allUserTable.username}"/>                        
                    <span class="row_identifier hidden_field" data-rowid="${allUserTable.username}">select_user_${allUserTable.username}</span>                            
                </display:column>                   
                <display:column titleKey="listTable.user.username" 
                    maxLength="60"
                    property="username" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                <display:column titleKey="listTable.user.firstName" 
                    maxLength="60"
                    property="firstName" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator" />
                <display:column titleKey="listTable.user.lastName" 
                    maxLength="60"
                    property="lastName" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                
                <display:column titleKey="listTable.user.email" 
                    maxLength="60"
                    property="email" sortable="false" escapeXml="true" sortProperty="lastModifiedDate"/>
                                                                                        
            </display:table>       
            
        </div>
        </div>
        <div class="lightbox_target_footer">
        </div>
    </div>
    </div>
        
        
</div>
</c:if>

<%@include file="editProjectJs.jspf" %>

