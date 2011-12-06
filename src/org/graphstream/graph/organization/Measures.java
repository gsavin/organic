package org.graphstream.graph.organization;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import org.graphstream.stream.SinkAdapter;

public class Measures extends SinkAdapter {

	public static class StepInfos {
		protected double step;
		protected double[] infos;

		public StepInfos(double step, double... infos) {
			this.step = step;
			this.infos = infos;
		}
	}

	public static class OrganizationInfos {
		double birthstep;
		double currentStep;
		double[] currentInfos;
		LinkedList<StepInfos> infos;

		public OrganizationInfos(double birthstep) {
			this.birthstep = birthstep;
			this.currentInfos = new double[1];
			Arrays.fill(currentInfos, 0);
			this.infos = new LinkedList<StepInfos>();
		}

		public void commit(double step) {
			StepInfos si = new StepInfos(currentStep, Arrays.copyOf(
					currentInfos, currentInfos.length));
			infos.add(si);
			Arrays.fill(currentInfos, 0);
			this.currentStep = step;
		}

		public double getCumulative(double from, double to, int i) {
			double c = 0;
			int f = 0;
			int t = infos.size() - 1;

			while (infos.get(f).step < from && f < infos.size() - 1)
				f++;
			while (infos.get(t).step > to && t > f)
				t--;

			if (t < f)
				return 0;

			for (int k = f; k <= t; k++)
				c += infos.get(k).infos[i];

			if (currentStep <= to)
				c += currentInfos[i];

			return c;
		}
	}

	public static class NodeInfos {
		double birthstep;
		Object metaOrganizationIndex;
	}

	public static enum ChangeType {
		ADD(1), REMOVE(1);
		double score;

		ChangeType(double score) {
			this.score = score;
		}
	}

	public static enum ElementType {
		NODE, EDGE
	}

	protected DefaultOrganizationManager manager;
	protected PrintStream out;
	protected double step = 0;

	protected HashMap<String, NodeInfos> nodeInfos;
	protected HashMap<Object, OrganizationInfos> organizationInfos;

	public Measures(DefaultOrganizationManager manager, String path) {
		this.manager = manager;

		try {
			out = new PrintStream(path);
		} catch (Exception e) {
			e.printStackTrace();
		}

		nodeInfos = new HashMap<String, NodeInfos>();
		organizationInfos = new HashMap<Object, OrganizationInfos>();
	}

	public void step() {
		int count = manager.getOrganizationCount();

		double from = step - 25.0;
		double to = step;

		double min = Double.MAX_VALUE, max = Double.MIN_VALUE, ave = 0;

		if (organizationInfos.values().size() > 0) {

			for (OrganizationInfos infos : organizationInfos.values()) {
				double c = infos.getCumulative(from, to, 0);
				min = Math.min(min, c);
				max = Math.max(max, c);
				ave += c;
			}
			ave /= organizationInfos.values().size();

			min /= (to - from);
			max /= (to - from);
			ave /= (to - from);
		} else {
			max = min = ave = 0;
		}
		out.printf(Locale.ROOT, "%f\t%d\t%f\t%f\t%f%n", step, count, min, ave,
				max);
		out.flush();
	}

	protected void registerOrganizationChange(Object orgIndex, ChangeType type,
			ElementType elementType, String elementId) {
		OrganizationInfos orgInfos = organizationInfos.get(orgIndex);

		if (orgInfos == null) {
			orgInfos = new OrganizationInfos(step);
			organizationInfos.put(orgIndex, orgInfos);
		}

		orgInfos.currentInfos[0] += type.score;
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		nodeInfos.put(nodeId, new NodeInfos());
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		nodeInfos.remove(nodeId);
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attrId, Object val) {
		if (attrId.equals(manager.metaOrganizationIndexAttribute)) {
			registerOrganizationChange(val, ChangeType.ADD, ElementType.NODE,
					nodeId);
		}
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attrId, Object oldVal, Object newVal) {
		if (attrId.equals(manager.metaOrganizationIndexAttribute)) {
			registerOrganizationChange(newVal, ChangeType.ADD,
					ElementType.NODE, nodeId);
			registerOrganizationChange(oldVal, ChangeType.REMOVE,
					ElementType.NODE, nodeId);
		}
	}

	public void stepBegins(String sourceId, long timeId, double step) {
		this.step = step;
		for (OrganizationInfos infos : organizationInfos.values())
			infos.commit(this.step);
		step();
	}

	public static void main(String... args) {
		OrganizationInfos infos = new OrganizationInfos(0);
		for (double i = 0; i < 5; i++) {
			infos.currentInfos[0]++;
			infos.commit(i + 1);
			System.out.printf("%f%n", infos.getCumulative(0, i + 1, 0));
		}
	}
}
