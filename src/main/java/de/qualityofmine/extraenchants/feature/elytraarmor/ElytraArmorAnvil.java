package de.qualityofmine.extraenchants.feature.elytraarmor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jspecify.annotations.Nullable;

/**
 * Diamantharnisch links und Elytren rechts ergeben im Amboss die Elytra-Rüstung. Sie übernimmt alles vom Harnisch,
 * etwa Verzauberungen, Namen und Rüstungsbesatz, dazu die Verzauberungen der Elytren und den Verschleiß beider Teile.
 */
public final class ElytraArmorAnvil {
	/** Erfahrungsstufen für das Zusammenfügen, dazu kommen wie im Spiel die Kosten früherer Ambossarbeiten. */
	static final int BASE_COST = 5;
	/** Ab dieser Stufe ist es auch im Spiel „Zu teuer!“, außer im Kreativ-Modus. */
	static final int TOO_EXPENSIVE = 40;

	/** Das Ergebnis im Amboss. Ein leeres Ergebnis mit Kosten zeigt das Spiel als „Zu teuer!“ an. */
	public record Result(ItemStack stack, int cost) {
	}

	private ElytraArmorAnvil() {
	}

	/**
	 * Leer, wenn die beiden Items nicht zusammengehören. Dann bleibt es beim normalen Amboss.
	 *
	 * @param itemName der Name im Eingabefeld des Ambosses, wie ihn das Spiel übergibt
	 */
	public static Optional<Result> combine(final ItemStack input, final ItemStack addition, final @Nullable String itemName,
		final boolean infiniteMaterials) {
		if (!input.is(Items.DIAMOND_CHESTPLATE) || !addition.is(Items.ELYTRA)) {
			return Optional.empty();
		}
		ItemStack result = input.transmuteCopy(ElytraArmors.ELYTRA_ARMOR);
		result.setDamageValue(combinedDamage(input.getDamageValue(), addition.getDamageValue()));
		EnchantmentHelper.setEnchantments(result, mergeEnchantments(input, addition));

		int chestRepairCost = input.getOrDefault(DataComponents.REPAIR_COST, 0);
		int elytraRepairCost = addition.getOrDefault(DataComponents.REPAIR_COST, 0);
		result.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(Math.max(chestRepairCost, elytraRepairCost)));
		int cost = cost(chestRepairCost, elytraRepairCost) + applyName(input, result, itemName);
		boolean affordable = cost < TOO_EXPENSIVE || infiniteMaterials;
		return Optional.of(new Result(affordable ? result : ItemStack.EMPTY, cost));
	}

	/** Die Haltbarkeit der Rüstung ist die Summe beider Teile, also auch ihr Verschleiß. */
	static int combinedDamage(final int chestDamage, final int elytraDamage) {
		return chestDamage + elytraDamage;
	}

	static int cost(final int chestRepairCost, final int elytraRepairCost) {
		return BASE_COST + chestRepairCost + elytraRepairCost;
	}

	/** Von jeder Verzauberung gilt die höhere Stufe. Elytren tragen ohnehin nur solche, die zum Harnisch passen. */
	private static ItemEnchantments mergeEnchantments(final ItemStack chestplate, final ItemStack elytra) {
		ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(chestplate));
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(elytra).entrySet()) {
			Holder<Enchantment> enchantment = entry.getKey();
			boolean compatible = merged.keySet().stream()
				.allMatch(other -> other.equals(enchantment) || Enchantment.areCompatible(enchantment, other));
			if (compatible) {
				merged.upgrade(enchantment, entry.getIntValue());
			}
		}
		return merged.toImmutable();
	}

	/** Umbenennen wie im Amboss des Spiels. Gibt die zusätzlichen Kosten zurück. */
	private static int applyName(final ItemStack input, final ItemStack result, final @Nullable String itemName) {
		if (itemName != null && !StringUtil.isBlank(itemName)) {
			if (itemName.equals(input.getHoverName().getString())) {
				return 0;
			}
			result.set(DataComponents.CUSTOM_NAME, Component.literal(itemName));
			return 1;
		}
		if (input.has(DataComponents.CUSTOM_NAME)) {
			result.remove(DataComponents.CUSTOM_NAME);
			return 1;
		}
		return 0;
	}
}
