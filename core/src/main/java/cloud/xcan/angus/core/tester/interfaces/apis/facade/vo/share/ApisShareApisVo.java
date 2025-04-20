package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.NameJoinField;
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
