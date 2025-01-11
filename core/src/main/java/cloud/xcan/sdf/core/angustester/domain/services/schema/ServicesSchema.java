package cloud.xcan.sdf.core.angustester.domain.services.schema;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.ExtensionsConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.ExternalDocConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.InfoConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.SecurityRequirementConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.ServersConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.TagsConverter;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Entity
@Table(name = "services_schema")
@EntityListeners({AuditingEntityListener.class})
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class ServicesSchema extends TenantEntity<ServicesSchema, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "service_id")
  private Long serviceId;

  private String openapi;

  @Convert(converter = InfoConverter.class)
  @Column(name = "info")
  private Info info;

  @Convert(converter = ExternalDocConverter.class)
  @Column(name = "external_docs")
  private ExternalDocumentation externalDocs;

  @Convert(converter = ServersConverter.class)
  @Column(name = "servers")
  private List<Server> servers = new ArrayList<>();

  @Convert(converter = SecurityRequirementConverter.class)
  @Column(name = "security")
  private List<SecurityRequirement> security = new ArrayList<>();

  @Convert(converter = TagsConverter.class)
  @Column(name = "tags")
  private List<Tag> tags = new ArrayList<>();

  @Convert(converter = ExtensionsConverter.class)
  @Column(name = "extensions")
  private Map<String, Object> extensions = new HashMap<>();

  @Column(name = "spec_version")
  private SpecVersion specVersion;

  @Column(name = "last_modified_by")
  @LastModifiedBy
  private Long lastModifiedBy;

  @Column(name = "last_modified_date")
  @LastModifiedDate
  private LocalDateTime lastModifiedDate;

  @Override
  public Long identity() {
    return this.id;
  }

  @JsonIgnore
  @Override
  public String getName() {
    return nonNull(info) ? info.getTitle() : "OpenAPI";
  }

  @JsonIgnore
  @Override
  public Long getParentId() {
    return DEFAULT_ROOT_PID;
  }
}
