package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class DatasetFindDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



