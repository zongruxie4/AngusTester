export type TaskType = 'API_TEST'|'SCENARIO_TEST'|'BUG'|'STORY'|'TASK'
export interface TaskInfo {
    id: string,
    // targetId: string,
    // targetName: string,
    name: string,
    // code: string,
    // targetParentId: string,
    // targetParentName: string,
    taskType: {
        value: TaskType,
        message: string
    },
    // testType: {
    //     value: string,
    //     message: string
    // },
    // startDate: string,
    // deadlineDate: string,
    // canceledDate: string,
    // completedDate: string,
    // processedDate: string,
    // assignees: [
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     }
    // ],
    // confirmors: [
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     },
    //     {
    //         id: string,
    //         name: string
    //     }
    // ],
    // tags: [],
    // priority: {
    //     value: string,
    //     message: string
    // },
    // storyPoint: string,
    // execNo: string,
    // execId: string,
    // execUserId: string,
    // execUserName: string,
    // execCompletedDate: string,
    // status: {
    //     value: string,
    //     message: string
    // },
    // execStatus: string,
    // execResult: string,
    // failNum: string,
    // totalNum: string,
    // description: string,
    // currentAssociateType: [
    //     {
    //         value: string,
    //         message: string
    //     }
    // ],
    // confirmTask: true,
    // overdue: false,
    // createdBy: string,
    // createdByName: string,
    // createdDate: string,
    // lastModifiedDate: string,
    // lastModifiedBy: string,
    // lastModifiedByName: string
}
