package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSource;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.remote.PageQuery;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class MockApisFindDto extends PageQuery {

  @Schema(description = "Mock API identifier for precise query")
  private Long id;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "Mock API name for filtering")
  private String summary;

  @Schema(description = "Mock API source type for filtering")
  private MockApisSource source;

  @Schema(description = "API import source for filtering")
  private ApiImportSource importSource;

  @Schema(description = "HTTP method for filtering")
  private HttpMethod method;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "Mock API endpoint path for filtering")
  private String endpoint;

  @Schema(description = "Mock service identifier for scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  @Schema(description = "Associated project identifier for filtering")
  private Long assocProjectId;

  @Schema(description = "Associated API identifier for filtering")
  private Long assocApisId;

  @Schema(description = "Total request count for performance filtering")
  private Long requestNum;

  @Schema(description = "Total pushback count for performance filtering")
  private Long pushbackNum;

  @Schema(description = "Simulated error count for performance filtering")
  private Long simulateErrorNum;

  @Schema(description = "Success count for performance filtering")
  private Long successNum;

  @Schema(description = "Exception count for performance filtering")
  private Long exceptionNum;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
