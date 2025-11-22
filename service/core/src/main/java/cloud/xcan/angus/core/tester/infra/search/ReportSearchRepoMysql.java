package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.report.ReportInfo;
import cloud.xcan.angus.core.tester.domain.report.ReportSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ReportSearchRepoMysql extends SimpleSearchRepository<ReportInfo>
    implements ReportSearchRepo {

}
