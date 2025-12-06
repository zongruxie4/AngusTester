package cloud.xcan.angus.core.tester.domain.project.template;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TemplateRepo extends BaseRepository<Template, Long> {

  long countByIsSystem(Boolean isSystem);

  List<Template> findByIsSystem(boolean isSystem);
}
