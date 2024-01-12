package org.playwright.common;

import lombok.Getter;

/**
 * General timeout constants.
 */
@Getter
public enum Timeout {
  ONE_SECOND(1000, 1),
  TWO_SECONDS(2000, 2),
  THREE_SECONDS(3000, 3),
  FIVE_SECONDS(5000, 5),
  TEN_SECONDS(10000, 10),
  TWENTY_SECONDS(20000, 20);

  private final int millisecond;
  private final int second;

  Timeout(int timeout, int second) {
    this.millisecond = timeout;
    this.second = second;
  }
}
