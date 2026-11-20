package de.qualityofmine.extraenchants.feature.elytraarmor;

/**
 * Der Raketenstart der Elytra-Rüstung. Gleiten geht erst in der Luft, deshalb springt der Spieler am Boden zuerst ab und
 * startet im Tick danach Gleitflug und Schub. Kommt er nicht vom Boden weg, etwa unter einer Decke, verfällt der Start.
 */
public final class RocketLaunch {
	static final int MAX_WAIT_TICKS = 5;
	private static final int IDLE = -1;

	private int waitedTicks = IDLE;

	public void request() {
		waitedTicks = 0;
	}

	/** Einmal pro Tick. Liefert true, wenn jetzt abgehoben wird. */
	public boolean tick(final boolean inAir) {
		if (waitedTicks == IDLE) {
			return false;
		}
		if (inAir) {
			waitedTicks = IDLE;
			return true;
		}
		waitedTicks++;
		if (waitedTicks > MAX_WAIT_TICKS) {
			waitedTicks = IDLE;
		}
		return false;
	}
}
