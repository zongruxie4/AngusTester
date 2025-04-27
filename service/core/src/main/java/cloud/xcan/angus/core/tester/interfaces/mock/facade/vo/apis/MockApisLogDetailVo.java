package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis;


import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Setter
@Getter
public class MockApisLogDetailVo {

  protected Long tenantId;
  private Long id;
  private String remote;

  private Long apiId;

  private String method;

  private String protocol;

  private String endpoint;

  private String url;

  private Integer status;

  private LocalDateTime requestDate;

  private Map<String, String> queryParam;

  private Map<String, String> requestHeaders;

  private String requestBody;

  private Map<String, String> responseHeaders;


  private Integer responseSize;

  private LocalDateTime responseDate;

  private String responseBody;

  private LocalDateTime createdDate;


}



