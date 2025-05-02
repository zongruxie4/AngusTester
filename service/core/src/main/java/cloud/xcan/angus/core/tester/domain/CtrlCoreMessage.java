package cloud.xcan.angus.core.tester.domain;

/**
 * BAA000-BAA099
 */
public interface CtrlCoreMessage {

  String EXEC_NO_OP_PERMISSION_CODE = "BAC051";
  String EXEC_NO_OP_PERMISSION = "xcm.tester.exec.no.op.permission";

  String EXEC_ALREADY_IN_RUNNING_T = "xcm.tester.exec.already.in.running.t";
  String EXEC_ALREADY_IN_STOPPED_T = "xcm.tester.exec.already.in.stopped.t";

  String EXEC_REMOTE_NODE_IS_REQUIRED = "xcm.tester.exec.remote.node.is.required";
  String EXEC_START_IS_IGNORED = "xcm.tester.exec.start.is.ignored";
  String EXEC_START_IGNORED_WITH_PARSE_ERROR_T = "xcm.tester.exec.start.ignored.with.parse.error.t";
  String EXEC_START_IGNORED_WITH_SCRIPT_MISSING = "xcm.tester.exec.start.ignored.with.script.missing";
  String EXEC_START_UP_TIMEOUT = "xcm.tester.exec.start.up.timeout";
  String EXEC_NOT_MEET_CONDITIONS_NODES = "xcm.tester.exec.not.meet.conditions.node";
  String EXEC_CONTROLLER_NODE_NOT_FOUND = "xcm.tester.exec.controller.node.not.found";
  String EXEC_AGENT_ROUTER_NOT_FOUND = "xcm.tester.exec.agent.router.not.found";
  String EXEC_AGENT_ROUTER_NOT_FOUND_T = "xcm.tester.exec.agent.router.not.found.t";
  String EXEC_CONTROLLER_INSTANCE_NOT_FOUND = "xcm.tester.exec.controller.instance.not.found";
  String EXEC_CONTROLLER_INSTANCE_NOT_FOUND_T = "xcm.tester.exec.controller.instance.not.found.t";
  String EXEC_REMOTE_CONTROLLER_IGNORED_T = "xcm.tester.exec.remote.controller.ignored.t";

  String EXEC_CONTROLLER_START_EXCEPTION = "xcm.tester.exec.controller.start.exception.t";
  String EXEC_STOP_IS_IGNORED = "xcm.tester.exec.stop.is.ignored";
  String EXEC_STOP_IGNORED_WITH_NO_NODES = "xcm.tester.exec.stop.ignored.with.no.nodes";
  String EXEC_CONTROLLER_STOP_EXCEPTION = "xcm.tester.exec.controller.stop.exception.t";

  String EXEC_NO_FREE_NODES = "xcm.tester.exec.no.free.nodes";
  String EXEC_NO_FREE_NODES_RETRY_LATER = "xcm.tester.exec.no.free.nodes.retry.later";
  String EXEC_NO_FREE_NODES_RETRY_LATER_T = "xcm.tester.exec.no.free.nodes.retry.later.t";
  String EXEC_NODES_LESS_AVAILABLE_T = "xcm.tester.exec.nodes.less.available.t";
  String EXEC_LAST_NODES_IS_INSUFFICIENT = "xcm.tester.exec.last.nodes.is.insufficient";
  String EXEC_VALID_NODES_IS_INSUFFICIENT_T = "xcm.tester.exec.valid.nodes.is.insufficient.t";
  String EXEC_TRIAL_TASK_OVER_LIMIT_T = "xcm.tester.exec.trial.task.over.limit.t";
  String EXEC_TRIAL_CONCURRENT_TASK_OVER_LIMIT_T = "xcm.tester.exec.trial.concurrent.task.over.limit.t";

