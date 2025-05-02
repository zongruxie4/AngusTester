package cloud.xcan.angus.api.commonlink.exec.result;

import cloud.xcan.angus.model.SampleResult;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleResultContent {

  private Long nodeId;

  private Long timestamp;

  private Long timestamp0;

  private String name;

  private Long iteration;

  private String key;

  private SampleResult sampleResult;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExecSampleResultContent)) {
      return false;
    }
    ExecSampleResultContent that = (ExecSampleResultContent) o;
    return Objects.equals(nodeId, that.nodeId)
        && Objects.equals(timestamp, that.timestamp)
        && Objects.equals(timestamp0, that.timestamp0)
        && Objects.equals(name, that.name)
        && Objects.equals(iteration, that.iteration)
        && Objects.equals(key, that.key)
        && Objects.equals(sampleResult, that.sampleResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeId, timestamp, timestamp0, name, iteration, key, sampleResult);
  }
}
