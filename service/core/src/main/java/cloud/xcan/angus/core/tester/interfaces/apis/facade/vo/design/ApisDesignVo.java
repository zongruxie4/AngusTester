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
public class ApisDesignVo {

  private Long id;

  private Long projectId;

  private String name;

  private Boolean released;

  private String openapiSpecVersion;

  private ApisDesignSource designSource;

  private Long designSourceId;

  private String designSourceName;

  private Long tenantId;

  private Long createdBy;

  private String creator;

  private String creatorAvatar;

  private LocalDateTime createdDate;

  protected Long modifiedBy;

  @NameJoinField(id = "modifiedBy", repository = "commonUserBaseRepo")
  private String modifier;

  private LocalDateTime modifiedDate;

}
