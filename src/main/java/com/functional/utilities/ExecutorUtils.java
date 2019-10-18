package com.functional.utilities;

import com.functional.exceptions.FunctionalRuntimeException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * User: asrivastava
 * Date: 18/10/19 4:02 PM
 */
@Slf4j
public abstract class ExecutorUtils {
  private static final Object EXECUTOR_LOCK = new Object();
  private static ExecutorService EXECUTOR;

  /**
   * Executor for completable futures.
   *
   * @return Executor
   * @see Executor
   * @see Executors to supply the cached thread pool.
   */
  public static ExecutorService getExecutor() {
    if (Objects.isNull(EXECUTOR)) {
      synchronized (EXECUTOR_LOCK) {

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("Internal-Runner-%d")
            .setUncaughtExceptionHandler((t, e) -> {
              log.error("Encountered uncaught exception in thread[t{}-{}]", t.getId(), t.getName(), e);
              throw new FunctionalRuntimeException(e);
            }).build();
        EXECUTOR = Executors.newCachedThreadPool(threadFactory);
      }
    }
    return EXECUTOR;
  }
}
