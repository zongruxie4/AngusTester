package cloud.xcan.sdf.core.angustester.domain.data.dataset;


import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.spec.annotations.SpecIgnore;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(name = "data_dataset")
@TypeDef(name = "json", typeClass = JsonStringType.class)
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

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "parameters")
  private List<DatasetParameter> parameters;

  @Type(type = "json")
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
