export type SMTPInfo = {
    target: 'SMTP';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    id: string;// 前端自动生成，用于给每条记录添加id
}
