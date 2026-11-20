package de.qualityofmine.extraenchants.feature.elytraarmor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RocketLaunchTest {
	private final RocketLaunch launch = new RocketLaunch();

	@Test
	void withoutRequestThereIsNoLaunch() {
		assertFalse(launch.tick(true));
	}

	@Test
	void launchesOnceTheJumpLeftTheGround() {
		launch.request();
		assertFalse(launch.tick(false));
		assertTrue(launch.tick(true));
		assertFalse(launch.tick(true));
	}

	@Test
	void launchExpiresWhenThePlayerStaysOnTheGround() {
		launch.request();
		for (int tick = 0; tick < RocketLaunch.MAX_WAIT_TICKS; tick++) {
			assertFalse(launch.tick(false));
		}
		assertFalse(launch.tick(false));
		assertFalse(launch.tick(true));
	}

	@Test
	void armorRocketsPullTwentyPercentHarder() {
		assertEquals(1.8, ArmorRocket.scale(1.5), 1.0E-9);
		assertEquals(0.12, ArmorRocket.scale(0.1), 1.0E-9);
	}
}
