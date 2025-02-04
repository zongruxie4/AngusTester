package cloud.xcan.sdf.core.angustester.domain.services.comp;


import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Entity
@Table(name = "services_comp")
@EntityListeners({AuditingEntityListener.class})
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class ServicesComp extends TenantEntity<ServicesComp, Long> {

  @Id
  private Long id;

  @Column(name = "service_id")
  private Long serviceId;

  @Enumerated(EnumType.STRING)
  private ServicesCompType type;

  @Column(name = "`key`")
  private String key;

  private String ref;

  private String model;

  private String description;

  @Column(name = "schema_hash")
  private int schemaHash;

  @Column(name = "last_modified_by")
  @LastModifiedBy
  private Long lastModifiedBy;

  @Column(name = "last_modified_date")
  @LastModifiedDate
  private LocalDateTime lastModifiedDate;

  // @Transient -> transient <- Json and jpa both ignore
  @JsonIgnore
  public transient Schema schema;
  @JsonIgnore
  public transient ApiResponse response;
  @JsonIgnore
  public transient Parameter parameter;
  @JsonIgnore
  public transient Example example;
  @JsonIgnore
  public transient RequestBody requestBody;
  @JsonIgnore
  public transient Header header;
  @JsonIgnore
  public transient SecurityScheme securityScheme;
  @JsonIgnore
  public transient Link link;
  //public transient Callback callback;
  @JsonIgnore
  public transient Object extension;
  //@OpenAPI31
  //public transient PathItem pathItem;
  @JsonIgnore
  private transient Map<String, String> resolvedRefModels;

  @SneakyThrows
  public <T> T toComponent(Class<T> clz) {
    return isEmpty(model) ? null : OPENAPI_MAPPER.readValue(model, clz);
  }

  @Override
  public Long identity() {
    return this.id;
  }

  /**
   * Use schemaHash() instead.
   */
  @Deprecated
  public boolean sameSchemaInfoAs(ServicesComp o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return serviceId.equals(o.serviceId) && type == o.type &&
        Objects.equals(schema, o.schema) &&
        Objects.equals(response, o.response) &&
        Objects.equals(parameter, o.parameter) &&
        Objects.equals(example, o.example) &&
        Objects.equals(requestBody, o.requestBody) &&
        Objects.equals(header, o.header) &&
        Objects.equals(securityScheme, o.securityScheme) &&
        Objects.equals(link, o.link) &&
        Objects.equals(extension, o.extension);
  }
}
