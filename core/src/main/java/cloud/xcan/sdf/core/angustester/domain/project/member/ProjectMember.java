package cloud.xcan.sdf.core.angustester.domain.project.member;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "project_members")
@EntityListeners({AuditingEntityListener.class})
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class ProjectMember extends TenantEntity<ProjectMember, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "member_type")
  @Enumerated(EnumType.STRING)
  private OrgTargetType memberType;

  @Column(name = "member_id")
  private Long memberId;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  protected LocalDateTime createdDate;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  protected Long createdBy;

  @Override
  public Long identity() {
    return this.id;
  }

}
