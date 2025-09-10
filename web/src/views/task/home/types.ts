export type SummaryInfo = {
  allSprint: string;
  sprintByLastWeek: string;
  sprintByLastMonth: string;
  allTask: string;
  taskByLastWeek: string;
  taskByLastMonth: string;
  taskByOverdue: string;
  allTag: string;
  tagByLastWeek: string;
  tagByLastMonth: string;
  // Optional backlog trend fields
  backlogByLastWeek?: string;
  backlogByLastMonth?: string;
  // Optional meeting summary fields
  allMeeting?: string;
  meetingByLastWeek?: string;
  meetingByLastMonth?: string;
  // Total backlog count across types
  allBacklog?: string;
  sprintByStatus: {
    PENDING: string;
    IN_PROGRESS: string;
    COMPLETED: string;
    BLOCKED: string;
  };
  taskByType: {
    STORY: string;
    REQUIREMENT: string;
    TASK: string;
    BUG: string;
    API_TEST: string;
    SCENARIO_TEST: string;
  };
  // Backlog breakdown by type (mirrors taskByType keys)
  backlogByType?: {
    STORY: string;
    REQUIREMENT: string;
    TASK: string;
    BUG: string;
    API_TEST: string;
    SCENARIO_TEST: string;
  };
  taskByStatus: {
    PENDING: string;
    IN_PROGRESS: string;
    CONFIRMING: string;
    COMPLETED: string;
    CANCELED: string;
  };
  taskByPriority: {
    HIGHEST: string;
    HIGH: string;
    MEDIUM: string;
    LOW: string;
    LOWEST: string;
  };
};
