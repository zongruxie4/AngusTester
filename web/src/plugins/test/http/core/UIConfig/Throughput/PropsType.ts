export type ThroughputConfig = {
    id:string;// 前端自动生成，用于给每条记录添加id
    target: 'THROUGHPUT';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    permitsPerSecond: string;
    timeoutInMs: string;
}
