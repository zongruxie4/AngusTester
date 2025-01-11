package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.log;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_APIS_URI_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class MockApisLogFindDto extends PageQuery {

  @ApiModelProperty(value = "Mock apis id")
  private Long mockApisId;

  @ApiModelProperty(value = "Mock name")
  private String summary;

  private String method;

  @Length(max = MAX_APIS_URI_LENGTH)
  @ApiModelProperty(value = "Mock apis path")
  private String endpoint;

  private Integer status;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}