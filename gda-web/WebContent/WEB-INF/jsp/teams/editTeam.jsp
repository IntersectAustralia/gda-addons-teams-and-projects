<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring"  uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<tiles:importAttribute name="pageMode" scope="request"/>

<c:choose>
	<c:when test="${pageMode == 'create'}">
		<c:set var="formAction">createTeam</c:set>
	</c:when>
	<c:otherwise>		
        <c:set var="formAction">editTeam</c:set>                               
	</c:otherwise>
</c:choose>

<form:form modelAttribute="team" method="post" action="${formAction}" autocomplete="off">
	<form:hidden path="id"/>
<div class="contentbox_header"><strong>Team Information</strong></div>
<div class="contentbox_body" style="">
	    <fieldset class="parameter">
		    
		    <form:errors path="*" cssClass="notification" element="p"/>
		    
		    <div class="input_group">
		        <form:label path="name">
		            <spring:message code="team.name"/>
		        </form:label>
   		        <form:input path="name" cssErrorClass="input_error" maxlength="50"/> 
                <span class="hint">
   		        Assign a name to the Team.
                </span>
		    </div>
 		    <div class="input_group">
   		        <form:label path="description">
		            <spring:message code="team.description"/>
		        </form:label>
   		        <form:input path="description" cssErrorClass="input_error" maxlength="255"/> 
                <span class="hint">
                Assign a description to the Team.
                </span>
		    </div>
	</fieldset>
</div>	
	<div class="contentbox_header">
        <strong><spring:message code="teams.editTeam.sectionTitle"/></strong>
	</div>
    
	<div id="team_check_table_container" class="contentbox_body check_table_wrap">
        
		
	  
		<div id="teamMembersTable" class="clear_float">
            <h4>Users in Team:
            <input id="search_team_member_input" class="search_table_input" type="text"/> 
            </h4>
            
            <div class="scroll_table_outer">
            <div class="scroll_table_inner"> 
            <div class="bulk_action_container">
                <div class="bulk_select_links">
                <a id="select_all_user_top" class="select_all_user_link clickable">Select All</a> / 
                <a id="deselect_all_user_top" class="deselect_all_user_link clickable">Deselect All</a>
              </div>        
              
                <button id="add_more_member_top" class="add_team_button">
                <spring:message 
                        code="teams.edit.addMembers" 
                        text="?Add Members?"/>        
                </button>
            
                <spring:message var="bulkRemoveMemberMsg" 
                    code="teams.edit.bulkRemoveMember.link.title" 
                    text="?Remove Selected?"/>
                <button id="bulk_remove_member_top" class="remove_team_button" >
                    ${bulkRemoveMemberMsg }
                </button>
            </div>         
            <div class="clear_float"></div> 
            
            
            <display:table class="results_table fixed_table check_table_to search_table_target" 
                name="selectedUsers" sort="list" export="false" uid="selectedUsers" requestURI="" 
                defaultsort="2">
            <display:setProperty name="basic.empty.showtable" value="true"/> 
                <display:setProperty name="basic.msg.empty_list" value=""/>
                <display:setProperty name="basic.msg.empty_list_row" value=""/>  
                <display:column>
                    <input type="hidden" name="member" value="${selectedUsers.username}"/>                        
                    <span class="row_identifier hidden_field" data-rowid="${selectedUsers.username}">select_user_${selectedUsers.username}</span>
                </display:column>
                <display:column class="wider" headerClass="wider" 
                    titleKey="listTable.user.username" 
                        maxLength="60"
                        property="username" sortable="false" escapeXml="true"/>   
                <display:column class="wider" headerClass="wider" 
                    titleKey="listTable.user.firstName" 
                    maxLength="60"
                    property="firstName" sortable="false" sortProperty="numericId" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
                <display:column class="wider" headerClass="wider" 
                    titleKey="listTable.user.lastName" 
                    maxLength="60"
                    property="lastName" sortable="false"decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>                
                <display:column class="wider" headerClass="wider" 
                    titleKey="listTable.user.email" 
                    maxLength="60"
                    property="email" sortable="false" escapeXml="true" sortProperty="lastModifiedDate"/>
                                                                                        
            </display:table>      
            <div class="bulk_action_container">
                <div class="bulk_select_links">
                <a id="select_all_user_bottom" class="select_all_user_link clickable">Select All</a> / 
                <a id="deselect_all_user_bottom" class="deselect_all_user_link clickable">Deselect All</a>
              </div>        
                <button id="add_more_member_bottom" class="add_team_button">
                <spring:message 
                        code="teams.edit.addMembers" 
                        text="?Add Members?"/>        
                </button>
            
                <spring:message var="bulkRemoveMemberMsg" 
                    code="teams.edit.bulkRemoveMember.link.title" 
                    text="?Remove Selected?"/>
                <button id="bulk_remove_member_bottom" class="remove_team_button" 
                    >
                    ${bulkRemoveMemberMsg }
                </button>
            </div>           
            
              
            </div>
            </div>
	    </div>
	    
		<div class="clear_float"></div>
	</div>   

	<div class="form_actions">
    <div class="add_variables">
	    <input type="submit" class="submit_button" name="_submit" value="Save Team" id="submit_button" />
	    
        <input id="form_hidden_submit" type="hidden" name="" value="" />
        <a id="cancel_submit" class="clickable">Cancel</a>
    </div>        
	</div>
