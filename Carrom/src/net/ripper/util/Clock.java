package net.ripper.util;

public class Clock {
	private long ticks;
	private boolean paused;
	private long pausedClockSnapshot;

	public Clock() {
		ticks = System.currentTimeMillis();
	}

	public long getTimeDelta() {
		long ret = !paused ? System.currentTimeMillis() - ticks
				: pausedClockSnapshot - ticks;
		ticks = System.currentTimeMillis();
		return ret;
	}

	public void reset() {
		ticks = System.currentTimeMillis();
	}

	public void pause() {
		paused = true;
		pausedClockSnapshot = System.currentTimeMillis();
	}
}
