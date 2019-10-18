package com.functional.exceptions;

/**
 * User: asrivastava
 * Date: 18/10/19 4:13 PM
 */
public class InvalidAccessException extends RuntimeException {
  public InvalidAccessException(String message) {
    super(message);
  }
}
