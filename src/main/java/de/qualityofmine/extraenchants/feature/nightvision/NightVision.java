package de.qualityofmine.extraenchants.feature.nightvision;

import de.qualityofmine.extraenchants.ModEnchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Verzauberung Nachtsicht für den Helm. Sie gibt denselben Effekt wie der Trank, nur ohne Partikel und Symbol.
 * Daran erkennt der Code auch, welcher Effekt von der Verzauberung stammt und welcher von einem Trank.
 */
public final class NightVision {
	/** Unter 200 Ticks Restzeit beginnt die Nachtsicht zu flackern. Der Effekt wird deshalb vorher aufgefrischt. */
	static final int DURATION = 400;
	static final int REFRESH_BELOW = 300;

	private NightVision() {
	}

	/**
	 * Auffrischen, wenn kein Effekt da ist oder der vorhandene bald endet. Ein länger laufender Trank bleibt unberührt.
	 * Endet ein Trank bald, übernimmt die Verzauberung nahtlos.
	 */
	static boolean shouldApply(final boolean active, final boolean hasEffect, final boolean infinite, final int duration) {
		return active && (!hasEffect || !infinite && duration < REFRESH_BELOW);
	}

	/** Entfernt wird nur der eigene Effekt, ein Trank läuft weiter. */
	static boolean shouldRemove(final boolean active, final boolean hasEffect, final boolean fromEnchantment) {
		return !active && hasEffect && fromEnchantment;
	}

	public static void tick(final Player player) {
		if (player.level().isClientSide()) {
			return;
		}
		boolean active = player.isAlive()
			&& NightVisionFeature.isEnabled(player)
			&& ModEnchantments.level(player, ModEnchantments.NIGHT_VISION) > 0;
		MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
		boolean hasEffect = current != null;
		if (shouldApply(active, hasEffect, hasEffect && current.isInfiniteDuration(), hasEffect ? current.getDuration() : 0)) {
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, true, false, false));
		} else if (shouldRemove(active, hasEffect, hasEffect && !current.isVisible() && !current.showIcon())) {
			player.removeEffect(MobEffects.NIGHT_VISION);
		}
	}
}
