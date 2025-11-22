package cloud.xcan.angus.core.tester.interfaces.report.facade.vo;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.CreatedAt;
import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportStatus;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ReportListVo {

  private Long id;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private String name;

  public Boolean auth;

  private String version;

  private String description;

  private ReportCategory category;

  private ReportTemplate template;

  private ReportStatus status;

  private String failureMessage;

  private CreatedAt createdAt;

  private LocalDateTime nextGenerationDate;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  @Schema(description = "Current user authorization information")
  private Set<ReportPermission> currentAuths;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
