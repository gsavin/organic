/*
 * Copyright 2011 - 2012
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of Organizations, a feature for GraphStream to manipulate
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.organization.OrganizationListener.ChangeType;
import org.graphstream.organization.OrganizationListener.ElementType;
import org.graphstream.stream.ElementSink;
import org.graphstream.stream.SinkAdapter;

/**
 * 
 * Algorithmes :
 * 
 * __meta_index__(e) : the meta index of element e
 * __meta_organization_index__(e) : the index of the organization of element e
 * __meta_index_added__(node,metaIndex)
 */
public class DefaultOrganizationManager extends SinkAdapter implements
		OrganizationManager {

	static class IdGenerator {

		public final String prefix;
		public final String suffix;
		public final AtomicInteger counter;

		protected final String format;

		public IdGenerator() {
			this("", "");
		}

		public IdGenerator(String prefix) {
			this(prefix, "");
		}

		public IdGenerator(String prefix, String suffix) {
			this(prefix, suffix, 5);
		}

		public IdGenerator(String prefix, String suffix, int size) {
			this.prefix = prefix;
			this.suffix = suffix;
			this.counter = new AtomicInteger(0);
			this.format = String.format("%%s%%0%dd%%s", size);
		}

		public String getNewId() {
			return String.format(format, prefix, counter.incrementAndGet(),
					suffix);
		}
	}

	protected String metaIndexAttribute = "meta.index";
	protected String metaOrganizationIndexAttribute = "meta.organization.index";

	final HashMap<Object, Organization> organizations;

	Graph graph;

	protected LinkedList<OrganizationListener> listeners;

	protected final IdGenerator idGenerator = new IdGenerator();

	protected Validator validator;

	protected Edge edgeBeingRemoved;
	
	public DefaultOrganizationManager() {
		this.organizations = new HashMap<Object, Organization>();
		this.listeners = new LinkedList<OrganizationListener>();

		validator = Validation.getValidator(this);

		System.out.printf("Validation level is set to '%s'\n", Validation
				.getValidationLevel().name().toLowerCase());
	}

	public DefaultOrganizationManager(String metaIndexAttribute) {
		this();

		this.metaIndexAttribute = metaIndexAttribute;
	}

	public DefaultOrganizationManager(String metaIndexAttribute,
			String metaOrganizationIndexAttribute) {
		this();

		this.metaIndexAttribute = metaIndexAttribute;
		this.metaOrganizationIndexAttribute = metaOrganizationIndexAttribute;
	}

	public void init(Graph g) {
		if (graph != null)
			graph.removeSink(this);

		graph = g;
		graph.addSink(this);
	}

	public Organization getOrganization(Object metaOrganizationIndex) {
		return organizations.get(metaOrganizationIndex);
	}

	public Iterator<Organization> iterator() {
		return organizations.values().iterator();
	}

	public void setMetaIndexAttribute(String metaIndexAttribute) {
		this.metaIndexAttribute = metaIndexAttribute;
	}

	public void setMetaOrganizationIndexAttribute(
			String metaOrganizationIndexAttribute) {
		this.metaOrganizationIndexAttribute = metaOrganizationIndexAttribute;
	}

	public String getMetaIndexAttribute() {
		return metaIndexAttribute;
	}

	public String getMetaOrganizationIndexAttribute() {
		return metaOrganizationIndexAttribute;
	}

	public void addOrganizationListener(OrganizationListener l) {
		listeners.addLast(l);
	}

	public void removeOrganizationListener(OrganizationListener l) {
		listeners.remove(l);
	}

	public boolean isAvailable(Element e) {
		return e != edgeBeingRemoved;
	}
	
	public int getOrganizationCount() {
		return organizations.size();
	}

	private void elementRemoved(Element e) {

		Object nmoi = e.getAttribute(metaOrganizationIndexAttribute);

		if (nmoi != null) {
			Organization org = organizations.get(nmoi);
			org.remove(e);
			org.check();
		}
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {

	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		Node n = graph.getNode(nodeId);
		elementRemoved(n);

		validator.validate("after remove node '%s'", nodeId);
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {

		Node n0 = graph.getNode(fromNodeId);
		Node n1 = graph.getNode(toNodeId);

		Object n0mi = n0.getAttribute(metaIndexAttribute);
		Object n1mi = n1.getAttribute(metaIndexAttribute);

		if (n0mi != null && n1mi != null && n0mi.equals(n1mi)) {
			Object n0moi = n0.getAttribute(metaOrganizationIndexAttribute);
			Object n1moi = n1.getAttribute(metaOrganizationIndexAttribute);

			if (n0moi != null && n1moi != null) {

				Organization org0 = organizations.get(n0moi);
				Organization org1 = organizations.get(n1moi);

				if (!n0moi.equals(n1moi))
					org0 = merge(org0, org1);

				org0.include(graph.getEdge(edgeId));
			}

			validator.validate("after adding edge '%s' between '%s' and '%s'",
					edgeId, fromNodeId, toNodeId);
		} else if (n0mi != null && n1mi != null) {
			Object n0moi = n0.getAttribute(metaOrganizationIndexAttribute);
			Object n1moi = n1.getAttribute(metaOrganizationIndexAttribute);

			if (n0moi != null && n1moi != null && !n0moi.equals(n1moi)) {
				for (OrganizationListener l : listeners)
					l.connectionCreated(n0mi, n0moi, n1mi, n1moi, edgeId);
			}

			validator.validate("after adding connection '%s' between '%s' and '%s'",
					edgeId, fromNodeId, toNodeId);
		}
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {

		edgeBeingRemoved = graph.getEdge(edgeId);
		elementRemoved(edgeBeingRemoved);
		edgeBeingRemoved = null;

		validator.validate("after removing edge '%s'", edgeId);
	}

	public void graphCleared(String sourceId, long timeId) {

	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attributeId, Object newValue) {
		nodeAttributeChanged(sourceId, timeId, nodeId, attributeId, null,
				newValue);
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attributeId) {

		if (metaIndexAttribute.equals(attributeId)) {

			Node n = graph.getNode(nodeId);
			Object nmoi = n.getAttribute(metaOrganizationIndexAttribute);

			if (nmoi != null) {
				Organization org = organizations.get(nmoi);
				org.remove(n);

				// If organization is empty,
				// remove this organization
				if (org.getNodeCount() == 0) {
					for (OrganizationListener l : listeners)
						l.organizationRemoved(org.metaIndex,
								org.metaOrganizationIndex);

					organizations.remove(org.metaOrganizationIndex);
				}
			}

			validator.validate("after removing node '%s' attribute '%s'",
					nodeId, attributeId);
		}
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attributeId, Object oldValue, Object newValue) {

		if (metaIndexAttribute.equals(attributeId)) {

			if (oldValue != null && newValue != null
					&& oldValue.equals(newValue))
				return;

			// System.out.printf("update meta index of \"%s\" %s --> %s%n",
			// nodeId, oldValue, newValue);

			Node n = graph.getNode(nodeId);

			if (oldValue != null
					&& n.hasAttribute(metaOrganizationIndexAttribute)) {
				Organization org = organizations.get(n
						.getAttribute(metaOrganizationIndexAttribute));
				org.remove(n);

				for (int i = 0; i < n.getDegree(); i++) {
					Edge e = n.getEdge(i);
					Object omoia = e.getOpposite(n).getAttribute(
							metaOrganizationIndexAttribute);
					Organization o = organizations.get(omoia);

					if (!org.metaOrganizationIndex.equals(omoia))
						for (OrganizationListener l : listeners)
							l.connectionRemoved(org.metaIndex,
									org.metaOrganizationIndex, o.metaIndex,
									o.metaOrganizationIndex, e.getId());

					org.remove(e);
				}

				// System.out.printf("remove node \"%s\" from %s@%s%n", nodeId,
				// org.metaOrganizationIndex, org.metaIndex);

				// If organization is empty,
				// remove this organization
				if (org.getNodeCount() == 0) {
					for (OrganizationListener l : listeners)
						l.organizationRemoved(org.metaIndex,
								org.metaOrganizationIndex);

					organizations.remove(org.metaOrganizationIndex);
				} else
					org.check();
			}

			if (newValue != null) {
				HashSet<Organization> potential = new HashSet<Organization>();
				Organization org = null;

				for (Edge e : n.getEdgeSet()) {
					Node o = e.getOpposite(n);

					if (o.hasAttribute(metaIndexAttribute)
							&& newValue.equals(o
									.getAttribute(metaIndexAttribute))) {
						if (o.hasAttribute(metaOrganizationIndexAttribute)) {
							Object orgId = o
									.getAttribute(metaOrganizationIndexAttribute);

							if (organizations.containsKey(orgId))
								potential.add(organizations.get(orgId));
						}
					}
				}

				switch (potential.size()) {
				case 0:
					// Create new organization
					// System.out.printf(
					// "create new organization from node \"%s\"%n",
					// n.getId());
					org = createNewOrganization(newValue, n);
					break;
				case 1:
					// Add node to organization
					for (Organization o : potential)
						org = o;

					org.include(n);

					break;
				default:
					// Fusion organizations
					Organization merged = null;

					for (Organization o : potential) {
						if (merged == null) {
							merged = o;
							merged.include(n);
						}
						else {
							if (!merged.metaIndex.equals(o.metaIndex))
								throw new Error(
										String
												.format(
														"try to merge organization with different metaindex (%s and %s)",
														merged.metaIndex,
														o.metaIndex));

							merged = merge(merged, o);
						}
					}

					org = merged;

					break;
				}

				for (int i = 0; i < n.getDegree(); i++) {
					Edge e = n.getEdge(i);
					Object omoia = e.getOpposite(n).getAttribute(
							metaOrganizationIndexAttribute);
					Organization o = organizations.get(omoia);

					if (o == null)
						continue;

					if (!org.metaOrganizationIndex.equals(omoia))
						for (OrganizationListener l : listeners)
							l.connectionCreated(org.metaIndex,
									org.metaOrganizationIndex, o.metaIndex,
									o.metaOrganizationIndex, e.getId());
					else
						org.include(e);
				}
			}

			validator.validate(
					"after changing node '%s' attribute '%s' to '%s'", nodeId,
					attributeId, newValue);
		}
	}

	protected Organization createNewOrganization(Object metaIndex, Node root) {
		String id = idGenerator.getNewId();

		Organization org = new Organization(this, metaIndex, id, graph, root);
		organizations.put(id, org);

		org.addElementSink(new OrganizationChangeListener(org.metaIndex,
				org.metaOrganizationIndex));

		for (OrganizationListener l : listeners)
			l.organizationCreated(metaIndex, id, root.getId());

		return org;
	}

	protected Organization merge(Organization org1, Organization org2) {

		// System.out.printf("merge %s@%s and %s@%s%n",
		// org1.metaOrganizationIndex, org1.metaIndex,
		// org2.metaOrganizationIndex, org2.metaIndex);

		// Make org1 the biggest organization
		if (org2.getNodeCount() > org1.getNodeCount()) {
			Organization t = org2;
			org2 = org1;
			org1 = t;
		}

		LinkedList<Node> nodes = new LinkedList<Node>();

		for (Node n : org2.getEachNode())
			nodes.add(n);

		for (Node n : nodes) {
			org2.remove(n);
			org1.include(n);
		}

		nodes.clear();

		for (OrganizationListener l : listeners)
			l.organizationMerged(org1.metaIndex, org1.metaOrganizationIndex,
					org2.metaOrganizationIndex, org1.organizationRoot.getId());

		for (OrganizationListener l : listeners)
			l.organizationRemoved(org2.metaIndex, org2.metaOrganizationIndex);

		org2.empty();
		organizations.remove(org2.metaOrganizationIndex);

		org1.checkRootNode();

		validator.validate("after merging organizations '%s' and '%s' of %s",
				org1.metaOrganizationIndex, org2.metaOrganizationIndex,
				org1.metaIndex);

		return org1;
	}

	void mitose(Object metaIndex, Organization base, LinkedList<String> orphans) {
		String id = idGenerator.getNewId();

		for (String nodeId : orphans) {
			Node n = graph.getNode(nodeId);
			base.remove(n);
		}

		if (base.getNodeCount() == 0)
			throw new Error("base is empty");

		Node subroot = graph.getNode(orphans.poll());
		Organization mitose = new Organization(this, base.metaIndex, id, graph,
				subroot, orphans);
		organizations.put(id, mitose);
		mitose.addElementSink(new OrganizationChangeListener(mitose.metaIndex,
				mitose.metaOrganizationIndex));

		for (OrganizationListener l : listeners)
			l.organizationCreated(mitose.metaIndex,
					mitose.metaOrganizationIndex, mitose.organizationRoot
							.getId());

		for (OrganizationListener l : listeners)
			l.organizationSplited(base.metaIndex, base.metaOrganizationIndex,
					id);

		mitose.check();

		validator.validate("after splitting organization '%s' of '%s' to '%s'",
				base.metaOrganizationIndex, base.metaIndex,
				mitose.metaOrganizationIndex);
	}

	void rootNodeUpdate(Organization org) {
		for (OrganizationListener l : listeners)
			l.organizationRootNodeUpdated(org.metaIndex,
					org.metaOrganizationIndex, org.organizationRoot.getId());
	}

	private class OrganizationChangeListener implements ElementSink {

		Object metaIndex;
		Object metaOrganizationIndex;

		OrganizationChangeListener(Object metaIndex,
				Object metaOrganizationIndex) {
			this.metaIndex = metaIndex;
			this.metaOrganizationIndex = metaOrganizationIndex;
		}

		void change(ChangeType changeType, ElementType elementType,
				String elementId) {
			for (OrganizationListener l : listeners)
				l.organizationChanged(metaIndex, metaOrganizationIndex,
						changeType, elementType, elementId);
		}

		public void edgeAdded(String sourceId, long timeId, String edgeId,
				String fromNodeId, String toNodeId, boolean directed) {
			change(ChangeType.ADD, ElementType.EDGE, edgeId);
		}

		public void edgeRemoved(String sourceId, long timeId, String edgeId) {
			change(ChangeType.REMOVE, ElementType.EDGE, edgeId);
		}

		public void graphCleared(String sourceId, long timeId) {

		}

		public void nodeAdded(String sourceId, long timeId, String nodeId) {
			change(ChangeType.ADD, ElementType.NODE, nodeId);
		}

		public void nodeRemoved(String sourceId, long timeId, String nodeId) {
			change(ChangeType.REMOVE, ElementType.NODE, nodeId);
		}

		public void stepBegins(String sourceId, long timeId, double step) {
		}
	}
}
