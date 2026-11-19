package de.qualityofmine.extraenchants.feature.elytraarmor;

import com.mojang.blaze3d.vertex.PoseStack;
import de.qualityofmine.extraenchants.ExtraEnchants;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Zeigt die geladenen Raketen der Elytra-Rüstung auf dem Rücken des Spielers. Ohne Ladung bleibt der Rücken frei. */
public class JetpackLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(ExtraEnchants.id("jetpack"), "main");
	private static final Identifier TEXTURE = ExtraEnchants.id("textures/entity/jetpack.png");

	private final JetpackModel model;

	public JetpackLayer(final RenderLayerParent<AvatarRenderState, PlayerModel> renderer, final EntityModelSet modelSet) {
		super(renderer);
		this.model = new JetpackModel(modelSet.bakeLayer(MODEL_LAYER));
	}

	@Override
	public void submit(final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords,
		final AvatarRenderState state, final float yRot, final float xRot) {
		if (state.isInvisible || ElytraArmors.chargeOf(state.chestEquipment) == 0) {
			return;
		}
		submitNodeCollector.submitModel(model, state, poseStack, RenderTypes.entityCutout(TEXTURE), lightCoords,
			LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
	}
}
