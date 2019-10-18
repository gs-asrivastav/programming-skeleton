/*
 * Copyright 2017 Gainsight. All rights reserved.
 */

package com.functional.result;

import com.functional.exceptions.InvalidAccessException;

/**
 * User: asrivastava
 * Date: 18/10/19 9:46 AM
 */
public class Failure<T> extends Result<T> {
  private Throwable throwable;

  public Failure(Throwable throwable, long duration) {
    super(duration);
    this.throwable = throwable;
  }

  @Override
  public Boolean isSuccess() {
    return Boolean.FALSE;
  }

  @Override
  public T getResult() {
    throw new InvalidAccessException("Result is not fetched for failure scenarios.");
  }

  @Override
  public Throwable getException() {
    return throwable;
  }
}
