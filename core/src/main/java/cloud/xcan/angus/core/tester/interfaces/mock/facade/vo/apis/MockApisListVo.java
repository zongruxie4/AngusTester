package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.apis;


import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSource;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.spec.http.HttpMethod;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class MockApisListVo {

  private Long id;

  private Long projectId;

  private String summary;

  private String description;

  private MockApisSource source;

  private ApiImportSource importSource;

  private HttpMethod method;

  private String endpoint;

  private Long mockServiceId;

  private String mockServiceName;

  private String mockServiceDomainUrl;

  private String mockServiceHostUrl;

  private Long assocProjectId;

  private Long assocApisId;

  private long requestNum;

  private long pushbackNum;

  private long simulateErrorNum;

  private long successNum;

  private long exceptionNum;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;
}
