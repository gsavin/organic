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
package org.graphstream.organic;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class OrganicProperties extends Properties {
	private static final long serialVersionUID = -1598036766326339078L;

	public static final String PREFIX = "org.graphstream.organic";
	public static final String PROPERTY = PREFIX + ".config";

	public OrganicProperties() {
		super();

		for (String key : System.getProperties().stringPropertyNames()) {
			if (key.startsWith(PREFIX))
				setProperty(key, System.getProperty(key));
		}
		
		if (getProperty(PROPERTY) != null) {
			try {
				loadFromXML(getClass().getClassLoader().getResourceAsStream(getProperty(PROPERTY)));
			} catch (InvalidPropertiesFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}