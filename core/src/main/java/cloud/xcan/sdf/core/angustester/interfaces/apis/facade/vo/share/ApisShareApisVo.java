package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ApisShareApisVo {

  private Long id;

  private ApiSource source;

  private ApiImportSource importSource;

  private String method;

  private String endpoint;

  private String summary;

  private String operationId;

  private Boolean deprecated;

  private ApiStatus status;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private Long ownerId;

  @NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  private String ownerName;

  private LocalDateTime createdDate;

  private LocalDateTime lastModifiedDate;
}
