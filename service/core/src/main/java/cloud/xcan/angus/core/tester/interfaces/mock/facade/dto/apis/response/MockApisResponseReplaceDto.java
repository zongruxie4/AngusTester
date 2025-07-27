package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.apis.response;

import cloud.xcan.angus.model.element.mock.apis.MatchRequest;
import cloud.xcan.angus.model.element.mock.apis.MockResponseContent;
import cloud.xcan.angus.model.element.mock.apis.MockResponsePushback;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockApisResponseReplaceDto {

  @NotEmpty
  @Schema(description = "Mock response name with unique constraint for identification", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Valid
  @Schema(description = "Request matching conditions for response selection with priority-based resolution and first-come-first-served fallback")
  private MatchRequest match;

  @Valid
  @NotNull
  @Schema(description = "Mock response content configuration for request simulation", requiredMode = RequiredMode.REQUIRED)
  private MockResponseContent content;

  @Valid
  @Schema(description = "Pushback request configuration for advanced response simulation")
  private MockResponsePushback pushback;

}
