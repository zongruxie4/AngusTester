package cloud.xcan.angus.core.tester.domain.script;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptInfoSearchRepo extends CustomBaseRepository<ScriptInfo> {

}
