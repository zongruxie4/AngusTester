package cloud.xcan.angus.core.tester.application.query.exec.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ExecSampleConverter.mergeToFirst;
import static cloud.xcan.angus.model.AngusConstant.SAMPLE_TOTAL_NAME;
import static cloud.xcan.angus.remote.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.angus.remote.search.SearchCriteria.lessThanEqual;
import static cloud.xcan.angus.spec.utils.DateUtils.asDate;
import static cloud.xcan.angus.spec.utils.ObjectUtils.distinctByKey;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.page.FixedPageImpl;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleQuery;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleCounter;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleError;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleMergeBase;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleScore;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleScoreRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleSummary;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleSummaryRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThread;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThreadRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThroughput;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThroughputRepo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleUploadResultProgress;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.unit.TimeValue;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Slf4j
@Biz
public class ExecSampleQueryImpl implements ExecSampleQuery {

  @Resource
  private ExecSampleRepo sampleRepo;

  @Resource
  private ExecSampleSummaryRepo sampleSummaryRepo;

  @Resource
  private ExecSampleScoreRepo sampleScoreRepo;

  @Resource
  private ExecSampleThroughputRepo sampleThroughputRepo;

  @Resource
  private ExecSampleThreadRepo sampleThreadRepo;

  @Resource
  private ExecSampleErrorRepo sampleErrorRepo;

  @Resource
  private ExecQuery execQuery;

  @Override
  public ExecSample summaryTotal(Long execId) {
    return new BizTemplate<ExecSample>() {

      @Override
      protected ExecSample process() {
        ExecInfo execDb = execQuery.findInfo(execId);
        if (isNull(execDb)) {
          return null;
        }

        setExecInfoLatestTotalMergeSample(execDb);

        ExecSample execSample = execDb.getFinishSample();
        if (nonNull(execSample)) {
          // Set progress parameters
          execSample.setDurationUnit(execDb.getDuration()).setIterations(execDb.getIterations());
        }
        return execSample;
      }
    }.execute();
  }

  @Override
  public Page<ExecSampleSummary> summaryList(Long execId,
      GenericSpecification<ExecSampleSummary> spec, PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleSummary>>() {

      @Override
      protected Page<ExecSampleSummary> process() {
        Long firstSampleNode = sampleRepo.findFirstNodeByExecId(execId);
        if (isNull(firstSampleNode)) {
          return null;
        }

        ExecInfo execDb = execQuery.findInfo(execId);
        if (isNull(execDb) || execDb.getStatus().isCreated()) {
          return null;
        }

        // Note: Merge sampling data based on the first node and Total.
        // If the node or name is specified, use the specified parameter as the base.
        String nameFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "name");
        String finalNameFilter = nonNull(nameFilter) ? nameFilter : SAMPLE_TOTAL_NAME;
        String nodeFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "nodeId");

        setSharingAndGroupBaseCondition(nameFilter, finalNameFilter, nodeFilter, firstSampleNode,
            spec, execDb);

        Page<ExecSampleSummary> page = sampleSummaryRepo.findAll(spec, pageable);
        if (page.isEmpty() ||
            /* Do not merge data when querying a single node and fixed name sample */
            ((execDb.isSingleTargetPipeline() || nonNull(nameFilter))
                && (execDb.isOneNodeTask() || nonNull(nodeFilter)))) {
          return page;
        }

        long offsetInterval = execDb.getReportInterval().toMilliSecond();
        ExecSampleSummary pageFirst = page.getContent().get(0);
        ExecSampleSummary pageLast = page.getContent().get(page.getContent().size() - 1);
        Set<SearchCriteria> mergeFilters = getMergeSearchCriteria(
            execDb, pageFirst.getTimestamp(), pageLast.getTimestamp(), offsetInterval);

        // Merge one node and multi name sampling data
        if (isNull(nameFilter) && (nonNull(nodeFilter) || execDb.isOneNodeTask())) {
          addNodeAndNameNotTotalFilters(nodeFilter, firstSampleNode, mergeFilters);
          List<ExecSampleSummary> otherNameSamp = sampleSummaryRepo.findAllByFilters(mergeFilters);
          List<ExecSampleSummary> firstNameSamp = page.getContent();
          return new FixedPageImpl<>(
              mergeMultiNameAndSingleNodeSamples(firstNameSamp, otherNameSamp),
              pageable, page.getTotalElements());
        }

        // Merge multi node and single name sampling data
        List<ExecSampleSummary> firstNodeSamp = page.getContent();
        List<ExecSampleSummary> mergedNodeSamp = firstNodeSamp;
        if ((nonNull(nameFilter) || execDb.isSingleTargetPipeline())
            && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          addNameAndNodeFilters(execDb, nameFilter, firstSampleNode, mergeFilters);
          Map<Long, List<ExecSampleSummary>> otherNodesSampMap = sampleSummaryRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleSummary::getNodeId));
          otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
          if (isNotEmpty(otherNodesSampMap)) {
            for (List<ExecSampleSummary> otherNodeSamp : otherNodesSampMap.values()) {
              mergedNodeSamp = mergeSingleNameAndMultiNodeSamples(offsetInterval, firstNodeSamp,
                  otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(mergedNodeSamp, pageable, page.getTotalElements());
        }

        // Merge multi node and multi name sampling data
        if (isNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          Map<Long, List<ExecSampleSummary>> otherNodesSampMap = sampleSummaryRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleSummary::getNodeId));
          otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
          if (isNotEmpty(otherNodesSampMap)) {
            Map<String, List<ExecSampleSummary>> firstNodeNameSampMap = firstNodeSamp
                .stream().collect(Collectors.groupingBy(ExecSampleSummary::getName));
            for (List<ExecSampleSummary> otherNodeSamp : otherNodesSampMap.values()) {
              mergedNodeSamp = mergeMultiNameAndNodeSamples(offsetInterval, firstNodeNameSampMap,
                  otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(mergedNodeSamp, pageable, page.getTotalElements());
        }

        throw SysException.of("Unprocessed business");
      }
    }.execute();
  }

