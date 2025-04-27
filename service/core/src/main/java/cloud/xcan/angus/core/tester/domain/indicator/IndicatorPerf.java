package cloud.xcan.angus.core.tester.domain.indicator;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.spec.unit.TimeValue;
import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;


@Entity
@Table(name = "indicator_perf")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class IndicatorPerf extends TenantEntity<IndicatorPerf, Long> {

  @Id
  private Long id;

  @Column(name = "target_id")
  private Long targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type")
  private CombinedTargetType targetType;

  private Integer threads;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "duration")
  private TimeValue duration;

  @Column(name = "ramp_up_threads")
  private Integer rampUpThreads;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "ramp_up_interval")
  private TimeValue rampUpInterval;

  private Long art;

  @Enumerated(EnumType.STRING)
  private Percentile percentile;

  private Integer tps;

  @Column(name = "error_rate")
  private Double errorRate;

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
