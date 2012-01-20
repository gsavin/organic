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
package org.graphstream.organic.plugins.replay;

import java.util.HashMap;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.organic.Organization;
import org.graphstream.organic.OrganizationManager;
import org.graphstream.organic.OrganizationListener.ChangeType;
import org.graphstream.organic.OrganizationListener.ElementType;

/**
 * <pre>
 * organic.metaIndexAttribute = "..."
 * organic.metaOrganizationIndexAttribute = "..."
 * organic.org.xxx.color
 * organic.event.connectionCreated = ""
 * organic.event.connectionRemoved = ""
 * organic.event.organizationChanged
 * organic.event.organizationCreated
 * organic.event.organizationMerged
 * organic.event.organizationRemoved
 * organic.event.organizationRootNotUpdated
 * organic.event.organizationSplited
 * </pre>
 */
public class ReplayOrganizationManager extends OrganizationManager {

	enum Event {
		CONNECTIONCREATED, CONNECTIONREMOVED, ORGANIZATIONCHANGED, ORGANIZATIONCREATED, ORGANIZATIONMERGED, ORGANIZATIONREMOVED, ORGANIZATIONROOTNODEUPDATED, ORGANIZATIONSPLITED, ORGANIZATIONATTRIBUTESET, ORGANIZATIONATTRIBUTEREMOVED
	}

	HashMap<String, Organization> node2organization;
	HashMap<String, Organization> edge2organization;

	public boolean isEdgeAutoInclusionEnable() {
		return false;
	}

	public void init(Graph g) {
		super.init(g);

		node2organization = new HashMap<String, Organization>();
		edge2organization = new HashMap<String, Organization>();

		for (String key : g.getAttributeKeySet())
			handleAttribute(key, g.getAttribute(key));
	}

	public void setMetaIndexAttribute(String key) {
		throw new UnsupportedOperationException();
	}

	public void setMetaOrganizationIndexAttribute(String key) {
		throw new UnsupportedOperationException();
	}

