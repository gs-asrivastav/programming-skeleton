package com.functional.exceptions;

/**
 * User: asrivastava
 * Date: 18/10/19 4:04 PM
 */
public class FunctionalRuntimeException extends RuntimeException {
  private final Throwable throwable;

  public FunctionalRuntimeException(Throwable throwable) {
    super(throwable.getMessage());
    this.throwable = throwable;
  }

  public Throwable propagatedBy() {
    return this.throwable;
  }
}
