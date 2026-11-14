package de.qualityofmine.extraenchants.feature.nightvision;

import com.mojang.blaze3d.platform.InputConstants;
import de.qualityofmine.extraenchants.Feature;
import de.qualityofmine.extraenchants.ExtraEnchantsClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class NightVisionClientFeature implements Feature {
	private static KeyMapping toggleKey;

	@Override
	public void initialize() {
		toggleKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.qualityofmine.toggle_night_vision", InputConstants.KEY_N, ExtraEnchantsClient.KEY_CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(NightVisionClientFeature::sendToggle);
	}

	/** Der Server prüft den Helm und schaltet um, der Client meldet nur den Tastendruck. */
	private static void sendToggle(final Minecraft minecraft) {
		while (toggleKey.consumeClick()) {
			if (minecraft.player != null) {
				ClientPlayNetworking.send(new ToggleNightVisionPayload());
			}
		}
	}
}
