package de.qualityofmine.extraenchants;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Die Verzauberungen des Mods. Ihre Werte stehen als Dateien unter {@code data/qualityofmine/enchantment},
 * die Wirkung steckt jeweils in der Klasse des Features.
 */
public final class ModEnchantments {
	public static final ResourceKey<Enchantment> SWIFTNESS = key("swiftness");
	public static final ResourceKey<Enchantment> HEALING = key("healing");
	public static final ResourceKey<Enchantment> DOUBLE_JUMP = key("double_jump");
	public static final ResourceKey<Enchantment> NIGHT_VISION = key("night_vision");
	public static final ResourceKey<Enchantment> SATURATION = key("saturation");

	private ModEnchantments() {
	}

	/** Höchste Stufe der Verzauberung an der getragenen Ausrüstung, 0 ohne Verzauberung. */
	public static int level(final LivingEntity entity, final ResourceKey<Enchantment> enchantment) {
		return entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment)
			.map(holder -> EnchantmentHelper.getEnchantmentLevel(holder, entity))
			.orElse(0);
	}

	private static ResourceKey<Enchantment> key(final String path) {
		return ResourceKey.create(Registries.ENCHANTMENT, ExtraEnchants.id(path));
	}
}
