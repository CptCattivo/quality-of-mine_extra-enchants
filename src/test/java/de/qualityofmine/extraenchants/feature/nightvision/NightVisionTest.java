package de.qualityofmine.extraenchants.feature.nightvision;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NightVisionTest {
	@Test
	void appliesWhenNoEffectIsActive() {
		assertTrue(NightVision.shouldApply(true, false, false, 0));
	}

	@Test
	void refreshesBeforeTheEffectStartsToFlicker() {
		assertTrue(NightVision.shouldApply(true, true, false, NightVision.REFRESH_BELOW - 1));
		assertFalse(NightVision.shouldApply(true, true, false, NightVision.REFRESH_BELOW));
	}

	@Test
	void leavesLongerPotionsAlone() {
		assertFalse(NightVision.shouldApply(true, true, false, 3600));
		assertFalse(NightVision.shouldApply(true, true, true, -1));
	}

	@Test
	void doesNothingWhenSwitchedOff() {
		assertFalse(NightVision.shouldApply(false, false, false, 0));
	}

	@Test
	void removesOnlyItsOwnEffect() {
		assertTrue(NightVision.shouldRemove(false, true, true));
		assertFalse(NightVision.shouldRemove(false, true, false));
		assertFalse(NightVision.shouldRemove(true, true, true));
		assertFalse(NightVision.shouldRemove(false, false, false));
	}
}
