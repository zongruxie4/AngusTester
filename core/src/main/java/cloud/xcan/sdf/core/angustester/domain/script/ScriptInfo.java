package cloud.xcan.sdf.core.angustester.domain.script;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.model.script.ScriptSource;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xiaolong.liu
 */
@Entity
@Table(name = "script")
@Setter
@Getter
@Accessors(chain = true)
public class ScriptInfo extends TenantAuditingEntity<ScriptInfo, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "service_id")
  private Long serviceId;

  private String name;

  @Enumerated(EnumType.STRING)
  private ScriptType type;

  @Enumerated(EnumType.STRING)
  private ScriptSource source;

  @Column(name = "source_id")
  private Long sourceId;

  @Column(name = "auth_flag")
  private Boolean authFlag;

  private String plugin;

  private String description;

  /**
   * Search for ID
   */
  @Column(name = "ext_search_merge")
  private String extSearchMerge;

  @Transient
  private String sourceName;
  @Transient
  private Long execId;
  @Transient
  private String execName;
  @Transient
  private MultipartFile file;
  @Transient
  private List<String> tags;
  @Transient
  private Set<ScriptPermission> permissions;

  public ScriptInfo() {
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }

  public boolean isEnabledAuth() {
    return Objects.nonNull(authFlag) && authFlag;
  }
}