  String EXEC_PERF_INDICATOR_IS_EMPTY = "xcm.tester.exec.perf.indicator.is.empty";
  String EXEC_PERF_SAMPLING_SUMMARY_IS_EMPTY = "xcm.tester.exec.perf.sampling.summary.is.empty";
  String EXEC_STABILITY_INDICATOR_IS_EMPTY = "xcm.tester.exec.stability.indicator.is.empty";
  String EXEC_STABILITY_SAMPLING_SUMMARY_IS_EMPTY = "xcm.tester.exec.stability.sampling.summary.is.empty";
  String EXEC_SAMPLING_SUMMARY_IS_EMPTY = "xcm.tester.exec.sampling.summary.is.empty";
  String EXEC_RESPONSE_TIME_EXCEEDS_T = "xcm.tester.exec.response.time.exceeds.t";
  String EXEC_TPS_NOT_EXCEEDS_T = "xcm.tester.exec.tps.not.exceeds.t";
  String EXEC_ERROR_RATE_EXCEEDS_T = "xcm.tester.exec.error.rate.exceeds.t";
  String EXEC_NODE_METRICS_IS_MISSING = "xcm.tester.exec.app.node.metrics.is.missing";
  String EXEC_NODE_CPU_MEAN_USAGE_EXCEEDS_T = "xcm.tester.exec.app.node.cpu.mean.usage.exceeds.t";
  String EXEC_NODE_MEMORY_MEAN_USAGE_EXCEEDS_T = "xcm.tester.exec.app.node.memory.mean.usage.exceeds.t";
  String EXEC_NODE_DISK_USAGE_EXCEEDS_T = "xcm.tester.exec.app.node.disk.usage.exceeds.t";
  String EXEC_NODE_NETWORK_MEAN_USAGE_EXCEEDS_T = "xcm.tester.exec.app.node.network.mean.usage.exceeds.t";
  String EXEC_SAMPLE_CONTENT_IGNORED_OR_MISSING = "xcm.tester.exec.sample.content.ignored.or.missing";

  String AGENT_PUSH_START_FAILED = "xcm.tester.agent.push.start.failed";
  String AGENT_NOT_RUNNING = "xcm.tester.push.agent.not.running";
  String BROADCAST_START_TO_REMOTE_EXCEPTION_T = "xcm.tester.broadcast.start.to.remote.exception.t";
  String AGENT_PUSH_STOP_FAILED = "xcm.tester.agent.push.stop.failed";
  String BROADCAST_STOP_TO_REMOTE_EXCEPTION_T = "xcm.tester.broadcast.stop.to.remote.exception.t";
  String NO_AVAILABLE_NODES = "xcm.tester.no.available.nodes";
  String NO_AVAILABLE_EXEC_ROLE_NODES = "xcm.tester.no.available.exec.role.nodes";
  String NODE_SPEC_INFO_MISSING_T = "xcm.tester.node.spec.info.is.missing.t";
  String NODE_AGENT_UNAVAILABLE_T = "xcm.tester.node.agent.unavailable.t";
  String AGENT_PUSH_STATUS_FAILED = "xcm.tester.agent.push.status.failed";
  String AGENT_PUSH_CHECK_STATUS_FAILED = "xcm.tester.agent.push.check.status.failed";
  String AGENT_PUSH_CHECK_PORT_FAILED = "xcm.tester.agent.push.check.port.failed";

  String MOCKSERV_INSTANCE_OR_AGENT_NOT_STARTED = "xcm.tester.mock.service.instance.or.agent.not.started";
  String MOCKSERV_BROADCAST_IGNORE_REMOTE_NODE = "xcm.tester.mock.service.broadcast.ignore.remote.node";

  String NODE_IS_NOT_EXEC_ROLE_T = "xcm.tester.node.not.exec.role.t";
  String NODE_IS_NOT_APP_ROLE_T = "xcm.tester.node.not.exec.app.t";

  //Not an execution role node: {0}
  //Not an application role node: {0}
}
