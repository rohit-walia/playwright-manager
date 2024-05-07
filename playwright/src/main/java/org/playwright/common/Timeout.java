package org.playwright.common;

import lombok.Getter;

/**
 * General timeout constants.
 */
@Getter
public enum Timeout {
  ONE(1000, 1),
  TWO(2000, 2),
  THREE(3000, 3),
  FIVE(5000, 5),
  TEN(10000, 10),
  TWENTY(20000, 20);

  private final int millisecond;
  private final int second;

  Timeout(int timeout, int second) {
    this.millisecond = timeout;
    this.second = second;
  }
}
