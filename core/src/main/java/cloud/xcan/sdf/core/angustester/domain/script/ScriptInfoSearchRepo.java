package cloud.xcan.sdf.core.angustester.domain.script;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptInfoSearchRepo extends CustomBaseRepository<ScriptInfo> {

}
