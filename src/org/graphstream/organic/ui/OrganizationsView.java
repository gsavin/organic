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
package org.graphstream.organic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.graphstream.organic.Organization;
import org.graphstream.organic.OrganizationListener;
import org.graphstream.organic.OrganizationsGraph;
import org.graphstream.ui.layout.springbox.SpringBox;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;
import org.graphstream.ui.swingViewer.Viewer.ThreadingModel;

public class OrganizationsView extends JPanel implements OrganizationListener,
		ViewerListener {
	/*
	 * @see java.io.Serializable
	 */
	private static final long serialVersionUID = 2470594044826918271L;

	OrganizationsGraph metaGraph;

	ViewerPipe metaPipe;

	Viewer entitiesViewer;
	Viewer metaViewer;

	Object organizationViewedId;
	Viewer organizationViewer;
	JPanel organizationViewerPanel;

	JFrame frame;

	public OrganizationsView(OrganizationsGraph graph) {
		this.metaGraph = graph;

		entitiesViewer = new Viewer(graph.getEntitiesGraph(),
				ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		metaViewer = new Viewer(graph, ThreadingModel.GRAPH_IN_ANOTHER_THREAD);

		entitiesViewer.addDefaultView(false);
		metaViewer.addDefaultView(false);

		organizationViewerPanel = new JPanel();

		entitiesViewer.getDefaultView().setPreferredSize(
				new Dimension(400, 400));
		metaViewer.getDefaultView().setPreferredSize(new Dimension(200, 200));

		metaPipe = metaViewer.newViewerPipe();
		metaPipe.addViewerListener(this);

		JPanel dv1, dv2;
		dv1 = new JPanel();
		dv1.add(entitiesViewer.getDefaultView());
		dv1.setBackground(Color.WHITE);
		dv2 = new JPanel();
		dv2.add(metaViewer.getDefaultView());
		dv2.setBackground(Color.WHITE);

		dv1.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		dv2.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		organizationViewerPanel.setBorder(BorderFactory
				.createLineBorder(Color.GRAY));

		setLayout(new BorderLayout());
		add(dv1, BorderLayout.CENTER);

		JPanel east = new JPanel();
		east.setLayout(new GridLayout(2, 1));
		east.add(dv2);
		east.add(organizationViewerPanel);

		add(east, BorderLayout.EAST);
	}

	public JFrame createFrame() {
		if (frame == null) {
			frame = new JFrame("Organizations");
			frame.add(this);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		frame.pack();
		frame.setVisible(true);

		return frame;
	}

	public void enableHQ() {
		metaGraph.addAttribute("ui.antialias");
		metaGraph.addAttribute("ui.quality");
		metaGraph.getEntitiesGraph().addAttribute("ui.antialias");
		metaGraph.getEntitiesGraph().addAttribute("ui.quality");
	}

	public Viewer getMetaViewer() {
		return metaViewer;
	}

	public Viewer getEntitiesViewer() {
		return entitiesViewer;
	}

	public void enableEntitiesLayout() {
		SpringBox box = new SpringBox();
		entitiesViewer.enableAutoLayout(box);
		box.addAttributeSink(metaGraph.getEntitiesGraph());
	}
	
	public void pumpEvents() {
		metaPipe.pump();
	}

	public void pumpLoop(AtomicBoolean alive, long pause) {
		while (alive.get()) {
			pumpEvents();

			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				alive.set(false);
			}
		}
	}

	public void connectionCreated(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		// TODO Auto-generated method stub

	}

	public void connectionRemoved(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
		// TODO Auto-generated method stub

	}

	public void organizationChanged(Object metaIndex,
			Object metaOrganizationIndex, ChangeType changeType,
			ElementType elementType, String elementId) {
		// TODO Auto-generated method stub

	}

	public void organizationCreated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		// TODO Auto-generated method stub

	}

	public void organizationMerged(Object metaIndex,
			Object metaOrganizationIndex1, Object metaOrganizationIndex2,
			String rootNodeId) {
		// TODO Auto-generated method stub

	}

	public void organizationRemoved(Object metaIndex,
			Object metaOrganizationIndex) {
		// TODO Auto-generated method stub

	}

	public void organizationRootNodeUpdated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		// TODO Auto-generated method stub

	}

	public void organizationSplited(Object metaIndex,
			Object metaOrganizationBase, Object metaOrganizationChild) {
		// TODO Auto-generated method stub

	}

	public void buttonPushed(String id) {
		System.out.printf("> pushed : '%s'\n", id);

		if (!id.equals(organizationViewedId)) {
			Organization org = metaGraph.getManager().getOrganization(id);

			if (org == null)
				throw new NullPointerException(id);

			if (organizationViewer != null) {
				organizationViewerPanel.remove(organizationViewer.getDefaultView());
				organizationViewer.close();
			}

			organizationViewer = new Viewer(org,
					ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
			organizationViewer.addDefaultView(false);
			organizationViewer.getDefaultView().setPreferredSize(
					new Dimension(200, 200));
			// organizationViewer.enableAutoLayout();
			organizationViewerPanel.add(organizationViewer.getDefaultView());

			if (frame != null) {
				frame.pack();
				organizationViewerPanel.repaint();
			}
		}
	}

	public void buttonReleased(String id) {
		// TODO Auto-generated method stub

	}

	public void viewClosed(String viewName) {
		// TODO Auto-generated method stub

	}

}
