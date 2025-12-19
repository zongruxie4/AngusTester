package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases;

import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.CaseTestMethod;
import cloud.xcan.angus.remote.PageQuery;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ApisCaseFindDto extends PageQuery {

  @Schema(description = "Test case identifier for precise query")
  private Long id;

  @Schema(description = "Project identifier for test case query scope definition")
  private Long projectId;

  @Schema(description = "Service identifier for test case filtering")
  private Long serviceId;

  @Schema(description = "API identifier for test case filtering")
  private Long apisId;

  @Schema(description = "Test case name for fuzzy search and filtering")
  private String name;

  @Schema(description = "Test case enablement flag for filtering")
  private Boolean enabled;

  @Schema(description = "API test case type for classification filtering")
  private ApisCaseType type;

  @Schema(description = "API test case method for specification filtering")
  private CaseTestMethod testMethod;

  @Schema(description = "API protocol for communication type filtering")
  private ApisProtocol protocol;

  @Schema(description = "HTTP method for request specification filtering")
  private HttpMethod method;

  @Schema(description = "API endpoint path for resource identification filtering")
  private String endpoint;

}



