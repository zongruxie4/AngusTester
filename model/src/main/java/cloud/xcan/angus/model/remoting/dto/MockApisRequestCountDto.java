package cloud.xcan.angus.model.remoting.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class MockApisRequestCountDto {

  private Map<Long, Counter> apisCounter = new ConcurrentHashMap<>();

  public synchronized void initApisCounter(Long apisId) {
    if (!apisCounter.containsKey(apisId)) {
      apisCounter.put(apisId, new Counter());
    }
  }

  public synchronized void deleteApisCounter(Long apisId) {
    apisCounter.remove(apisId);
  }

  public MockApisRequestCountDto addRequestNum(Long apisId, long inc) {
    if (!apisCounter.containsKey(apisId)) {
      apisCounter.put(apisId, new Counter());
    }
    apisCounter.get(apisId).requestNum.add(inc);
    return this;
  }

  public MockApisRequestCountDto addPushbackNum(Long apisId, long inc) {
    if (!apisCounter.containsKey(apisId)) {
      apisCounter.put(apisId, new Counter());
    }
    apisCounter.get(apisId).pushbackNum.add(inc);
    return this;
  }

  public MockApisRequestCountDto addSimulateErrorNum(Long apisId, long inc) {
    if (!apisCounter.containsKey(apisId)) {
      apisCounter.put(apisId, new Counter());
    }
    apisCounter.get(apisId).simulateErrorNum.add(inc);
    return this;
  }

  public MockApisRequestCountDto addSuccessNum(Long apisId, long inc) {
    if (!apisCounter.containsKey(apisId)) {
      apisCounter.put(apisId, new Counter());
    }
    apisCounter.get(apisId).successNum.add(inc);
    return this;
  }

  public MockApisRequestCountDto addExceptionNum(Long apisId, long inc) {
    if (!apisCounter.containsKey(apisId)) {
      apisCounter.put(apisId, new Counter());
    }
    apisCounter.get(apisId).exceptionNum.add(inc);
    return this;
  }

  public Map<Long, Counter> getApisCounter() {
    return apisCounter.entrySet().stream().filter(x -> x.getValue().hasCounterValue())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  @JsonIgnore
  public Map<Long, Counter> getApisCounter0() {
    return apisCounter.entrySet().stream().filter(x -> x.getValue().hasCounterValue0())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  public synchronized void resetApisCounter() {
    for (Entry<Long, Counter> entry : apisCounter.entrySet()) {
      entry.getValue().reset();
    }
  }

  public MockApisRequestCountDto setApisCounter(Map<Long, Counter> apisCounter) {
    this.apisCounter = apisCounter;
    return this;
  }

  public static class Counter {

    /* Json serializes temporary fields */
    public long requestNum0;
    public long pushbackNum0;
    public long simulateErrorNum0;
    public long successNum0;
    public long exceptionNum0;
    /* Json serializes temporary fields */

    // Fix:: com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `java.util.concurrent.atomic.LongAdder` (although at least one Creator exi... (15245 bytes)]
    @JsonIgnore
    public LongAdder requestNum = new LongAdder();
    @JsonIgnore
    public LongAdder pushbackNum = new LongAdder();
    @JsonIgnore
    public LongAdder simulateErrorNum = new LongAdder();
    @JsonIgnore
    public LongAdder successNum = new LongAdder();
    @JsonIgnore
    public LongAdder exceptionNum = new LongAdder();

    public void reset() {
      requestNum.reset();
      pushbackNum.reset();
      simulateErrorNum.reset();
      successNum.reset();
      exceptionNum.reset();
    }

    public long getRequestNum0() {
      return requestNum.longValue();
    }

    public void setRequestNum0(long requestNum0) {
      this.requestNum0 = requestNum0;
    }

    public long getPushbackNum0() {
      return pushbackNum.longValue();
    }

    public void setPushbackNum0(long pushbackNum0) {
      this.pushbackNum0 = pushbackNum0;
    }

    public long getSimulateErrorNum0() {
      return simulateErrorNum.longValue();
    }

    public void setSimulateErrorNum0(long simulateErrorNum0) {
      this.simulateErrorNum0 = simulateErrorNum0;
    }

    public long getSuccessNum0() {
      return successNum.longValue();
    }

    public void setSuccessNum0(long successNum0) {
      this.successNum0 = successNum0;
    }

    public long getExceptionNum0() {
      return exceptionNum.longValue();
    }

    public void setExceptionNum0(long exceptionNum0) {
      this.exceptionNum0 = exceptionNum0;
    }

    public boolean hasCounterValue() {
      return requestNum.longValue() > 0 || pushbackNum.longValue() > 0
          || simulateErrorNum.longValue() > 0 || successNum.longValue() > 0
          || exceptionNum.longValue() > 0;
    }

    public boolean hasCounterValue0() {
      return requestNum0 > 0 || pushbackNum0 > 0
          || simulateErrorNum0 > 0 || successNum0 > 0
          || exceptionNum0 > 0;
    }
  }
}
