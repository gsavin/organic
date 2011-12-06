package org.graphstream.graph.organization;

import org.graphstream.graph.Graph;

public interface OrganizationManager {
	void init(Graph g);

	String getMetaIndexAttribute();

	String getMetaOrganizationIndexAttribute();

	void setMetaIndexAttribute(String key);

	void setMetaOrganizationIndexAttribute(String key);

	Organization getOrganization(Object id);

	void addOrganizationListener(OrganizationListener l);

	void removeOrganizationListener(OrganizationListener l);
}
