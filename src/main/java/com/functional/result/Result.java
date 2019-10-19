package com.functional.result;

/**
 * User: asrivastava
 * Date: 18/10/19 3:49 PM
 */

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * "Producer Extends" - If you need a List to produce T values (you want to read Ts from the list),
 * you need to declare it with ? extends T, e.g. List<? extends Integer>. But you cannot add to this list.
 * <p>
 * "Consumer Super" - If you need a List to consume T values (you want to write Ts into the list),
 * you need to declare it with ? super T, e.g. List<? super Integer>. But there are no guarantees what type of object you may read from this list.
 * <p>
 * If you need to both read from and write to a list, you need to declare it exactly with no wildcards, e.g. List<Integer>.
 * User: asrivastava
 * Date: 18/10/19 9:42 AM
 */
public abstract class Result<T> {
  private long duration;

  Result(long duration) {
    this.duration = duration;
  }

  public abstract Boolean isSuccess();

  public abstract T getResult();

  public abstract Throwable getException();

  public Long getDuration() {
    return this.duration;
  }

  /**
   * Applies a function which returns Result to value inside if it's Success, otherwise does nothing.
   *
   * @param successCallback
   * @param failureCallback
   * @param <R>
   * @return
   */
  public <R> R run(Function<? super T, ? extends R> successCallback,
                   Function<Failure<R>, ? extends R> failureCallback) {
    return isSuccess() ? successCallback.apply(getResult()) : failureCallback.apply((Failure<R>) this);
  }

  /**
   * Applies a function which returns Result to value inside if it's Success, otherwise does nothing.
   *
   * @param mapFn
   * @param <R>
   * @return
   */
  public <R> Result<R> flatMap(Function<? super T, Result<R>> mapFn) {
    return isSuccess() ? mapFn.apply(getResult()) : (Failure<R>) this;
  }

  /**
   * @param mapFn
   * @param <R>
   * @return
   */
  public <R> Result<R> map(Function<? super T, ? extends R> mapFn) {
    Result<R> result;
    if (isSuccess()) {
      long start = System.currentTimeMillis();
      R output = mapFn.apply(getResult());
      this.duration += System.currentTimeMillis() - start;
      result = new Success<>(output, this.duration);
    } else {
      result = (Failure<R>) this;
    }
    return result;
  }

  /**
   * @param errorCallbackFn
   * @param <R>
   * @return
   */
  public <R> Result<R> onError(Function<Throwable, Result<R>> errorCallbackFn) {
    return isSuccess() ? (Success<R>) this : errorCallbackFn.apply(getException());
  }

  public <R> Result<R> onError(Map<Class<? extends Throwable>, Function<Throwable, Result<R>>> errorCallbackFns,
                               Function<Throwable, Result<R>> defaultErrorCallback) {
    Validate.notNull(defaultErrorCallback, "Default error callback cannot be null.");
    if (isSuccess()) {
      return (Success<R>) this;
    } else if (!isSuccess() && MapUtils.isNotEmpty(errorCallbackFns)) {
      Optional<Map.Entry<Class<? extends Throwable>, Function<Throwable, Result<R>>>> first =
          errorCallbackFns.entrySet().stream()
              .filter(entry -> entry.getKey().isAssignableFrom(getException().getClass())).findFirst();
      if (first.isPresent()) {
        return first.get().getValue().apply(getException());
      }
    }
    return defaultErrorCallback.apply(getException());
  }

  public static <T> Result<T> of(T result, Throwable throwable, long duration) {
    if (Objects.nonNull(throwable)) {
      return new Failure<>(throwable, duration);
    }
    return new Success<>(result, duration);
  }
}