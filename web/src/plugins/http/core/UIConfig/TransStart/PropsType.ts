export type TransStartConfig = {
    id: string; // 前端自动生成，用于给每条记录添加id
    target: 'TRANS_START';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
}
