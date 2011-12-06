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
