package com.icfolson.aem.library.core.servlets.replication

import com.day.cq.replication.AgentManager
import com.day.cq.replication.ReplicationActionType
import com.day.cq.replication.ReplicationException
import com.day.cq.replication.Replicator
import com.icfolson.aem.library.core.services.SelectiveReplicationService
import com.icfolson.aem.library.core.servlets.AbstractJsonResponseServlet
import org.apache.sling.api.SlingHttpServletRequest
import org.apache.sling.api.SlingHttpServletResponse
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference

import javax.jcr.Session
import javax.servlet.Servlet
import javax.servlet.ServletException

import static com.google.common.base.Preconditions.checkArgument

@Component(service = Servlet, property = [
    "sling.servlet.paths=/bin/replicate/selective"
])
class SelectiveReplicationServlet extends AbstractJsonResponseServlet {

    private static final String PARAMETER_ACTION = "action"

    private static final String PARAMETER_AGENT_IDS = "agentIds"

    private static final String PARAMETER_PATHS = "paths"

    @Reference
    protected AgentManager agentManager

    @Reference
    protected Replicator replicator

    @Reference
    protected SelectiveReplicationService replicationService

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws
        ServletException, IOException {
        def paths = request.getParameterValues(PARAMETER_PATHS) as LinkedHashSet
        def agentIds = request.getParameterValues(PARAMETER_AGENT_IDS) as LinkedHashSet
        def action = request.getParameter(PARAMETER_ACTION)

        checkArgument(paths as Boolean, "paths parameter must be non-null and non-empty" as Object)
        checkArgument(agentIds as Boolean, "agentIds parameter must be non-null and non-empty" as Object)
        checkArgument(action as Boolean, "invalid action parameter = %s", action)

        def session = request.resourceResolver.adaptTo(Session)
        def actionType = ReplicationActionType.fromName(action)

        paths.each { path ->
            try {
                replicationService.replicate(session, actionType, path, agentIds)
            } catch (ReplicationException e) {
                throw new ServletException(e)
            }
        }

        writeJsonResponse(response, paths)
    }
}
