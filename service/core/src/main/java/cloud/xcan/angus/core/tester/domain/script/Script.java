package cloud.xcan.angus.core.tester.domain.script;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author XiaoLong Liu
 */
@Entity
@Table(name = "script")
@Setter
@Getter
@Accessors(chain = true)
public class Script extends TenantAuditingEntity<Script, Long> implements ActivityResource {

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

  private Boolean auth;

  private String plugin;

  private String description;

  private String content;

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
  private AngusScript angusScript;
  @Transient
  private List<String> tags;
  @Transient
  private List<ScriptPermission> permissions;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }

  public boolean isEnabledAuth() {
    return Objects.nonNull(auth) && auth;
  }
}
