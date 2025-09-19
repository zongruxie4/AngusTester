export type WaitingTimeConfig = {
    id: string; // 前端自动生成，用于给每条记录添加id
    beforeName:string;
    transactionName:string;
    target: 'WAITING_TIME';
    name: string;
    description: string;
    enabled: boolean;
    maxWaitTimeInMs: string;
    minWaitTimeInMs?: string;// 固定等待时间不用传
}
