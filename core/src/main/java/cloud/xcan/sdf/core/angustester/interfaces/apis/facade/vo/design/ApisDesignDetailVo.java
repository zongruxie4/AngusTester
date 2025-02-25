package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.design;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignSource;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisDesignDetailVo {

  private Long id;

  private Long projectId;

  private String name;

  private Boolean releaseFlag;

  private String openapiSpecVersion;

  private String openapi;

  private ApisDesignSource designSource;

  private Long designSourceId;

  private String designSourceName;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  protected Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
