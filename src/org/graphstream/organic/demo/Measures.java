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
import org.graphstream.organic.measure.AverageCentroidByOrganizationMeasure;
import org.graphstream.organic.measure.AverageConnectivityMeasure;
import org.graphstream.organic.measure.AverageOrganizationSizeMeasure;
import org.graphstream.stream.file.FileSourceDGS;

public class Measures {
	
	
	public static void main(String... args) throws Exception {
		System.setProperty(Validation.PROPERTY, "none");
		System.setProperty(OrganizationManagerFactory.PROPERTY,
				"plugins.replay.ReplayOrganizationManager");

		String what = "replayable.dgs_centroid.dgs";

		FileSourceDGS dgs = new FileSourceDGS();
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		OrganizationsGraph metaGraph = new OrganizationsGraph(g);
		AverageOrganizationSizeMeasure aosm = new AverageOrganizationSizeMeasure();
		AverageCentroidByOrganizationMeasure acbom = new AverageCentroidByOrganizationMeasure();
		AverageConnectivityMeasure acm = new AverageConnectivityMeasure();

		aosm.setSeparateMinMaxAxis(false);
		aosm.init(metaGraph.getManager());
		aosm.plot();
		
		acbom.setSeparateMinMaxAxis(false);
		acbom.init(metaGraph.getManager());
		acbom.plot();
		
		acm.init(metaGraph.getManager());
		acm.plot();
		
		dgs.addSink(g);
		dgs.begin(what);

		while (dgs.nextStep()) {
			Thread.sleep(50);
		}

		dgs.end();
	}
}
