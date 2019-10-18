/*
 * Copyright 2017 Gainsight. All rights reserved.
 */

package com.functional.result;

import com.functional.exceptions.InvalidAccessException;

/**
 * User: asrivastava
 * Date: 18/10/19 9:46 AM
 */
public class Success<T> extends Result<T> {
  private T result;

  public Success(T result, long duration) {
    super(duration);
    this.result = result;
  }

  @Override
  public Boolean isSuccess() {
    return Boolean.TRUE;
  }

  @Override
  public T getResult() {
    return this.result;
  }

  @Override
  public Throwable getException() {
    throw new InvalidAccessException("Exceptions do not exist in case of success scenarios.");
  }

}
