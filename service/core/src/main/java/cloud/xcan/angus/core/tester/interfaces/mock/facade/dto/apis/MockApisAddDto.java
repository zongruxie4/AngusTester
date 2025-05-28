package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class MockApisAddDto {

  @NotNull
  @Schema(description = "Mock service id", requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  @NotBlank
  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "Mock api name", example = "Query user list", requiredMode = RequiredMode.REQUIRED)
  private String summary;

  @Schema(description = "Mock api detailed description")
  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  private String description;

  @NotNull
  @Schema(description = "Mock api method", example = "GET", requiredMode = RequiredMode.REQUIRED)
  private HttpMethod method;

  @NotBlank
  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "Mock api endpoint", example = "/api/v1/user", requiredMode = RequiredMode.REQUIRED)
  private String endpoint;

}
