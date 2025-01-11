package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ServicesSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Services name")
  private String name;

  @ApiModelProperty(value = "Required when app administrators query all projects")
  private Boolean adminFlag;

  @ApiModelProperty(value = "Required when the user query has the one permission project")
  private ServicesPermission hasPermission;

  private Boolean queryHasApisFlag;

  private Boolean queryHasMockServiceFlag;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}




