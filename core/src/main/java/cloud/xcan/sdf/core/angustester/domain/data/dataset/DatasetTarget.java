package cloud.xcan.sdf.core.angustester.domain.data.dataset;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.domain.BaseTarget;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import java.time.LocalDateTime;
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
