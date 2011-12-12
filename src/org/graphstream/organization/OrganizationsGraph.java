/*
 * Copyright 2011 - 2012
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of organic, a feature for GraphStream to manipulate
 * organizations in a dynamic graph.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.organization;

import java.util.HashSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.SinkAdapter;

public class OrganizationsGraph extends AdjacencyListGraph implements
		OrganizationListener {

	public static final String DEFAULT_CSS = "node { size: 40px; } edge { fill-color: gray; }";

	protected class StepForwarding extends SinkAdapter {
		public void stepBegins(String sourceId, long timeId, double time) {
			OrganizationsGraph.this.stepBegins(time);
		}
	}

	Graph entitiesGraph;
	OrganizationManager manager;

	public OrganizationsGraph(Graph g) {
		this(g, null);
	}

	public OrganizationsGraph(Graph g, OrganizationManagerFactory factory) {
		super(g.getId() + "-meta");

		entitiesGraph = g;
		entitiesGraph.addElementSink(new StepForwarding());

		if (factory != null)
			manager = factory.newOrganizationManager();
		else
			manager = new DefaultOrganizationManager();

		manager.init(g);
		manager.addOrganizationListener(this);

		addAttribute("ui.stylesheet", DEFAULT_CSS);
	}

	public OrganizationManager getManager() {
		return manager;
	}

	public void checkConnections() {

	}

	public Organization getNodeOrganization(String nodeId) {
		Node n = entitiesGraph.getNode(nodeId);

		if (n == null)
			return null;

		if (!n.hasAttribute(manager.getMetaOrganizationIndexAttribute()))
			return null;

		return manager.getOrganization(n.getAttribute(manager
				.getMetaOrganizationIndexAttribute()));
	}

	public void organizationCreated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		Node n = addNode(metaOrganizationIndex.toString());
		n.addAttribute("meta.index", metaIndex);
		n.addAttribute("meta.root", rootNodeId);
		n.addAttribute("ui.style", "fill-color: "
				+ manager.getOrganization(metaOrganizationIndex).color + ";");
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
		
		if (e == null)
			return;
		
		HashSet<String> edges = e.getAttribute("connection.elements");
		edges.remove(connection);

		if (edges.size() == 0)
			removeEdge(eid);
	}

	protected String getEdgeID(String nid1, String nid2) {
		if (nid1.hashCode() < nid2.hashCode())
			return String.format("('%s';'%s')", nid1, nid2);

		if (nid1.hashCode() == nid2.hashCode() && nid1.compareTo(nid2) < 0)
			return String.format("('%s';'%s')", nid1, nid2);

		return String.format("('%s';'%s')", nid2, nid1);
	}
}
