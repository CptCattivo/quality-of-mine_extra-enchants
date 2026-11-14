package de.qualityofmine.extraenchants.feature.doublejump;

import de.qualityofmine.extraenchants.ModEnchantments;
import net.minecraft.client.player.LocalPlayer;

/**
 * Verzauberung Doppelsprung für Schuhe. Die Bewegung des eigenen Spielers berechnet der Client,
 * der Server übernimmt sie. Deshalb reicht es, den Sprung hier auszulösen.
 */
public final class DoubleJumpClient {
	private static final AirJump AIR_JUMP = new AirJump();

	private DoubleJumpClient() {
	}

	/** Zu Beginn jedes Ticks des Spielers. */
	public static void tick(final LocalPlayer player) {
		if (player.onGround() || player.isInLiquid() || player.onClimbable() || player.isPassenger() || player.getAbilities().flying) {
			AIR_JUMP.land();
		}
	}

	/**
	 * Die Sprungtaste wurde in der Luft frisch gedrückt. Liefert true, wenn der Spieler ein zweites Mal springt.
	 * Mit Elytren gleitet man dann erst beim nächsten Drücken.
	 */
	public static boolean tryAirJump(final LocalPlayer player) {
		boolean inAir = !player.onGround()
			&& !player.isInLiquid()
			&& !player.isPassenger()
			&& !player.isFallFlying()
			// Im Kreativ-Modus schaltet doppeltes Drücken das Fliegen um
			&& !player.getAbilities().mayfly;
		boolean allowed = inAir && ModEnchantments.level(player, ModEnchantments.DOUBLE_JUMP) > 0;
		if (!AIR_JUMP.tryUse(allowed)) {
			return false;
		}
		player.jumpFromGround();
		return true;
	}
}
