package cloud.xcan.angus.core.tester.domain.test.favourite;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAware;
import cloud.xcan.angus.core.jpa.multitenancy.TenantListener;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import cloud.xcan.angus.spec.experimental.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;


@MultiTenant
@Entity
@Table(name = "func_case_favourite")
@EntityListeners({TenantListener.class, AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseFavourite extends EntitySupport<FuncCaseFavourite, Long>
    implements TenantAware {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "case_id")
  private Long caseId;

  @Column(name = "tenant_id")
  private Long tenantId;

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