</form:form>


<div class="hidden_field">
    <!-- Hidden teams -->
<div id="lightbox_all_users" class="lightbox_target_container search_potential_parent_container lightbox_tall">
<div class="lightbox_target_inner_wrap">
    <div class="lightbox_target_header">
    
    </div>
    <div class="lightbox_target_content">
        <div id="allUsersTable" class="">
            <h4>All Users:
            <input id="search_all_user_input" class="search_table_input" type="text"/></h4>
            <display:table excludedParams="infoNotice" 
                class="results_table check_table_from search_table_target" 
                name="allUsers" sort="list" export="false" uid="allUsers" requestURI="" 
                defaultsort="2">
            <display:setProperty name="basic.empty.showtable" value="true"/> 
                <display:setProperty name="basic.msg.empty_list" value=""/>
                <display:setProperty name="basic.msg.empty_list_row" value=""/>  
                <display:column class="action_column" headerClass="action_column">
                    <input type="hidden" name="member" value="${allUsers.username}"/>                        
                    <span class="row_identifier hidden_field" data-rowid="${allUsers.username}">select_user_${allUsers.username}</span>                 
                </display:column>
                <display:column titleKey="listTable.user.username" 
                    maxLength="60"
                    property="username" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>   
                <display:column titleKey="listTable.user.firstName" 
                    maxLength="60"
                    property="firstName" sortable="false" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator" sortProperty="numericId"/>
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

<spring:message var="confirmRemoveMemberMsg" 
    code="teams.edit.confirmRemoveMember" text="?Confirm Remove %X ?"/>
    
<script type="text/javascript">

    jQuery(document).ready(
        function()
        {
            var cancelSubmit = jQuery("#cancel_submit");

            GDA.preventMultipleClick(cancelSubmit);

            var submitCancelFunc = function()
            {
                var hiddenSubmit = jQuery("#form_hidden_submit"); 
                hiddenSubmit.attr("name", "_cancel");
                hiddenSubmit.attr("value", "Cancel");
                jQuery("#team").submit();
            };
            
            cancelSubmit.click(submitCancelFunc);

        	var allUsersSearch = GDA.setupSearch('#allUsersTable');
        	
            var cancelAlertFunc = function(container)
            {
                container.remove();
            };

            var submitSubmitFunc = function()
            {
                var hiddenSubmit = jQuery("#form_hidden_submit"); 
	            hiddenSubmit.attr("name", "_submit");
            	hiddenSubmit.attr("value", "submit");
                jQuery('#team').submit();

            };

            var validateSubmit = function()
            {
               	var msg = "";
               	var showAlert = false;
               	var hiddenUsers = jQuery("#teamMembersTable .search_hidden_entry");

               	if (hiddenUsers.length > 0)
               	{
               		msg = "You have " + hiddenUsers.length + " user(s) in the team which are filtered out. ";
               		msg += "These users will be added to the team.  Are you sure you wish to submit?";
               		showAlert = true;
               	}
               	if (showAlert)
               	{
                    var opt = 
                    {
                        content : msg,
                        confirm : "Confirm",
                        cancel : "Cancel"
                    };
                    var callbacks = 
                    {
                        confirm : submitSubmitFunc,
                        cancel : cancelAlertFunc
                    };
                    
                    GDA.showAlert(opt, callbacks);
               	} else
               	{
                   	submitSubmitFunc();
               	}

            };
            GDA.preventMultipleClick('#submit_button', validateSubmit, false);

        	
            var userLightboxPartialOpt = 
            {
                linkSelector : ".add_team_button",
                href : "#lightbox_all_users"
            };

            var userTableOpt = 
            {
                    tableAllId : "#allUsers", 
                    tableInId : "#selectedUsers", 
                    searchContainerId : "#teamMembersTable",
                    removeButtonId : ".remove_team_button",
                    confirmRemoveMessage : "${confirmRemoveMemberMsg}",
                    selectAllSelector : ".select_all_user_link",
                    unselectAllSelector : ".deselect_all_user_link"
                    
            };

            var userTableControl = new TableControl(
                    userTableOpt,
                    userLightboxPartialOpt);


            //plug in sort for the all table
            
            var allTable = jQuery("#allUsers");
            if (allTable.find("tbody tr").length > 1)
            {
                var defaultSort = [[1,0]];

                var allTableSortOpt = 
                {
                        sortList : defaultSort,
                        cssHeader : "sortable_header",
                        cssAsc : "list_table_sorted order2",
                        cssDesc : "list_table_sorted order1",
                        headers: 
                        {
                            0 : {sorter: false} //no sorting on first column
                        }
                };


                allTable.tablesorter(allTableSortOpt); 
            };
        }
    );

</script>

 