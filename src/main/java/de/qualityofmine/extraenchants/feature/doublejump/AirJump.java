package de.qualityofmine.extraenchants.feature.doublejump;

/** Merkt sich, ob der zusätzliche Sprung in der Luft seit der letzten Landung schon verbraucht ist. */
public final class AirJump {
	private boolean used;

	/** Am Boden, im Wasser oder an einer Leiter gibt es den Sprung zurück. */
	public void land() {
		used = false;
	}

	/** Verbraucht den Sprung, falls er erlaubt und noch frei ist. Liefert true, wenn gesprungen werden darf. */
	public boolean tryUse(final boolean allowed) {
		if (!allowed || used) {
			return false;
		}
		used = true;
		return true;
	}
}
