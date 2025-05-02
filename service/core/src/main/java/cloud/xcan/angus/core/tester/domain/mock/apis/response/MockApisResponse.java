package cloud.xcan.angus.core.tester.domain.mock.apis.response;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.model.element.mock.apis.MatchRequest;
import cloud.xcan.angus.model.element.mock.apis.MockResponseContent;
import cloud.xcan.angus.model.element.mock.apis.MockResponsePushback;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;


/**
 * @author XiaoLong Liu
 */
@Entity
@Table(name = "mock_apis_response")
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

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "`match`")
  private MatchRequest match;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "content")
  private MockResponseContent content;

  @Column(name = "enable_pushback")
  private Boolean enablePushback;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "pushback")
  private MockResponsePushback pushback;

  @Override
  public Long identity() {
    return this.id;
  }

  public MockApisResponse copy() {
    return new MockApisResponse(this.id, this.projectId, this.mockServiceId, this.mockApisId,
        this.name, this.match, this.content, this.enablePushback, this.pushback);
  }
}