  @Override
  public Page<ExecSampleScore> scoreList(Long execId,
      GenericSpecification<ExecSampleScore> spec, PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleScore>>() {

      @Override
      protected Page<ExecSampleScore> process() {
        Long firstSampleNode = sampleRepo.findFirstNodeByExecId(execId);
        if (isNull(firstSampleNode)) {
          return null;
        }

        ExecInfo execDb = execQuery.findInfo(execId);
        if (isNull(execDb) || execDb.getStatus().isCreated()) {
          return null;
        }

        // Note: Merge sampling data based on the first node and Total.
        // If the node or name is specified, use the specified parameter as the base.
        String nameFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "name");
        String finalNameFilter = nonNull(nameFilter) ? nameFilter : SAMPLE_TOTAL_NAME;
        String nodeFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "nodeId");

        setSharingAndGroupBaseCondition(nameFilter, finalNameFilter, nodeFilter, firstSampleNode,
            spec, execDb);

        Page<ExecSampleScore> page = sampleScoreRepo.findAll(spec, pageable);
        if (page.isEmpty() ||
            /* Do not merge data when querying a single node and fixed name sample */
            ((execDb.isSingleTargetPipeline() || nonNull(nameFilter))
                && (execDb.isOneNodeTask() || nonNull(nodeFilter)))) {
          return page;
        }

        long offsetInterval = execDb.getReportInterval().toMilliSecond();
        ExecSampleScore pageFirst = page.getContent().get(0);
        ExecSampleScore pageLast = page.getContent().get(page.getContent().size() - 1);
        Set<SearchCriteria> mergeFilters = getMergeSearchCriteria(
            execDb, pageFirst.getTimestamp(), pageLast.getTimestamp(), offsetInterval);

        // Merge one node and multi name sampling data
        if (isNull(nameFilter) && (nonNull(nodeFilter) || execDb.isOneNodeTask())) {
          addNodeAndNameNotTotalFilters(nodeFilter, firstSampleNode, mergeFilters);
          List<ExecSampleScore> otherNameSamp = sampleScoreRepo.findAllByFilters(mergeFilters);
          List<ExecSampleScore> firstNameSamp = page.getContent();
          return new FixedPageImpl<>(
              mergeMultiNameAndSingleNodeSamples(firstNameSamp, otherNameSamp),
              pageable, page.getTotalElements());
        }

        // Merge multi node and single name sampling data
        List<ExecSampleScore> firstNodeSamp = page.getContent();
        if (nonNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          addNameAndNodeFilters(execDb, finalNameFilter, firstSampleNode, mergeFilters);
          Map<Long, List<ExecSampleScore>> otherNodesSampMap = sampleScoreRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleScore::getNodeId));
          otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
          if (isNotEmpty(otherNodesSampMap)) {
            for (List<ExecSampleScore> otherNodeSamp : otherNodesSampMap.values()) {
              mergeSingleNameAndMultiNodeSamples(offsetInterval, firstNodeSamp, otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(firstNodeSamp, pageable, page.getTotalElements());
        }

        // Merge multi node and multi name sampling data
        if (isNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          Map<Long, List<ExecSampleScore>> otherNodesSampMap = sampleScoreRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleScore::getNodeId));
          if (isNotEmpty(otherNodesSampMap)) {
            Map<String, List<ExecSampleScore>> firstNodeNameSampMap = firstNodeSamp
                .stream().collect(Collectors.groupingBy(ExecSampleScore::getName));
            otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
            for (List<ExecSampleScore> otherNodeSamp : otherNodesSampMap.values()) {
              mergeMultiNameAndNodeSamples(offsetInterval, firstNodeNameSampMap, otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(firstNodeSamp, pageable, page.getTotalElements());
        }
        throw SysException.of("Unprocessed business");
      }
    }.execute();
  }

