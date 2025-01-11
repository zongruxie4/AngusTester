package cloud.xcan.sdf.core.angustester.domain.indicator;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.Percentile;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.sdf.spec.unit.TimeValue;
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
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;


@Entity
@Table(name = "indicator_stability")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class IndicatorStability extends TenantEntity<IndicatorStability, Long> {

  @Id
  private Long id;

  @Column(name = "target_id")
  private Long targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type")
  private CombinedTargetType targetType;

  private Integer threads;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "duration")
  private TimeValue duration;

  private Long art;

  @Enumerated(EnumType.STRING)
  private Percentile percentile;

  private Integer tps;

  @Column(name = "error_rate")
  private Double errorRate;

  private Double cpu;

  private Double memory;

  private Double disk;

  private Double network;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  private Long createdBy;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  private LocalDateTime createdDate;

  @Transient
  private String targetName;

  @Override
  public Long identity() {
    return this.id;
  }

}
