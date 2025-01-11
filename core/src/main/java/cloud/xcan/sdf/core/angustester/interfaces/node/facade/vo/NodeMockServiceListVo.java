package cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceSource;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
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

  private boolean authFlag;

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
