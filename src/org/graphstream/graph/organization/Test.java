package org.graphstream.graph.organization;

import java.io.IOException;

import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.file.FileSourceDGS;

public class Test implements OrganizationListener {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		FileSourceDGS dgs = new FileSourceDGS();
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		OrganizationsGraph metaGraph = new OrganizationsGraph(g);
		
		metaGraph.getManager().setMetaIndexAttribute("meta.index");
		metaGraph.getManager().addOrganizationListener(new Test());
		
		dgs.addSink(g);
		dgs.readAll(Test.class.getResourceAsStream("test.dgs"));
		
		g.display();
		metaGraph.display();
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
		System.out.printf("* organizations [%s|%s] and [%s|%s] merged, root @ %s\n",
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
