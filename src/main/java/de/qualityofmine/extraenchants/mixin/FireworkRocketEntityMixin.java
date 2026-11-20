package de.qualityofmine.extraenchants.mixin;

import de.qualityofmine.extraenchants.feature.elytraarmor.ArmorRocket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FireworkRocketEntity.class)
abstract class FireworkRocketEntityMixin {
	@Shadow
	@Final
	private static EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM;

	/**
	 * Eine angehängte Rakete zieht den Spieler mit 0,1 und 1,5 mal der Blickrichtung. Beide Werte kommen in {@code tick}
	 * nur an dieser Stelle vor. Das Raketen-Item wird übertragen, so rechnen Server und Client gleich.
	 */
	@ModifyConstant(method = "tick", constant = {@Constant(doubleValue = 0.1), @Constant(doubleValue = 1.5)})
	private double extraenchants$armorRocketPower(final double value) {
		return ArmorRocket.power(value, ((Entity) (Object) this).getEntityData().get(DATA_ID_FIREWORKS_ITEM));
	}
}
