<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring"  uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>

<sec:authentication var="currentUser" property="principal.username" />

<sec:authorize access="hasRole('EDIT_ALL_TEAMS')">
    <c:set var="isAdmin" value="true"/>
</sec:authorize>

<c:url value="editTeam" var="editUrl">
	<c:param name="id" value="${team.id}"/>
</c:url>

<div class="contentbox_header">
    
	<div class="left">
    	<strong>Team Name:</strong> <c:out value="${team.name}"/>
    	<strong>Description: </strong> <c:out value="${team.description}"/>
    </div>
	<div class="right">
    	<strong>Owner:</strong> <c:out value="${team.owner.username}"/>
    </div>
    <div class="clear_float"></div>
</div>

<div class="contentbox_body">
	Created <fmt:formatDate value="${team.createdDate}" pattern="dd-MM-yyyy HH:mm"/>
	&nbsp;&nbsp;&nbsp;
	Last Modified <fmt:formatDate value="${team.lastModifiedDate}" pattern="dd-MM-yyyy HH:mm"/>

	<br/>
	<br/>
	
    <div id="teamMembersTable" >
        <h3>Users in Team:
        <input id="search_team_user_input" class="search_table_input" type="text"/> </h3>
        <display:table excludedParams="infoNotice" class="results_table search_table_target" 
            name="team.members" sort="list" export="false" uid="teamMembers" requestURI="" 
            defaultsort="1">
        <display:setProperty name="basic.empty.showtable" value="true"/> 
            
            <display:column titleKey="listTable.user.username" 
                maxLength="60"
                property="username" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>   
                
            <display:column titleKey="listTable.user.firstName" 
                maxLength="60"
                property="firstName" 
                sortable="true" escapeXml="true" />
            <display:column titleKey="listTable.user.lastName" 
                maxLength="60"
                property="lastName" 
                sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
            
            <display:column titleKey="listTable.user.email" 
                maxLength="60"
                property="email" 
                sortable="true" escapeXml="true"/>
                                                                                    
        </display:table>       
    </div>
       
</div>

<div class="form_actions">

    <div class="add_variables">

	<c:if test="${currentUser == team.owner.username || isAdmin}">
    	<button id="edit_team_button">Edit Team</button>
    </c:if>
        
    <a href="<c:url value="listTeams"/>">Back to View Teams</a>
    </div>
</div>

<script type="text/javascript">


    jQuery(document).ready(   
        function()
        {
            var editTeamFunc = function()
            {
                window.location = "${editUrl}";
            };
            jQuery("#edit_team_button").click(editTeamFunc);

            GDA.setupSearch('#teamMembersTable');
        }
    );
</script>