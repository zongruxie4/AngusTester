package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisDesignSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project ID", required = true)
  private Long projectId;

  private String name;

  private Boolean releaseFlag;

  private Long tenantId;

  private Long createdBy;

  private LocalDateTime createdDate;

  protected Long lastModifiedBy;

  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
