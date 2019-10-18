package com.functional.utilities;

import com.functional.enums.ComparisonOperator;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * User: asrivastava
 * Date: 18/10/19 3:53 PM
 */
public abstract class Predicates {
  public static <T> Predicate<T> alwaysFalse() {
    return o -> false;
  }

  public static <T> Predicate<T> alwaysTrue() {
    return o -> true;
  }

  @SafeVarargs
  public static Predicate<Throwable> canRetryWithException(Class<? extends Throwable>... exceptions) {
    return t -> {
      if (Objects.isNull(exceptions)) {
        return false;
      }

      for (Class<? extends Throwable> failureType : exceptions) {
        if (failureType.isAssignableFrom(t.getClass())) {
          return true;
        }
      }

      return false;
    };
  }

  public static Predicate<Number> numeric(ComparisonOperator operator, Number... values) {
    return number -> {
      Validate.isTrue(Objects.nonNull(values) && values.length >= 1, "Please provide valid values.");
      Number value = values[0];
      validate(value);
      switch (operator) {
        case LT:
          return number.doubleValue() < value.doubleValue();
        case GT:
          return number.doubleValue() > value.doubleValue();
        case LTE:
          return number.doubleValue() <= value.doubleValue();
        case GTE:
          return number.doubleValue() >= value.doubleValue();
        case EQ:
          return number.doubleValue() == value.doubleValue();
        case NE:
          return number.doubleValue() != value.doubleValue();
        case BTW:
          Validate.isTrue(values.length > 1, "Please provide valid values.");
          Number upper = values[1];
          validate(upper);
          return number.doubleValue() >= value.doubleValue() && number.doubleValue() <= upper.doubleValue();
        default:
          throw new UnsupportedOperationException("Invalid Operator Used: " + operator.toString());
      }
    };
  }

  private static void validate(Number number) {
    Validate.isTrue(Objects.nonNull(number), "Please provide valid values.");
  }
}
