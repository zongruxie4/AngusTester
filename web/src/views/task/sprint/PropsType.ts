export type SprintInfo = {
    id: string;
    projectId: string;
    projectName: string;
    name: string;
    status: {
        value: string;
        message: string;
    };
    attachments: {
        id: string;
        name: string;
        url: string;
    }[];
    description: string;
    auth: boolean;
    startDate: string;
    deadlineDate: string;
    ownerId: string;
    ownerName: string;
    ownerAvatar: string;
    acceptanceCriteria: string;
    otherInformation: string;
    autoUpdateResultByExec: boolean;
    evalWorkloadMethod: {
        value: string;
        message: string;
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
    taskPrefix: string;
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
    showMembers: {
        id: string;
        username: string;
        fullName: string;
        mobile: string;
        email: string;
        avatar: string;
    }[];
    meetings?: {
        type: {
            value: string;
            message: string
        };
        date: string;
        time: string;
        location: string;
        moderator: {id:string;fullName:string;};
        participants: {id:string;fullName:string;}[];
        content: string;
    }[];
}
