package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.design;

import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignSource;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ApisDesignDetailVo {

  private Long id;

  private Long projectId;

  private String name;

  private Boolean release;

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
