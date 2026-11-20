package de.qualityofmine.extraenchants.feature.elytraarmor;

import de.qualityofmine.extraenchants.ExtraEnchants;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

/** Die Registrierung der Elytra-Rüstungen samt der Komponente für ihre Raketenladung. */
public final class ElytraArmors {
	/** Haltbarkeit der Elytren im Originalspiel. */
	public static final int ELYTRA_DURABILITY = 432;

	/** Verbleibende Schübe. Ohne Ladung fehlt die Komponente, dann verschwinden auch die Raketen auf dem Rücken. */
	public static final DataComponentType<Integer> ROCKET_CHARGE = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
		ExtraEnchants.id("rocket_charge"),
		DataComponentType.<Integer>builder().persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

	/** Kennzeichnet die Rakete hinter einem Schub, siehe {@link ArmorRocket}. */
	public static final DataComponentType<Unit> ARMOR_ROCKET = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
		ExtraEnchants.id("armor_rocket"),
		DataComponentType.<Unit>builder().persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).build());

	public static final Item ELYTRA_ARMOR = register("elytra_armor", ArmorMaterials.DIAMOND, false);
	public static final Item NETHERITE_ELYTRA_ARMOR = register("netherite_elytra_armor", ArmorMaterials.NETHERITE, true);

	private ElytraArmors() {
	}

	public static int chargeOf(final ItemStack stack) {
		return stack.getOrDefault(ROCKET_CHARGE, 0);
	}

	public static void setCharge(final ItemStack stack, final int charge) {
		if (charge > 0) {
			stack.set(ROCKET_CHARGE, charge);
		} else {
			stack.remove(ROCKET_CHARGE);
		}
	}

	/**
	 * Schützt wie der Harnisch aus dem Material und gleitet wie Elytren. Die Haltbarkeit ist die Summe beider Teile, weil
	 * Treffer und Fliegen am selben Balken zehren. Das Aussehen kommt aus {@code assets/qualityofmine/equipment/<name>.json}.
	 */
	private static Item register(final String path, final ArmorMaterial material, final boolean fireResistant) {
		Identifier id = ExtraEnchants.id(path);
		ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, id);
		Item.Properties properties = new Item.Properties()
			.setId(ResourceKey.create(Registries.ITEM, id))
			.humanoidArmor(material, ArmorType.CHESTPLATE)
			.durability(ArmorType.CHESTPLATE.getDurability(material.durability()) + ELYTRA_DURABILITY)
			.component(DataComponents.GLIDER, Unit.INSTANCE)
			.component(DataComponents.EQUIPPABLE,
				Equippable.builder(EquipmentSlot.CHEST).setEquipSound(material.equipSound()).setAsset(asset).build())
			.repairable(TagKey.create(Registries.ITEM, ExtraEnchants.id("repairs_" + path)))
			.rarity(Rarity.EPIC);
		if (fireResistant) {
			properties.fireResistant();
		}
		return Registry.register(BuiltInRegistries.ITEM, id, new ElytraArmorItem(properties));
	}

	/** Lädt die Klasse und damit die Registrierung. */
	static void load() {
	}
}
