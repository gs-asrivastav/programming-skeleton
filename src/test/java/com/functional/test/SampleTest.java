package com.functional.test;

import com.functional.Functional;
import com.functional.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: asrivastava
 * Date: 18/10/19 4:11 PM
 */
@Slf4j
public class SampleTest {
  @Test
  public void sampleWithoutRetry() {
    AtomicInteger i = new AtomicInteger(0);
    Callable<Integer> runner = () -> {
      if (i.incrementAndGet() != 4) {
        throw new NotImplementedException("Lol!!");
      }
      return i.get();
    };
    Result<Integer> runResult =
        new Functional<Integer>().retrySupportedOn(NotImplementedException.class).withRetries(5)
            .onSuccess(o -> log.info("Output: {}", o))
            .onRetry((count, err) -> log.info("Retry:: " + count + ", Error:: " + err.getMessage()))
            .withEnableRetry(Boolean.FALSE)
            .run(runner)
            .map(result -> result * 10);
    Assert.assertEquals(1, i.get());
    Assert.assertFalse(runResult.isSuccess());
  }

  @Test
  public void sampleWithRetry() {
    AtomicInteger i = new AtomicInteger(0);
    Callable<Integer> runner = () -> {
      if (i.incrementAndGet() != 4) {
        throw new NotImplementedException("Lol!!");
      }
      return i.get();
    };
    Result<Integer> runResult =
        new Functional<Integer>()
            .onSuccess(o -> log.info("Output: {}", o))
            .onRetry((count, err) -> log.info("Retry:: " + count + ", Error:: " + err.getMessage()))
            .withEnableRetry(Boolean.TRUE)
            .withRetryDuration(1, TimeUnit.SECONDS)
            .retrySupportedOn(NotImplementedException.class)
            .withRetries(5)
            .run(runner)
            .map(result -> result * 10);
    Assert.assertTrue(runResult.isSuccess());
  }
}
