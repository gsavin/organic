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

import org.graphstream.algorithm.measure.MaxSimultaneousNodeCount;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.organic.OrganizationManagerFactory;
import org.graphstream.organic.OrganizationsGraph;
import org.graphstream.organic.Validation;
import org.graphstream.organic.plugins.Colorize;
import org.graphstream.organic.ui.OrganizationsView;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.util.VerboseSink;

public class ReplayIt {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("gs.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty(OrganizationManagerFactory.PROPERTY,
				"plugins.replay.ReplayOrganizationManager");
		System.setProperty(Validation.PROPERTY, "none");

		String what = "replayable.dgs";

		FileSourceDGS dgs = new FileSourceDGS();
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		OrganizationsGraph metaGraph = new OrganizationsGraph(g);
		
		OrganizationsView ui = new OrganizationsView(metaGraph);
		ui.enableHQ();
		ui.getMetaViewer().enableAutoLayout();
		ui.createFrame().repaint();

		g.addAttribute("ui.stylesheet", "graph {fill-mode:gradient-radial;fill-color:#FFFFFF,#EEEEEE;} node {stroke-mode:plain;stroke-color:#1D1D1D;stroke-width:1px;}");
		metaGraph.addAttribute("ui.stylesheet", "graph {fill-mode:gradient-radial;fill-color:#FFFFFF,#EEEEEE;} node {stroke-mode:plain;stroke-color:#1D1D1D;stroke-width:2px;}");
		
		// g.addSink(new VerboseSink(System.err));
		dgs.addSink(g);

		dgs.begin(what);

		while (dgs.nextStep()) {
			ui.pumpEvents();
			Thread.sleep(100);
		}

		dgs.end();
	}

}
