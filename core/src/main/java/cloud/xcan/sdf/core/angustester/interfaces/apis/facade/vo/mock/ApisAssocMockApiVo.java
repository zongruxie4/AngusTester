package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.mock;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisSource;
import cloud.xcan.sdf.spec.http.HttpMethod;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ApisAssocMockApiVo {

  private Long id;

  private String name;

  private Long mockServiceId;

  private String mockServiceName;

  private Long parentMockServiceId;

  private String parentMockServiceName;

  private String mockServiceDomainUrl;

  private String mockServiceHostUrl;

  private HttpMethod method;

  private String endpoint;

  private MockApisSource source;

  private ApiImportSource importSource;

  private Long requestNum;

  private Long pushbackNum;

  private Long simulateErrorNum;

  private Long successNum;

  private Long exceptionNum;

  private Boolean inconsistentOperationFlag;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;


}
