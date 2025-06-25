package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share;

import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareScope;
import cloud.xcan.angus.core.tester.domain.apis.share.DisplayOptions;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ApisShareVo {

  private Long id;

  private String projectId;

  @Schema(description = "Share name")
  private String name;

  @Schema(description = "Share remark")
  private String remark;

  @Schema(description = "Share expired date")
  private LocalDateTime expiredDate;

  @Schema(description = "Share display options")
  private DisplayOptions displayOptions;

  @Schema(description = "Share scope")
  private ApisShareScope shareScope;

  @Schema(description = "Share services id")
  private Long servicesId;

  @Schema(description = "Share apis ids, it is required when share scope is apis")
  private Set<Long> apisIds;

  @Schema(description = "Web front end sharing page address")
  private String url;

  private Integer viewNum;

  private boolean isExpired;

  private Long tenantId;

  private Long createdBy;

  private String createdByName;

  private String createdByAvatar;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
