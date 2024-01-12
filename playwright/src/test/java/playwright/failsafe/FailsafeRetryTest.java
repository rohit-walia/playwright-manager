package playwright.failsafe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.playwright.failsafe.FailsafeRetry;

import java.util.ArrayList;
import java.util.List;

class FailsafeRetryTest {
  @Test
  void testRetry_TryAgain_WhenNoError() {
    var list = new ArrayList<String>();
    FailsafeRetry.tryAgain(() -> list.add("test"), 1, 1);
    Assertions.assertEquals(1, list.size());
    Assertions.assertEquals(List.of("test"), list);
  }

  @Test
  void testRetry_TryAgain_WhenOnlyFirstError() {
    var list = new ArrayList<String>();
    final var retryAttempts = 2;

    FailsafeRetry.tryAgain(() -> {
      list.add("test");

      // this mimics an exception occurring on the first attempt and succeeding on the second
      if (list.size() < retryAttempts) {
        Assertions.fail();
      }
    }, 1, 1);

    Assertions.assertEquals(retryAttempts, list.size());
    Assertions.assertEquals(List.of("test", "test"), list);
  }

  @Test
  void testRetry_TryAgain_WhenAlwaysError() {
    Assertions.assertThrows(AssertionFailedError.class, () -> FailsafeRetry.tryAgain(Assertions::fail, 1, 1));
  }

  @Test
  void testRetry_WithDefault_WhenNoError() {
    var list = new ArrayList<String>();
    FailsafeRetry.withDefault(() -> list.add("test"));
    Assertions.assertEquals(1, list.size());
    Assertions.assertEquals(List.of("test"), list);
  }

  @Test
  void testRetry_WithDefault_WhenOnlyFirstError() {
    var list = new ArrayList<String>();
    final var retryAttempts = 2;

    FailsafeRetry.withDefault(() -> {
      list.add("test");

      // this mimics an exception occurring on the first attempt and succeeding on the second
      if (list.size() < retryAttempts) {
        Assertions.fail();
      }
    });

    Assertions.assertEquals(retryAttempts, list.size());
    Assertions.assertEquals(List.of("test", "test"), list);
  }

  @Test
  void testRetry_WithDefault_WhenAlwaysError() {
    Assertions.assertThrows(AssertionFailedError.class, () -> FailsafeRetry.withDefault(Assertions::fail));
  }
}