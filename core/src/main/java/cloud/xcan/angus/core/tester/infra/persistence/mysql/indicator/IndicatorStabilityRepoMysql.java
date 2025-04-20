package cloud.xcan.angus.core.tester.infra.persistence.mysql.indicator;

import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilityRepo;
import org.springframework.stereotype.Repository;

@Repository("indicatorStabilityRepo")
public interface IndicatorStabilityRepoMysql extends IndicatorStabilityRepo {


}
