package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.apis.log;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.spec.http.HttpHeader;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Setter
@Getter
@Accessors(chain = true)
public class MockApisLogDetailVo {

  private Long id;

  private String requestId;

  private String remote;

  private Long mockApisId;

  private String summary;

  private Long mockServiceId;

  @NameJoinField(id = "mockServiceId", repository = "mockApisLogInfoRepo")
  private String mockServiceName;

  private String method;

  private String endpoint;

  private Boolean pushbackFlag;

  private String pushbackRequestId;

  private String queryParameters;

  private List<HttpHeader> requestHeaders;

  private String requestContentEncoding;

  private String requestBody;

  private LocalDateTime requestDate;

  private int responseStatus;

  private List<HttpHeader> responseHeaders;

  private String responseBody;

  private LocalDateTime responseDate;

  private LocalDateTime createdDate;

}
