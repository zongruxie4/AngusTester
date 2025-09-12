export type TaskInfo = {
    activityNum?: string;
    actualWorkload: string;
    assigneeId: string;
    assigneeName: string;
    attachments: {
        name: string;
        url: string;
    }[];
    subTaskProgress: {
        completed: string;
        completedRate: string;
        total: string
    };
    subTaskInfos: {
        assigneeId: string;
        assigneeName: string;
        code: string;
        confirmerId: string;
        confirmerName: string;
        createdBy: string;
        createdByName: string;
        createdDate: string;
        deadlineDate: string;
        id: string;
        lastModifiedBy: string;
        lastModifiedByName: string;
        lastModifiedDate: string;
        name: string;
        priority: string;
        projectId: string;
        sprintId: string;
        startDate: string;
        taskType: string;
        testType: string;
    }[];
    assigneeAvatar: string;
    canceledDate: string;
    code: string;
    commentNum: string;
    completedDate: string;
    confirmTask: boolean;
    confirmedDate: string;
    moduleId: string;
    moduleName: string;
    confirmerId: string;
    confirmerName: string;
    createdBy: string;
    createdByName: string;
    createdDate: string;
    currentAssociateType: { message: string; value: string; }[];
    deadlineDate: string;
    description: string;
    evalWorkload: string;
    evalWorkloadMethod: {
        message: string;
        value: 'WORKING_HOURS' | 'STORY_POINT';
    };
    execBy: string;
    execByName: string;
    execDate: string;
    execFailureMessage: string;
    execId: string;
    execName: string;
    execResult: {
        value: 'SUCCESS' | 'FAIL',
        message: string;
    };
    execTestFailureNum: string;
    execTestNum: string;
    failNum: string;
    favourite: boolean;
    follow: boolean;
    id: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    name: string;
    overdue: boolean;
    priority: {
        message: string;
        value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
    };
    processedDate: string;
    projectId: string;
    projectName: string;
    serviceId: string;
    sprintId: string;
    sprintName: string;
    startDate: string;
    scriptId: string;
    scriptName: string;
    status: {
        message: string;
        value: 'CANCELED' | 'COMPLETED' | 'CONFIRMING' | 'IN_PROGRESS' | 'PENDING';
    };
    tags: {
        id: string;
        name: string;
    }[];
    refTaskInfos: {
        id: string;
        name: string;
        code: string;
        projectId: string;
        sprintId: string;
        taskType: {
            message: string;
            value: 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'TASK' | 'REQUIREMENT';
        };
    }[];
    refCaseInfos: {
        id: string;
        name: string;
        code: string;
        projectId: string;
        sprintId: string;
    }[];
    targetId: string;
    targetName: string;
    targetParentId: string;
    targetParentName: string;
    taskType: {
        message: string;
        value: 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'TASK' | 'REQUIREMENT';
    };
    testType: {
        message: string;
        value: 'FUNCTIONAL' | 'PERFORMANCE' | 'STABILITY' | 'CUSTOMIZATION';
    };
    totalNum: string;
    linkUrl?: string;// 跳转到详情页面的链接地址，前端自动把所需参数添加到query
    bugLevel?: { value: 'CRITICAL' | 'MAJOR'| 'MINOR' | 'TRIVIAL', message: string };
    testerId?: string;
    missingBug?: boolean;
    testerName?: string;
    softwareVersion?: string;
}
