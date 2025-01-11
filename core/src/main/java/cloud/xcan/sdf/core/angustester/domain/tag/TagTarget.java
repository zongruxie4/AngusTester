package cloud.xcan.sdf.core.angustester.domain.tag;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantListener;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "tag_target")
@EntityListeners({TenantListener.class, AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class TagTarget extends TenantEntity<TagTarget, Long> {

  @Id
  private Long id;

  @Column(name = "tag_id")
  private Long tagId;

  @Column(name = "target_type")
  @Enumerated(EnumType.STRING)
  private CombinedTargetType targetType;

  @Column(name = "target_id")
  private Long targetId;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  private Long createdBy;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime createdDate;

  @Transient
  private String targetName;
  @Transient
  private String tagName;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public boolean sameIdentityAs(TagTarget other) {
    return Objects.equals(this.tagId, other.tagId) && Objects.equals(this.targetId, other.targetId);
  }
}