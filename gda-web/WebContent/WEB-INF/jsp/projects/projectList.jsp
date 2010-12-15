<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="spring"  uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<%@ page import="au.org.intersect.gda.domain.ProjectStatus" %>
<c:set var="projectProcessed">
    <%=ProjectStatus.PROCESSED%>
</c:set>


<sec:authentication var="currentUser" property="principal.username" />

<h1>
    <spring:message code="projects.projectList.pageHeading"/>
</h1>


<p class="page_description">
<spring:message code="projects.projectList.description" text="?Project Description?"/>

<span class="add_variables">
<button id="add_project_button"/>
    <spring:message code="projects.createProject.link" text="?Create?"/>
</button>
</span>

<c:url var="createProjectUrl" value="/createProject"/>        
</p>


<sec:authorize access="hasRole('DELETE_ALL_PROJECTS')">
    <c:set var="canDeleteAll" value="true"/>
</sec:authorize>

<display:table excludedParams="infoNotice" name="projects" sort="list" export="false" uid="rowItem" requestURI="" defaultsort="2">
	<display:setProperty name="sort.amount" value="list" />
    <display:setProperty name="basic.empty.showtable" value="true"/>

	<display:column titleKey="listTable.actions" sortable="false" >
		<c:url value="editProject" var="editUrl">
            <c:param name="id" value="${rowItem.id}"/>
        </c:url>
        <c:url value="viewProject" var="viewUrl">
            <c:param name="id" value="${rowItem.id}"/>
        </c:url>
        <c:url value="deleteProject" var="deleteUrl">
	        <c:param name="id" value="${rowItem.id}"/>
	    </c:url>
	    
        <c:if test="${rowItem.status == projectProcessed}">
        <spring:message var="editTitle" code="projects.editProject.link.title" text="?Edit Project?"/>

        <a id="editProject${rowItem.id}" title="${editTitle }" class="edit_button" href="${editUrl}"></a>
        
        <c:if test="${currentUser==rowItem.owner.username || canDeleteAll}">            
            <spring:message var="deleteTitle" code="projects.deleteProject.link.title" text="?Delete Project?"/>            
            <a id="deleteProject${rowItem.id}" title="${deleteTitle }" class="delete_icon" href="${deleteUrl}"></a>
        </c:if>
        </c:if>
        <spring:message var="viewTitle" code="projects.viewProject.link.title" text="?View Project?"/>
        <a id="viewProject${rowItem.id}" title="${viewTitle }" class="view_button" href="${viewUrl}"></a>
        
	</display:column>
	<display:column titleKey="listTable.project.name" maxLength="60"
         property="name" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
    <display:column titleKey="listTable.project.description" maxLength="60"
         property="description" sortable="true" decorator="au.org.intersect.gda.util.HTMLEscapeDecorator"/>
    
	<display:column titleKey="listTable.project.owner" 
        maxLength="60"
        property="owner.username" sortable="true" escapeXml="true"/>
    <display:column titleKey="listTable.project.status" property="status.displayName" sortable="true" escapeXml="true"/>
    
</display:table>

<spring:message var="confirmDeleteMessage" code="projects.list.confirmDelete"/>
<spring:message var="noDeleteMessage" code="projects.list.noDelete"/>

<script type="text/javascript">

jQuery(document).ready(
        function()
        {

            
            var goCreate = function()
            {
                window.location = "${createProjectUrl}";
            };
            jQuery("#add_project_button").click(goCreate);
            
            var showConfirmDelete = function(linkClicked)
            {
                var confirmYes = function()
                {
                	window.location=jQuery(linkClicked).attr('href');
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

            jQuery('a[id^=deleteProject]').click(onDelete);

        });

</script>
