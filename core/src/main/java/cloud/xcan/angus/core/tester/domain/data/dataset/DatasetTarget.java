package cloud.xcan.angus.core.tester.domain.data.dataset;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.core.tester.domain.BaseTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "data_dataset_target")
@EntityListeners({AuditingEntityListener.class})
@DynamicInsert
@Setter
@Getter
public class DatasetTarget extends TenantEntity<DatasetTarget, Long> implements BaseTarget {

  @Id
  private Long id;

  @Column(name = "dataset_id")
  private Long datasetId;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "target_id")
  private Long targetId;

  @Column(name = "target_type")
  @Enumerated(EnumType.STRING)
  private CombinedTargetType targetType;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  protected LocalDateTime createdDate;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  protected Long createdBy;

  @Transient
  private String targetName;

  @Override
  public Long identity() {
    return id;
  }
}
