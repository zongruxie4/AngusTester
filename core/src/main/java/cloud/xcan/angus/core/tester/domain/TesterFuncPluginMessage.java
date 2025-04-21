package cloud.xcan.angus.core.tester.domain;

/**
 * ATF001 ~ ATF100
 */
public interface TesterFuncPluginMessage {

  /*<******************Plan#Auth(ATF021 ~ ATF030)******************>*/
  String FUNC_PLAN_NO_AUTH_CODE = "ATF021";
  String FUNC_PLAN_NO_AUTH = "xcm.tester.func.plan.no.auth.t";

  /*Plan*/
  /*<******************Plan(ATF031 ~ ATF040)******************>*/
  String FUNC_PLAN_NAME_REPEATED_T = "xcm.tester.func.plan.name.repeated.t";
  //String FUNC_PLAN_DATE_RANGE_ERROR_T = "xcm.tester.func.plan.date.range.error.t";
  String PLAN_STATUS_MISMATCH_T = "xcm.tester.func.plan.status.mismatch.t";
  String PLAN_REVIEW_NOT_ENABLED_CODE = "ATF031";
  String PLAN_REVIEW_NOT_ENABLED = "xcm.tester.func.plan.review.not.enabled";
  String PLAN_NOT_STARTED_CODE = "ATF032";
  String PLAN_NOT_STARTED = "xcm.tester.func.plan.not.started";
  String PLAN_CASE_NOT_COMPLETED = "xcm.tester.func.plan.case.not.completed";

  /*Review*/
  /*<******************Review(ATF041 ~ ATF050)******************>*/
  String FUNC_REVIEW_NAME_REPEATED_T = "xcm.tester.func.review.name.repeated.t";
  String REVIEW_STATUS_MISMATCH_T = "xcm.tester.func.review.status.mismatch.t";
  String REVIEW_CANNOT_END = "xcm.tester.func.review.cannot.end";
  String REVIEW_NOT_STARTED_CODE = "ATF052";
  String REVIEW_NOT_STARTED = "xcm.tester.func.review.not.started";

  /*Case*/
  /*<******************Case(ATF051 ~ ATF060)******************>*/
  String CASE_NAME_REPEATED_T = "xcm.tester.case.name.repeated.t";
  String CASE_DEADLINE_GREATER_PLAN_T = "xcm.tester.case.deadline.greater.plan.t";
  String CASE_NOT_REVIEWED_CODE = "ATF051";
  String CASE_NOT_REVIEWED_T = "xcm.tester.case.not.reviewed.t";
  String CASE_CAN_NOT_REVIEWED_T = "xcm.tester.case.can.not.reviewed.t";
  String CASE_REVIEW_REPEATED_T = "xcm.tester.case.review.is.repeated.t";

  /*<******************Case#Favourite(ATF071 ~ ATS075)******************>*/
  String CASE_FAVOURITE_REPEATED_T = "xcm.tester.case.favorite.repeated.t";
  /*<******************Case#Follow(ATF076 ~ ATS080)******************>*/
  String CASE_FOLLOW_REPEATED_T = "xcm.tester.case.follow.repeated.t";

  /**********************ExportHeaderName********************/
  //String EXPORT_FUNC_CREATION_STATISTICS = "xcm.export.funcCreationStatistics";
  String EXPORT_CASE_STATISTICS = "xcm.export.caseStatistics";
  //String EXPORT_TESTER_STATISTICS = "xcm.export.testerStatistics";
  //String EXPORT_TESTER_PROGRESS_STATISTICS = "xcm.export.testerProgressStatistics";
  String EXPORT_CASE_LIST = "xcm.export.caseListExport";
  //--------------------------ExportAnalysisCaseHeaders--------------
  String EXPORT_ANALYSIS_CASE_PROGRESS = "xcm.export.analysisCase.progress";
  String EXPORT_ANALYSIS_CASE_BURNDOWN = "xcm.export.analysisCase.burndown";
  String EXPORT_ANALYSIS_CASE_WORKLOAD = "xcm.export.analysisCase.workload";
  String EXPORT_ANALYSIS_CASE_OVERDUE_ASSESSMENT = "xcm.export.analysisCase.overdueAssessment";
  String EXPORT_ANALYSIS_CASE_SUBMITTED_BUGS = "xcm.export.analysisCase.submittedBugs";
  String EXPORT_ANALYSIS_CASE_TESTING_EFFICIENCY = "xcm.export.analysisCase.testingEfficiency";
  String EXPORT_ANALYSIS_CASE_CORE_KPI = "xcm.export.analysisCase.coreKpi";
  String EXPORT_ANALYSIS_CASE_REVIEW_EFFICIENCY = "xcm.export.analysisCase.reviewEfficiency";
  String EXPORT_ANALYSIS_CASE_BACKLOG_CASES = "xcm.export.analysisCase.backlogCases";
  String EXPORT_ANALYSIS_CASE_RECENT_DELIVERY = "xcm.export.analysisCase.recentDelivery";
  String EXPORT_ANALYSIS_CASE_LEAD_TIME = "xcm.export.analysisCase.leadTime";
  String EXPORT_ANALYSIS_CASE_UNPLANNED_CASE = "xcm.export.analysisCase.unplannedCases";
  String EXPORT_ANALYSIS_CASE_GROWTH_TREND = "xcm.export.analysisCase.caseGrowthTrend";
  String EXPORT_ANALYSIS_CASE_RESOURCE_CREATION = "xcm.export.analysisCase.resourceCreation";

}