	protected void handleAttribute(String key, Object value) {
		if (!key.startsWith("organic."))
			return;

		key = key.substring(8);

		if (key.startsWith("event.")) {
			key = key.substring(6);

			Event e = Event.valueOf(key.toUpperCase());
			Object[] args = (Object[]) value;
			Organization org;
			Element elt = null;

			switch (e) {
			case CONNECTIONCREATED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).connectionCreated(args[0], args[1],
							args[2], args[3], (String) args[4]);

				break;
			case CONNECTIONREMOVED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).connectionRemoved(args[0], args[1],
							args[2], args[3], (String) args[4]);

				break;
			case ORGANIZATIONCHANGED:
				// org = organizations.get(args[1]);
				/*
				 * if (args[2].toString().equalsIgnoreCase("ADD")) { if
				 * (args[3].toString().equalsIgnoreCase("NODE")) { Node n =
				 * entitiesGraph.getNode(args[4].toString()); org.include(n); }
				 * else { Edge o = entitiesGraph.getEdge(args[4].toString());
				 * org.include(o); } } else { if
				 * (args[3].toString().equalsIgnoreCase("NODE")) { Node n =
				 * entitiesGraph.getNode(args[4].toString()); org.notInclude(n);
				 * } else { Edge o = entitiesGraph.getEdge(args[4].toString());
				 * org.notInclude(o); } }
				 */
				break;
			case ORGANIZATIONCREATED:
				elt = entitiesGraph.getNode((String) args[2]);
				org = new Organization(this, args[0], args[1], entitiesGraph,
						(Node) elt);

				organizations.put(args[1], org);

				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationCreated(args[0], args[1],
							(String) args[2]);

				break;
			case ORGANIZATIONMERGED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationMerged(args[0], args[1],
							args[2], (String) args[3]);

				break;
			case ORGANIZATIONREMOVED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationRemoved(args[0], args[1]);

				organizations.remove(args[1]);

				break;
			case ORGANIZATIONROOTNODEUPDATED:
				org = organizations.get(args[1]);
				org.setRootNode(entitiesGraph.getNode((String) args[2]));

				break;
			case ORGANIZATIONSPLITED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationSplited(args[0], args[1],
							args[2]);

				break;
			case ORGANIZATIONATTRIBUTESET:
				org = organizations.get(args[0]);
				org.setAttribute((String) args[1], args[2]);

				break;
			case ORGANIZATIONATTRIBUTEREMOVED:
				org = organizations.get(args[0]);
				org.removeAttribute((String) args[1]);

				break;
			}
		} else if (key.equalsIgnoreCase("metaIndexAttribute")) {
			super.setMetaIndexAttribute((String) value);
		} else if (key.equalsIgnoreCase("metaOrganizationIndexAttribute")) {
			super.setMetaOrganizationIndexAttribute((String) value);
		}
	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		handleAttribute(attribute, value);
	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		handleAttribute(attribute, newValue);
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		nodeAttributeChanged(sourceId, timeId, nodeId, attribute, null, value);
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		if (attribute.equals(metaOrganizationIndexAttribute)) {
			Organization org1;
			Organization org2 = organizations.get(newValue);
			Node n = entitiesGraph.getNode(nodeId);

			org1 = node2organization.get(nodeId);

			if (org1 != null) {
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationChanged(org1.getMetaIndex(),
							org1.getMetaOrganizationIndex(), ChangeType.REMOVE,
							ElementType.NODE, nodeId);

				org1.notInclude(n);
			}

			if (org2 == null)
				throw new NullPointerException(String.format(
						"node '%s' : '%s'", nodeId, newValue.toString()));

			node2organization.put(nodeId, org2);
			org2.include(n);

			for (int i = 0; i < listeners.size(); i++)
				listeners.get(i).organizationChanged(org2.getMetaIndex(),
						org2.getMetaOrganizationIndex(), ChangeType.ADD,
						ElementType.NODE, nodeId);
		}
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		if (attribute.equals(metaOrganizationIndexAttribute)) {
			Node n = entitiesGraph.getNode(nodeId);
			Organization org1;

			if (n == null)
				throw new ElementNotFoundException("node \"%s\"", nodeId);

			org1 = node2organization.get(nodeId);
			
			if (org1 == null) {
				System.err.printf("[warning] organization unknown for node '%s'\n", nodeId);
				return;
			}

			for (int i = 0; i < listeners.size(); i++)
				listeners.get(i).organizationChanged(org1.getMetaIndex(),
						org1.getMetaOrganizationIndex(), ChangeType.REMOVE,
						ElementType.NODE, nodeId);

			node2organization.remove(nodeId);
			org1.notInclude(n);
		}
	}

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		edgeAttributeChanged(sourceId, timeId, edgeId, attribute, null, value);
	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		if (attribute.equals(metaOrganizationIndexAttribute)) {
			Organization org1;
			Organization org2 = organizations.get(newValue);
			Edge e = entitiesGraph.getEdge(edgeId);

			if (e == null)
				throw new ElementNotFoundException("edge \"%s\"", edgeId);

			org1 = edge2organization.get(edgeId);

			if (org1 != null) {
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationChanged(org1.getMetaIndex(),
							org1.getMetaOrganizationIndex(), ChangeType.REMOVE,
							ElementType.EDGE, edgeId);

				org1.notInclude(e);
			}

			org2.include(e);
			edge2organization.put(edgeId, org2);

			for (int i = 0; i < listeners.size(); i++)
				listeners.get(i).organizationChanged(org2.getMetaIndex(),
						org2.getMetaOrganizationIndex(), ChangeType.ADD,
						ElementType.EDGE, edgeId);
		}
	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		if (attribute.equals(metaOrganizationIndexAttribute)) {
			Edge e = entitiesGraph.getEdge(edgeId);
			Organization org1;

			if (e == null)
				throw new ElementNotFoundException("edge \"%s\"", edgeId);

			org1 = edge2organization.get(edgeId);

			if (org1 == null) {
				System.err.printf("[warning] organization unknown for edge '%s'\n", edgeId);
				return;
			}

			for (int i = 0; i < listeners.size(); i++)
				listeners.get(i).organizationChanged(org1.getMetaIndex(),
						org1.getMetaOrganizationIndex(), ChangeType.REMOVE,
						ElementType.EDGE, edgeId);

			org1.notInclude(e);
			edge2organization.remove(edgeId);
		}
	}

	public void mitose(Object metaIndex, Organization base,
			LinkedList<String> orphans) {
		throw new UnsupportedOperationException();
	}
}
