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

import java.util.LinkedList;

import org.graphstream.graph.Graph;
import org.graphstream.organic.Organization;
import org.graphstream.organic.OrganizationListener;
import org.graphstream.organic.OrganizationManager;
import org.graphstream.organic.Plugin;
import org.graphstream.stream.SinkAdapter;

public class Replayable implements Plugin, OrganizationListener {

	protected OrganizationManager manager;
	protected Graph graph;
	protected OrganizationInternalSink internalSink;
	protected LinkedList<PendingEvent> events;

	public void init(OrganizationManager manager) {
		manager.addOrganizationListener(this);
		this.manager = manager;
		graph = manager.getEntitiesGraph();
		internalSink = new OrganizationInternalSink();
		events = new LinkedList<PendingEvent>();
	}

	public void connectionCreated(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		flushPendingEvents();
		
		graph.addAttribute("organic.event.connectionCreated", metaIndex1,
				metaOrganizationIndex1, metaIndex2, metaOrganizationIndex2,
				connection);
	}

	public void connectionRemoved(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		flushPendingEvents();
		
		graph.addAttribute("organic.event.connectionRemoved", metaIndex1,
				metaOrganizationIndex1, metaIndex2, metaOrganizationIndex2,
				connection);
	}

	public void organizationChanged(Object metaIndex,
			Object metaOrganizationIndex, ChangeType changeType,
			ElementType elementType, String elementId) {
		flushPendingEvents();
		
		graph.addAttribute("organic.event.organizationChanged", metaIndex,
				metaOrganizationIndex, changeType.name(), elementType.name(),
				elementId);
	}

	public void organizationCreated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		flushPendingEvents();
		
		Organization org = manager.getOrganization(metaOrganizationIndex);
		org.addAttributeSink(internalSink);

		for (String key : org.getAttributeKeySet())
			internalSink.graphAttributeAdded(metaOrganizationIndex.toString(),
					0, key, org.getAttribute(key));

		graph.addAttribute("organic.event.organizationCreated", metaIndex,
				metaOrganizationIndex, rootNodeId);
	}

	public void organizationMerged(Object metaIndex,
			Object metaOrganizationIndex1, Object metaOrganizationIndex2,
			String rootNodeId) {
		flushPendingEvents();
		
		graph.addAttribute("organic.event.organizationMerged", metaIndex,
				metaOrganizationIndex1, metaOrganizationIndex2, rootNodeId);
	}

	public void organizationRemoved(Object metaIndex,
			Object metaOrganizationIndex) {
		flushPendingEvents();
		
		graph.addAttribute("organic.event.organizationRemoved", metaIndex,
				metaOrganizationIndex);
		manager.getOrganization(metaOrganizationIndex).removeAttributeSink(
				internalSink);
	}

	public void organizationRootNodeUpdated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		flushPendingEvents();
		
		graph.addAttribute("organic.event.organizationRootNodeUpdated",
				metaIndex, metaOrganizationIndex, rootNodeId);
	}

	public void organizationSplited(Object metaIndex,
			Object metaOrganizationBase, Object metaOrganizationChild) {
		flushPendingEvents();
		
		graph.addAttribute("organic.event.organizationSplited", metaIndex,
				metaOrganizationBase, metaOrganizationChild);
	}

	private void flushPendingEvents() {
		PendingEvent e;

		while (events.size() > 0) {
			e = events.poll();
			graph.addAttribute(e.key, e.args);
		}
	}

	protected class OrganizationInternalSink extends SinkAdapter {
		public void graphAttributeAdded(String sourceId, long timeId,
				String attribute, Object value) {
			events.add(new PendingEvent(
					"organic.event.organizationAttributeSet", sourceId,
					attribute, value));
		}

		public void graphAttributeChanged(String sourceId, long timeId,
				String attribute, Object oldValue, Object newValue) {
			graphAttributeAdded(sourceId, timeId, attribute, newValue);
		}

		public void graphAttributeRemoved(String sourceId, long timeId,
				String attribute) {
			events.add(new PendingEvent(
					"organic.event.organizationAttributeRemoved", sourceId,
					attribute));
		}
	}

	protected class PendingEvent {
		String key;
		Object[] args;

		PendingEvent(String key, Object... args) {
			this.key = key;
			this.args = args;
		}
	}
}
