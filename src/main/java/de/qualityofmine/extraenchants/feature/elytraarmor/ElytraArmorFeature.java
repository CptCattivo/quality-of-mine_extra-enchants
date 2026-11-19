package de.qualityofmine.extraenchants.feature.elytraarmor;

import de.qualityofmine.extraenchants.Feature;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Elytra-Rüstung: Diamantharnisch und Elytren in einem Teil, am Schmiedetisch zu Netherit aufwertbar. Mit geladenen
 * Feuerwerksraketen gibt die Sprungtaste im Flug einen Schub wie eine Rakete in der Hand.
 */
public final class ElytraArmorFeature implements Feature {
	@Override
	public void initialize() {
		ElytraArmors.load();
		PayloadTypeRegistry.serverboundPlay().register(RocketBoostPayload.TYPE, RocketBoostPayload.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(RocketBoostPayload.TYPE, (payload, context) -> boost(context.player()));
		ResourceKey<CreativeModeTab> combat = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("combat"));
		CreativeModeTabEvents.modifyOutputEvent(combat).register(output -> {
			output.accept(ElytraArmors.ELYTRA_ARMOR);
			output.accept(ElytraArmors.NETHERITE_ELYTRA_ARMOR);
		});
	}

	/** Der Server prüft Flug und Ladung, der Client meldet nur den Tastendruck. Im Kreativ-Modus bleibt die Ladung voll. */
	static void boost(final ServerPlayer player) {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		int charge = ElytraArmors.chargeOf(chest);
		if (!player.isFallFlying() || charge <= 0) {
			return;
		}
		if (!player.hasInfiniteMaterials()) {
			ElytraArmors.setCharge(chest, charge - 1);
		}
		// Eine schlichte Rakete ohne Feuerwerksstern: Flugdauer 1 und keine Explosion, die Schaden macht
		player.level().addFreshEntity(new FireworkRocketEntity(player.level(), new ItemStack(Items.FIREWORK_ROCKET), player));
	}
}
