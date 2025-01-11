package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

/**
 * @author xiaolong.liu
 */
public interface ApisUnarchivedCmd {

  List<IdKey<Long, Object>> add(List<ApisUnarchived> apis);

  void update(List<ApisUnarchived> apis);

  void delete(Long id, boolean deleteAllFlag);

  void rename(Long id, String name);
}
