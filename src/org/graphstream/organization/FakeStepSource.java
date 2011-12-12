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
package org.graphstream.organization;

import java.util.concurrent.TimeUnit;

import org.graphstream.stream.SourceBase;

public class FakeStepSource extends SourceBase implements Runnable {
	protected long delay;
	protected TimeUnit unit;
	protected boolean active;
	protected double step;
	
	public FakeStepSource(long delay, TimeUnit unit) {
		this.delay = delay;
		this.unit  = unit;
	}
	
	public void start() {
		if(!active) {
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}
	}
	
	public void run() {
		active = true;
		while(active) {
			this.sendStepBegins(sourceId, step++);
			try {
				Thread.sleep(TimeUnit.MILLISECONDS.convert(delay, unit));
			} catch(Exception e ) { e.printStackTrace(); }
		}
	}
}
