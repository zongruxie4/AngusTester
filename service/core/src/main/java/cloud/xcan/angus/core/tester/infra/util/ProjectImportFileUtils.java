package cloud.xcan.angus.core.tester.infra.util;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.utils.CoreUtils.randomUUID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.spec.utils.FileSearchUtils;
import cloud.xcan.angus.spec.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/**
 * Utility class for project data import file operations.
 * <p>
 * Provides file handling utilities for extracting ZIP and TAR archives, classifying files by
 * business type, and managing temporary paths.
 */
public class ProjectImportFileUtils {

  private static final String IMPORT_PROJECT_DIR = "import/project/";

  /**
   * Creates a temporary directory path for project import operations.
   */
  public static File getImportTmpPath() {
    SpringAppDirUtils utils = new SpringAppDirUtils();
    String tmpPath = utils.getTmpDir() + IMPORT_PROJECT_DIR + randomUUID();
    File file = new File(tmpPath);
    file.mkdirs();
    return file;
  }

  /**
   * Extracts archive file (ZIP or TAR) and returns list of extracted files.
   */
  @SneakyThrows
  public static List<File> extractArchiveFiles(File tmpPath, File archiveFile) {
    List<File> extractedFiles = new ArrayList<>();
    String fileName = archiveFile.getName().toLowerCase();

    if (fileName.endsWith(".zip")) {
      // Extract ZIP file
      FileUtils.extract(archiveFile.getPath(), tmpPath.getPath());
      extractedFiles = FileSearchUtils.newBuilder()
          .filter(pathname -> pathname.getName().endsWith(".json")
              || pathname.getName().endsWith(".yaml"))
          .build().search();
    } else if (fileName.endsWith(".tar") || fileName.endsWith(".tar.gz") || fileName.endsWith(
        ".tgz")) {
      // Extract TAR file
      extractTarFile(archiveFile, tmpPath);
      extractedFiles = FileSearchUtils.newBuilder()
          .filter(pathname -> pathname.getName().endsWith(".json")
              || pathname.getName().endsWith(".yaml"))
          .build().search();
    } else {
      throw new IllegalArgumentException(
          "不支持的文件格式，仅支持 ZIP (.zip) 或 TAR (.tar, .tar.gz, .tgz) 格式");
    }

    assertNotEmpty(extractedFiles, "导入文件中未找到业务数据文件（JSON格式）或脚本文件（YAML格式）");
    return extractedFiles;
  }

  /**
   * Extracts TAR archive file.
   */
  private static void extractTarFile(File tarFile, File destDir) throws IOException {
    Path destPath = destDir.toPath();
    Files.createDirectories(destPath);

    try (TarArchiveInputStream tarInput = createTarInputStream(tarFile)) {
      TarArchiveEntry entry;
      while ((entry = tarInput.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          Files.createDirectories(destPath.resolve(entry.getName()));
        } else {
          Path entryPath = destPath.resolve(entry.getName());
          Files.createDirectories(entryPath.getParent());
          Files.copy(tarInput, entryPath);
        }
      }
    }
  }

  /**
   * Creates TarArchiveInputStream from file, handling both plain TAR and GZIP-compressed TAR.
   */
  private static TarArchiveInputStream createTarInputStream(File tarFile) throws IOException {
    String fileName = tarFile.getName().toLowerCase();
    if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")) {
      return new TarArchiveInputStream(
          new GzipCompressorInputStream(Files.newInputStream(tarFile.toPath())));
    } else {
      return new TarArchiveInputStream(Files.newInputStream(tarFile.toPath()));
    }
  }

  /**
   * Classifies files by business type based on filename keywords. Returns a map where key is
   * ExampleDataType and value is list of files. For SCRIPT type, returns all YAML files. For other
   * types, returns only the first matching file.
   */
  public static Map<ExampleDataType, List<File>> classifyFilesByType(List<File> files) {
    Map<ExampleDataType, List<File>> classifiedFiles = new HashMap<>();

    for (File file : files) {
      String fileName = file.getName().toLowerCase();
      boolean isYaml = fileName.endsWith(".yaml");
      boolean isJson = fileName.endsWith(".json");

      if (isYaml) {
        // YAML files are scripts - support multiple files
        classifiedFiles.computeIfAbsent(ExampleDataType.SCRIPT, k -> new ArrayList<>()).add(file);
      } else if (isJson) {
        // JSON files are business data - match by keywords and take only first file per type
        ExampleDataType dataType = matchBusinessType(fileName);
        if (dataType != null) {
          classifiedFiles.computeIfAbsent(dataType, k -> new ArrayList<>()).add(file);
        }
      }
    }

    // For non-SCRIPT types, keep only the first file
    for (Map.Entry<ExampleDataType, List<File>> entry : classifiedFiles.entrySet()) {
      if (entry.getKey() != ExampleDataType.SCRIPT && entry.getValue().size() > 1) {
        entry.setValue(entry.getValue().subList(0, 1));
      }
    }

    return classifiedFiles;
  }

  /**
   * Matches business type based on filename keywords.
   */
  private static ExampleDataType matchBusinessType(String fileName) {
    String lowerFileName = fileName.toLowerCase();

    // Project - contains "项目" or "project"
    if (lowerFileName.contains("项目") || lowerFileName.contains("project")) {
      return null; // Project is handled separately
    }

    // Tag - contains "标签" or "tag"
    if (lowerFileName.contains("标签") || lowerFileName.contains("tag")) {
      return ExampleDataType.TAG;
    }

    // Module - contains "模块" or "module"
    if (lowerFileName.contains("模块") || lowerFileName.contains("module")) {
      return ExampleDataType.MODULE;
    }

    // Task - contains "任务" or "task"
    if (lowerFileName.contains("任务") || lowerFileName.contains("task")) {
      return ExampleDataType.TASK;
    }

    // Func (Test Case) - contains "用例" or "case" or "func"
    if (lowerFileName.contains("用例") || lowerFileName.contains("case") || lowerFileName.contains(
        "func")) {
      return ExampleDataType.FUNC;
    }

    // Services - contains "服务" or "service"
    if (lowerFileName.contains("服务") || lowerFileName.contains("service")) {
      return ExampleDataType.SERVICES;
    }

    // Scenario - contains "场景" or "scenario"
    if (lowerFileName.contains("场景") || lowerFileName.contains("scenario")) {
      return ExampleDataType.SCENARIO;
    }

    // Variable - contains "变量" or "variable"
    if (lowerFileName.contains("变量") || lowerFileName.contains("variable")) {
      return ExampleDataType.VARIABLE;
    }

    // Dataset - contains "数据集" or "dataset"
    if (lowerFileName.contains("数据集") || lowerFileName.contains("dataset")) {
      return ExampleDataType.DATASET;
    }

    // Mock - contains "mock"
    if (lowerFileName.contains("mock")) {
      return ExampleDataType.MOCK;
    }

    // Execution - contains "执行" or "execution" or "exec"
    if (lowerFileName.contains("执行") || lowerFileName.contains("execution")
        || lowerFileName.contains("exec")) {
      return ExampleDataType.EXECUTION;
    }

    // Report - contains "报告" or "report"
    if (lowerFileName.contains("报告") || lowerFileName.contains("report")) {
      return ExampleDataType.REPORT;
    }

    return null; // Unknown type
  }

  /**
   * Finds project JSON file from list of files.
   */
  public static File findProjectFile(List<File> files) {
    for (File file : files) {
      String fileName = file.getName().toLowerCase();
      if (fileName.endsWith(".json") &&
          (fileName.contains("项目") || fileName.contains("project"))) {
        return file;
      }
    }
    return null;
  }
}

