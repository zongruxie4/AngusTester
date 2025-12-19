package cloud.xcan.angus.core.tester.domain.services.schema;


import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.converter.ExtensionsConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ExternalDocConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.InfoConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.SecurityRequirementConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ServersConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.TagsConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private Long projectId;

  private Long serviceId;

  private String openapi;

  @Convert(converter = InfoConverter.class)
  private Info info;

  @Convert(converter = ExternalDocConverter.class)
  private ExternalDocumentation externalDocs;

  @Convert(converter = ServersConverter.class)
  private List<Server> servers = new ArrayList<>();

  @Convert(converter = SecurityRequirementConverter.class)
  private List<SecurityRequirement> security = new ArrayList<>();

  @Convert(converter = TagsConverter.class)
  private List<Tag> tags = new ArrayList<>();

  @Convert(converter = ExtensionsConverter.class)
  private Map<String, Object> extensions = new HashMap<>();

  @Enumerated(EnumType.STRING)
  private SpecVersion specVersion;

  @LastModifiedBy
  private Long modifiedBy;

  @LastModifiedDate
  private LocalDateTime modifiedDate;

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
