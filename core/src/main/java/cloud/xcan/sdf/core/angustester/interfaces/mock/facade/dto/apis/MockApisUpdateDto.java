package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;

import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
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
public class MockApisUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Mock apis id", example = "1", required = true)
  private Long id;

  //@NotNull
  //@ApiModelProperty(value = "Mock service id", example = "1", required = true)
  //private Long mockServiceId;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @ApiModelProperty(value = "Mock api name", example = "Query users")
  private String summary;

  @ApiModelProperty(value = "Mock api detailed description")
  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  private String description;

  @NotNull // Required by MockApisQueryImpl#checkUpdateOperationExists()
  @ApiModelProperty(value = "Mock api method", example = "GET", required = true)
  private HttpMethod method;

  @NotEmpty // Required by MockApisQueryImpl#checkUpdateOperationExists()
  @Length(max = DEFAULT_URL_LENGTH_X4)
  @ApiModelProperty(value = "Mock api endpoint", example = "/api/v1/user", required = true)
  private String endpoint;

}