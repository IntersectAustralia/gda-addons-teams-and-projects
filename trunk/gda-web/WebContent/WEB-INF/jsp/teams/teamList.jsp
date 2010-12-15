<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="spring"  uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<sec:authentication var="currentUser" property="principal.username" />
<sec:authorize access="hasRole('DELETE_ALL_TEAMS')">
    <c:set var="canDeleteAll" value="true"/>
</sec:authorize>
<sec:authorize access="hasRole('EDIT_ALL_TEAMS')">
    <c:set var="canEditAll" value="true"/>
</sec:authorize>

<spring:message var="confirmDeleteMessage" code="teams.teamList.confirmDelete"/>
<spring:message var="noDeleteMessage" code="teams.teamList.noDelete"/>

<c:url value='createTeam' var='createUrl'/>

<h1>
    <spring:message code="teams.teamList.pageHeading"/>
</h1>

<p class="page_description">
<spring:message code="teams.teamList.description" text="?Team Description?"/>

<span class="add_variables">
<button id="createTeam" class="create_team">Create New Team</button>
</span>
</p>

<display:table excludedParams="infoNotice" name="teams" sort="list" export="false" uid="rowItem" requestURI="" defaultsort="2">
	<display:setProperty name="sort.amount" value="list" />
    <display:setProperty name="basic.empty.showtable" value="true"/>

	<display:column titleKey="listTable.actions" sortable="false" >
		<c:url value="editTeam" var="editUrl">
            <c:param name="id" value="${rowItem.id}"/>
        </c:url>
        <c:url value="viewTeam" var="viewUrl">
            <c:param name="id" value="${rowItem.id}"/>
        </c:url>
        <c:url value="deleteTeam" var="deleteUrl">
	        <c:param name="id" value="${rowItem.id}"/>
	    </c:url>
	    
        <spring:message var="editTitle" code="teams.editTeam.link.title" text="?Edit Team?"/>

		<c:if test="${currentUser==rowItem.owner.username || canEditAll}">
        <a id="editTeam${rowItem.id}" title="${editTitle}" class="edit_button" href="${editUrl}"></a>
        </c:if>
        
        <c:if test="${currentUser==rowItem.owner.username || canDeleteAll}">            
            <spring:message var="deleteTitle" code="teams.deleteTeam.link.title" text="?Delete Team?"/>            
            <a id="deleteTeam${rowItem.id}" title="${deleteTitle}" class="delete_icon delete_team" href="${deleteUrl}"></a>
        </c:if>
        <spring:message var="viewTitle" code="teams.viewTeam.link.title" text="?View Team?"/>
        <a id="viewTeam${rowItem.id}" title="${viewTitle}" class="view_button" href="${viewUrl}"></a>
        
	</display:column>
	<display:column titleKey="listTable.team.name" maxLength="60" property="name" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
	<display:column titleKey="listTable.team.description" maxLength="60" property="description" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
	<display:column titleKey="listTable.team.owner" 
        maxLength="60"
        property="owner.username" sortable="true" escapeXml="true"/>
</display:table>

<script type="text/javascript">

jQuery(document).ready(
	function()
	{
	    var showConfirmDelete = function(linkClicked)
	    {
	        var confirmYes = function()
	        {
		        var inputOpt =
		        {
		        	url : jQuery(linkClicked).attr('href')
		        };
		        GDA.doPost(inputOpt);
	        };
	
	        var confirmNo = function(alertContainer)
	        {
	            alertContainer.remove();
	            GDA.showNotice("${noDeleteMessage}");
	        };
	        
	        var msgOpt = 
	        {
                content : "${confirmDeleteMessage}",
                confirm : "Confirm",
                cancel : "Cancel"                            
	        };
	
	        var callbackOpt = 
	        {
                confirm : confirmYes,
                cancel : confirmNo 	
	        };
	        GDA.showAlert(msgOpt, callbackOpt);
	    };
	
	    var onDelete = function()
	    {
	        showConfirmDelete(this);
	        return false;
	    };
	
	    jQuery('.delete_team').click(onDelete);
	    var onCreate = function()
	    {
		    window.location='${createUrl}';
	    };
	    jQuery('#createTeam').click(onCreate);
	});

</script>
