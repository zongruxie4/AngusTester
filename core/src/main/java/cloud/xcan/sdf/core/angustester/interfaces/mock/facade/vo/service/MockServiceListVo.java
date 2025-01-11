package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceSource;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceStatus;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class MockServiceListVo {

  private Long id;

  private Long projectId;

  private String name;

  private MockServiceSource source;

  private MockServiceStatus status;

  private Long nodeId;

  @NameJoinField(id = "nodeId", repository = "nodeRepo")
  private String nodeName;

  private String nodeIp;

  private String nodePublicIp;

  private Integer servicePort;

  private String serviceDomainUrl;

  private String serviceHostUrl;

  private boolean authFlag;

  @ApiModelProperty(value = "Current user authorization information")
  private Set<MockServicePermission> currentAuths;

  @ApiModelProperty(value = "Whether the angus services is associated")
  private boolean assocServicesFlag;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;
}
