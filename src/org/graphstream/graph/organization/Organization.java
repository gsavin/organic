package org.graphstream.graph.organization;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.subgraph.Conditions;
import org.graphstream.graph.subgraph.IncludeCondition;
import org.graphstream.graph.subgraph.SubGraph;

/**
 * An organization in a graph. Organizations are a connected set of nodes which
 * share a common meta index.
 * 
 * @author Guilhelm Savin
 * 
 */
public class Organization extends SubGraph implements IncludeCondition {

	static class ColorProvider {

		int r, g, b;
		int p;
		Random rand;

		public ColorProvider() {
			rand = new Random();

			r = rand.nextInt(256);
			g = rand.nextInt(256);
			b = rand.nextInt(256);

			p = rand.nextInt(3);
		}

		public String getNewColor() {
			String c = String.format("#%02X%02X%02X", r, g, b);

			int delta = 70 + rand.nextInt(10);

			switch (p) {
			case 0:
				r = (r + delta) % 256;
				break;
			case 1:
				g = (g + delta) % 256;
				break;
			case 2:
				b = (b + delta) % 256;
				break;
			}

			p = rand.nextInt(3);

			return c;
		}
	}

	protected static final ColorProvider cp = new ColorProvider();

	protected String color = cp.getNewColor();

	protected Object metaIndex;
	protected Object metaOrganizationIndex;

	protected Node organizationRoot;

	protected DefaultOrganizationManager manager;
	
	public Organization(DefaultOrganizationManager manager, Object metaIndex,
			Object metaOrganizationIndex, Graph fullGraph, Node root) {
		super(metaOrganizationIndex.toString(), fullGraph, Conditions.none(),
				true);

		//System.out.printf("*** created %s@%s ***%n", metaOrganizationIndex,
		//		metaIndex);

		this.metaIndex = metaIndex;
		this.metaOrganizationIndex = metaOrganizationIndex;
		this.manager = manager;
		this.condition = this;
		this.organizationRoot = root;

		if (root.hasAttribute(manager.metaIndexAttribute)
				&& !metaIndex.equals(root
						.getAttribute(manager.metaIndexAttribute)))
			throw new Error("meta index of organization ("
					+ metaOrganizationIndex + "@" + metaIndex
					+ ") differ from meta index of root node \"" + root.getId()
					+ "\" (" + root.getAttribute(manager.metaIndexAttribute)
					+ ")");

		LinkedList<Node> toVisit = new LinkedList<Node>();
		toVisit.add(root);

		while (toVisit.size() > 0) {
			Node n = toVisit.poll();
			if (!nodes.contains(n.getId()))
				include(n);

			for (Edge e : n.getEdgeSet()) {
				Node o = e.getOpposite(n);

				if (!nodes.contains(o.getId())
						&& o.hasAttribute(manager.metaIndexAttribute)
						&& metaIndex.equals(o
								.getAttribute(manager.metaIndexAttribute))) {
					toVisit.addLast(o);
				}
			}
		}
	}

