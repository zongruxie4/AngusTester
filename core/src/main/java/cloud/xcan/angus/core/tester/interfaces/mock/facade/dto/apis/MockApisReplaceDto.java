package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class MockApisReplaceDto {

  //@NotNull
  @Schema(description = "Mock apis id", example = "1"/*, requiredMode = RequiredMode.REQUIRED*/)
  private Long id;

  //Modifications are not allowed
  @NotNull
  @Schema(description = "Mock service id", requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  @NotEmpty
  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "Mock api name", example = "Query users", requiredMode = RequiredMode.REQUIRED)
  private String summary;

  @Schema(description = "Mock api detailed description")
  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  private String description;

  @NotNull
  @Schema(description = "Mock api method", example = "GET", requiredMode = RequiredMode.REQUIRED)
  private HttpMethod method;

  @NotEmpty
  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "Mock api endpoint", example = "/api/v1/user", requiredMode = RequiredMode.REQUIRED)
  private String endpoint;

}
