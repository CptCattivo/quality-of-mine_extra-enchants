package de.qualityofmine.extraenchants.feature.elytraarmor;

import de.qualityofmine.extraenchants.Feature;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ExtractItemDecorationsCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class ElytraArmorClientFeature implements Feature {
	/** Dieselbe Breite wie der Haltbarkeitsbalken des Spiels, der zwei Pixel tiefer liegt. */
	private static final int BAR_WIDTH = 13;
	private static final int BAR_LEFT = 2;
	private static final int BAR_TOP = 11;
	private static final int BAR_BACKGROUND = 0xFF000000;
	/** Orange wie die Flamme einer Rakete, gut zu unterscheiden vom grünen bis roten Haltbarkeitsbalken. */
	private static final int BAR_COLOR = 0xFFFF8C1A;

	@Override
	public void initialize() {
		ModelLayerRegistry.registerModelLayer(JetpackLayer.MODEL_LAYER, JetpackModel::createLayer);
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
			if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
				helper.register(new JetpackLayer(avatarRenderer, context.getModelSet()));
			}
		});
		ExtractItemDecorationsCallback.EVENT.register((graphics, font, stack, x, y) -> drawChargeBar(graphics, stack, x, y));
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
		if (!player.isFallFlying() || ElytraArmors.chargeOf(player.getItemBySlot(EquipmentSlot.CHEST)) == 0) {
			return false;
		}
		ClientPlayNetworking.send(new RocketBoostPayload());
		return true;
	}
}
