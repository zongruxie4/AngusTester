package cloud.xcan.angus.core.tester.domain.tag;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.core.jpa.multitenancy.TenantListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Objects;
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
