package org.graphstream.graph.organization;

/**
 * What event is triggered ?
 * <dl>
 * <dt>a new organization is created :</dt>
 * <dd>
 * <ol>
 * <li>organizationCreated</li>
 * </ol>
 * </dd>
 * <dt>an organization is removed :</dt>
 * <dd>
 * <ol>
 * <li>organizationRemoved</li>
 * </ol>
 * </dd>
 * <dt>an organization is splited :</dt>
 * <dd>
 * <ol>
 * <li>organizationCreated</li>
 * <li>organizationSplited</li>
 * </ol>
 * </dd>
 * <dt>two organizations are merged :</dt>
 * <dd>
 * <ol>
 * <li>organizationMerged</li>
 * <li>organizationRemoved</li>
 * </ol>
 * </dd>
 * </dl>
 * 
 */
public interface OrganizationListener {
	void organizationCreated(Object metaIndex, Object metaOrganizationIndex,
			String rootNodeId);

	void organizationRootNodeUpdated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId);

	void organizationMerged(Object metaIndex, Object metaOrganizationIndex1,
			Object metaOrganizationIndex2, String rootNodeId);

	void organizationSplited(Object metaIndex, Object metaOrganizationBase,
			Object metaOrganizationChild);

	void organizationRemoved(Object metaIndex, Object metaOrganizationIndex);

	public static enum ChangeType {
		ADD, REMOVE
	}

	public static enum ElementType {
		NODE, EDGE
	}

	void organizationChanged(Object metaIndex, Object metaOrganizationIndex,
			ChangeType changeType, ElementType elementType, String elementId);

	void connectionCreated(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection);

	void connectionRemoved(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection);
}
