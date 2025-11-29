package cloud.xcan.angus.core.tester.domain.config.tenant;


import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.core.tester.domain.config.converter.FuncDataConverter;
import cloud.xcan.angus.core.tester.domain.config.converter.PerfDataConverter;
import cloud.xcan.angus.core.tester.domain.config.converter.ServerApiProxyDataConverter;
import cloud.xcan.angus.core.tester.domain.config.converter.StabilityDataConverter;
import cloud.xcan.angus.core.tester.domain.config.converter.TesterEventConverter;
import cloud.xcan.angus.core.tester.domain.config.indicator.FuncData;
import cloud.xcan.angus.core.tester.domain.config.indicator.PerfData;
import cloud.xcan.angus.core.tester.domain.config.indicator.StabilityData;
import cloud.xcan.angus.core.tester.domain.config.tenant.apiproxy.ServerApiProxy;
import cloud.xcan.angus.core.tester.domain.config.tenant.event.TesterEvent;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Except for tree and hierarchy related restrictions, other quota restrictions are not written in
 * the code!! This makes it easy to modify after privatization deployment.
 *
 * @author XiaoLong Liu
 */
@Entity
@Table(name = "tester_setting_tenant")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Setter
@Getter
@Accessors(chain = true)
public class SettingTenant extends TenantEntity<SettingTenant, Long> {

  @Id
  private Long id;

  @Column(name = "func_data", columnDefinition = "json")
  @Convert(converter = FuncDataConverter.class)
  private FuncData funcData;

  @Column(name = "perf_data", columnDefinition = "json")
  @Convert(converter = PerfDataConverter.class)
  private PerfData perfData;

  @Column(name = "stability_data")
  @Convert(converter = StabilityDataConverter.class)
  private StabilityData stabilityData;

  @Column(name = "tester_event_data")
  @Convert(converter = TesterEventConverter.class)
  private List<TesterEvent> testerEventData;
  ////////////////////////////Automatic initialization//////////////////////////////

  @Column(name = "server_api_proxy_data")
  @Convert(converter = ServerApiProxyDataConverter.class)
  private ServerApiProxy serverApiProxyData;

  @Override
  public Long identity() {
    return this.id;
  }
}
