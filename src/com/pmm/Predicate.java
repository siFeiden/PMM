package com.pmm;

/**
 * A function f:T -> boolean.
 * Test if an object has some property.
 *
 * @param <T> Type of the objects to test.
 */
public interface Predicate<T> {

  /**
   * Evaluate the predicate on the given argument.
   *
   * @param value the input argument
   * @return true if the input argument matches the predicate, otherwise false
   */
  boolean test(T value);
}
