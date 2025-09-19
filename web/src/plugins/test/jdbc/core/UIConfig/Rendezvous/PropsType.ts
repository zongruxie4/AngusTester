export type RendezvousConfig = {
    id: string; // 前端自动生成，用于给每条记录添加id
    target: 'RENDEZVOUS';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName:string;
    timeoutInMs: string;
    threads: string;
}
