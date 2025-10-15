
export type PipelineConfig = {
    target: 'JMS';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    server: {
        ip: string;
        port: string;
    };
    remoteUrl: string;
    localUrl: string;
    transmissionMode: 'ascii' | 'binary';
    method: 'RETR' | 'STOR';
    download: boolean;
    username:string;
    password:string;
    id: string;// 前端自动生成，用于给每条记录添加id
}

export type JmsPipelineInfo = PipelineConfig;

