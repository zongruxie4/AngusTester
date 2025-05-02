package cloud.xcan.angus.core.tester.domain.exec.result.summary;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_MS_FORMAT;
import static cloud.xcan.angus.spec.utils.DateUtils.formatDate;
import static java.util.Objects.nonNull;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleContent {

  private Boolean finish;

  @Schema(description = "Node ID where the error occurred")
  private Long nodeId;

  //@JsonIgnore
  @Schema(description = "The runner records the sampling time")
  private Long timestamp0;

  @Schema(description = "Sampling task name")
  private String name;

  @Schema(description = "Supported values: ExtContent1、ExtContent2、ExtContent3")
  private String extField;

  @Schema(description = "Number of iterations during functional testing and debugging")
  private Long iteration;

  @Schema(description = "Sampling trace id")
  private String key;

  @Schema(description = "Sampling error cause")
  private Object content;

  public String getTimestamp() {
    return nonNull(timestamp0) ?
        formatDate(new Date(timestamp0), DEFAULT_DATE_TIME_MS_FORMAT) : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExecSampleContent that)) {
      return false;
    }
    return Objects.equals(finish, that.finish)
        && Objects.equals(nodeId, that.nodeId)
        && Objects.equals(timestamp0, that.timestamp0)
        && Objects.equals(name, that.name)
        && Objects.equals(extField, that.extField)
        && Objects.equals(iteration, that.iteration)
        && Objects.equals(key, that.key)
        && Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(finish, nodeId, timestamp0, name, extField, iteration, key, content);
  }
}
