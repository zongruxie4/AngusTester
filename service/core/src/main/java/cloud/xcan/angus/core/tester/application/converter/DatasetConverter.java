package cloud.xcan.angus.core.tester.application.converter;


import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import java.util.List;

public class DatasetConverter {

  public static Dataset toCloneDataset(Dataset datasetDb) {
    return new Dataset().setProjectId(datasetDb.getProjectId())
        .setName(datasetDb.getName())
        .setExtracted(datasetDb.getExtracted())
        .setParameters(datasetDb.getParameters())
        .setExtraction(datasetDb.getExtraction());
  }

  public static cloud.xcan.angus.model.element.dataset.Dataset toAngusDataset(Dataset datasetDb) {
    return cloud.xcan.angus.model.element.dataset.Dataset.newBuilder()
        .name(datasetDb.getName()).parameters(datasetDb.getParameters())
        .extraction(datasetDb.getExtraction())
        .build();
  }

  public static cloud.xcan.angus.model.element.dataset.Dataset toAngusDataset(String name,
      List<DatasetParameter> parameters, DefaultExtraction extraction) {
    return cloud.xcan.angus.model.element.dataset.Dataset.newBuilder()
        .name(name).parameters(parameters).extraction(extraction)
        .build();
  }

  public static Dataset toDataset(Long projectId,
      cloud.xcan.angus.model.element.dataset.Dataset x) {
    return new Dataset().setId(BIDUtils.getId(BIDKey.datasetId))
        .setProjectId(projectId)
        .setName(x.getName()).setDescription(x.getDescription())
        .setParameters(x.getParameters())
        .setExtracted(nonNull(x.getExtraction()))
        .setExtraction(x.getExtraction());
  }
}
