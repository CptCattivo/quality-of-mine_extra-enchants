package de.qualityofmine.extraenchants.feature.elytraarmor;

/**
 * Die Raketenladung der Elytra-Rüstung, gezählt in Schüben. Raketen passen nur hinein, solange keine Ladung verfällt.
 */
public final class RocketCharge {
	/** Erster Erfahrungswert, gern anpassen. */
	public static final int BOOSTS_PER_ROCKET = 48;
	public static final int MAX_ROCKETS = 2;
	public static final int MAX_BOOSTS = BOOSTS_PER_ROCKET * MAX_ROCKETS;

	private RocketCharge() {
	}

	/** Wie viele der angebotenen Raketen hineinpassen, ohne dass Ladung verfällt. */
	public static int rocketsThatFit(final int charge, final int offeredRockets) {
		int free = (MAX_BOOSTS - charge) / BOOSTS_PER_ROCKET;
		return Math.clamp(offeredRockets, 0, Math.max(free, 0));
	}

	public static int load(final int charge, final int rockets) {
		return Math.min(charge + rockets * BOOSTS_PER_ROCKET, MAX_BOOSTS);
	}

	/** Breite des Ladebalkens in Pixeln, wie beim Haltbarkeitsbalken des Spiels. */
	public static int barWidth(final int charge, final int fullWidth) {
		return Math.round(fullWidth * Math.clamp(charge, 0, MAX_BOOSTS) / (float) MAX_BOOSTS);
	}
}
