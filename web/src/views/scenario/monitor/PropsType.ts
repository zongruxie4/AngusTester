export type MonitorInfo = {
    id: string;
    scenarioId: string;
    description?: string;
    noticeSetting: {
        enabled: boolean;
        orgType: 'DEPT'|'GROUP'|'USER',
        orgs: string[] | {id: string; name: string}[]
    };
    serverSetting?: {url: string; description?: string; variables?: {[key: string]: {[key: string]: string}}}[];
    timeSetting: {
        createdAt: 'AT_SOME_DATE'| 'NOW'|'PERIODICALLY';
        createdAtSomeDate?: string;
        dayOfMonth?: string;
        dayOfWeek?: string;
        periodicCreationUnit?: 'DAILY'| 'MONTHLY'| 'WEEKLY';
        timeOfDay?: string;
    };
    name: string;
    createdByName: string;
    createdDate: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    lastMonitorDate?: string;
    lastMonitorHistoryId?: string;
    nextExecDate: string;
    projectId: string;
    scenarioName: string;
    status: {
        value: string;
        message: string;
    };
    failureMessage?: string;
    count?: {
        avgDelayTime: string;
        failureNum: string;
        last7DayNum: string;
        last7DaySuccessNum: string;
        last7DaySuccessRate: string;
        last24HoursNum: string;
        last24HoursSuccessNum: string;
        last24HoursSuccessRate: string;
        last30DayNum: string;
        last30DaySuccessNum: string;
        last30DaySuccessRate: string;
        maxDelayTime: string;
        minDelayTime: string;
        p50DelayTime: string;
        p75DelayTime: string;
        p90DelayTime: string;
        successNum: string;
        totalNum: string;
        successRate: string;
    }

}

export type IPane = {
    _id: string;// pane唯一标识
    name: string; // pane的tab文案
    value: string;// pane内部的组件标识
    closable?: boolean;// 是否允许关闭，true - 允许关闭，false - 禁止关闭
    forceRender?: boolean;// 被隐藏时是否渲染 DOM 结构，可选
    icon?: string;// tab文案前面的icon，可选
    active?: boolean; // 是否选中，添加不用设置，缓存时用于记录上次激活的tab pane，可选

    // 组件需要的属性
    data?: { [key: string]: any; };
};
