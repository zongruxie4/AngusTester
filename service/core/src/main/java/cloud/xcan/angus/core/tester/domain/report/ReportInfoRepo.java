package cloud.xcan.angus.core.tester.domain.report;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReportInfoRepo extends BaseRepository<ReportInfo, Long>,
    NameJoinRepository<ReportInfo, Long> {

}
