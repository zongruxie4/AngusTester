package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.IPV4_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceSource;
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
public class MockServiceSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project ID, required when creating a service", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @Length(max = IPV4_LENGTH)
  private String nodeIp;

  private Long nodeId;

  private MockServiceSource source;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
