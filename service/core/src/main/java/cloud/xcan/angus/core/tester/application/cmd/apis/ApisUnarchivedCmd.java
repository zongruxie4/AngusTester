package cloud.xcan.angus.core.tester.application.cmd.apis;

import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

/**
 * @author XiaoLong Liu
 */
public interface ApisUnarchivedCmd {

  List<IdKey<Long, Object>> add(List<ApisUnarchived> apis);

  void update(List<ApisUnarchived> apis);

  void delete(Long id, boolean deleteAll);

  void rename(Long id, String name);
}
