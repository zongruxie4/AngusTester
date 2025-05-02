package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_MS_FORMAT;
import static cloud.xcan.angus.spec.utils.DateUtils.formatDate;
import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleVo {

  @JsonIgnore
  private Long timestamp0;

  private List<ScoreValue> values;

  public String getTimestamp() {
    return nonNull(timestamp0) ?
        formatDate(new Date(timestamp0), DEFAULT_DATE_TIME_MS_FORMAT) : null;
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  public static class ScoreValue {

    private String name;

    @Schema(description="Comma-separated sample value")
    private String cvsValue;

    public static ScoreValue of(String name, String cvsValue) {
      return new ScoreValue().setName(name).setCvsValue(cvsValue);
    }
  }
}
