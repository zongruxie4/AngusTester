package cloud.xcan.sdf.core.angustester.domain;

public interface TesterEventMessage {

  String TaskWillOverdueCode = "TaskWillOverdue";
  String TaskWillOverdue = "xcm.altester.event.TaskWillOverdue";
  String TaskOverdueCode = "TaskOverdue";
  String TaskOverdue = "xcm.altester.event.TaskOverdue";
  String TaskAssignmentCode = "TaskAssignment";
  String TaskAssignment = "xcm.altester.event.TaskAssignment";
  String TaskPendingConfirmationCode = "TaskPendingConfirmation";
  String TaskPendingConfirmation = "xcm.altester.event.TaskPendingConfirmation";
  String TaskModificationCode = "TaskModification";
  String TaskModification = "xcm.altester.event.TaskModification";

  String FunctionCaseWillOverdueCode = "FunctionCaseWillOverdue";
  String FunctionCaseWillOverdue = "xcm.altester.event.FunctionCaseWillOverdue";
  String FunctionCaseOverdueCode = "FunctionCaseOverdue";
  String FunctionCaseOverdue = "xcm.altester.event.FunctionCaseOverdue";
  String FunctionCaseAssignmentCode = "FunctionCaseAssignment";
  String FunctionCaseAssignment = "xcm.altester.event.FunctionCaseAssignment";
  String FunctionCaseModificationCode = "FunctionCaseModification";
  String FunctionCaseModification = "xcm.altester.event.FunctionCaseModification";

  String ApisModificationCode = "ApisModification";
  String ApisModification = "xcm.altester.event.ApisModification";

  String ScenarioModificationCode = "ScenarioModification";
  String ScenarioModification = "xcm.altester.event.ScenarioModification";

  String ReportGenerationSuccessfulCode = "ReportGenerationSuccessful";
  String ReportGenerationSuccessful = "xcm.altester.event.ReportGenerationSuccessful";

  String ScenarioMonitorFailedCode = "ScenarioMonitorFailed";
  String ScenarioMonitorFailed = "xcm.altester.event.ScenarioMonitorFailed";
}
