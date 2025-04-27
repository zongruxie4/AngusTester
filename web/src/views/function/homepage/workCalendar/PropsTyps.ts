export type DataItem = {
    id: string;
    name: string;
    code: string;
    projectId: string;
    sprintId: string;
    sprintName: string;
    targetId: string;
    targetParentId: string;
    taskType: {
        message: string;
        value: 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'TASK' | 'REQUIREMENT';
    };
    testType: {
        message: string;
        value: 'FUNCTIONAL' | 'PERFORMANCE' | 'STABILITY' | 'CUSTOMIZATION';
    };
    deadlineDate: string;
    assigneeId: string;
    assigneeName: string;
    priority: {
        message: string;
        value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
    };
    evalWorkloadMethod: {
        message: string;
        value: 'WORKING_HOURS' | 'STORY_POINT';
    };
    testResult: {
        value: string;
        message: string;
    };
    execResult: {
        value: 'SUCCESS' | 'FAIL',
        message: string;
    };
    execFailureMessage: string;
    execTestNum: string;
    execTestFailureNum: string;
    execId: string;
    execName: string;
    execBy: string;
    execByName: string;
    execDate: string;
    failNum: string;
    totalNum: string;
    overdueFlag: false;
    createdBy: string;
    createdByName: string;
    createdDate: string;
}
