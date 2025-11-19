package cloud.xcan.angus.core.tester.domain.test.template;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TestTemplateRepo extends BaseRepository<TestTemplate, Long> {

  long countByIsSystem(Boolean isSystem);

  List<TestTemplate> findByIsSystem(boolean isSystem);
}
