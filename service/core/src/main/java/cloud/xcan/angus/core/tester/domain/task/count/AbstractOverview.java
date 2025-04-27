package cloud.xcan.angus.core.tester.domain.task.count;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MESSAGE_DATA_DETAIL;
import static cloud.xcan.angus.extraction.utils.PoiUtils.writeExcelData;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractOverview {

  @Schema(hidden = true)
  public abstract String[] getDataDetailTitles();

  @Schema(hidden = true)
  public abstract Object getDataDetails();

  public String[][] getTableData() {
    if (getDataDetails() instanceof List) {
      return getTableData0((List<DataDetailBase>) getDataDetails());
    }
    return new String[0][0];
  }

  public Map<String, String[][]> getMultiTableData() {
    Map<String, String[][]> tableData = new HashMap<>();
    if (getDataDetails() instanceof Map) {
      Map<String, List<DataDetailBase>> dataMap = (Map<String, List<DataDetailBase>>) getDataDetails();
      for (Entry<String, List<DataDetailBase>> entry : dataMap.entrySet()) {
        String[][] data = getTableData0(entry.getValue());
        tableData.put(entry.getKey(), data);
      }
    }
    return tableData;
  }

  @JsonIgnore
  @Schema(hidden = true)
  private String[][] getTableData0(List<DataDetailBase> entry) {
    String[][] data = new String[entry.size() + 1][getDataDetailTitles().length];
    data[0] = getDataDetailTitles();
    for (int i = 0; i < entry.size(); i++) {
      data[i + 1] = entry.get(i).toValues();
    }
    return data;
  }

  public void writeExcelFile(File file) throws IOException {
    if (getDataDetails() instanceof List) {
      String[][] data = getTableData0((List<DataDetailBase>) getDataDetails());
      writeExcelData(message(MESSAGE_DATA_DETAIL), file, data);
    } else if (getDataDetails() instanceof Map) {
      Map<String, List<DataDetailBase>> dataMap = (Map<String, List<DataDetailBase>>) getDataDetails();
      List<String> sheetNames = new ArrayList<>();
      List<String[][]> sheetDatas = new ArrayList<>();
      for (Entry<String, List<DataDetailBase>> entry : dataMap.entrySet()) {
        sheetNames.add(entry.getKey());
        sheetDatas.add(getTableData0(entry.getValue()));
      }
      writeExcelData(sheetNames, file, sheetDatas);
    }
  }

}