  @Override
  public Page<ExecSampleThroughput> throughputList(Long execId,
      GenericSpecification<ExecSampleThroughput> spec, PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleThroughput>>() {

      @Override
      protected Page<ExecSampleThroughput> process() {
        Long firstSampleNode = sampleRepo.findFirstNodeByExecId(execId);
        if (isNull(firstSampleNode)) {
          return null;
        }

        ExecInfo execDb = execQuery.findInfo(execId);
        if (isNull(execDb) || execDb.getStatus().isCreated()) {
          return null;
        }

        // Note: Merge sampling data based on the first node and Total.
        // If the node or name is specified, use the specified parameter as the base.
        String nameFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "name");
        String finalNameFilter = nonNull(nameFilter) ? nameFilter : SAMPLE_TOTAL_NAME;
        String nodeFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "nodeId");

        setSharingAndGroupBaseCondition(nameFilter, finalNameFilter, nodeFilter, firstSampleNode,
            spec, execDb);

        Page<ExecSampleThroughput> page = sampleThroughputRepo.findAll(spec, pageable);
        if (page.isEmpty() ||
            /* Do not merge data when querying a single node and fixed name sample */
            ((execDb.isSingleTargetPipeline() || nonNull(nameFilter))
                && (execDb.isOneNodeTask() || nonNull(nodeFilter)))) {
          return page;
        }

        long offsetInterval = execDb.getReportInterval().toMilliSecond();
        ExecSampleThroughput pageFirst = page.getContent().get(0);
        ExecSampleThroughput pageLast = page.getContent().get(page.getContent().size() - 1);
        Set<SearchCriteria> mergeFilters = getMergeSearchCriteria(
            execDb, pageFirst.getTimestamp(), pageLast.getTimestamp(), offsetInterval);

        // Merge one node and multi name sampling data
        if (isNull(nameFilter) && (nonNull(nodeFilter) || execDb.isOneNodeTask())) {
          addNodeAndNameNotTotalFilters(nodeFilter, firstSampleNode, mergeFilters);
          List<ExecSampleThroughput> otherNameSamp = sampleThroughputRepo
              .findAllByFilters(mergeFilters);
          List<ExecSampleThroughput> firstNameSamp = page.getContent();
          return new FixedPageImpl<>(
              mergeMultiNameAndSingleNodeSamples(firstNameSamp, otherNameSamp),
              pageable, page.getTotalElements());
        }

        // Merge multi node and single name sampling data
        List<ExecSampleThroughput> firstNodeSamp = page.getContent();
        if (nonNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          addNameAndNodeFilters(execDb, finalNameFilter, firstSampleNode, mergeFilters);
          Map<Long, List<ExecSampleThroughput>> otherNodesSampMap = sampleThroughputRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleThroughput::getNodeId));
          otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
          if (isNotEmpty(otherNodesSampMap)) {
            for (List<ExecSampleThroughput> otherNodeSamp : otherNodesSampMap.values()) {
              mergeSingleNameAndMultiNodeSamples(offsetInterval, firstNodeSamp, otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(firstNodeSamp, pageable, page.getTotalElements());
        }

        // Merge multi node and multi name sampling data
        if (isNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          Map<Long, List<ExecSampleThroughput>> otherNodesSampMap = sampleThroughputRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleThroughput::getNodeId));
          otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
          if (isNotEmpty(otherNodesSampMap)) {
            Map<String, List<ExecSampleThroughput>> firstNodeNameSampMap = firstNodeSamp
                .stream().collect(Collectors.groupingBy(ExecSampleThroughput::getName));
            for (List<ExecSampleThroughput> otherNodeSamp : otherNodesSampMap.values()) {
              mergeMultiNameAndNodeSamples(offsetInterval, firstNodeNameSampMap, otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(firstNodeSamp, pageable, page.getTotalElements());
        }
        throw SysException.of("Unprocessed business");
      }
    }.execute();
  }