	public Organization(DefaultOrganizationManager manager, Object metaIndex,
			Object metaOrganizationIndex, Graph fullGraph, Node root,
			Collection<String> members) {
		this(manager, metaIndex, metaOrganizationIndex, fullGraph, root);

		for (String nodeId : members) {
			Node n = fullGraph.getNode(nodeId);
			include(n);
		}
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

		if (e.hasAttribute(manager.metaIndexAttribute)
				&& !metaIndex
						.equals(e.getAttribute(manager.metaIndexAttribute)))
			throw new Error("try to include a node with bad meta index");

		if (e.hasAttribute(manager.metaOrganizationIndexAttribute)) {
			if (metaOrganizationIndex.equals(e
					.getAttribute(manager.metaOrganizationIndexAttribute))) {
				/*System.err.printf(
						"warning: include twice node \"%s\" in %s@%s%n",
						e.getId(), metaOrganizationIndex, metaIndex);*/
			} else {
				throw new Error(String.format(
						"element \"%s\" already in %s@%s, including in %s@%s",
						e.getId(),
						e.getAttribute(manager.metaOrganizationIndexAttribute),
						e.getAttribute(manager.metaIndexAttribute),
						metaOrganizationIndex, metaIndex));
			}
		} else {
			e.setAttribute(manager.metaOrganizationIndexAttribute,
					metaOrganizationIndex);
			e.setAttribute("ui.style", String.format("fill-color: %s;", color));
			
			super.include(e);
			//printContent();

			if (e instanceof Node) {
				Node n = (Node) e;

				//System.out.printf("[%s@%s] include node \"%s\"%n",
				//		metaOrganizationIndex, metaIndex, n.getId());

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
					String.format(
							"remove element %s which is not in the organization %s@%s%n",
							e.getId(), metaOrganizationIndex, metaIndex));

		e.removeAttribute(manager.metaOrganizationIndexAttribute);
		
		super.remove(e);
		//printContent();

		if (e instanceof Node) {
			//System.out.printf("[%s@%s] remove node %s%n",
			//		metaOrganizationIndex, metaIndex, e.getId());
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
	
	public boolean hardTest() {
		LinkedList<String> reached = new LinkedList<String>();
		LinkedList<Node> toVisit = new LinkedList<Node>();

		if (organizationRoot == null)
			throw new Error("root is null");

		toVisit.add(organizationRoot);

		while (toVisit.size() > 0) {
			Node n = toVisit.poll();
			reached.add(n.getId());

			for (Edge e : n.getEdgeSet()) {
				Node o = e.getOpposite(n);
				/*
				 * if (nodes.contains(o.getId()) && !reached.contains(o.getId())
				 * && !toVisit.contains(o)) { toVisit.addLast(o); }
				 */
				if (nodes.contains(o.getId())
						&& metaOrganizationIndex
								.equals(o
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

		for (String id : nodes) {
			stable = stable && reached.contains(id);
		}

		for (String id : nodes) {
			Node n = fullGraph.getNode(id);
			stable = stable && metaIndex.equals(n.getAttribute(manager.metaIndexAttribute));
			for (Edge e : n.getEdgeSet()) {
				Node o = e.getOpposite(n);
				stable = stable
						&& ((nodes.contains(o.getId()) && metaOrganizationIndex
								.equals(o
										.getAttribute(manager.metaOrganizationIndexAttribute))) || (!nodes
								.contains(o.getId()) && !metaOrganizationIndex
								.equals(manager.metaOrganizationIndexAttribute)));
			}
		}

		return stable;
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
	public boolean isInside(Element n) {
		return n.hasAttribute(manager.metaOrganizationIndexAttribute)
				&& metaOrganizationIndex.equals(n.getAttribute(getId()));
	}
	*/
	
	public void check() {
		//System.out.printf("[%s@%s] check%n", metaOrganizationIndex, metaIndex);
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
				Node o = e.getOpposite(n);
				/*
				 * if (nodes.contains(o.getId()) && !reached.contains(o.getId())
				 * && !toVisit.contains(o)) { toVisit.addLast(o); }
				 */
				if (nodes.contains(o.getId())
						&& metaOrganizationIndex
								.equals(o
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

			//System.out.printf("\tnot stable%n");

			LinkedList<String> notReached = new LinkedList<String>(nodes);
			notReached.removeAll(reached);

			if (notReached.size() > 0) {
				manager.mitose(metaIndex, this, notReached);
			}

			checkRootNode();
		} //else
		//	System.out.printf("\tstable%n");
	}

	public static class AloneOrganization extends Organization {

		public AloneOrganization(Object metaIndex,
				Object metaOrganizationIndex, Graph fullGraph, Node root) {
			super(null, metaIndex, metaOrganizationIndex, fullGraph, root);
		}

		public void check() {

		}
	}
}
