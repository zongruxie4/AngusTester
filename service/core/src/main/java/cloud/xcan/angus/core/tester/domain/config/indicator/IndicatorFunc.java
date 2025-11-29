package cloud.xcan.angus.core.tester.domain.config.indicator;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
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
@Table(name = "indicator_func")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class IndicatorFunc extends TenantEntity<IndicatorFunc, Long> {

  @Id
  private Long id;

  @Column(name = "target_id")
  private Long targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type")
  private CombinedTargetType targetType;

  private boolean smoke;

  @Enumerated(EnumType.STRING)
  @Column(name = "smoke_check_setting")
  private SmokeCheckSetting smokeCheckSetting;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "user_defined_smoke_assertion")
  private Assertion<HttpExtraction> userDefinedSmokeAssertion;

  private boolean security;

  @Enumerated(EnumType.STRING)
  @Column(name = "security_check_setting")
  private SecurityCheckSetting securityCheckSetting;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "user_defined_security_assertion")
  private Assertion<HttpExtraction> userDefinedSecurityAssertion;

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
