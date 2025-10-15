
export type PipelineConfig = {
    uuid?: string;
    activeKey?: string;
    target: 'SMTP';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    id: string;// 前端自动生成，用于给每条记录添加id,
    server: any;
    mail: any;
}

export type SmtpPipelineInfo = PipelineConfig;

