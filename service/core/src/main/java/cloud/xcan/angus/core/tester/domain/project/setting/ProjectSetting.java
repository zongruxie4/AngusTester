package cloud.xcan.angus.core.tester.domain.project.setting;

import cloud.xcan.angus.api.enums.CalcProgressMethod;
import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ProjectSetting {

  public static ProjectSetting DEFAULT_SETTING = new ProjectSetting();

  @Schema(description = "Task or functional case progress calculation method, default is `WORKLOAD`", example = "WORKLOAD")
  private CalcProgressMethod calcProgressMethod = CalcProgressMethod.WORKLOAD;

  @Schema(description = "Workload evaluation method, default is `STORY_POINT`", example = "STORY_POINT")
  private EvalWorkloadMethod evalWorkloadMethod = EvalWorkloadMethod.STORY_POINT;

  @Schema(description = "Whether to enable data permission control, if enabled, access to the corresponding business data will only be allowed after authorization,"
      + " default is `false`", example = "false")
  private boolean enableDataAuth = false;

}
