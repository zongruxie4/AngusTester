package cloud.xcan.angus.core.tester.application.cmd.apis;

import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import java.util.List;

public interface ApisTrashCmd {

  void add0(List<ApisTrash> trashes);

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);
}




