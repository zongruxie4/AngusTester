package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis.response;

import cloud.xcan.angus.model.element.mock.apis.MatchRequest;
import cloud.xcan.angus.model.element.mock.apis.MockResponseContent;
import cloud.xcan.angus.model.element.mock.apis.MockResponsePushback;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockApiResponseVo {

  private Long id;

  private Long apisId;

  private Long mockServiceId;

  private String name;

  private MatchRequest match;

  private MockResponseContent content;

  private MockResponsePushback pushback;

}
