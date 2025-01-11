package cloud.xcan.sdf.core.angustester.domain.report;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReportInfoRepo extends BaseRepository<ReportInfo, Long> ,
    NameJoinRepository<ReportInfo, Long> {

}
