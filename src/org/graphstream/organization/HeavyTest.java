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
package org.graphstream.organization;

import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.organization.OrganizationListener.ChangeType;
import org.graphstream.organization.OrganizationListener.ElementType;
import org.graphstream.stream.ElementSink;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.Resolutions;

public class HeavyTest implements OrganizationListener, ElementSink {

	public static void main(String[] args) throws Exception {
		System.setProperty("org.graphstream.organization.validation", "none");
		
		FileSourceDGS dgs = new FileSourceDGS();
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		OrganizationsGraph metaGraph = new OrganizationsGraph(g);

		FileSinkImages images1 = new FileSinkImages();
		images1.setQuality(Quality.HIGH);
		images1.setOutputPolicy(OutputPolicy.BY_STEP);

		FileSinkImages images2 = new FileSinkImages();
		images2.setResolution(Resolutions.VGA);
		images2.setQuality(Quality.HIGH);
		images2.setOutputPolicy(OutputPolicy.BY_STEP);
		images2.setLayoutPolicy(LayoutPolicy.COMPUTED_AT_NEW_IMAGE);
		images2.setLayoutStepPerFrame(5);

		g.addSink(images1);
		metaGraph.addSink(images2);

		images1.begin("entities_");
		images2.begin("meta_");
		
		metaGraph.getManager().setMetaIndexAttribute("meta.index");
		//metaGraph.getManager().addOrganizationListener(new HeavyTest());

		//dgs.addElementSink(new HeavyTest());
		dgs.addSink(g);
		dgs.begin(Test.class.getResourceAsStream("BoidsMovie+antco2.dgs"));

		//g.display(false);
		//metaGraph.display();
		
		int step = 0;

		while (dgs.nextStep())
			System.out.printf("step #%d\n", step++);
		
		images1.end();
		images2.end();
	}

	public void connectionCreated(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		System.out.printf("- new connection [%s|%s] -- %s -- [%s|%s]\n",
				metaIndex1, metaOrganizationIndex1, connection, metaIndex2,
				metaOrganizationIndex2);
	}

	public void connectionRemoved(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		System.out.printf("- del connection [%s|%s] xx %s xx [%s|%s]\n",
				metaIndex1, metaOrganizationIndex1, connection, metaIndex1,
				metaOrganizationIndex1);
	}

	public void organizationChanged(Object metaIndex,
			Object metaOrganizationIndex, ChangeType changeType,
			ElementType elementType, String elementId) {
		System.out.printf("# organization [%s|%s] changed : %s, %s, %s\n",
				metaIndex, metaOrganizationIndex, changeType, elementType,
				elementId);
	}

	public void organizationCreated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		System.out.printf("* new organization [%s|%s] root @ %s\n", metaIndex,
				metaOrganizationIndex, rootNodeId);
	}

	public void organizationMerged(Object metaIndex,
			Object metaOrganizationIndex1, Object metaOrganizationIndex2,
			String rootNodeId) {
		System.out.printf(
				"* organizations [%s|%s] and [%s|%s] merged, root @ %s\n",
				metaIndex, metaOrganizationIndex1, metaIndex,
				metaOrganizationIndex2, rootNodeId);
	}

	public void organizationRemoved(Object metaIndex,
			Object metaOrganizationIndex) {
		System.out.printf("* del organization [%s|%s]\n", metaIndex,
				metaOrganizationIndex);
	}

	public void organizationRootNodeUpdated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		System.out.printf("* root of [%s|%s] is %s\n", metaIndex,
				metaOrganizationIndex, rootNodeId);
	}

	public void organizationSplited(Object metaIndex,
			Object metaOrganizationBase, Object metaOrganizationChild) {
		System.out.printf("* organization [%s|%s] splited, child is %s\n",
				metaIndex, metaOrganizationBase, metaOrganizationChild);
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		System.out.printf("~ EDGE ADDED '%s' '%s' -- '%s'\n", edgeId,
				fromNodeId, toNodeId);
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		System.out.printf("~ EDGE REMOVED '%s'\n", edgeId);
	}

	public void graphCleared(String sourceId, long timeId) {
		// TODO Auto-generated method stub

	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		System.out.printf("~ NODE ADDED '%s'\n", nodeId);
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		System.out.printf("~ NODE REMOVED '%s'\n", nodeId);
	}

	public void stepBegins(String sourceId, long timeId, double step) {
		System.out.printf("~ STEP BEGINS %f\n", step);
	}
}
