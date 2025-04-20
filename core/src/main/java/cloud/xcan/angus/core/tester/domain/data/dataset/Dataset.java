package cloud.xcan.angus.core.tester.domain.data.dataset;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.angus.spec.annotations.SpecIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;


@Entity
@Table(name = "data_dataset")
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class Dataset extends TenantAuditingEntity<Dataset, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  private String name;

  private String description;

  private Boolean extracted;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "parameters")
  private List<DatasetParameter> parameters;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "extraction")
  private DefaultExtraction extraction;

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long getProjectId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return id;
  }

  @SpecIgnore
  public boolean isExtractable() {
    return extracted && nonNull(extraction) && isNotEmpty(parameters);
  }
}
