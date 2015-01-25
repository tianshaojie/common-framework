package io.github.jsbd.common.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 阻塞队列服务配置入口，客户类需要实现IBatchExecutor接口
 *
 * @param <T>
 */
public class DelayExecuteBuffer<T> {

  private static final Logger            logger        = LoggerFactory.getLogger(DelayExecuteBuffer.class);
  private AtomicReference<RecordPool<T>> recordPool    = new AtomicReference<RecordPool<T>>();
  private ScheduledExecutorService       scheduler     = Executors.newSingleThreadScheduledExecutor();
  private IBatchExecutor<T>              batchExecutor;
  private String                         name;
  private long                           checkInterval = 1000;
  private int                            poolSize      = 1024;
  private int                            batchSize     = 10;
  private int                            threads       = 1;
  private ExceptionListener<T>           exceptionListener;

  public synchronized boolean add(T record) {
    if (!full()) {
      boolean ret = getRecordPool().add(record);

      if (logger.isTraceEnabled()) {
        logger.trace("add record to pool [{}]. poolSize=[{}], remainCapacity=[{}], record=[{}], ret=[{}]", new Object[] { name, poolSize,
            getRecordPool().remainCapacity(), record, ret });
      }
      return ret;
    } else {
      logger.warn("record pool [{}] is full.", name);
      return false;
    }
  }

  public void flush() {
    List<T> records = getRecordPool().getWholeRecords();
    execute(records);
  }

  public void start() {

    this.recordPool.set(new RecordPool<T>(poolSize));
    this.recordPool.get().setBatchSize(batchSize);

    for (int i = 0; i < threads; i++) {

      scheduler.scheduleWithFixedDelay(new Runnable() {

        @Override
        public void run() {

          if (logger.isTraceEnabled()) {
            logger.trace("schedule: pop from record pool [{}]. poolSize=[{}], remainCapacity=[{}]", new Object[] { name, poolSize,
                getRecordPool().remainCapacity() });
          }

          final List<T> records = getRecordPool().asList();

          if (!records.isEmpty()) {
            if (logger.isTraceEnabled()) {
              logger.trace("schedule: pop from record pool [{}]. poolSize=[{}], remainCapacity=[{}], size=[{}]", new Object[] { name, poolSize,
                  getRecordPool().remainCapacity(), records.size() });
            }
            execute(records);
          }

        }
      }, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
    }
  }

  private void execute(List<T> records) {
    logger.debug("Execute records. size=[{}]", records.size());
    try {
      batchExecutor.execute(records);
    } catch (Exception e) {
      logger.error("", e);
      if (exceptionListener != null) {
        logger.error("Execute records failed, calling exception listener...pool=[{}]", name);
        exceptionListener.onException(records);
      }
    }
  }

  public boolean full() {
    return getRecordPool().remainCapacity() == 0;
  }

  public int size() {
    return getRecordPool().size();
  }

  public void setName(String name) {
    this.name = name;
  }

  public RecordPool<T> getRecordPool() {
    return recordPool.get();
  }

  public void setThreads(int threads) {
    this.threads = threads;
    this.scheduler = Executors.newScheduledThreadPool(poolSize);
  }

  public void setCheckInterval(long checkInterval) {
    this.checkInterval = checkInterval;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
  }

  public void setExceptionListener(ExceptionListener<T> exceptionListener) {
    this.exceptionListener = exceptionListener;
  }

  public void setBatchExecutor(IBatchExecutor<T> batchExecutor) {
    this.batchExecutor = batchExecutor;
  }
}
