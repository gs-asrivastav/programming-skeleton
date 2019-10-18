<h1>Sample Usage</h1>

```
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
```