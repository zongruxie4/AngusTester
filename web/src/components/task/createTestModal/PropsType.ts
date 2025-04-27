// 优先级
export type Priority = 'HIGH'| 'HIGHEST'| 'LOW'| 'LOWEST'| 'MEDIUM'

// 测试类型
export type TestTyep = 'FUNCTIONAL'| 'PERFORMANCE'| 'STABILITY'

// 传给后台的数据结构
export interface FormData {
    deadlineDate: string;
    priority:Priority;
    assigneeId: string|undefined;
    testType?: TestTyep;
    selectOptions?:{id: string; name: string }[];
    duration?: string;
    threads?: number;
}