  @Override
  public Page<ExecSampleThread> threadList(Long execId, GenericSpecification<ExecSampleThread> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleThread>>() {

      @Override
      protected Page<ExecSampleThread> process() {
        Long firstSampleNode = sampleRepo.findFirstNodeByExecId(execId);
        if (isNull(firstSampleNode)) {
          return null;
        }

        ExecInfo execDb = execQuery.findInfo(execId);
        if (isNull(execDb) || execDb.getStatus().isCreated()) {
          return null;
        }

        // Note: Merge sampling data based on the first node and Total.
        // If the node or name is specified, use the specified parameter as the base.
        String nameFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "name");
        String finalNameFilter = nonNull(nameFilter) ? nameFilter : SAMPLE_TOTAL_NAME;
        String nodeFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "nodeId");

        setSharingAndGroupBaseCondition(nameFilter, finalNameFilter, nodeFilter, firstSampleNode,
            spec, execDb);

        Page<ExecSampleThread> page = sampleThreadRepo.findAll(spec, pageable);
        if (page.isEmpty() ||
            /* Do not merge data when querying a single node and fixed name sample */
            ((execDb.isSingleTargetPipeline() || nonNull(nameFilter))
                && (execDb.isOneNodeTask() || nonNull(nodeFilter)))) {
          return page;
        }

        long offsetInterval = execDb.getReportInterval().toMilliSecond();
        ExecSampleThread pageFirst = page.getContent().get(0);
        ExecSampleThread pageLast = page.getContent().get(page.getContent().size() - 1);
        Set<SearchCriteria> mergeFilters = getMergeSearchCriteria(
            execDb, pageFirst.getTimestamp(), pageLast.getTimestamp(), offsetInterval);

        // Merge one node and multi name sampling data
        if (isNull(nameFilter) && (nonNull(nodeFilter) || execDb.isOneNodeTask())) {
          addNodeAndNameNotTotalFilters(nodeFilter, firstSampleNode, mergeFilters);
          List<ExecSampleThread> otherNameSamp = sampleThreadRepo.findAllByFilters(mergeFilters);
          List<ExecSampleThread> firstNameSamp = page.getContent();
          return new FixedPageImpl<>(
              mergeMultiNameAndSingleNodeSamples(firstNameSamp, otherNameSamp),
              pageable, page.getTotalElements());
        }

        // Merge multi node and single name sampling data
        List<ExecSampleThread> firstNodeSamp = page.getContent();
        if (nonNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          addNameAndNodeFilters(execDb, finalNameFilter, firstSampleNode, mergeFilters);
          Map<Long, List<ExecSampleThread>> otherNodesSampMap = sampleThreadRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleThread::getNodeId));
          otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
          if (isNotEmpty(otherNodesSampMap)) {
            for (List<ExecSampleThread> otherNodeSamp : otherNodesSampMap.values()) {
              mergeSingleNameAndMultiNodeSamples(offsetInterval, firstNodeSamp, otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(firstNodeSamp, pageable, page.getTotalElements());
        }

        // Merge multi node and multi name sampling data
        if (isNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          Map<Long, List<ExecSampleThread>> otherNodesSampMap = sampleThreadRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleThread::getNodeId));
          if (isNotEmpty(otherNodesSampMap)) {
            Map<String, List<ExecSampleThread>> firstNodeNameSampMap = firstNodeSamp
                .stream().collect(Collectors.groupingBy(ExecSampleThread::getName));
            otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
            for (List<ExecSampleThread> otherNodeSamp : otherNodesSampMap.values()) {
              mergeMultiNameAndNodeSamples(offsetInterval, firstNodeNameSampMap, otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(firstNodeSamp, pageable, page.getTotalElements());
        }
        throw SysException.of("Unprocessed business");
      }
    }.execute();
  }

