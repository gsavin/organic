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
package org.graphstream.organic;

import java.util.Arrays;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.subgraph.Conditions;
import org.graphstream.graph.subgraph.IncludeCondition;
import org.graphstream.graph.subgraph.SubGraph;
import org.graphstream.organic.Validation.Level;

/**
 * An organization in a graph. Organizations are a connected set of nodes which
 * share a common meta index.
 * 
 * @author Guilhelm Savin
 * 
 */
public class Organization extends SubGraph implements IncludeCondition,
		Validable {
	protected Object metaIndex;
	protected Object metaOrganizationIndex;

	protected Node organizationRoot;

	protected DefaultOrganizationManager manager;
	
	private boolean initialized;

	public Organization(DefaultOrganizationManager manager, Object metaIndex,
			Object metaOrganizationIndex, Graph fullGraph, Node root) {
		super(metaOrganizationIndex.toString(), fullGraph, Conditions.none(),
				true);

		// System.out.printf("*** created %s@%s ***%n", metaOrganizationIndex,
		// metaIndex);

		this.metaIndex = metaIndex;
		this.metaOrganizationIndex = metaOrganizationIndex;
		this.manager = manager;
		this.condition = this;
		this.organizationRoot = root;
		this.initialized = false;
	}

	public synchronized void init() {
		LinkedList<Node> toVisit = new LinkedList<Node>();

		if (initialized)
			throw new RuntimeException("already initialized organization");

		if (organizationRoot.hasAttribute(manager.metaIndexAttribute)
				&& !metaIndex.equals(organizationRoot
						.getAttribute(manager.metaIndexAttribute)))
			throw new Error("meta index of organization ("
					+ metaOrganizationIndex + "@" + metaIndex
					+ ") differ from meta index of root node \""
					+ organizationRoot.getId() + "\" ("
					+ organizationRoot.getAttribute(manager.metaIndexAttribute)
					+ ")");

		toVisit.add(organizationRoot);

		while (toVisit.size() > 0) {
			Node n = toVisit.poll();
			if (!nodes.contains(n.getId()))
				include(n);

			for (Edge e : n.getEdgeSet()) {
				if (!manager.isAvailable(e))
					continue;

				Node o = e.getOpposite(n);

				if (!nodes.contains(o.getId())
						&& o.hasAttribute(manager.metaIndexAttribute)
						&& metaIndex.equals(o
								.getAttribute(manager.metaIndexAttribute))) {
					Object omoi = o
							.getAttribute(manager.metaOrganizationIndexAttribute);

					if (omoi != null && !omoi.equals(metaOrganizationIndex))
						throw new RuntimeException(
								String
										.format(
												"WTF ? Merge missing via edge '%s' (node is '%s')",
												e.getId(), o.getId()));

					toVisit.addLast(o);
				}
			}
		}
		
		initialized = true;
	}

	public boolean isInside(Element e) {
		if (e instanceof Node) {
			return nodes.contains(e.getId());
		} else if (e instanceof Edge) {
			return edges.contains(e.getId());
		} else {
			// WTF ?
		}

		return false;
	}

	public void include(Element e) {
		Object emi = e.getAttribute(manager.metaIndexAttribute);
		Object emoi = e.getAttribute(manager.metaOrganizationIndexAttribute);

		if (emi != null && !metaIndex.equals(emi))
			throw new Error("try to include a node with bad meta index");

		if (emoi != null) {
			if (metaOrganizationIndex.equals(emoi)) {
				/*
				 * System.err.printf(
				 * "warning: include twice node \"%s\" in %s@%s%n", e.getId(),
				 * metaOrganizationIndex, metaIndex);
				 */
			} else {
				throw new Error(
						String
								.format(
										"element \"%s\" already in %s@%s, including in %s@%s (really ? %s)",
										e.getId(), emoi, emi,
										metaOrganizationIndex, metaIndex,
										manager.getOrganization(emoi).isInside(
												e)));
			}
		} else {
			e.setAttribute(manager.metaOrganizationIndexAttribute,
					metaOrganizationIndex);
			// e.setAttribute("ui.style", String.format("fill-color: %s;",
			// color));

			super.include(e);
			// printContent();

			if (e instanceof Node) {
				Node n = (Node) e;

				// System.out.printf("[%s@%s] include node \"%s\"%n",
				// metaOrganizationIndex, metaIndex, n.getId());

				for (Edge edge : n.getEdgeSet()) {
					Node o = edge.getOpposite(n);

					if (isInside(o))
						include(edge);
				}
			}
		}
	}

	public void remove(Element e) {

		Object emoi = e.getAttribute(manager.metaOrganizationIndexAttribute);

		if (emoi != null && !emoi.equals(metaOrganizationIndex))
			throw new Error(
					String
							.format(
									"remove element %s which is not in the organization %s@%s%n",
									e.getId(), metaOrganizationIndex, metaIndex));

		e.removeAttribute(manager.metaOrganizationIndexAttribute);

		super.remove(e);
		// printContent();

		if (e instanceof Node) {
			// System.out.printf("[%s@%s] remove node %s%n",
			// metaOrganizationIndex, metaIndex, e.getId());
			Node n = (Node) e;

			for (Edge edge : n.getEdgeSet()) {
				if (isInside(edge))
					remove(edge);
			}

			if (e.getId().equals(organizationRoot.getId()))
				checkRootNode();
		}

		// check();
	}

	public void empty() {
		LinkedList<String> nodes = new LinkedList<String>(this.nodes);
		while (nodes.size() > 0) {
			Node n = fullGraph.getNode(nodes.poll());
			remove(n);
		}
		super.empty();
	}

	protected void printContent() {
		System.out.printf("[%s@%s] content: %s%n", metaOrganizationIndex,
				metaIndex, Arrays.toString(nodes.toArray()));
	}

	public void validate(Level level) throws ValidationException {
		LinkedList<String> reached = new LinkedList<String>();
		LinkedList<Node> toVisit = new LinkedList<Node>();
		// LinkedList<String> crossed = new LinkedList<String>();

		if (organizationRoot == null)
			throw new Error("root is null");

		toVisit.add(organizationRoot);

		while (toVisit.size() > 0) {
			Node n = toVisit.poll();
			reached.add(n.getId());

			for (Edge e : n.getEdgeSet()) {
				if (!edges.contains(e.getId()))
					continue;

				Node o = e.getOpposite(n);

				if (metaOrganizationIndex.equals(o
						.getAttribute(manager.metaOrganizationIndexAttribute))) {
					if (!reached.contains(o.getId()) && !toVisit.contains(o))
						toVisit.addLast(o);
					// if (!crossed.contains(e.getId()))
					// crossed.add(e.getId());
				}
			}
		}

		for (String id : reached) {
			if (!nodes.contains(id))
				throw new ValidationException(this,
						"unknown node reached, '%s'", id);
		}

		for (String id : nodes) {
			if (!reached.contains(id))
				throw new ValidationException(this,
						"known node unreached, '%s'", id);
		}
		/*
		 * for (String id : crossed) { if (!edges.contains(id)) throw new
		 * ValidationException(this, "unknown edge crossed, '%s'", id); }
		 * 
		 * for (String id : edges) { if (!crossed.contains(id)) throw new
		 * ValidationException(this, "known edge uncrossed, '%s'", id); }
		 */
		for (String id : nodes) {
			Node n = fullGraph.getNode(id);

			if (!metaIndex.equals(n.getAttribute(manager.metaIndexAttribute)))
				throw new ValidationException(this,
						"known node unreached, '%s'", id);

			for (Edge e : n.getEdgeSet()) {
				Node o = e.getOpposite(n);
				Object omoi = o
						.getAttribute(manager.metaOrganizationIndexAttribute);

				if (nodes.contains(o.getId())) {
					if (!metaOrganizationIndex.equals(omoi))
						throw new ValidationException(this,
								"node should not be included '%s'", o.getId());
				} else {
					if (metaOrganizationIndex.equals(omoi))
						throw new ValidationException(this,
								"node should be included '%s'", o.getId());
				}
			}
		}
	}

	protected void checkRootNode() {
		if (!nodes.contains(organizationRoot.getId()))
			organizationRoot = null;

		Node root = organizationRoot;
		int count = 0;

		if (root != null) {
			for (Edge e : root.getEdgeSet()) {
				Node o = e.getOpposite(root);
				if (isInside(o))
					count++;
			}
		} else {
			count = -1;
		}

		for (String nodeId : nodes) {
			Node n = fullGraph.getNode(nodeId);
			int c = 0;

			for (Edge e : n.getEdgeSet()) {
				Node o = e.getOpposite(n);
				if (isInside(o))
					c++;
			}

			if (c > count) {
				root = n;
				count = c;
			}
		}

		if (root != organizationRoot) {
			this.organizationRoot = root;
			manager.rootNodeUpdate(this);
		}
	}

	/*
	 * public boolean isInside(Element n) { return
	 * n.hasAttribute(manager.metaOrganizationIndexAttribute) &&
	 * metaOrganizationIndex.equals(n.getAttribute(getId())); }
	 */

	public void check() {
		// System.out.printf("[%s@%s] check%n", metaOrganizationIndex,
		// metaIndex);
		if (getNodeCount() == 0)
			return;

		LinkedList<String> reached = new LinkedList<String>();
		LinkedList<Node> toVisit = new LinkedList<Node>();

		if (organizationRoot == null)
			throw new Error("root is null");

		toVisit.add(organizationRoot);

		while (toVisit.size() > 0) {
			Node n = toVisit.poll();
			reached.add(n.getId());

			for (Edge e : n.getEdgeSet()) {
				if (!edges.contains(e.getId()))
					continue;

				Node o = e.getOpposite(n);
				/*
				 * if (nodes.contains(o.getId()) && !reached.contains(o.getId())
				 * && !toVisit.contains(o)) { toVisit.addLast(o); }
				 */
				if (metaOrganizationIndex.equals(o
						.getAttribute(manager.metaOrganizationIndexAttribute))
						&& !reached.contains(o.getId()) && !toVisit.contains(o)) {
					toVisit.addLast(o);
				}
			}
		}

		boolean stable = nodes.size() == reached.size();

		for (String id : reached) {
			stable = stable && nodes.contains(id);
		}

		if (!stable) {

			// System.out.printf("\tnot stable%n");

			LinkedList<String> notReached = new LinkedList<String>(nodes);
			notReached.removeAll(reached);

			if (notReached.size() > 0) {
				manager.mitose(metaIndex, this, notReached);
			}

			checkRootNode();
		} // else
		// System.out.printf("\tstable%n");
	}

	public static class AloneOrganization extends Organization {

		public AloneOrganization(Object metaIndex,
				Object metaOrganizationIndex, Graph fullGraph) {
			super(null, metaIndex, metaOrganizationIndex, fullGraph, null);
		}

		public void check() {

		}
	}
}
