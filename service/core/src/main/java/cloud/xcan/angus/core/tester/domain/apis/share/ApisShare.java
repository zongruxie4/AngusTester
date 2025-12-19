package cloud.xcan.angus.core.tester.domain.apis.share;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting.UserApiProxyVo;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "apis_share")
@Setter
@Getter
@Accessors(chain = true)
public class ApisShare extends TenantAuditingEntity<ApisShare, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "name")
  private String name;

  private String remark;

  @Column(name = "expired_date")
  private LocalDateTime expiredDate;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "display_options")
  private DisplayOptions displayOptions;

  @Enumerated(EnumType.STRING)
  @Column(name = "share_scope")
  private ApisShareScope shareScope;

  @Column(name = "services_id")
  private Long servicesId;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "apis_ids")
  private Set<Long> apisIds;

  @Column(name = "base_url")
  private String baseUrl;

  /**
   * Share public access token.
   */
  private String pat;

  @Column(name = "view_num")
  private Integer viewNum;

  @Transient
  private UserApiProxyVo apiProxy;
  @Transient
  private String openapi;
  @Transient
  private String url;
  @Transient
  private String creator;
  @Transient
  private String avatar;

  public Boolean isNotExpired() {
    return Objects.isNull(expiredDate) || expiredDate.isAfter(LocalDateTime.now());
  }

  @Override
  public Long identity() {
    return this.id;
  }
}