  @Override
  public Page<ExecSampleError> errorList(Long execId, GenericSpecification<ExecSampleError> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<ExecSampleError>>() {

      @Override
      protected Page<ExecSampleError> process() {
        Long firstSampleNode = sampleRepo.findFirstNodeByExecId(execId);
        if (isNull(firstSampleNode)) {
          return null;
        }

        ExecInfo execDb = execQuery.findInfo(execId);
        if (isNull(execDb) || execDb.getStatus().isCreated()) {
          return null;
        }

        // Note: Merge sampling data based on the first node and Total.
        // If the node or name is specified, use the specified parameter as the base.
        String nameFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "name");
        String finalNameFilter = nonNull(nameFilter) ? nameFilter : SAMPLE_TOTAL_NAME;
        String nodeFilter = CriteriaUtils.findFirstValue(spec.getCriteria(), "nodeId");

        setSharingAndGroupBaseCondition(nameFilter, finalNameFilter, nodeFilter, firstSampleNode,
            spec, execDb);

        Page<ExecSampleError> page = sampleErrorRepo.findAll(spec, pageable);
        if (page.isEmpty() ||
            /* Do not merge data when querying a single node and fixed name sample */
            ((execDb.isSingleTargetPipeline() || nonNull(nameFilter))
                && (execDb.isOneNodeTask() || nonNull(nodeFilter)))) {
          return page;
        }

        long offsetInterval = execDb.getReportInterval().toMilliSecond();
        ExecSampleError pageFirst = page.getContent().get(0);
        ExecSampleError pageLast = page.getContent().get(page.getContent().size() - 1);
        Set<SearchCriteria> mergeFilters = getMergeSearchCriteria(
            execDb, pageFirst.getTimestamp(), pageLast.getTimestamp(), offsetInterval);

        // Merge one node and multi name sampling data
        if (isNull(nameFilter) && (nonNull(nodeFilter) || execDb.isOneNodeTask())) {
          addNodeAndNameNotTotalFilters(nodeFilter, firstSampleNode, mergeFilters);
          List<ExecSampleError> otherNameSamp = sampleErrorRepo.findAllByFilters(mergeFilters);
          List<ExecSampleError> firstNameSamp = page.getContent();
          return new FixedPageImpl<>(
              mergeMultiNameAndSingleNodeSamples(firstNameSamp, otherNameSamp),
              pageable, page.getTotalElements());
        }

        // Merge multi node and single name sampling data
        List<ExecSampleError> firstNodeSamp = page.getContent();
        if (nonNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          addNameAndNodeFilters(execDb, nameFilter, firstSampleNode, mergeFilters);
          Map<Long, List<ExecSampleError>> otherNodesSampMap = sampleErrorRepo
              .findAllByFilters(mergeFilters)
              .stream().collect(Collectors.groupingBy(ExecSampleError::getNodeId));
          otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
          if (isNotEmpty(otherNodesSampMap)) {
            for (List<ExecSampleError> otherNodeSamp : otherNodesSampMap.values()) {
              mergeSingleNameAndMultiNodeSamples(offsetInterval, firstNodeSamp, otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(firstNodeSamp, pageable, page.getTotalElements());
        }

        // Merge multi node and multi name sampling data
        if (isNull(nameFilter) && !execDb.isOneNodeTask() && isNull(nodeFilter)) {
          Map<Long, List<ExecSampleError>> otherNodesSampMap = sampleErrorRepo
              .findAllByFilters(mergeFilters).stream()
              .collect(Collectors.groupingBy(ExecSampleError::getNodeId));
          if (isNotEmpty(otherNodesSampMap)) {
            Map<String, List<ExecSampleError>> firstNodeNameSampMap = firstNodeSamp
                .stream().collect(Collectors.groupingBy(ExecSampleError::getName));
            otherNodesSampMap.remove(firstNodeSamp.get(0).getNodeId());
            for (List<ExecSampleError> otherNodeSamp : otherNodesSampMap.values()) {
              mergeMultiNameAndNodeSamples(offsetInterval, firstNodeNameSampMap, otherNodeSamp);
            }
          }
          return new FixedPageImpl<>(firstNodeSamp, pageable, page.getTotalElements());
        }
        throw SysException.of("Unprocessed business");
      }
    }.execute();
  }

  @Override
  public List<ExecSampleCounter> latestErrorsCounter(Long execId, Long nodeId, String name) {
    return new BizTemplate<List<ExecSampleCounter>>() {

      @Override
      protected List<ExecSampleCounter> process() {
        return sampleRepo.findLatestErrorCauseCounter(execId, nodeId, name);
      }
    }.execute();
  }

  @Override
  public ExecSampleUploadResultProgress latestUploadResultProgress(Long execId) {
    return new BizTemplate<ExecSampleUploadResultProgress>() {

      @Override
      protected ExecSampleUploadResultProgress process() {
        return sampleRepo.findLastSampleUploadResultProgress(execId);
      }
    }.execute();
  }

  @Override
  public List<ExecSampleCounter> latestExtCounterMap1(Long execId, Long nodeId, String name) {
    return new BizTemplate<List<ExecSampleCounter>>() {

      @Override
      protected List<ExecSampleCounter> process() {
        return sampleRepo.findLatestExtCounterMap1(execId, nodeId, name);
      }
    }.execute();
  }

  @Override
  public ExecSample getSingleTaskExecSampleResult(Long execId) {
    return sampleRepo.findLatestByExecIdAndName(execId, SAMPLE_TOTAL_NAME);
  }

  @Override
  public ExecSample getSingleTaskExecSampleResult(Long execId, String name) {
    return sampleRepo.findLatestByExecIdAndName(execId, name);
  }

  @Override
  public ExecSample getSingleTaskExecSampleResult(Long execId, String name, int rampNum) {
    return sampleRepo.findLatestByExecIdAndNameAndRampNum(execId, name, rampNum);
  }

  @Override
  public void setExecInfoLatestTotalMergeSample(List<ExecInfo> execs) {
    for (ExecInfo execInfo : execs) {
      setExecInfoLatestTotalMergeSample(execInfo);
    }
  }

  @Override
  public void setExecLatestTotalMergeSample(List<Exec> execs) {
    for (Exec execInfo : execs) {
      setExecLatestTotalMergeSample(execInfo);
    }
  }

  @Override
  public void setExecInfoLatestTotalMergeSample(ExecInfo execInfo) {
    ExecSample latest = getExecLatestTotalMergeSample(execInfo);
    execInfo.setFinishSample(latest);
  }

  @Override
  public ExecSample getExecLatestTotalMergeSample(ExecInfo execInfo) {
    ExecSample latest = null;
    if (execInfo.isOneNodeTask()) {
      latest = sampleRepo.findLatestByExecIdAndName(execInfo.getId(), SAMPLE_TOTAL_NAME);
    } else {
      Long latestDate = sampleRepo.findLatestTimestampByExecIdAndName(
          execInfo.getId(), SAMPLE_TOTAL_NAME);
      if (nonNull(latestDate)) {
        latest = mergeExecSample(execInfo.getId(), SAMPLE_TOTAL_NAME, execInfo.getReportInterval(),
            latestDate);
      }
    }
    return latest;
  }

  @Override
  public void setExecLatestTotalMergeSample(Exec exec) {
    ExecSample latest = getExecLatestTotalMergeSample(exec);
    exec.setFinishSampleResult(latest);
  }

  @Override
  public ExecSample getExecLatestTotalMergeSample(Exec exec) {
    return getExecLatestMergeSample(exec, SAMPLE_TOTAL_NAME);
  }

  @Override
  public ExecSample getExecLatestMergeSample(Exec exec, String name) {
    ExecSample latest = null;
    if (exec.isOneNodeTask()) {
      latest = getSingleTaskExecSampleResult(exec.getId(), name);
    } else {
      Long latestDate = sampleRepo.findLatestTimestampByExecIdAndName(exec.getId(), name);
      if (nonNull(latestDate)) {
        latest = mergeExecSample(exec.getId(), name, exec.getReportInterval(), latestDate);
      }
    }
    return latest;
  }

  @Override
  public ExecSample getExecLatestMergeSample(Exec exec, String name, int rampNum) {
    ExecSample latest = null;
    if (exec.isOneNodeTask()) {
      latest = getSingleTaskExecSampleResult(exec.getId(), name, rampNum);
    } else {
      Long latestDate = sampleRepo
          .findLatestTimestampByExecIdAndNameAndRampNum(exec.getId(), name, rampNum);
      if (nonNull(latestDate)) {
        latest = mergeExecSample(exec.getId(), name, exec.getReportInterval(), latestDate, rampNum);
      }
    }
    return latest;
  }

  @Nullable
  private ExecSample mergeExecSample(Long execId, String name, TimeValue reportInterval,
      Long latestDate) {
    List<ExecSample> latestSamples = sampleRepo
        .findLatestByExecIdAndNameAndLastSampleDateAndReportInterval(
            execId, name, latestDate - reportInterval.toMilliSecond());
    return mergeExecSample0(latestSamples);
  }

  @Nullable
  private ExecSample mergeExecSample(Long execId, String name, TimeValue reportInterval,
      Long latestDate, int rampNum) {
    List<ExecSample> latestSamples = sampleRepo
        .findLatestByExecIdAndNameAndLastSampleDateAndReportIntervalAndRampNum(
            execId, name, latestDate - reportInterval.toMilliSecond(), rampNum);
    return mergeExecSample0(latestSamples);
  }

  private ExecSample mergeExecSample0(List<ExecSample> latestSamples) {
    ExecSample firstSample = null;
    if (isNotEmpty(latestSamples)) {
      List<ExecSample> nodeLastSamples = latestSamples.stream()
          .filter(distinctByKey(ExecSample::getNodeId)).collect(Collectors.toList());
      for (ExecSample sample : nodeLastSamples) {
        if (isNull(firstSample)) {
          firstSample = sample;
        } else {
          mergeToFirst(firstSample, sample);
        }
      }
    }
    return firstSample;
  }

  @Override
  public List<ExecSample> getExecLatestTotalMergeSampleByRampNum(Exec exec) {
    return getExecLatestMergeSampleByRampNum(exec, SAMPLE_TOTAL_NAME);
  }

  @Override
  public List<ExecSample> getExecLatestMergeSampleByRampNum(Exec exec, String name) {
    List<Integer> rampNums = sampleRepo.findRampNum(exec.getId());
    if (isEmpty(rampNums) || rampNums.size() == 1) {
      ExecSample sample = getExecLatestMergeSample(exec, name);
      return isNull(sample) ? null : List.of(sample);
    }

    List<ExecSample> rampSamples = new ArrayList<>();
    for (Integer rampNum : rampNums) {
      ExecSample sample = getExecLatestMergeSample(exec, name, rampNum);
      if (nonNull(sample)) {
        rampSamples.add(sample);
      }
    }
    return rampSamples;
  }

  public void setSharingAndGroupBaseCondition(String nameFilter, String finalNameFilter,
      String nodeFilter, Long firstSampleNode, GenericSpecification<?> spec, ExecInfo exec) {
    //spec.getCriteria().add(SearchCriteria.equal(DEFAULT_SHARDING_KEY, tenantId));
    spec.getCriteria().add(SearchCriteria.equal("execId", exec.getId()));

    // Important: Paging and merging based on `Total` as timestamp!!!
    if (isNull(nameFilter) /*&& !exec.isOneNodeTask() && isNull(nodeFilter)*/) {
      spec.getCriteria().add(SearchCriteria.equal("name", finalNameFilter));
    }

    if (isNull(nodeFilter)) {
      // Important: Paging and merging based on first node as timestamp!!!
      ProtocolAssert.assertNotNull(firstSampleNode, "Exec node is missing");
      spec.getCriteria().add(SearchCriteria.equal("nodeId", firstSampleNode));
    }

    SearchCriteria startTimeFilter = CriteriaUtils.findFirst(spec.getCriteria(),
        "timestamp", SearchOperation.GREATER_THAN_EQUAL);
    if (isNull(startTimeFilter)) {
      LocalDateTime startTime = nullSafe(exec.getActualStartDate(), exec.getCreatedDate());
      spec.getCriteria().add(greaterThanEqual("timestamp", asDate(startTime).getTime()));
    } else {
      assertTrue(startTimeFilter.isValidCriteria(), "The start timestamp is required and valid");
    }

    SearchCriteria endTimeFilter = CriteriaUtils.findFirst(spec.getCriteria(),
        "timestamp", SearchOperation.LESS_THAN_EQUAL);
    if (isNull(endTimeFilter)) {
      LocalDateTime endTime = nullSafe(exec.getEndDate(), LocalDateTime.now());
      spec.getCriteria().add(lessThanEqual("timestamp", asDate(endTime).getTime()
          /* Fix: The last row was missed
           *   1700185885000 -> 2023-11-17 09:51:25
           *   1700185885324 -> 2023-11-17 09:51:25
           * */
          + exec.getReportInterval().toMilliSecond()));
    } else {
      assertTrue(endTimeFilter.isValidCriteria(), "The end timestamp is required and valid");
    }

    //assertTrue((long) endTimeFilter.getValue() - (long) startTimeFilter.getValue()
    //    > MAX_EXEC_DURATION_IN_MS, "The query time range cannot exceed 1 day");
  }

  private Set<SearchCriteria> getMergeSearchCriteria(
      ExecInfo exec, long firstTimeFilter, long secondTimeFilter, long offsetInterval) {
    Set<SearchCriteria> filters = new HashSet<>();
    //filters.add(SearchCriteria.equal(DEFAULT_SHARDING_KEY, tenantId));
    filters.add(SearchCriteria.equal("execId", exec.getId()));
    /* Note: Main page can be in ascending or descending order */
    filters.add(lessThanEqual("timestamp", Math.max(firstTimeFilter, secondTimeFilter)
        + offsetInterval));
    filters.add(greaterThanEqual("timestamp", Math.min(firstTimeFilter, secondTimeFilter)
        - offsetInterval));
    return filters;
  }

  private void addNodeAndNameNotTotalFilters(String nodeFilter, Long firstSampleNode,
      Set<SearchCriteria> filters) {
    Assert.assertTrue(nonNull(nodeFilter)
        || nonNull(firstSampleNode), "Node filter is missing");
    String nodeFilterSafe = isNull(nodeFilter) ? String.valueOf(firstSampleNode) : nodeFilter;
    filters.add(SearchCriteria.equal("nodeId", nodeFilterSafe));
    filters.add(SearchCriteria.notEqual("name", SAMPLE_TOTAL_NAME));
  }

  private void addNameAndNodeFilters(ExecInfo execDb, String finalNameFilter, Long firstSampleNode,
      Set<SearchCriteria> filters) {
    filters.add(SearchCriteria.equal("name", finalNameFilter));
    Set<Long> otherNodeIds = execDb.getExecNodeIds();
    otherNodeIds.remove(firstSampleNode);
    if (isNotEmpty(otherNodeIds)) {
      filters.add(SearchCriteria.in("nodeId", otherNodeIds));
    }
  }

  private <T extends ExecSampleMergeBase> List<T> mergeMultiNameAndSingleNodeSamples(
      List<T> firstNameSamples, List<T> otherNameSamples) {
    if (isEmpty(otherNameSamples)) {
      return firstNameSamples;
    }
    List<T> mergeSamples = new ArrayList<>(firstNameSamples);
    Map<String, List<T>> otherNameSampMap = otherNameSamples.stream()
        .collect(Collectors.groupingBy(T::getName));
    for (List<T> others : otherNameSampMap.values()) {
      mergeSamples.addAll(others);
    }
    return mergeSamples;
  }

  private <T extends ExecSampleMergeBase> List<T> mergeMultiNameAndNodeSamples(long offsetInterval,
      Map<String, List<T>> firstNodeNameSampMap, List<T> otherNodeSamp) {
    if (isEmpty(otherNodeSamp)) {
      return firstNodeNameSampMap.values().stream().flatMap(Collection::stream)
          .collect(Collectors.toList());
    }

    otherNodeSamp.sort(Comparator.comparing(T::getTimestamp));
    Map<String, List<T>> otherNameSampMap = otherNodeSamp.stream()
        .collect(Collectors.groupingBy(T::getName));
    List<T> mergedNodeSamp = new ArrayList<>();

    // After the first node is not start sampling, other nodes are starting sampling
    if (firstNodeNameSampMap.size() <= 0) {
      mergedNodeSamp.addAll(otherNodeSamp);
      return mergedNodeSamp;
    }

    for (Entry<String, List<T>> entry : firstNodeNameSampMap.entrySet()) {
      mergedNodeSamp.addAll(mergeSingleNameAndMultiNodeSamples(offsetInterval,
          entry.getValue(), otherNameSampMap.get(entry.getKey())));
    }
    return mergedNodeSamp;
  }

  private <T extends ExecSampleMergeBase> List<T> mergeSingleNameAndMultiNodeSamples(
      long offsetInterval, List<T> firstNodeSamp, List<T> otherNodeSamp) {
    if (isEmpty(otherNodeSamp)) {
      return firstNodeSamp;
    }
    otherNodeSamp.sort(Comparator.comparing(T::getTimestamp));
    int firstSize = firstNodeSamp.size();
    int otherSize = otherNodeSamp.size();
    //int size = Math.max(firstSize, otherSize);
    List<T> mergedNodeSamp = new ArrayList<>();
    int otherOffset = 0;
    long mergeNodeId = otherNodeSamp.get(otherOffset).getNodeId();

    // After the first node is not start sampling, other nodes are starting sampling
    if (firstSize <= 0) {
      mergedNodeSamp.addAll(otherNodeSamp);
      return mergedNodeSamp;
    }

    for (int i = 0; i < firstSize; ) {
      if (otherOffset >= otherSize) {
        log.warn("Node `{}` subsequent sampling data loss or interruption", mergeNodeId);
        break;
      }

      long offset = otherNodeSamp.get(otherOffset).getTimestamp()
          - firstNodeSamp.get(i).getTimestamp();
      if (Math.abs(offset) <= offsetInterval) { // Positive deviation
        // Merge other to first sample
        mergeToFirst(firstNodeSamp.get(i), otherNodeSamp.get(otherOffset));
        mergedNodeSamp.add(firstNodeSamp.get(i));
        otherOffset++;
        i++;
      } else {
        // Negative deviation
        log.warn(
            "There is a negative deviation in the sampling time of subsequent sampling nodes, nodeId:{}, deviation: {}ms",
            mergeNodeId, offset);
        if (offset > offsetInterval) {
          // The delay duration of other nodes is greater than the sampling interval
          // mergeToFirst(firstNodeSamp.get(i), null); // By default
          mergedNodeSamp.add(firstNodeSamp.get(i));
          i++;
        } else {
          // The delay duration of other nodes is earlier than the sampling interval
          mergedNodeSamp.add(otherNodeSamp.get(otherOffset));
          otherOffset++;
        }
      }
    }
    return mergedNodeSamp;
  }

}
