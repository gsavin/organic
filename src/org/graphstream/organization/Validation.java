/*
 * Copyright 2011 - 2012
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of Organizations, a feature for GraphStream to manipulate
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
package org.graphstream.organization;

import org.graphstream.organization.Validator.Level;

public class Validation {
	public static final String PROPERTY = "org.graphstream.organization.validation";

	public static Level getValidationLevel() {
		Level l;

		String p = System.getProperty(PROPERTY, "HARD");
		p = p.toUpperCase();

		try {
			l = Level.valueOf(p);
		} catch (IllegalArgumentException e) {
			System.err.printf("Invalid validation level '%s'\n", p);
			System.err.printf("Available level are :\n");

			for (Level al : Level.values())
				System.err.printf("- %s\n", al.name());

			System.err.printf("Switch to NONE\n");
			l = Level.NONE;
		}

		return l;
	}

	public static Validator getValidator(OrganizationManager manager) {
		Level l = getValidationLevel();

		switch (l) {
		case HARD:
			return new HardLevelValidator(manager);
		case NONE:
			return new PassiveValidator();
		}

		return null;
	}

	static class PassiveValidator implements Validator {
		public void validate(String context, Object... args) {
			// Nothing because it is passive.
		}
	}
	
	static class HardLevelValidator implements Validator {
		OrganizationManager manager;

		HardLevelValidator(OrganizationManager manager) {
			this.manager = manager;
		}

		public void validate(String context, Object... args) {
			for (Organization org : manager) {
				try {
					org.hardTest();
				} catch (HardTestException e) {
					throw new ValidationException(e, context, args);
				}
			}
		}
	}
}
