export type ResourceInfo = {
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
