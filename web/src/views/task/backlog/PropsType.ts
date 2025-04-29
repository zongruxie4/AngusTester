export type MemberCount = {
    assigneeId: string;
    assigneeName: string;
    assigneeAvatar: string;
    totalTaskNum: string;
    validTaskNum: string;
    evalWorkload: string;
    actualWorkload: string;
    completedNum: string;
    completedRate: string;
    overdueNum: string;
    overdueRate: string;
    progress: string;
}

export type SprintInfo = {
    id: string;
    projectId: string;
    projectName: string;
    name: string;
    status: {
        value: string;
        message: string;
    };
    authFlag: false;
    startDate: string;
    deadlineDate: string;
    ownerId: string;
    ownerName: string;
    ownerAvatar: string;
    taskPrefix: string;
    priority: {
        message: string;
        value: 'HIGH' | 'HIGHEST' | 'LOW' | 'LOWEST' | 'MEDIUM';
    };
    evalWorkloadMethod: {
        message: string;
        value: 'WORKING_HOURS' | 'STORY_POINT';
    };
    tenantId: string;
    tenantName: string;
    createdBy: string;
    createdByName: string;
    createdDate: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    taskNum: string;
    validNum: string;
    progress: {
        total: string;
        completed: string;
        completedRate: string;
    };
    members: {
        id: string;
        username: string;
        fullName: string;
        mobile: string;
        email: string;
        avatar: string;
    }[];
}
