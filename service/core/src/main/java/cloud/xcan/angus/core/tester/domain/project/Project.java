package cloud.xcan.angus.core.tester.domain.project;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "project")
@SQLRestriction("deleted = 0")
@SQLDelete(sql = "update project set deleted = 1 where id = ?")
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class Project extends TenantAuditingEntity<Project, Long> implements ActivityResource {

  @Id
  private Long id;

  @Nullable
  @Enumerated(EnumType.STRING)
  private ProjectType type;

  private String name;

  private String avatar;

  private String description;

  @Column(name = "owner_id")
  private Long ownerId;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "deadline_date")
  private LocalDateTime deadlineDate;

  @Column(name = "version")
  private String version;

  private Boolean deleted;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted_date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deletedDate;

  @Transient
  private String ownerName;
  @Transient
  private String ownerAvatar;

  @Transient
  private LinkedHashMap<OrgTargetType, LinkedHashSet<OrgTargetInfo>> members;
  @Transient
  private LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds;

  @Transient
  private boolean importExample = false;

  public boolean isAgile() {
    return isNull(type) || type.isAgile();
  }

  @Override
  public Long getParentId() {
    return -1L;
  }

  @Override
  public Long getProjectId() {
    return id; // For activity project name
  }

  @Override
  public Long identity() {
    return this.id;
  }


}
