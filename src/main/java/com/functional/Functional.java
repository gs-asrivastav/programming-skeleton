/*
 * Copyright 2017 Gainsight. All rights reserved.
 */

package com.functional;

import com.functional.enums.ComparisonOperator;
import com.functional.result.Result;
import com.functional.utilities.ExecutorUtils;
import com.functional.utilities.Predicates;
import com.functional.utilities.Procedure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Sample::
 * <pre>
 * AtomicInteger i = new AtomicInteger(0);
 *     Result<Integer> runner =
 *         new GSFunctional<Integer>().retrySupportedOn(NotImplementedException.class).withRetries(5)
 *             .onSuccess(o -> System.out.println(o.longValue()))
 *             .onRetry((count, err) -> System.out.println("Retry:: " + count + ", Error:: " + err.getMessage()))
 *             .run((Callable<Integer>) () -> {
 *               if (i.incrementAndGet() != 4) {
 *                 throw new NotImplementedException("Lol!!");
 *               }
 *               return i.get();
 *             }).map(result -> result * 10);
 *     if (runner.isSuccess()) {
 *       System.out.println(runner.getResult());
 *     }
 * </pre>
 * User: asrivastava
 * Date: 18/10/19 10:24 AM
 */
@Slf4j
public class Functional<T> {
  private long start;
  private static final Integer UPPER_RETRY_COUNT = 10;
  private static final Integer LOWER_RETRY_COUNT = 1;
  private Consumer<T> sCallback;
  private Consumer<Throwable> fCallback;
  private BiConsumer<Integer, Throwable> rCallback;
  private BiConsumer<T, Throwable> finalCallback;
  private Predicate<Throwable> retryable;
  private Integer retries;
  private boolean enableRetry;
  private Procedure before;
  private long duration;
  private TimeUnit retryUnit;

  public Functional() {
    this.retryable = Predicates.alwaysFalse();
    this.retries = 0;
    this.start = System.currentTimeMillis();
    this.enableRetry = false;
    this.duration = 0;
    this.retryUnit = null;
  }

  public Functional<T> beforeStart(Procedure procedure) {
    this.before = procedure;
    return this;
  }

  public Functional<T> onSuccess(Consumer<T> callback) {
    this.sCallback = callback;
    return this;
  }

  public Functional<T> onFailure(Consumer<Throwable> callback) {
    this.fCallback = callback;
    return this;
  }

  public Functional<T> onRetry(BiConsumer<Integer, Throwable> callback) {
    this.rCallback = callback;
    return this;
  }


  public Functional<T> doFinally(BiConsumer<T, Throwable> callback) {
    this.finalCallback = callback;
    return this;
  }

  @SafeVarargs
  public final Functional<T> retrySupportedOn(Class<? extends Throwable>... supported) {
    this.enableRetry = true;
    this.retryable = Predicates.canRetryWithException(supported);
    return this;
  }

  public Functional<T> withRetries(Integer n) {
    this.enableRetry = true;
    this.retries = n;
    return this;
  }

  public Functional<T> withRetryPredicate(Predicate<Throwable> predicate) {
    this.enableRetry = true;
    this.retryable = predicate;
    return this;
  }

  public Functional<T> withEnableRetry(Boolean enable) {
    this.enableRetry = BooleanUtils.isTrue(enable);
    return this;
  }

  public Functional<T> withRetryDuration(long duration, TimeUnit unit) {
    this.duration = duration;
    this.retryUnit = unit;
    return this;
  }

  public Result<T> run(Callable<T> callable) {
    return this.run(callable, ExecutorUtils.getExecutor());
  }

  public Result<T> run(Supplier<T> supplier) {
    return this.run(supplier, ExecutorUtils.getExecutor());
  }

  public Result<T> run(Supplier<T> supplier, ExecutorService executorService) {
    this.validate();
    CompletableFuture<T> tCompletableFuture;
    int retries = 0;
    T result = null;
    Throwable error = null;
    while (true) {
      try {
        if (Objects.nonNull(executorService)) {
          tCompletableFuture = CompletableFuture.supplyAsync(supplier, executorService);
          result = tCompletableFuture.get();
        } else {
          result = supplier.get();
        }
        if (Objects.nonNull(sCallback)) {
          sCallback.accept(result);
        }
        error = null;
        break;
      } catch (Throwable t) {
        error = t;
        if (!this.doRetry(t, retries)) {
          break;
        }
      } finally {
        if (Objects.nonNull(finalCallback)) {
          finalCallback.accept(result, error);
        }
      }
    }
    return Result.of(result, error, System.currentTimeMillis() - this.start);
  }

  public Result<T> run(Callable<T> callable, ExecutorService executorService) {
    this.validate();
    int retries = 0;
    T result = null;
    Throwable error = null;
    while (true) {
      try {
        if (Objects.nonNull(executorService)) {
          result = executorService.submit(callable).get();
        } else {
          result = callable.call();
        }
        if (Objects.nonNull(sCallback)) {
          sCallback.accept(result);
        }
        error = null;
        break;
      } catch (Throwable t) {
        error = t;
        if (!this.doRetry(t, retries)) {
          break;
        }
      } finally {
        if (Objects.nonNull(finalCallback)) {
          finalCallback.accept(result, error);
        }
      }
    }
    return Result.of(result, error, System.currentTimeMillis() - this.start);
  }

  private boolean isRetryable(Throwable throwable) {
    return this.retryable.test(throwable);
  }

  private boolean doRetry(Throwable exception, int retries) {
    if (this.enableRetry && exception instanceof Exception) {
      Throwable cause = exception;
      if (exception instanceof ExecutionException) {
        cause = exception.getCause();
      }
      if (this.isRetryable(cause)) {
        if (retries < this.retries) {
          if (this.rCallback != null) {
            this.rCallback.accept(retries, exception);
          }
          if (Objects.nonNull(this.retryUnit) && this.duration > 0) {
            try {
              this.retryUnit.sleep(0);
            } catch (InterruptedException e) {
              log.warn("Failed to sleep before retry.");
              log.trace("Failed to sleep before retry.", e);
            }
          }
          return true;
        } else if (this.fCallback != null) {
          this.fCallback.accept(exception);
        }
      } else if (this.fCallback != null) {
        this.fCallback.accept(exception);
      }
    }

    return false;
  }

  private void validate() {
    if (BooleanUtils.isTrue(this.enableRetry)) {
      Objects.requireNonNull(this.retryable, "Retryable predicate cannot be null.");
      Objects.requireNonNull(this.retries, "Retry count cannot be null.");
      Validate.isTrue(Predicates.numeric(ComparisonOperator.BTW, LOWER_RETRY_COUNT, UPPER_RETRY_COUNT)
          .test(this.retries), "Retry count cannot be greater than 10.");
    }
    if (Objects.nonNull(before)) {
      before.run();
    }
  }
}
