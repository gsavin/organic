package org.graphstream.graph.organization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.organization.OrganizationListener.ChangeType;
import org.graphstream.graph.organization.OrganizationListener.ElementType;
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

	public DefaultOrganizationManager() {
		this.organizations = new HashMap<Object, Organization>();
		this.listeners = new LinkedList<OrganizationListener>();
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

	public int getOrganizationCount() {
		return organizations.size();
	}

	public void hardTest() {
		boolean test = true;

		for (Organization org : organizations.values())
			test = test && org.hardTest();

		if (!test) {
			System.err.printf("*** HARD TEST FAILED ***%n");
			System.exit(1);
		}
	}

	protected void elementRemoved(Element e) {

		Object nmoi = e.getAttribute(metaOrganizationIndexAttribute);

		if (nmoi != null) {
			Organization org = organizations.get(nmoi);
			org.remove(e);
			org.check();
		}

		hardTest();
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {

	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {

		Node n = graph.getNode(nodeId);
		elementRemoved(n);
		hardTest();
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

			if (n0moi != null && n1moi != null && !n0moi.equals(n1moi)) {

				Organization org0 = organizations.get(n0moi);
				Organization org1 = organizations.get(n1moi);

				merge(org0, org1);
			}
		} else if (n0mi != null && n1mi != null) {
			Object n0moi = n0.getAttribute(metaOrganizationIndexAttribute);
			Object n1moi = n1.getAttribute(metaOrganizationIndexAttribute);

			if (n0moi != null && n1moi != null && !n0moi.equals(n1moi)) {
				for (OrganizationListener l : listeners)
					l.connectionCreated(n0mi, n0moi, n1mi, n1moi, edgeId);
			}
		}

		hardTest();
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {

		Edge e = graph.getEdge(edgeId);
		elementRemoved(e);

		hardTest();
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
			hardTest();
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
						if (merged == null)
							merged = o;
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

					merged.include(n);
					org = merged;

					break;
				}

				for (int i = 0; i < n.getDegree(); i++) {
					Edge e = n.getEdge(i);
					Object omoia = e.getOpposite(n).getAttribute(
							metaOrganizationIndexAttribute);
					Organization o = organizations.get(omoia);

					System.out.printf("here\n");

					if (!org.metaOrganizationIndex.equals(omoia))
						for (OrganizationListener l : listeners)
							l.connectionCreated(org.metaIndex,
									org.metaOrganizationIndex, o.metaIndex,
									o.metaOrganizationIndex, e.getId());
				}
			}
			hardTest();
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

		return org1;
	}

	void mitose(Object metaIndex, Organization base, LinkedList<String> orphans) {

		String id = idGenerator.getNewId();

		// System.out.printf("[manager] MITOSE of %s@%s into %s%n",
		// base.metaOrganizationIndex, base.metaIndex, id);

		// System.out.printf("\torphans: %s%n",
		// Arrays.toString(orphans.toArray()));
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

		// System.out.printf("[manager] END of mitose of %s@%s%n",base.metaOrganizationIndex,base.metaIndex);
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
