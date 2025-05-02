package cloud.xcan.angus.core.tester.domain.exec;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExecInfoSearchRepo extends CustomBaseRepository<ExecInfo> {

}
