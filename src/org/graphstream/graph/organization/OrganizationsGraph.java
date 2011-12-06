package org.graphstream.graph.organization;

import java.util.HashSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;

public class OrganizationsGraph extends AdjacencyListGraph implements
		OrganizationListener {

	Graph entitiesGraph;
	OrganizationManager manager;

	public OrganizationsGraph(Graph g) {
		this(g, null);
	}
	
	public OrganizationsGraph(Graph g, OrganizationManagerFactory factory) {
		super(g.getId() + "-meta");

		this.entitiesGraph = g;
		
		if (factory != null)
			manager = factory.newOrganizationManager();
		else
			manager = new DefaultOrganizationManager();
		
		manager.init(g);
		manager.addOrganizationListener(this);
	}

	public OrganizationManager getManager() {
		return manager;
	}
	
	public void checkConnections() {

	}

	public void organizationCreated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		Node n = addNode(metaOrganizationIndex.toString());
		n.addAttribute("meta.index", metaIndex);
		n.addAttribute("meta.root", rootNodeId);
	}

	public void organizationMerged(Object metaIndex,
			Object metaOrganizationIndex1, Object metaOrganizationIndex2,
			String rootNodeId) {
	}

	public void organizationRemoved(Object metaIndex,
			Object metaOrganizationIndex) {
		removeNode(metaOrganizationIndex.toString());
	}

	public void organizationRootNodeUpdated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		Node n = getNode(metaOrganizationIndex.toString());
		n.setAttribute("meta.root", rootNodeId);
	}

	public void organizationSplited(Object metaIndex,
			Object metaOrganizationBase, Object metaOrganizationChild) {
		// TODO Auto-generated method stub

	}

	public void organizationChanged(Object metaIndex,
			Object metaOrganizationIndex, ChangeType changeType,
			ElementType elementType, String elementId) {
		// TODO Auto-generated method stub

	}

	public void connectionCreated(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		String nid1 = metaOrganizationIndex1.toString();
		String nid2 = metaOrganizationIndex2.toString();
		String eid = getEdgeID(nid1, nid2);

		Edge e = getEdge(eid);

		if (e == null) {
			e = addEdge(eid, nid1, nid2, false);
			e.addAttribute("connection.elements", new HashSet<String>());
		}

		HashSet<String> edges = e.getAttribute("connection.elements");
		edges.add(connection);
	}

	public void connectionRemoved(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		String nid1 = metaOrganizationIndex1.toString();
		String nid2 = metaOrganizationIndex2.toString();
		String eid = getEdgeID(nid1, nid2);

		Edge e = getEdge(eid);
		HashSet<String> edges = e.getAttribute("connection.elements");
		edges.remove(connection);

		if (edges.size() == 0)
			removeEdge(eid);
	}

	protected String getEdgeID(String nid1, String nid2) {
		if (nid1.hashCode() < nid2.hashCode())
			return String.format("('%s';'%s')", nid1, nid2);

		return String.format("('%s';'%s')", nid2, nid1);
	}
}
