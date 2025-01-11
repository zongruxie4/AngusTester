package cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X10;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;

import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ProjectUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Project name, must be unique", example = "DemoProject")
  private String name;

  @ApiModelProperty(value = "Project avatar")
  @Length(max = DEFAULT_URL_LENGTH_X2)
  private String avatar;

  @ApiModelProperty(value = "Owner id", example = "1")
  private Long ownerId;

  @ApiModelProperty(value = "Project start date, Determine the start times of the research and testing activities"
      + " to ensure completion within the project cycle.", example = "2023-06-10 00:00:00")
  private LocalDateTime startDate;

  @ApiModelProperty(value = "Project deadline date, Determine the end times of the research and testing activities"
      + " to ensure completion within the project cycle.", example = "2029-06-20 00:00:00")
  private LocalDateTime deadlineDate;

  @ApiModelProperty(value = "Project description")
  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  private String description;

  @ApiModelProperty(value = "Project member type and ids")
  private LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds;
}




