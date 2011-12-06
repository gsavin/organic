package org.graphstream.graph.organization;

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
