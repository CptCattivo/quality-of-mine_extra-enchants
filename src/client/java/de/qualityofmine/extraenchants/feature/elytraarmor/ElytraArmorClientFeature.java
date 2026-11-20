package de.qualityofmine.extraenchants.feature.elytraarmor;

import com.mojang.blaze3d.platform.InputConstants;
import de.qualityofmine.extraenchants.ExtraEnchantsClient;
import de.qualityofmine.extraenchants.Feature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ExtractItemDecorationsCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ElytraArmorClientFeature implements Feature {
	/** Dieselbe Breite wie der Haltbarkeitsbalken des Spiels, der zwei Pixel tiefer liegt. */
	private static final int BAR_WIDTH = 13;
	private static final int BAR_LEFT = 2;
	private static final int BAR_TOP = 11;
	private static final int BAR_BACKGROUND = 0xFF000000;
	/** Orange wie die Flamme einer Rakete, gut zu unterscheiden vom grünen bis roten Haltbarkeitsbalken. */
	private static final int BAR_COLOR = 0xFFFF8C1A;
	/** Steiler als ein Sprung (0,42), damit der Spieler beim Raketenstart gut vom Boden wegkommt. */
	private static final double LAUNCH_VELOCITY = 1.0;
	private static final RocketLaunch LAUNCH = new RocketLaunch();
	private static KeyMapping launchKey;

	@Override
	public void initialize() {
		ModelLayerRegistry.registerModelLayer(JetpackLayer.MODEL_LAYER, JetpackModel::createLayer);
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new JetpackLayer(avatarRenderer, context.getModelSet()));
			}
		});
		ExtractItemDecorationsCallback.EVENT.register((graphics, font, stack, x, y) -> drawChargeBar(graphics, stack, x, y));
		launchKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.qualityofmine.rocket_launch", InputConstants.KEY_V, ExtraEnchantsClient.KEY_CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(ElytraArmorClientFeature::handleLaunchKey);
	}

	private static void drawChargeBar(final GuiGraphicsExtractor graphics, final ItemStack stack, final int x, final int y) {
		int charge = ElytraArmors.chargeOf(stack);
		if (charge == 0) {
			return;
		}
		int left = x + BAR_LEFT;
		int top = y + BAR_TOP;
		graphics.fill(RenderPipelines.GUI, left, top, left + BAR_WIDTH, top + 2, BAR_BACKGROUND);
		graphics.fill(RenderPipelines.GUI, left, top, left + RocketCharge.barWidth(charge, BAR_WIDTH), top + 1, BAR_COLOR);
	}

	/**
	 * Die Sprungtaste wurde in der Luft frisch gedrückt. Gleitet der Spieler schon und hat Ladung, bittet der Client den
	 * Server um einen Schub. Liefert true, wenn der Tastendruck damit verbraucht ist.
	 */
	public static boolean tryRocketBoost(final LocalPlayer player) {
		if (!player.isFallFlying() || !isCharged(player)) {
			return false;
		}
		ClientPlayNetworking.send(new RocketBoostPayload());
		return true;
	}

	/**
	 * Die Taste für den Raketenstart: Im Gleitflug gibt sie Schub wie die Sprungtaste. Sonst startet sie den Flug, am Boden
	 * mit einem kräftigen Absprung. Gleiten lässt sich erst in der Luft, deshalb folgen Gleitflug und Schub im nächsten Tick.
	 */
	private static void handleLaunchKey(final Minecraft minecraft) {
		while (launchKey.consumeClick()) {
			LocalPlayer player = minecraft.player;
			if (player == null || !isCharged(player) || player.isInLiquid() || player.isPassenger() || player.getAbilities().flying) {
				continue;
			}
			if (tryRocketBoost(player)) {
				continue;
			}
			if (player.onGround()) {
				Vec3 movement = player.getDeltaMovement();
				player.setDeltaMovement(movement.x, LAUNCH_VELOCITY, movement.z);
			}
			LAUNCH.request();
		}
	}

	/** Zu Beginn jedes Ticks des Spielers: Ist er nach dem Raketenstart in der Luft, öffnet er die Flügel und bekommt Schub. */
	public static void tick(final LocalPlayer player) {
		if (!LAUNCH.tick(!player.onGround())) {
			return;
		}
		if (player.tryToStartFallFlying()) {
			// Dasselbe Paket schickt das Spiel, wenn man Elytren per Sprungtaste öffnet
			player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
			ClientPlayNetworking.send(new RocketBoostPayload());
		}
	}

	private static boolean isCharged(final LocalPlayer player) {
		return ElytraArmors.chargeOf(player.getItemBySlot(EquipmentSlot.CHEST)) > 0;
	}
}
