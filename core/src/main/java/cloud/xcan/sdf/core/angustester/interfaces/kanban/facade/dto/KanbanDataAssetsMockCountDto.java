package cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Getter
@Setter
@Accessors(chain = true)
public class KanbanDataAssetsMockCountDto {

  @ApiModelProperty(value = "Query organization type, default USER")
  private AuthObjectType creatorObjectType;

  @ApiModelProperty(value = "Query organization id")
  private Long creatorObjectId;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @DateTimeFormat(pattern = DATE_FMT)
  @ApiModelProperty(value = "Resources creation start date")
  private LocalDateTime createdDateStart;

  @DateTimeFormat(pattern = DATE_FMT)
  @ApiModelProperty(value = "Resources creation start date")
  private LocalDateTime createdDateEnd;

}
