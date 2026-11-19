package de.qualityofmine.extraenchants.mixin;

import de.qualityofmine.extraenchants.feature.elytraarmor.ElytraArmorAnvil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Erbt von ItemCombinerMenu, um an dessen Ein- und Ausgabeplätze zu kommen. */
@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin extends ItemCombinerMenu {
	@Shadow
	@Final
	private DataSlot cost;
	@Shadow
	private int repairItemCountCost;
	@Shadow
	private boolean onlyRenaming;
	@Shadow
	private @Nullable String itemName;

	private AnvilMenuMixin(final @Nullable MenuType<?> menuType, final int containerId, final Inventory inventory,
		final ContainerLevelAccess access, final ItemCombinerMenuSlotDefinition itemInputSlots) {
		super(menuType, containerId, inventory, access, itemInputSlots);
	}

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void extraenchants$combineElytraArmor(final CallbackInfo ci) {
		ElytraArmorAnvil.combine(inputSlots.getItem(AnvilMenu.INPUT_SLOT), inputSlots.getItem(AnvilMenu.ADDITIONAL_SLOT), itemName,
			player.hasInfiniteMaterials()).ifPresent(result -> {
				// Beide Teile gehen ganz in der Rüstung auf
				repairItemCountCost = 0;
				onlyRenaming = false;
				cost.set(result.cost());
				resultSlots.setItem(0, result.stack());
				broadcastChanges();
				ci.cancel();
			});
	}
}
