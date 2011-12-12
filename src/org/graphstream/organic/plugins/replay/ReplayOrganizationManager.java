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
		CONNECTIONCREATED, CONNECTIONREMOVED, ORGANIZATIONCHANGED, ORGANIZATIONCREATED, ORGANIZATIONMERGED, ORGANIZATIONREMOVED, ORGANIZATIONROOTNODEUPDATED, ORGANIZATIONSPLITED
	}

	public void init(Graph g) {
		super.init(g);

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
				ChangeType changeType = ChangeType.valueOf((String) args[2]);
				ElementType elementType = ElementType.valueOf((String) args[3]);

				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationChanged(args[0], args[1],
							changeType, elementType, (String) args[4]);

				break;
			case ORGANIZATIONCREATED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationCreated(args[0], args[1],
							(String) args[3]);

				break;
			case ORGANIZATIONMERGED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationMerged(args[0], args[1],
							args[2], (String) args[3]);

				break;
			case ORGANIZATIONREMOVED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationRemoved(args[0], args[1]);

				break;
			case ORGANIZATIONROOTNODEUPDATED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationRootNodeUpdated(args[0],
							args[1], (String) args[2]);

				break;
			case ORGANIZATIONSPLITED:
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).organizationSplited(args[0], args[1],
							args[2]);

				break;
			}
		} else if (key.equalsIgnoreCase("metaIndexAttribute")) {
			super.setMetaIndexAttribute((String) value);
		} else if (key.equalsIgnoreCase("metaOrganizationIndexAttribute")) {
			super.setMetaOrganizationIndexAttribute((String) value);
		} else {
			String orgId = key.substring(0, key.indexOf('.'));
			key = key.substring(orgId.length() + 1);
			Organization org = organizations.get(orgId);
			
			if (org == null)
				throw new NullPointerException();
			
			
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
}
