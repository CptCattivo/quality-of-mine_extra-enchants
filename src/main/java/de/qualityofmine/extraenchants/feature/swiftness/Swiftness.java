package de.qualityofmine.extraenchants.feature.swiftness;

import de.qualityofmine.extraenchants.ModEnchantments;
import de.qualityofmine.extraenchants.ExtraEnchants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Die Verzauberung selbst steht als Datei unter {@code data/qualityofmine/enchantment}. Die Spieldaten
 * kennen aber keine Bedingung „nur beim Sprinten“, die sofort greift. Deshalb setzt dieser Code den
 * Tempo-Bonus in jedem Tick passend zum aktuellen Zustand, auf Client und Server.
 */
public final class Swiftness {
	private static final Identifier MODIFIER_ID = ExtraEnchants.id("swiftness");
	private static final double BONUS_PER_LEVEL = 0.1;

	private Swiftness() {
	}

	/** Bonus auf das Sprint-Tempo: 0,1 für Stufe I, 0,2 für Stufe II, 0,3 für Stufe III. */
	static double sprintBonus(final int level, final boolean sprinting) {
		return sprinting ? level * BONUS_PER_LEVEL : 0.0;
	}

	public static void update(final Player player) {
		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		double bonus = sprintBonus(ModEnchantments.level(player, ModEnchantments.SWIFTNESS), player.isSprinting());
		AttributeModifier current = speed.getModifier(MODIFIER_ID);
		double currentBonus = current == null ? 0.0 : current.amount();
		if (bonus == currentBonus) {
			return;
		}
		speed.removeModifier(MODIFIER_ID);
		if (bonus > 0.0) {
			// Wirkt wie der Sprint-Modifier auf das Gesamttempo, also genau x Prozent schneller als normales Sprinten
			speed.addTransientModifier(new AttributeModifier(MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}
}
