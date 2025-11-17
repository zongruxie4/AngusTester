export type SummaryInfo = {
  allSprint: string;
  sprintByLast7Days: string;
  sprintByLastMonth: string;
  allTask: string;
  taskByLast7Days: string;
  taskByLastMonth: string;
  taskByOverdue: string;
  allTag: string;
  tagByLast7Days: string;
  tagByLastMonth: string;
  // Optional backlog trend fields
  backlogByLast7Days?: string;
  backlogByLastMonth?: string;
  // Optional meeting summary fields
  allMeeting?: string;
  meetingByLast7Days?: string;
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
    DESIGN: string;
  };
  // Backlog breakdown by type (mirrors taskByType keys)
  backlogByType?: {
    STORY: string;
    REQUIREMENT: string;
    TASK: string;
    BUG: string;
    DESIGN: string;
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
