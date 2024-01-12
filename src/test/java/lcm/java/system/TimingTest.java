package lcm.java.system;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lcm.java.system.Timing.TimeNode;

class TimingTest {
	
	private static final float ACCEPTABLE_ERROR_PERCENTAGE = 0.1f;
	
	private static void simulateWaiting(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private static void verifyTime(String clockName, TimeNode benchRoot, long expectedTime) {
		assertEquals(1, benchRoot.getAllDescendants().stream().filter(c -> c.getName().equals(clockName)).count());
		Stopwatch clock = benchRoot.getAllDescendants().stream().filter(c -> c.getName().equals(clockName)).findFirst().get();
		assertTrue(clock.getTotalTime() >= expectedTime * (1 - ACCEPTABLE_ERROR_PERCENTAGE), () -> "Validation failed for clock " + clockName + ": actual time = " + clock.getTotalTime());
		assertTrue(clock.getTotalTime() <= expectedTime * (1 + ACCEPTABLE_ERROR_PERCENTAGE) + 2, () -> "Validation failed for clock " + clockName + ": actual time = " + clock.getTotalTime());
	}
	
	@BeforeEach
	private void prepareTiming() {
		Timing.getResultsAsString(); // Cleans any possible previous results
	}
	
	@Test
	void TimingSingleThreadTestOK() throws InterruptedException {
		Timing.start("step0");
		
		Timing.start("step1");
		simulateWaiting(100);
		Timing.stop("step1");
		
		Timing.start("step2");
		simulateWaiting(50);
		Timing.stop("step2");
		
		Timing.start("step3");
		simulateWaiting(20);
		Timing.start("step4");
		simulateWaiting(20);
		Timing.stop("step4");
		Timing.stop("step3");
		
		Timing.start("step5");
		Timing.start("step6");
		Timing.start("step7");
		simulateWaiting(10);
		Timing.stop("step7");
		Timing.start("step8");
		simulateWaiting(10);
		Timing.stop("step8");
		simulateWaiting(10);
		Timing.stop("step6");
		simulateWaiting(10);
		Timing.stop("step5");
		
		Timing.stop("step0");
		
		Timing.start("step9");
		Timing.start("step10");
		simulateWaiting(15);
		Timing.stop("step10");
		simulateWaiting(10);
		Timing.stop("step9");
		
		// String result = Timing.getResultsAsString();
		// System.out.println(result);
		
		TimeNode benchRoot = Timing.getResults();
		assertEquals(11, benchRoot.getAllDescendants().size());
		verifyTime("step0", benchRoot, 230);
		verifyTime("step1", benchRoot, 100);
		verifyTime("step2", benchRoot, 50);
		verifyTime("step3", benchRoot, 40);
		verifyTime("step4", benchRoot, 20);
		verifyTime("step5", benchRoot, 40);
		verifyTime("step6", benchRoot, 30);
		verifyTime("step7", benchRoot, 10);
		verifyTime("step8", benchRoot, 10);
		verifyTime("step9", benchRoot, 25);
		verifyTime("step10", benchRoot, 15);
	}
	
	@Test
	void TimingMultiThreadTestOK() throws InterruptedException {
		Timing.start("step0");
		
		Timing.start("step1");
		simulateWaiting(100);
		Timing.stop("step1");
		
		Timing.start("step2");
		simulateWaiting(50);
		Timing.stop("step2");
		
		Timing.start("step3");
		simulateWaiting(20);
		Timing.start("step4");
		simulateWaiting(20);
		Timing.stop("step4");
		Timing.stop("step3");
		
		Thread threadB = new Thread(() -> {
			Timing.start("step1");
			Timing.start("step2");
			Timing.start("step3");
			simulateWaiting(10);
			Timing.stop("step3");
			Timing.start("step4");
			simulateWaiting(10);
			Timing.stop("step4");
			simulateWaiting(10);
			Timing.stop("step2");
			simulateWaiting(10);
			Timing.stop("step1");
			
			Timing.start("step5");
			Timing.start("step6");
			simulateWaiting(15);
			Timing.stop("step6");
			simulateWaiting(10);
			Timing.stop("step5");
			
			TimeNode benchRoot = Timing.getResults();
			assertEquals(6, benchRoot.getAllDescendants().size());
			verifyTime("step1", benchRoot, 40);
			verifyTime("step2", benchRoot, 30);
			verifyTime("step3", benchRoot, 10);
			verifyTime("step4", benchRoot, 10);
			verifyTime("step5", benchRoot, 25);
			verifyTime("step6", benchRoot, 15);
			
//			System.out.println(Timing.getResultsAsString());
		});
		
		threadB.start();
		
		Timing.stop("step0");
		
		TimeNode benchRoot = Timing.getResults();
		assertEquals(5, benchRoot.getAllDescendants().size());
		verifyTime("step0", benchRoot, 190);
		verifyTime("step1", benchRoot, 100);
		verifyTime("step2", benchRoot, 50);
		verifyTime("step3", benchRoot, 40);
		verifyTime("step4", benchRoot, 20);
		
		// String result = Timing.getResultsAsString();
		// System.out.println(result);
		
		try {
			threadB.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	void TimingTestRepeatedStartFail() {
		Timing.start("step1");
		Timing.start("step1");
		Timing.stop("step1");
		Timing.stop("step1");
		try {
			Timing.getResults();
			fail();
		}
		catch(IllegalStateException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException);
			assertEquals(String.format(Timing.FATAL_INCONSISTENCY_NAME_ALREADY_STARTED, "step1"), e.getCause().getMessage());
		}
	}
	
	@Test
	void TimingTestRepeatedStartFailAsString() {
		Timing.start("step1");
		Timing.start("step1");
		Timing.stop("step1");
		Timing.stop("step1");
		assertDoesNotThrow(() -> {
			String result = Timing.getResultsAsString();
			assertTrue(result.contains("There has been a fatal inconsistency while running Timing"));
			assertTrue(result.contains(String.format(Timing.FATAL_INCONSISTENCY_NAME_ALREADY_STARTED, "step1")));
		});
	}
	
	@Test
	void TimingRepeatedClockOK() {
		Timing.start("step1");
		Timing.stop("step1");
		Timing.start("step1");
		Timing.stop("step1");
		Timing.start("step2");
		Timing.stop("step2");
		Timing.start("step1");
		Timing.stop("step1");

		String result = Timing.getResultsAsString();
		assertTrue(result.contains("[step1] {calls: 3}"));
	}

	@Test
	void TimingRepeatedNameDifferentClocks() {
		Timing.start("step1");
		Timing.stop("step1");
		Timing.start("step1");
		Timing.stop("step1");
		
		Timing.start("step2");
		Timing.start("step1");
		Timing.stop("step1");
		Timing.stop("step2");

		String result = Timing.getResultsAsString();
		assertTrue(result.contains("[step1 (1)] {calls: 2}"));
		assertTrue(result.contains("[step1 (2)] {calls: 1}"));
	}
	
	@Test
	void TimingStopClockFail() {
		Timing.start("step1");
		Timing.stop("step2");
		Timing.stop("step1");
		try {
			Timing.getResults();
			fail();
		}
		catch(IllegalStateException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException);
			assertEquals(String.format(Timing.FATAL_INCONSISTENCY_NO_CLOCK_TO_STOP, "step2"), e.getCause().getMessage());
		}
	}
	
	@Test
	void TimingStopClockFailAsString() {
		Timing.start("step1");
		assertDoesNotThrow(() -> {
			Timing.stop("step2");
			String result = Timing.getResultsAsString();
			assertTrue(result.contains("There has been a fatal inconsistency while running Timing"));
			assertTrue(result.contains(String.format(Timing.FATAL_INCONSISTENCY_NO_CLOCK_TO_STOP, "step2")));
		});
	}
	
	@Test
	void TimingTestStopSequenceFail() {
		Timing.start("step1");
		Timing.start("step2");
		Timing.stop("step1");
		Timing.stop("step2");
		try {
			Timing.getResults();
			fail();
		}
		catch(IllegalStateException e) {
			assertTrue(e.getCause() instanceof IllegalStateException);
			assertEquals(String.format(Timing.FATAL_INCONSISTENCY_WRONG_STOP_ORDER, "step2", "step1"), e.getCause().getMessage());
		}
	}
	
	@Test
	void TimingTestStopSequenceFailAsString() {
		Timing.start("step1");
		Timing.start("step2");
		Timing.stop("step1");
		Timing.stop("step2");
		assertDoesNotThrow(() -> {
			String result = Timing.getResultsAsString();
			assertTrue(result.contains("There has been a fatal inconsistency while running Timing"));
			assertTrue(result.contains(String.format(Timing.FATAL_INCONSISTENCY_WRONG_STOP_ORDER, "step2", "step1")));
		});
	}

	@Test
	void TimingStopAllAfterInclusive() {
		Timing.start("step1");
		Timing.start("step2");
		Timing.start("step3");
		Timing.start("step4");
		Timing.stopAllAfter("step2", true);
		simulateWaiting(50);
		Timing.stop("step1");
		TimeNode benchRoot = Timing.getResults();
		verifyTime("step1", benchRoot, 50);
		verifyTime("step2", benchRoot, 0);
		verifyTime("step3", benchRoot, 0);
		verifyTime("step4", benchRoot, 0);
	}

	@Test
	void TimingStopAllAfterExclusive() {
		Timing.start("step1");
		Timing.start("step2");
		Timing.start("step3");
		Timing.start("step4");
		Timing.stopAllAfter("step2", false);
		simulateWaiting(50);
		Timing.stop("step2");
		Timing.stop("step1");
		TimeNode benchRoot = Timing.getResults();
		verifyTime("step1", benchRoot, 50);
		verifyTime("step2", benchRoot, 50);
		verifyTime("step3", benchRoot, 0);
		verifyTime("step4", benchRoot, 0);
	}

	@Test
	void TimingStopAllAfterFail() {
		Timing.start("step1");
		Timing.start("step2");
		Timing.stopAllAfter("step3", true);
		try {
			Timing.getResults();
			fail();
		}
		catch(IllegalStateException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException);
			assertEquals(String.format(Timing.FATAL_INCONSISTENCY_NO_CLOCK_TO_STOP, "step3"), e.getCause().getMessage());
		}
	}
}

