package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_MS_FORMAT;
import static cloud.xcan.angus.spec.utils.DateUtils.formatDate;
import static java.util.Objects.nonNull;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleErrorContentVo {

  private boolean finish;

  @Schema(description = "Node ID where the error occurred")
  private Long nodeId;

  @Schema(description = "The runner records the sampling time")
  private Long timestamp0;

  @Schema(description = "Sampling task name")
  private String name;

  @Schema(description = "Sampling trace id")
  private String key;

  @Schema(description = "Sampling error content")
  private String content;

  public String getTimestamp() {
    return nonNull(timestamp0) ?
        formatDate(new Date(timestamp0), DEFAULT_DATE_TIME_MS_FORMAT) : null;
  }
}
