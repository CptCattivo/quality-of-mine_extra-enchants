package de.qualityofmine.extraenchants.feature.elytraarmor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.player.PlayerModel;

/**
 * Zwei Raketen links und rechts auf dem Rücken. Wie bei den Ohren von deadmau5 ist es ein Spielermodell, in dem nur die
 * Raketen Würfel haben. So folgen sie dem Oberkörper beim Schleichen und im Flug.
 */
public class JetpackModel extends PlayerModel {
	public JetpackModel(final ModelPart root) {
		super(root, false);
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
		PartDefinition body = mesh.getRoot().clearRecursively().getChild("body");
		CubeListBuilder rocket = CubeListBuilder.create()
			.texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F)
			.texOffs(0, 11).addBox(-1.0F, 8.0F, -1.0F, 2.0F, 2.0F, 2.0F);
		// Der Rücken liegt bei z = 2, die angelegten Elytren reichen bis etwa z = 4. Dahinter sitzen die Raketen.
		body.addOrReplaceChild("left_rocket", rocket, PartPose.offset(2.5F, 1.0F, 5.5F));
		body.addOrReplaceChild("right_rocket", rocket, PartPose.offset(-2.5F, 1.0F, 5.5F));
		return LayerDefinition.create(mesh, 32, 32);
	}
}
