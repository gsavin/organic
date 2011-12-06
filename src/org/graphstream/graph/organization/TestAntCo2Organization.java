package org.graphstream.graph.organization;

import java.io.IOException;
import java.util.HashMap;

import org.graphstream.algorithm.antco2.AntCo2Algorithm;
import org.graphstream.algorithm.myrmex.AntParams;
import org.graphstream.algorithm.myrmex.centroid.AntCentroidAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.swingViewer.Viewer;
import static org.graphstream.algorithm.Parameter.parameter;

public class TestAntCo2Organization implements OrganizationListener {

	public static void main(String... args) {
		System.setProperty("gs.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		FileSourceDGS dgs = new FileSourceDGS();
		// Generator gen = new GridGenerator();
		Graph g = new DefaultGraph("test",false,true);
		TestAntCo2Organization test = new TestAntCo2Organization(g);

		g.display(false);
		String stylesheet = "graph { " + "  fill-color: #333333;"
				+ "  padding: 50px;" + "}"
				+ "node { fill-color: blue,yellow,red; fill-mode: dyn-plain;"
				+ "text-alignment: center; text-size: 15px; text-style: bold;"
				+ " size-mode: dyn-size;" + "}" + "edge {"
				+ "  fill-color: white; size: 2px;" + "}";

		g.addAttribute("ui.stylesheet", stylesheet);

		// orgManager.addOrganizationListener(test);

		g.addAttribute("antco2.resources", "+ A");
		g.addAttribute("antco2.resources", "+ B");
		// g.addAttribute("antco2.resources", "+ C");
		// g.addAttribute("antco2.resources", "+ D");

		dgs.addSink(g);
		
		try {
			dgs.begin("/home/raziel/workspace/build/BoidsMovie.dgs");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// gen.addSink(g);
		// gen.begin();

		// for (int i = 0; i < 20; i++)
		// gen.nextEvents();

		// FakeStepSource fss = new FakeStepSource(100,TimeUnit.MILLISECONDS);
		// ThreadProxyPipe proxy = new ThreadProxyPipe(fss);
		// proxy.addSink(g);
		// fss.start();
		boolean ok = true;
		long timeId = 0;
		double step = 0;
		while (ok) {
			// proxy.pump();
				try {
					ok = dgs.nextStep();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			
			for(int i=0;i<10;i++)
				test.compute();
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
			
		}
	}

	AntCo2Algorithm antco2;
	DefaultOrganizationManager orgManager;

	HashMap<Object, AntCentroidAlgorithm> centroids;
	HashMap<Object, Viewer> viewers;

	Measures measures;

	public TestAntCo2Organization(Graph g) {
		centroids = new HashMap<Object, AntCentroidAlgorithm>();
		viewers = new HashMap<Object, Viewer>();
		antco2 = new AntCo2Algorithm();
		orgManager = new DefaultOrganizationManager(g, antco2.getMetaIndexAttribute());
		orgManager.addOrganizationListener(this);
		antco2.init(g);
		measures = new Measures(orgManager, "organization-measures.dat");
		g.addSink(measures);
	}

	public void compute() {
		antco2.compute();
		for (AntCentroidAlgorithm aca : centroids.values())
			aca.compute();
	}

	public void organizationCreated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		Organization org = orgManager.getOrganization(metaOrganizationIndex);
		AntCentroidAlgorithm aca = new AntCentroidAlgorithm();
		aca.init(parameter("graph", org),
				parameter("ant.params.dropOn", AntParams.DropOn.NODES),
				parameter("ant.params.evaporation", 0.8f));
		centroids.put(metaOrganizationIndex, aca);
		/*
		 * Viewer v = org.display(false);
		 * if(viewers.containsKey(metaOrganizationIndex))
		 * viewers.get(metaOrganizationIndex).close();
		 * viewers.put(metaOrganizationIndex, v);
		 */
	}

	public void organizationRootNodeUpdated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
	}

	public void organizationMerged(Object metaIndex,
			Object metaOrganizationIndex1, Object metaOrganizationIndex2,
			String rootNodeId) {
		organizationRemoved(metaIndex, metaOrganizationIndex2);
	}

	public void organizationSplited(Object metaIndex,
			Object metaOrganizationBase, Object metaOrganizationChild) {
		organizationCreated(metaIndex, metaOrganizationChild, null);
	}

	public void organizationRemoved(Object metaIndex,
			Object metaOrganizationIndex) {
		AntCentroidAlgorithm aca = centroids.remove(metaOrganizationIndex);
		if (aca != null)
			aca.terminate();
		if (viewers.containsKey(metaOrganizationIndex))
			viewers.remove(metaOrganizationIndex).close();
	}

	public void organizationChanged(Object metaIndex,
			Object metaOrganizationIndex, ChangeType changeType,
			ElementType elementType, String elementId) {
		// TODO Auto-generated method stub
		
	}
}
