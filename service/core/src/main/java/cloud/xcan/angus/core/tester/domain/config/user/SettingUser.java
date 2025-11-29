package cloud.xcan.angus.core.tester.domain.config.user;

import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.core.tester.domain.config.tenant.SettingTenant;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.UserApiProxy;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "tester_setting_user")
@Setter
@Getter
@Accessors(chain = true)
public class SettingUser extends TenantEntity<SettingTenant, Long> {

  @Id
  private Long id;

  /**
   * The server proxy may be modified. Use {@link SettingTenant#getServerApiProxyData()} overwrite
   * server proxy.
   */
  @Type(JsonType.class)
  @Column(name = "api_proxy", columnDefinition = "json")
  private UserApiProxy apiProxy;

  @Transient
  private UserApiProxy apiProxyRefresh;

  @Override
  public Long identity() {
    return id;
  }

}
