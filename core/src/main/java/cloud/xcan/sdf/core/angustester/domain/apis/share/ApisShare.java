package cloud.xcan.sdf.core.angustester.domain.apis.share;

import cloud.xcan.sdf.api.gm.setting.vo.UserApiProxyVo;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;


@Entity
@Table(name = "apis_share")
@TypeDef(name = "json", typeClass = JsonStringType.class)
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

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "display_options")
  private DisplayOptions displayOptions;

  @Enumerated(EnumType.STRING)
  @Column(name = "share_scope")
  private ApisShareScope shareScope;

  @Column(name = "services_id")
  private Long servicesId;

  @Type(type = "json")
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

  public Boolean isNotExpired() {
    return Objects.isNull(expiredDate) || expiredDate.isAfter(LocalDateTime.now());
  }

  @Override
  public Long identity() {
    return this.id;
  }
}
