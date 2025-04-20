package cloud.xcan.angus.core.tester.domain.func.cases;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseInfoSearchRepo extends CustomBaseRepository<FuncCaseInfo> {

}
