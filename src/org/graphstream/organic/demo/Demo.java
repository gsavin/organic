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
package org.graphstream.organic.demo;

import java.util.concurrent.atomic.AtomicBoolean;

import org.graphstream.algorithm.measure.MaxSimultaneousNodeCount;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.organic.OrganizationListener;
import org.graphstream.organic.OrganizationsGraph;
import org.graphstream.organic.Validation;
import org.graphstream.organic.OrganizationListener.ChangeType;
import org.graphstream.organic.OrganizationListener.ElementType;
import org.graphstream.organic.plugins.Colorize;
import org.graphstream.organic.plugins.replay.Replayable;
import org.graphstream.organic.ui.OrganizationsView;
import org.graphstream.stream.file.FileSinkDGS;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.util.VerboseSink;

public class Demo implements OrganizationListener {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Thread.sleep(10000);
		// System.setProperty("gs.ui.renderer",
		// "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty(Validation.PROPERTY, "none");

		FileSourceDGS dgs = new FileSourceDGS();
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		OrganizationsGraph metaGraph = new OrganizationsGraph(g);

		Colorize c = new Colorize();
		c.loadPatternFromStream(MakeReplay.class.getResourceAsStream("colors"));
		
		metaGraph.getManager().enablePlugin(c);
		metaGraph.getManager().setMetaIndexAttribute("meta.index");
		
		MaxSimultaneousNodeCount nCount1 = new MaxSimultaneousNodeCount();
		MaxSimultaneousNodeCount nCount2 = new MaxSimultaneousNodeCount();
		
		g.addSink(nCount1);
		metaGraph.addSink(nCount2);
		// metaGraph.getManager().addOrganizationListener(new Test());

		// g.addSink(new VerboseSink());
		dgs.addSink(g);

		OrganizationsView ui = new OrganizationsView(metaGraph);
		// ui.enableHQ();
		//ui.getMetaViewer().enableAutoLayout();
		//ui.createFrame().repaint();

		//dgsOut.begin("replayable.dgs");

		//test(dgs, ui);
		boids(dgs, null);
		
		System.out.printf("max nodes in entities graph : %d\n", nCount1.getMaxSimultaneousNodeCount());
		System.out.printf("max nodes in meta graph     : %d\n", nCount2.getMaxSimultaneousNodeCount());

		//dgsOut.end();
	}

	public static void test(FileSourceDGS dgs, OrganizationsView ui)
			throws Exception {
		// ui.enableEntitiesLayout();
		dgs.readAll(Demo.class.getResourceAsStream("test.dgs"));

		ui.pumpLoop(new AtomicBoolean(true), 250);
	}

	public static void boids(FileSourceDGS dgs, OrganizationsView ui)
			throws Exception {
		dgs.begin(Demo.class.getResourceAsStream("BoidsMovie+antco2.dgs"));

		int step = 0;
		while (dgs.nextStep()) System.out.printf("step #%d\n", step++);
		//	ui.pumpEvents();

		dgs.end();
		//ui.pumpLoop(new AtomicBoolean(true), 250);
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
		System.out.printf("* organization [%s|%s] changed : %s, %s, %s\n",
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
}
