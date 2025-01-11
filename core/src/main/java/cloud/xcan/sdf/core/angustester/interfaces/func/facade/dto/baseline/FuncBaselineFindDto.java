package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineFindDto extends PageQuery {

  private Long id;

  private String name;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  private Long planId;

  private Boolean establishedFlag;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

}
