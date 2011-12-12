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

import org.graphstream.graph.Graph;
import org.graphstream.organic.OrganizationListener;
import org.graphstream.organic.OrganizationManager;
import org.graphstream.organic.Plugin;

public class Replayable implements Plugin, OrganizationListener {

	protected Graph graph;

	public void init(OrganizationManager manager) {
		manager.addOrganizationListener(this);
		graph = manager.getEntitiesGraph();
	}

	public void connectionCreated(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		graph.addAttribute("organic.event.connectionCreated", metaIndex1,
				metaOrganizationIndex1, metaIndex2, metaOrganizationIndex2,
				connection);
	}

	public void connectionRemoved(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		graph.addAttribute("organic.event.connectionRemoved", metaIndex1,
				metaOrganizationIndex1, metaIndex2, metaOrganizationIndex2,
				connection);
	}

	public void organizationChanged(Object metaIndex,
			Object metaOrganizationIndex, ChangeType changeType,
			ElementType elementType, String elementId) {
		graph.addAttribute("organic.event.organizationChanged", metaIndex,
				metaOrganizationIndex, changeType.name(), elementType.name(),
				elementId);
	}

	public void organizationCreated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		graph.addAttribute("organic.event.organizationCreated", metaIndex,
				metaOrganizationIndex, rootNodeId);
	}

	public void organizationMerged(Object metaIndex,
			Object metaOrganizationIndex1, Object metaOrganizationIndex2,
			String rootNodeId) {
		graph.addAttribute("organic.event.organizationMerged", metaIndex,
				metaOrganizationIndex1, metaOrganizationIndex2, rootNodeId);
	}

	public void organizationRemoved(Object metaIndex,
			Object metaOrganizationIndex) {
		graph.addAttribute("organic.event.organizationRemoved", metaIndex,
				metaOrganizationIndex);
	}

	public void organizationRootNodeUpdated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		graph.addAttribute("organic.event.organizationRootNodeUpdated",
				metaIndex, metaOrganizationIndex, rootNodeId);
	}

	public void organizationSplited(Object metaIndex,
			Object metaOrganizationBase, Object metaOrganizationChild) {
		graph.addAttribute("organic.event.organizationRootNodeSplited",
				metaIndex, metaOrganizationBase, metaOrganizationChild);
	}

}
