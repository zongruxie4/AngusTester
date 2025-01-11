package cloud.xcan.sdf.model.remoting.vo;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.element.mock.apis.MatchRequest;
import cloud.xcan.angus.model.element.mock.apis.MockResponseContent;
import cloud.xcan.angus.model.element.mock.apis.MockResponsePushback;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockApiResponseInfoVo {

  private String name;

  private MatchRequest match;

  private MockResponseContent content;

  private MockResponsePushback pushback;

  @JsonIgnore
  private boolean matchExceptionResponse;

  /**
   * Used by mock service
   */
  public boolean hasResponseHeader() {
    return nonNull(content) && isNotEmpty(content.getHeaders());
  }

}
