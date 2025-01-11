package cloud.xcan.sdf.core.angustester.domain.mock.apis.response;

import cloud.xcan.angus.model.element.mock.apis.MatchRequest;
import cloud.xcan.angus.model.element.mock.apis.MockResponseContent;
import cloud.xcan.angus.model.element.mock.apis.MockResponsePushback;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 * @author xiaolong.liu
 */
@Entity
@Table(name = "mock_apis_response")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Accessors(chain = true)
public class MockApisResponse extends TenantAuditingEntity<MockApisResponse, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "mock_service_id")
  private Long mockServiceId;

  @Column(name = "mock_apis_id")
  private Long mockApisId;

  private String name;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "`match`")
  private MatchRequest match;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "content")
  private MockResponseContent content;

  @Column(name = "pushback_flag")
  private Boolean pushbackFlag;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "pushback")
  private MockResponsePushback pushback;

  @Override
  public Long identity() {
    return this.id;
  }

  public MockApisResponse copy() {
    return new MockApisResponse(this.id, this.projectId, this.mockServiceId, this.mockApisId,
        this.name, this.match, this.content, this.pushbackFlag, this.pushback);
  }
}
