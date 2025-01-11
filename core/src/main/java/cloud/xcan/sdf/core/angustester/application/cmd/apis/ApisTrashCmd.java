package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrash;
import java.util.List;

public interface ApisTrashCmd {

  void add0(List<ApisTrash> trashes);

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);
}




