package org.playwright.failsafe;


import dev.failsafe.Fallback;
import dev.failsafe.RetryPolicy;
import lombok.extern.slf4j.Slf4j;
import org.playwright.common.Timeout;

import java.time.Duration;

@Slf4j
public class FailsafePolicy {
  public static final int DEFAULT_DELAY_SECONDS = Timeout.ONE_SECOND.getSecond();
  public static int DEFAULT_MAX_ATTEMPTS = 3;

  public RetryPolicy<Object> getDefaultRetryPolicy() {
    return createRetryPolicy(DEFAULT_DELAY_SECONDS, DEFAULT_MAX_ATTEMPTS, Throwable.class);
  }

  /**
   * Creates retry failsafe policy.
   *
   * @param delayInSeconds delay between retry attempts
   * @param maxAttempts    max attempts to retry
   * @param exceptions     exceptions to handle as failures
   * @return RetryPolicy obj
   * @see <a href="https://failsafe.dev/retry/">official FailSafe documentation</a>
   */
  @SafeVarargs
  public final RetryPolicy<Object> createRetryPolicy(int delayInSeconds, int maxAttempts,
                                                     Class<? extends Throwable>... exceptions) {
    return RetryPolicy.builder()
        .handle(exceptions)
        .withDelay(Duration.ofSeconds(delayInSeconds))
        .withMaxRetries(maxAttempts)
        .onRetry(e -> log.info("Failed on #{} retry! Error: {}. Retrying...", e.getAttemptCount(),
            e.getLastException().getMessage().lines().toList().get(1)))
        .onRetriesExceeded(e -> log.error("Max attempts reached.", e.getException()))
        .build();
  }

  /**
   * Creates fallback failsafe policy.
   *
   * @param logMessage message to log
   * @return Fallback obj
   * @see <a href="https://failsafe.dev/fallback/">official FailSafe documentation</a>
   */
  public Fallback<Object> fallbackPolicyToLog(String logMessage) {
    return Fallback.of(() -> {
      log.info("Fallback...");
      log.info(logMessage);
    });
  }
}
