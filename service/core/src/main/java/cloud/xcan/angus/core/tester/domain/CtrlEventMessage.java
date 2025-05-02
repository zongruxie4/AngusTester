package cloud.xcan.angus.core.tester.domain;

public interface CtrlEventMessage {

  //String NodeConnectionStatusAbnormalCode = "NodeConnectionStatusAbnormal";
  //String NodeConnectionStatusAbnormal = "xcm.tester.event.NodeConnectionStatusAbnormal";

  String ExecutionTestCompletedCode = "ExecutionTestCompleted";
  String ExecutionTestCompleted = "xcm.tester.event.ExecutionTestCompleted";
  String ExecutionTestFailedCode = "ExecutionTestFailed";
  String ExecutionTestFailed = "xcm.tester.event.ExecutionTestFailed";

}
