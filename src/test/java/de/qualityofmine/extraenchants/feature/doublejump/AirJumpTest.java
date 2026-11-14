package de.qualityofmine.extraenchants.feature.doublejump;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AirJumpTest {
	private final AirJump airJump = new AirJump();

	@Test
	void onlyOneJumpUntilLanding() {
		assertTrue(airJump.tryUse(true));
		assertFalse(airJump.tryUse(true));
	}

	@Test
	void landingGivesTheJumpBack() {
		airJump.tryUse(true);
		airJump.land();
		assertTrue(airJump.tryUse(true));
	}

	@Test
	void forbiddenJumpIsNotUsedUp() {
		assertFalse(airJump.tryUse(false));
		assertTrue(airJump.tryUse(true));
	}
}
