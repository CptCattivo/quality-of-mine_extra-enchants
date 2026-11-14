package de.qualityofmine.extraenchants.feature.nightvision;

import com.mojang.serialization.Codec;
import de.qualityofmine.extraenchants.Feature;
import de.qualityofmine.extraenchants.ModEnchantments;
import de.qualityofmine.extraenchants.ExtraEnchants;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class NightVisionFeature implements Feature {
	/** Ob der Spieler die Nachtsicht eingeschaltet hat. Gehört zum Spielstand und übersteht den Tod. */
	private static final AttachmentType<Boolean> ENABLED = AttachmentRegistry.<Boolean>builder()
		.initializer(() -> true)
		.persistent(Codec.BOOL)
		.copyOnDeath()
		.buildAndRegister(ExtraEnchants.id("night_vision_enabled"));

	public static boolean isEnabled(final Player player) {
		return player.getAttachedOrElse(ENABLED, true);
	}

	@Override
	public void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(ToggleNightVisionPayload.TYPE, ToggleNightVisionPayload.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ToggleNightVisionPayload.TYPE, (payload, context) -> toggle(context.player()));
	}

	private static void toggle(final ServerPlayer player) {
		if (ModEnchantments.level(player, ModEnchantments.NIGHT_VISION) == 0) {
			player.sendOverlayMessage(Component.translatable("message.qualityofmine.night_vision.missing"));
			return;
		}
		boolean enabled = !isEnabled(player);
		player.setAttached(ENABLED, enabled);
		player.sendOverlayMessage(Component.translatable(enabled ? "message.qualityofmine.night_vision.on" : "message.qualityofmine.night_vision.off"));
	}
}
