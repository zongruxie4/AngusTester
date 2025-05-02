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
@Valid
@Setter
@Getter
@Accessors(chain = true)
public class MockApisResponseReplaceDto {

  @NotEmpty
  @Schema(description = "Mock response name, the name must be unique.", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Valid
  @Schema(description =
      "Specifies the matching request conditions that need to be met in order to return the current response. "
          + "Note: When multiple responses that meet the conditions are matched, the one with the highest priority will be returned. If no matching conditions are configured or the priorities "
          + "are the same, the first one (the one configured first) will be returned.")
  private MatchRequest match;

  @Valid
  @NotNull
  @Schema(description = "The mock response content returned after receiving the request", requiredMode = RequiredMode.REQUIRED)
  private MockResponseContent content;

  @Valid
  @Schema(description = "Pushback request content returned after receiving the request")
  private MockResponsePushback pushback;

}
