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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.stream.SinkAdapter;

public abstract class OrganizationManager extends SinkAdapter implements
		Iterable<Organization> {

	protected Graph entitiesGraph;

	protected String metaIndexAttribute = "meta.index";
	protected String metaOrganizationIndexAttribute = "meta.organization.index";

	protected final HashMap<Object, Organization> organizations;

	protected final LinkedList<OrganizationListener> listeners;

	protected final Validator validator;

	protected final LinkedList<Plugin> plugins;
	
	protected OrganizationManager() {
		organizations = new HashMap<Object, Organization>();
		listeners = new LinkedList<OrganizationListener>();
		plugins = new LinkedList<Plugin>();
		validator = Validation.getValidator(this);

		System.out.printf("Validation level is set to '%s'\n", Validation
				.getValidationLevel().name().toLowerCase());
	}

	public void enablePlugin(Plugin plugin) {
		if (plugins.contains(plugin))
			return;

		plugin.init(this);
		plugins.add(plugin);
	}

	public Iterator<Organization> iterator() {
		return organizations.values().iterator();
	}

	public void init(Graph g) {
		if (entitiesGraph != null)
			entitiesGraph.removeSink(this);

		entitiesGraph = g;
		entitiesGraph.addSink(this);
	}

	public Graph getEntitiesGraph() {
		return entitiesGraph;
	}

	public String getMetaIndexAttribute() {
		return metaIndexAttribute;
	}

	public String getMetaOrganizationIndexAttribute() {
		return metaOrganizationIndexAttribute;
	}

	public void setMetaIndexAttribute(String key) {
		this.metaIndexAttribute = key;
	}

	public void setMetaOrganizationIndexAttribute(String key) {
		this.metaOrganizationIndexAttribute = key;
	}

	public Organization getOrganization(Object id) {
		return organizations.get(id);
	}

	public void addOrganizationListener(OrganizationListener l) {
		listeners.add(l);
	}

	public void removeOrganizationListener(OrganizationListener l) {
		listeners.remove(l);
	}

	public boolean isAvailable(Element e) {
		return true;
	}
	
	public abstract void mitose(Object metaIndex, Organization base, LinkedList<String> orphans);
	
	public abstract boolean isEdgeAutoInclusionEnable();
	
	public void rootNodeUpdate(Organization org) {
		for (OrganizationListener l : listeners)
			l.organizationRootNodeUpdated(org.metaIndex,
					org.metaOrganizationIndex, org.organizationRoot.getId());
	}
}
