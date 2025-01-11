package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.apis.response;

import cloud.xcan.angus.model.element.mock.apis.MatchRequest;
import cloud.xcan.angus.model.element.mock.apis.MockResponseContent;
import cloud.xcan.angus.model.element.mock.apis.MockResponsePushback;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Valid
@Setter
@Getter
@Accessors(chain = true)
public class MockApisResponseReplaceDto {

  @NotEmpty
  @ApiModelProperty(value = "Mock response name, the name must be unique.", required = true)
  private String name;

  @Valid
  @ApiModelProperty(value =
      "Specifies the matching request conditions that need to be met in order to return the current response. "
          + "Note: When multiple responses that meet the conditions are matched, the one with the highest priority will be returned. If no matching conditions are configured or the priorities "
          + "are the same, the first one (the one configured first) will be returned.")
  private MatchRequest match;

  @Valid
  @NotNull
  @ApiModelProperty(value = "The mock response content returned after receiving the request", required = true)
  private MockResponseContent content;

  @Valid
  @ApiModelProperty(value = "Pushback request content returned after receiving the request")
  private MockResponsePushback pushback;

}
