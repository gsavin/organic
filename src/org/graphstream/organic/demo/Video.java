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

import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.organic.OrganizationManagerFactory;
import org.graphstream.organic.OrganizationsGraph;
import org.graphstream.organic.Validation;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.RendererType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;

public class Video {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty(OrganizationManagerFactory.PROPERTY,
				"plugins.replay.ReplayOrganizationManager");
		System.setProperty(Validation.PROPERTY, "none");

		FileSourceDGS dgs = new FileSourceDGS();
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		OrganizationsGraph metaGraph = new OrganizationsGraph(g);

		FileSinkImages images1 = new FileSinkImages();
		images1.setResolution(Resolutions.HD1080);
		images1.setRenderer(RendererType.SCALA);
		images1.setQuality(Quality.HIGH);
		images1.setOutputPolicy(OutputPolicy.BY_STEP);

		FileSinkImages images2 = new FileSinkImages();
		images2.setResolution(Resolutions.VGA);
		images2.setRenderer(RendererType.SCALA);
		images2.setQuality(Quality.HIGH);
		images2.setOutputPolicy(OutputPolicy.BY_STEP);
		images2.setLayoutPolicy(LayoutPolicy.COMPUTED_ONCE_AT_NEW_IMAGE);
		images2.setLayoutStepPerFrame(2);
		images2.setClearImageBeforeOutputEnabled(true);
		
		images1
				.setStyleSheet("graph {fill-mode:gradient-radial;fill-color:#FFFFFF,#EEEEEE;} node {size:15px;stroke-mode:plain;stroke-color:#1D1D1D;stroke-width:1px;}");
		images2
				.setStyleSheet("graph {fill-mode:none;} node {stroke-mode:plain;stroke-color:#1D1D1D;stroke-width:2px;} node { size: 45px; } edge { fill-color: gray; }");

		// g.addSink(new VerboseSink(System.err));
		dgs.addSink(g);

		g.addSink(images1);
		metaGraph.addSink(images2); 
		
		dgs.begin("replayable.dgs");

		images1.begin("entities_");
		images2.begin("meta_");

		while (dgs.nextStep())
			;

		
		images1.end();
		images2.end();
		dgs.end();
	}

}
