package cloud.xcan.angus.core.tester.domain.test.template;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TestTemplateRepo extends BaseRepository<TestTemplate, Long> {

}
