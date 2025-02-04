package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.model.script.ScriptSource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author xiaolong.liu
 */
@ApiModel
@Getter
@Setter
@Accessors(chain = true)
public class ScriptStatisticsDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  private ScriptType type;

  private ScriptSource source;

  private Long serviceId;

  private String sourceId;

  private Long scenarioId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String tag;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String plugin;

  @ApiModelProperty(value = "Required when app administrators query all scripts")
  private Boolean adminFlag;

  @ApiModelProperty(value = "Required when the user query has the one permission script")
  private ScriptPermission hasPermission;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

}
