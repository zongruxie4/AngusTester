package cloud.xcan.angus.core.tester.domain;

public interface TesterEventMessage {

  String TaskWillOverdueCode = "TaskWillOverdue";
  String TaskWillOverdue = "xcm.angustester.event.TaskWillOverdue";
  String TaskOverdueCode = "TaskOverdue";
  String TaskOverdue = "xcm.angustester.event.TaskOverdue";
  String TaskAssignmentCode = "TaskAssignment";
  String TaskAssignment = "xcm.angustester.event.TaskAssignment";
  String TaskPendingConfirmationCode = "TaskPendingConfirmation";
  String TaskPendingConfirmation = "xcm.angustester.event.TaskPendingConfirmation";
  String TaskModificationCode = "TaskModification";
  String TaskModification = "xcm.angustester.event.TaskModification";

  String FunctionCaseWillOverdueCode = "FunctionCaseWillOverdue";
  String FunctionCaseWillOverdue = "xcm.angustester.event.FunctionCaseWillOverdue";
  String FunctionCaseOverdueCode = "FunctionCaseOverdue";
  String FunctionCaseOverdue = "xcm.angustester.event.FunctionCaseOverdue";
  String FunctionCaseAssignmentCode = "FunctionCaseAssignment";
  String FunctionCaseAssignment = "xcm.angustester.event.FunctionCaseAssignment";
  String FunctionCaseModificationCode = "FunctionCaseModification";
  String FunctionCaseModification = "xcm.angustester.event.FunctionCaseModification";

  String ApisModificationCode = "ApisModification";
  String ApisModification = "xcm.angustester.event.ApisModification";

  String ScenarioModificationCode = "ScenarioModification";
  String ScenarioModification = "xcm.angustester.event.ScenarioModification";

  String ReportGenerationSuccessfulCode = "ReportGenerationSuccessful";
  String ReportGenerationSuccessful = "xcm.angustester.event.ReportGenerationSuccessful";

  String ScenarioMonitorFailedCode = "ScenarioMonitorFailed";
  String ScenarioMonitorFailed = "xcm.angustester.event.ScenarioMonitorFailed";
}
