package cloud.xcan.sdf.core.angustester.application.converter;


import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
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
    return new Dataset().setProjectId(projectId)
        .setName(x.getName()).setDescription(x.getDescription())
        .setParameters(x.getParameters())
        .setExtracted(nonNull(x.getExtraction()))
        .setExtraction(x.getExtraction());
  }
}
