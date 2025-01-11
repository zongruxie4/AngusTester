package cloud.xcan.sdf.core.angustester.domain.setting;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ContentFilterSetting {

  @NotNull
  @ApiModelProperty(value = "Report resource or activity type", required = true)
  private CombinedTargetType targetType;

  @NotNull
  @ApiModelProperty(value = "Report resource id", required = true)
  private Long targetId;

  @ApiModelProperty(value = "Query organization type, default USER")
  private AuthObjectType creatorObjectType;

  @ApiModelProperty(value = "Query organization id")
  private Long creatorObjectId;

  // Specify during report creation
  //@NotNull
  //@ApiModelProperty(value = "Project id", required = true)
  //private Long projectId;

  @ApiModelProperty(value = "Case plan or task sprint id. Just for the front-end to display the parent of the target.")
  private Long planOrSprintId;

  @ApiModelProperty(value = "Resources creation start date")
  private LocalDateTime createdDateStart;

  @ApiModelProperty(value = "Resources creation end date")
  private LocalDateTime createdDateEnd;

}
