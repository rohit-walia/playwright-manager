package org.playwright.utils;

import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class ObjUtils {

  /**
   * Filters an array of objects to only contain those objects that are the same type as provided class.
   *
   * @param arrayOfObjects array of Objects
   * @param clazzToCollect class to filter array of objects
   * @return list
   */
  public static <T> List<T> filterFromArray(Object[] arrayOfObjects, @NonNull Class<T> clazzToCollect) {
    return Arrays.stream(arrayOfObjects)
        .filter(arg -> arg.getClass().isAssignableFrom(clazzToCollect))
        .map(arg -> (T) arg)
        .toList();
  }

  /**
   * Get the first element from array of Objects that matches the class type provided.
   *
   * @param arrayOfObjects array of Objects
   * @param clazzToCollect class to filter array of objects
   * @return first element matched
   */
  public static <T> Optional<T> getFromArray(Object[] arrayOfObjects, @NonNull Class<T> clazzToCollect) {
    return Arrays.stream(arrayOfObjects)
        .filter(arg -> arg.getClass().isAssignableFrom(clazzToCollect))
        .map(arg -> (T) arg)
        .findFirst();
  }
}
