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
package org.graphstream.organic.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.graphstream.graph.Graph;
import org.graphstream.organic.OrganizationListener;
import org.graphstream.organic.OrganizationManager;
import org.graphstream.organic.Plugin;

public class Colorize implements Plugin, OrganizationListener {

	int r, g, b;
	int p;
	Random rand;

	OrganizationManager manager;
	Graph entitiesGraph;

	HashMap<Object, String> colors;

	String[] palette;

	boolean usePalette;

	protected long seed;

	protected int palettePosition;

	protected boolean paletteRandom;

	public Colorize() {
		usePalette = false;
		paletteRandom = false;
		rand = new Random();
		setSeed(rand.nextLong());

		colors = new HashMap<Object, String>();
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long s) {
		rand = new Random(s);
		seed = s;

		r = rand.nextInt(256);
		g = rand.nextInt(256);
		b = rand.nextInt(256);

		p = rand.nextInt(3);
	}

	public void setPalette(String... colors) {
		palette = colors;
		usePalette = true;
		palettePosition = 0;
	}

	public void loadPatternFromStream(InputStream stream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		LinkedList<String> colors = new LinkedList<String>();

		while (in.ready())
			colors.add(in.readLine());

		palette = colors.toArray(new String[colors.size()]);
		usePalette = true;
		palettePosition = 0;

		colors.clear();
	}

	public String getNewColor() {
		if (usePalette) {
			if (paletteRandom)
				return palette[rand.nextInt(palette.length)];

			if (palettePosition >= palette.length)
				palettePosition = 0;

			return palette[palettePosition++];
		}

		String c = String.format("#%02X%02X%02X", r, g, b);

		int delta = 70 + rand.nextInt(10);

		switch (p) {
		case 0:
			r = (r + delta) % 256;
			break;
		case 1:
			g = (g + delta) % 256;
			break;
		case 2:
			b = (b + delta) % 256;
			break;
		}

		p = rand.nextInt(3);

		return c;
	}

	public void init(OrganizationManager manager) {
		manager.addOrganizationListener(this);
		entitiesGraph = manager.getEntitiesGraph();
		this.manager = manager;
	}

	public void organizationChanged(Object metaIndex,
			Object metaOrganizationIndex, ChangeType changeType,
			ElementType elementType, String elementId) {
		String color = colors.get(metaOrganizationIndex);

		switch (changeType) {
		case ADD:
			switch (elementType) {
			case NODE:
				entitiesGraph.getNode(elementId).addAttribute("ui.style",
						String.format("fill-color: %s;", color));
				break;
			case EDGE:
				entitiesGraph.getEdge(elementId).addAttribute("ui.style",
						String.format("fill-color: %s;", color));
				break;
			}

			break;
		}
	}

	public void organizationCreated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
		String color = getNewColor();

		colors.put(metaOrganizationIndex, color);
		manager.getOrganization(metaOrganizationIndex).addAttribute("ui.style",
				String.format("fill-color: %s;", color));
	}

	public void organizationRemoved(Object metaIndex,
			Object metaOrganizationIndex) {
		colors.remove(metaOrganizationIndex);
	}

	public void connectionCreated(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
	}

	public void connectionRemoved(Object metaIndex1,
			Object metaOrganizationIndex1, Object metaIndex2,
			Object metaOrganizationIndex2, String connection) {
	}

	public void organizationMerged(Object metaIndex,
			Object metaOrganizationIndex1, Object metaOrganizationIndex2,
			String rootNodeId) {
	}

	public void organizationRootNodeUpdated(Object metaIndex,
			Object metaOrganizationIndex, String rootNodeId) {
	}

	public void organizationSplited(Object metaIndex,
			Object metaOrganizationBase, Object metaOrganizationChild) {
	}

}
