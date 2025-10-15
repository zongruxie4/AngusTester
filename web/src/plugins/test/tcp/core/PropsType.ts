
export type PipelineConfig = {
    target: 'TCP';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    server: {
        server: string;
        port: string;
        connectTimeout: string;// 1s ～ 86400s
        responseTimeout: string;// 1s ～ 86400s
    };
    data: string; // 2097152字节
    dataEncoding: 'none' | 'base64' | 'gzip_base64';
    setting: {
        tcpClientImplClass: string;
        reUseConnection: boolean;
        closeConnection: boolean;
        soLinger: string;// 0s ～ 86400s
        tcpNoDelay: boolean;
        tcpCharset: string;// 80字节
        eolByte: string;// number -128 ~ 127
        eomByte: string;// number -128 ~ 127
        binaryPrefixLength: string;// number
    };
    variables?: {
        [key: string]: string;
    }[];
    id: string;// 前端自动生成，用于给每条记录添加id
}

export type TcpPipelineInfo = PipelineConfig;

