package org.playwright.failsafe;


import dev.failsafe.Failsafe;
import dev.failsafe.function.CheckedRunnable;

/**
 * Holds useful retry functions with pre-built policies.
 */
public class FailsafeRetry {
  public static void withDefault(CheckedRunnable testSteps) {
    Failsafe.with(new FailsafePolicy().getDefaultRetryPolicy()).run(testSteps);
  }

  /**
   * Retry with automatic wait on completion and screenshot on failure.
   *
   * @param testSteps Runnable test steps
   */
  public static void tryAgain(CheckedRunnable testSteps, int delayInSeconds, int maxAttempts) {
    var policy = new FailsafePolicy().createRetryPolicy(delayInSeconds, maxAttempts, Throwable.class);
    Failsafe.with(policy).run(testSteps);
  }
}
