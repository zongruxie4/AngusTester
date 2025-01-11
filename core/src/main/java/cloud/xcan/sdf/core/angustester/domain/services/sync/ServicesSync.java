package cloud.xcan.sdf.core.angustester.domain.services.sync;


import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import java.time.LocalDateTime;
import java.util.List;
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
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "services_sync")
@EntityListeners({AuditingEntityListener.class})
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class ServicesSync extends TenantEntity<ServicesSync, Long> {

  @Id
  private Long id;

  @Column(name = "service_id")
  private Long serviceId;

  private String name;

  @Column(name = "api_docs_url")
  private String apiDocsUrl;

  @Column(name = "strategy_when_duplicated")
  @Enumerated(EnumType.STRING)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @Column(name = "delete_when_not_existed")
  private Boolean deleteWhenNotExisted;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "auths")
  private List<SimpleHttpAuth> auths;

  @Column(name = "sync_success_flag")
  private Boolean syncSuccessFlag;

  /**
   * Note:: Maximum cut out 200 characters the cause.
   */
  @Column(name = "sync_failure_cause")
  private String syncFailureCause;

  @Column(name = "last_sync_date")
  private LocalDateTime lastSyncDate;

  @Column(name = "last_modified_by")
  @LastModifiedBy
  private Long lastModifiedBy;

  @Column(name = "last_modified_date")
  @LastModifiedDate
  private LocalDateTime lastModifiedDate;

  @Override
  public boolean sameIdentityAs(ServicesSync other) {
    return this.name.equalsIgnoreCase(other.name);
  }

  @Override
  public Long identity() {
    return this.id;
  }

}
