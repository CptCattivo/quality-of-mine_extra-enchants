package de.qualityofmine.extraenchants.feature.swiftness;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SwiftnessTest {
	private static final double TOLERANCE = 1.0E-9;

	@ParameterizedTest
	@CsvSource({"0, 0.0", "1, 0.1", "2, 0.2", "3, 0.3"})
	void bonusGrowsByTenPercentPerLevelWhileSprinting(final int level, final double expected) {
		assertEquals(expected, Swiftness.sprintBonus(level, true), TOLERANCE);
	}

	@ParameterizedTest
	@ValueSource(ints = {0, 1, 2, 3})
	void noBonusWithoutSprinting(final int level) {
		assertEquals(0.0, Swiftness.sprintBonus(level, false), TOLERANCE);
	}
}
