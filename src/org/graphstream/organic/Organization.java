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

import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.filtered.FilteredGraph;
import org.graphstream.graph.filtered.FilteredNode;
import org.graphstream.organic.Validation.Level;

/**
 * An organization in a graph. Organizations are a connected set of nodes which
 * share a common meta index.
 * 
 * @author Guilhelm Savin
 * 
 */
public class Organization extends FilteredGraph implements Validable {
	protected Object metaIndex;
	protected Object metaOrganizationIndex;

	protected Node organizationRoot;

	protected OrganizationManager manager;

	private boolean initialized;

	public Organization(OrganizationManager manager, Object metaIndex,
			Object metaOrganizationIndex, Graph fullGraph, Node root) {
		super(metaOrganizationIndex.toString(), fullGraph);

		// System.out.printf("*** created %s@%s ***%n", metaOrganizationIndex,
		// metaIndex);

		this.metaIndex = metaIndex;
		this.metaOrganizationIndex = metaOrganizationIndex;
		this.manager = manager;
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
			if (!contains(n))
				include(n);

			for (Edge e : n.getEdgeSet()) {
				if (!manager.isAvailable(e))
					continue;

				Node o = e.getOpposite(n);

				if (!contains(o)
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

	public void setRootNode(Node root) {
		if (contains(root)) {
			organizationRoot = root;
			manager.rootNodeUpdate(this);
		} else
			throw new Error(
					String
							.format(
									"try to set root '%s' which is not in the organization [%s|%s]",
									root.getId(), metaIndex.toString(),
									metaOrganizationIndex.toString()));
	}

	public Object getMetaIndex() {
		return metaIndex;
	}

	public Object getMetaOrganizationIndex() {
		return metaOrganizationIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.graph.filtered.FilteredGraph#include(org.graphstream.
	 * graph.Edge)
	 */
	@Override
	public void include(Edge e) {
		Object emi = e.getAttribute(manager.metaIndexAttribute);
		Object emoi = e.getAttribute(manager.metaOrganizationIndexAttribute);

		if (emi != null && !metaIndex.equals(emi) && e instanceof Node)
			throw new Error("try to include node '" + e.getId()
					+ "' with bad meta index '" + emi + "' ('" + metaIndex
					+ "' expected) in '" + metaOrganizationIndex + "'");

		if (emi == null)
			e.setAttribute(manager.metaIndexAttribute, metaIndex);

		if (emoi != null) {
			if (metaOrganizationIndex.equals(emoi)) {
				// System.err.printf(
				// "warning: include twice \"%s\" in %s@%s%n", e
				// .getId(), metaOrganizationIndex, metaIndex);
			} else {
				throw new Error(
						String
								.format(
										"element \"%s\" already in %s@%s, including in %s@%s (really ? %s)",
										e.getId(), emoi, emi,
										metaOrganizationIndex, metaIndex,
										manager.getOrganization(emoi).contains(
												e)));
			}
		} else {
			e.setAttribute(manager.metaOrganizationIndexAttribute,
					metaOrganizationIndex);
			super.include(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.graph.filtered.FilteredGraph#include(org.graphstream.
	 * graph.Node)
	 */
	@Override
	public void include(Node n) {
		Object emi = n.getAttribute(manager.metaIndexAttribute);
		Object emoi = n.getAttribute(manager.metaOrganizationIndexAttribute);

		if (emi != null && !metaIndex.equals(emi) && n instanceof Node)
			throw new Error("try to include node '" + n.getId()
					+ "' with bad meta index '" + emi + "' ('" + metaIndex
					+ "' expected) in '" + metaOrganizationIndex + "'");

		if (emi == null)
			n.setAttribute(manager.metaIndexAttribute, metaIndex);

		if (emoi != null) {
			if (metaOrganizationIndex.equals(emoi)) {
				// System.err.printf(
				// "warning: include twice \"%s\" in %s@%s%n", e
				// .getId(), metaOrganizationIndex, metaIndex);
			} else {
				throw new Error(
						String
								.format(
										"element \"%s\" already in %s@%s, including in %s@%s (really ? %s)",
										n.getId(), emoi, emi,
										metaOrganizationIndex, metaIndex,
										manager.getOrganization(emoi).contains(
												n)));
			}
		} else {
			n.setAttribute(manager.metaOrganizationIndexAttribute,
					metaOrganizationIndex);

			super.include(n);

			for (Edge edge : n.getEdgeSet()) {
				Node o = edge.getOpposite(n);

				if (contains(o))
					include(edge);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.graph.filtered.FilteredGraph#notInclude(org.graphstream
	 * .graph.Edge)
	 */
	@Override
	public void notInclude(Edge e) {
		Object emoi = e.getAttribute(manager.metaOrganizationIndexAttribute);

		if (emoi != null && !emoi.equals(metaOrganizationIndex))
			throw new Error(
					String
							.format(
									"remove element %s which is not in the organization %s@%s%n",
									e.getId(), metaOrganizationIndex, metaIndex));

		e.removeAttribute(manager.metaOrganizationIndexAttribute);

		super.notInclude(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.graph.filtered.FilteredGraph#notInclude(org.graphstream
	 * .graph.Node)
	 */
	@Override
	public void notInclude(Node n) {
		Object emoi = n.getAttribute(manager.metaOrganizationIndexAttribute);

		if (emoi != null && !emoi.equals(metaOrganizationIndex))
			throw new Error(
					String
							.format(
									"remove element %s which is not in the organization %s@%s%n",
									n.getId(), metaOrganizationIndex, metaIndex));

		n.removeAttribute(manager.metaOrganizationIndexAttribute);

		super.notInclude(n);

		for (Edge edge : n.getEdgeSet()) {
			if (contains(edge))
				notInclude(edge);
		}

		if (organizationRoot == null
				|| n.getId().equals(organizationRoot.getId()))
			checkRootNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.graph.filtered.FilteredGraph#empty()
	 */
	@Override
	public void empty() {
		LinkedList<Node> nodes = new LinkedList<Node>(getNodeSet());

		while (nodes.size() > 0) {
			Node n = element.getNode(nodes.poll().getId());
			notInclude(n);
		}

		super.empty();
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
				if (!contains(e))
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
			Node n = element.getNode(id);

			if (!contains(n))
				throw new ValidationException(this,
						"unknown node reached, '%s'", n.getId());
		}

		for (Node n : getEachNode()) {
			if (!reached.contains(n.getId()))
				throw new ValidationException(
						this,
						"known node unreached, '%s' (reached are %s, nodes are %s and edges %s)",
						n.getId(), reached, getNodeSet(), getEdgeSet());
		}
		/*
		 * for (String id : crossed) { if (!edges.contains(id)) throw new
		 * ValidationException(this, "unknown edge crossed, '%s'", id); }
		 * 
		 * for (String id : edges) { if (!crossed.contains(id)) throw new
		 * ValidationException(this, "known edge uncrossed, '%s'", id); }
		 */
		for (FilteredNode fn : this.<FilteredNode> getEachNode()) {
			Node n = fn.getFilteredElement();

			if (!metaIndex.equals(n.getAttribute(manager.metaIndexAttribute)))
				throw new ValidationException(this,
						"invalid meta index of node '%s'", n.getId());

			for (Edge e : n.getEdgeSet()) {
				Node o = e.getOpposite(n);
				Object omoi = o
						.getAttribute(manager.metaOrganizationIndexAttribute);

				if (contains(o)) {
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
		if (organizationRoot != null && !contains(organizationRoot))
			organizationRoot = null;

		Node root = organizationRoot;
		int count = 0;

		if (root != null) {
			for (Edge e : root.getEdgeSet()) {
				Node o = e.getOpposite(root);
				if (contains(o))
					count++;
			}
		} else {
			count = -1;
		}

		for (FilteredNode fn : this.<FilteredNode> getEachNode()) {
			Node n = fn.getFilteredElement();
			int c = 0;

			for (Edge e : n.getEdgeSet()) {
				Node o = e.getOpposite(n);
				if (contains(o))
					c++;
			}

			if (c > count) {
				root = n;
				count = c;
			}
		}

		if (root != organizationRoot) {
			setRootNode(root);
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

		if (!contains(organizationRoot))
			throw new Error("root is not in organization");

		toVisit.add(organizationRoot);

		while (toVisit.size() > 0) {
			Node n = toVisit.poll();
			reached.add(n.getId());

			for (Edge e : n.getEdgeSet()) {
				if (!contains(e))
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

		boolean stable = getNodeCount() == reached.size();

		for (String id : reached) {
			Node n = element.getNode(id);
			stable = stable && contains(n);
		}

		if (!stable) {

			// System.out.printf("\tnot stable%n");

			LinkedList<String> notReached = new LinkedList<String>();

			for (Node n : getEachNode())
				if (!reached.contains(n.getId()))
					notReached.add(n.getId());

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
