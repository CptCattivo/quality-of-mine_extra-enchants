package de.qualityofmine.extraenchants.feature.saturation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SaturationTest {
	private static final float VANILLA = 4.0F;
	private static final float TOLERANCE = 1.0E-4F;

	@ParameterizedTest
	@CsvSource({"0, 0.0", "1, 0.10", "2, 0.25", "3, 0.50"})
	void consumptionDropsWithLevel(final int level, final float expected) {
		assertEquals(expected, Saturation.reduction(level), TOLERANCE);
	}

	@ParameterizedTest
	@CsvSource({"0, 4.0", "1, 4.4444", "2, 5.3333", "3, 8.0"})
	void moreExhaustionIsNeededPerPoint(final int level, final float expected) {
		assertEquals(expected, Saturation.exhaustionPerPoint(level, VANILLA), TOLERANCE);
	}

	@ParameterizedTest
	@CsvSource({"0, 1.0", "1, 0.9", "2, 0.75", "3, 0.5"})
	void pointsLostMatchTheReducedConsumption(final int level, final float expectedShare) {
		float exhaustion = 1000.0F;
		float pointsLost = exhaustion / Saturation.exhaustionPerPoint(level, VANILLA);
		assertEquals(expectedShare, pointsLost / (exhaustion / VANILLA), TOLERANCE);
	}
}
