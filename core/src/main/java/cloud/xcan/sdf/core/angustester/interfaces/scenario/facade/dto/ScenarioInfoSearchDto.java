package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioPermission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@Setter
@Getter
@ApiModel
public class ScenarioInfoSearchDto extends PageQuery {

  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  @NotNull
  @ApiModelProperty(value = "Project id", example = "1", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String plugin;

  private ScriptType scriptType;

  @ApiModelProperty(value = "Required when app administrators query all scenarios")
  private Boolean adminFlag;

  @ApiModelProperty(value = "Required when the user query has the one permission scenario")
  private ScenarioPermission hasPermission;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  private Long favouriteBy;

  private Long followBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



