package cloud.xcan.sdf.core.angustester.domain.indicator;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.gm.indicator.SecurityCheckSetting;
import cloud.xcan.sdf.api.gm.indicator.SmokeCheckSetting;
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

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "user_defined_smoke_assertion")
  private Assertion<HttpExtraction> userDefinedSmokeAssertion;

  private boolean security;

  @Enumerated(EnumType.STRING)
  @Column(name = "security_check_setting")
  private SecurityCheckSetting securityCheckSetting;

  @Type(type = "json")
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
