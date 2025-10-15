
export type PipelineConfig = {
    target: 'MAIL';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    server: {
        server: string;
        port: string;
        readTimeout: string;
        connectTimeout: string;
        useAuth: boolean;
        security?: {
            use: 'NONE'|'USE_SSL'|'USE_START_TLS',
            trustStoreBase64Content?: string;
            tlsProtocols: string;
            trustStorePath: string;
            enforceStartTLS: boolean;
            trustAllCerts: boolean;
            useLocalTrustStore: boolean;
        },
        username:string;
        password:string;
    };
    transmissionMode: 'all'|'local'|'content';
    id: string;// 前端自动生成，用于给每条记录添加id
    messageType?: 'all'|'other',
    protocol: 'POP3'|'IMAP',
    mail: {
        numMessages: number;
        deleteMessage: boolean;
        storeMimeMessage: boolean;
        folder: string;
    },

}

export type MailPipelineInfo = PipelineConfig;

