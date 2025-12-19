package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;


import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSource;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceStatus;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class NodeMockServiceListVo {

  private Long id;

  private Long projectId;

  private String name;

  private MockServiceSource source;

  private MockServiceStatus status;

  private Integer servicePort;

  private String serviceDomainUrl;

  private String serviceHostUrl;

  private boolean auth;

  @Schema(description = "Whether the angus services is associated")
  private boolean assocServices;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  private LocalDateTime createdDate;

  private Long modifiedBy;

  @NameJoinField(id = "modifiedBy", repository = "commonUserBaseRepo")
  private String modifier;

  private LocalDateTime modifiedDate;
}
