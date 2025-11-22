package cloud.xcan.angus.core.tester.domain.report;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReportSearchRepo extends CustomBaseRepository<ReportInfo> {

}
