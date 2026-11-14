package de.qualityofmine.extraenchants.feature.saturation;

import de.qualityofmine.extraenchants.ModEnchantments;
import net.minecraft.world.entity.player.Player;

/**
 * Verzauberung Sättigung für den Helm. Das Spiel sammelt Erschöpfung durch Laufen, Springen, Kämpfen und Heilen.
 * Je volle Schwelle kostet das einen Sättigungspunkt, ist keiner mehr da, einen Hungerpunkt. Die Verzauberung
 * hebt diese Schwelle an, damit sinkt der Verbrauch.
 */
public final class Saturation {
	private Saturation() {
	}

	/** Anteil, um den der Verbrauch sinkt: Stufe I 10 %, Stufe II 25 %, Stufe III 50 %. */
	static float reduction(final int level) {
		return switch (level) {
			case 0 -> 0.0F;
			case 1 -> 0.10F;
			case 2 -> 0.25F;
			default -> 0.50F;
		};
	}

	/** Schwelle für einen Punkt: Bei halbem Verbrauch braucht es doppelt so viel Erschöpfung. */
	static float exhaustionPerPoint(final int level, final float vanillaExhaustionPerPoint) {
		return vanillaExhaustionPerPoint / (1.0F - reduction(level));
	}

	public static float exhaustionPerPoint(final Player player, final float vanillaExhaustionPerPoint) {
		return exhaustionPerPoint(ModEnchantments.level(player, ModEnchantments.SATURATION), vanillaExhaustionPerPoint);
	}
}
