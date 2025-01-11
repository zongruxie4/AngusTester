package cloud.xcan.sdf.core.angustester.domain.project;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "project")
@Where(clause = "deleted_flag = 0")
@SQLDelete(sql = "update project set deleted_flag = 1 where id = ?")
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class Project extends TenantAuditingEntity<Project, Long> implements ActivityResource {

  @Id
  private Long id;

  @Enumerated(EnumType.STRING)
  private ProjectType type;

  @Column(name = "name")
  private String name;

  @Column(name = "avatar")
  private String avatar;

  @Column(name = "description")
  private String description;

  @Column(name = "owner_id")
  private Long ownerId;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "deadline_date")
  private LocalDateTime deadlineDate;

  @Column(name = "deleted_flag")
  private Boolean deletedFlag;

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
