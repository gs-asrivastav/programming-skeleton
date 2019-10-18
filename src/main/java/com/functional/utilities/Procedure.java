package com.functional.utilities;

/**
 * User: asrivastava
 * Date: 18/10/19 1:41 PM
 */
@FunctionalInterface
public interface Procedure {
  void run();

  default Procedure andThen(Procedure after) {
    return () -> {
      this.run();
      after.run();
    };
  }

  default Procedure compose(Procedure before) {
    return () -> {
      before.run();
      this.run();
    };
  }
}