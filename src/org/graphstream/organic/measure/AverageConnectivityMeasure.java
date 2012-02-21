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
package org.graphstream.organic.measure;

import org.graphstream.algorithm.measure.ChartConnectivityMeasure;
import org.graphstream.algorithm.measure.ConnectivityMeasure;
import org.graphstream.organic.Organization;
import org.graphstream.organic.OrganizationManager;

public class AverageConnectivityMeasure extends ChartConnectivityMeasure {
	public static class AverageVertexConnectivityMeasure extends
			ChartVertexConnectivityMeasure {
		OrganizationManager manager;

		public void compute() {
			double sum = 0;

			if (manager.getOrganizationCount() > 0) {
				for (Organization org : manager)
					sum += ConnectivityMeasure.getVertexConnectivity(org);

				sum /= manager.getOrganizationCount();
			}

			addValue(manager.getEntitiesGraph().getStep(), sum);
		}

		public void init(OrganizationManager manager) {
			init(manager.getEntitiesGraph());
			this.manager = manager;
		}
	}

	public static class AverageEdgeConnectivityMeasure extends
			ChartEdgeConnectivityMeasure {
		OrganizationManager manager;

		public void compute() {
			addValue(manager.getEntitiesGraph().getStep(),
					getAverageEdgeConnectivity());
		}

		public double getAverageEdgeConnectivity() {
			double sum = 0;

			if (manager.getOrganizationCount() > 0) {
				for (Organization org : manager)
					sum += ConnectivityMeasure.getEdgeConnectivity(org);

				sum /= manager.getOrganizationCount();
			}

			return sum;
		}

		public void init(OrganizationManager manager) {
			init(manager.getEntitiesGraph());
			this.manager = manager;
		}
	}

	public AverageConnectivityMeasure() {
		super(new AverageVertexConnectivityMeasure(),
				new AverageEdgeConnectivityMeasure());
	}

	public void init(OrganizationManager manager) {
		((AverageVertexConnectivityMeasure) vertexConnectivity).init(manager);
		((AverageEdgeConnectivityMeasure) edgeConnectivity).init(manager);
	}
}
