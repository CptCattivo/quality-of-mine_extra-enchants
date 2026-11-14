package de.qualityofmine.extraenchants.feature.healing;

import de.qualityofmine.extraenchants.ModEnchantments;
import net.minecraft.world.entity.player.Player;

/**
 * Verzauberung Heilung für den Harnisch: Sie gibt in festen Abständen ein halbes Herz zurück,
 * unabhängig vom Hunger. Läuft nur auf dem Server, der Client bekommt die Lebenspunkte vom Spiel.
 */
public final class Healing {
	/** Ein halbes Herz. */
	static final float HEAL_AMOUNT = 1.0F;
	private static final int TICKS_PER_SECOND = 20;

	private Healing() {
	}

	/** Abstand in Ticks: Stufe I alle 60 s, Stufe II alle 40 s, Stufe III alle 20 s. 0 bedeutet keine Heilung. */
	static int intervalTicks(final int level) {
		return switch (level) {
			case 0 -> 0;
			case 1 -> 60 * TICKS_PER_SECOND;
			case 2 -> 40 * TICKS_PER_SECOND;
			default -> 20 * TICKS_PER_SECOND;
		};
	}

	static boolean healsAt(final int level, final int tickCount) {
		int interval = intervalTicks(level);
		return interval > 0 && tickCount > 0 && tickCount % interval == 0;
	}

	public static void tick(final Player player) {
		if (player.level().isClientSide() || !player.isAlive()) {
			return;
		}
		if (healsAt(ModEnchantments.level(player, ModEnchantments.HEALING), player.tickCount)) {
			player.heal(HEAL_AMOUNT);
		}
	}
}
