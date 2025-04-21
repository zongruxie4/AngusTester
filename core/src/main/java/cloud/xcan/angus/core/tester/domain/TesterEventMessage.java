package cloud.xcan.angus.core.tester.domain;

public interface TesterEventMessage {

  String TaskWillOverdueCode = "TaskWillOverdue";
  String TaskWillOverdue = "xcm.tester.event.TaskWillOverdue";
  String TaskOverdueCode = "TaskOverdue";
  String TaskOverdue = "xcm.tester.event.TaskOverdue";
  String TaskAssignmentCode = "TaskAssignment";
  String TaskAssignment = "xcm.tester.event.TaskAssignment";
  String TaskPendingConfirmationCode = "TaskPendingConfirmation";
  String TaskPendingConfirmation = "xcm.tester.event.TaskPendingConfirmation";
  String TaskModificationCode = "TaskModification";
  String TaskModification = "xcm.tester.event.TaskModification";

  String FunctionCaseWillOverdueCode = "FunctionCaseWillOverdue";
  String FunctionCaseWillOverdue = "xcm.tester.event.FunctionCaseWillOverdue";
  String FunctionCaseOverdueCode = "FunctionCaseOverdue";
  String FunctionCaseOverdue = "xcm.tester.event.FunctionCaseOverdue";
  String FunctionCaseAssignmentCode = "FunctionCaseAssignment";
  String FunctionCaseAssignment = "xcm.tester.event.FunctionCaseAssignment";
  String FunctionCaseModificationCode = "FunctionCaseModification";
  String FunctionCaseModification = "xcm.tester.event.FunctionCaseModification";

  String ApisModificationCode = "ApisModification";
  String ApisModification = "xcm.tester.event.ApisModification";

  String ScenarioModificationCode = "ScenarioModification";
  String ScenarioModification = "xcm.tester.event.ScenarioModification";

  String ReportGenerationSuccessfulCode = "ReportGenerationSuccessful";
  String ReportGenerationSuccessful = "xcm.tester.event.ReportGenerationSuccessful";

  String ScenarioMonitorFailedCode = "ScenarioMonitorFailed";
  String ScenarioMonitorFailed = "xcm.tester.event.ScenarioMonitorFailed";
}
