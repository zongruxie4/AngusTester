package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class MockApisUpdateDto {

  @NotNull
  @Schema(description = "Mock API identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //@NotNull
  //@Schema(description = "Mock service id", requiredMode = RequiredMode.REQUIRED)
  //private Long mockServiceId;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "Mock API name for identification and display", example = "Query users")
  private String summary;

  @Schema(description = "Mock API detailed description for comprehensive documentation")
  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  private String description;

  @NotNull // Required by MockApisQueryImpl#checkUpdateOperationExists()
  @Schema(description = "HTTP method for API request simulation", example = "GET", requiredMode = RequiredMode.REQUIRED)
  private HttpMethod method;

  @NotEmpty // Required by MockApisQueryImpl#checkUpdateOperationExists()
  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "Mock API endpoint path for request routing", example = "/api/v1/user", requiredMode = RequiredMode.REQUIRED)
  private String endpoint;

}
