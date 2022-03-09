package cmsc433.mp3.util;

public class SleepStep {
	private final long durationMs;
	
	public SleepStep (long durationMs) {
		this.durationMs = durationMs;
	}
	
	public long getDurationMs () {
		return durationMs;
	}
}
