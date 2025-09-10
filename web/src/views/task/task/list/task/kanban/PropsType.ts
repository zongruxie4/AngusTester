// TODO 重复代码

export type ActivityItem = {
    id: string;
    projectId: string;
    userId: string;
    fullName: string;
    avatar: string;
    targetId: string;
    parentTargetId: string;
    targetType: {
        value: string;
        message: string
    };
    targetName: string;
    optDate: string;
    description: string;
    detail: string
}
