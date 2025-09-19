export type FtpInfo = {
    target: 'FTP';
    name: string;
    description: string;
    enabled: boolean;
    server: {
        server: string;
        port: string;
        username: string;// length - 400
        password: string;// length - 4096
        connectTimeout: string;// 1s ～ 86400s
        readTimeout: string;// 1s ～ 86400s
    };
    uploadFile: boolean;
    uploadFileSource: 'REMOTE_FILE' | 'REMOTE_URL' | 'LOCAL_FILE';
    remoteFileName: string;
    remoteFileUrl: string;
    localFileName: string;
    localFileContent: string;
    localFileContentEncoding: 'none' | 'base64' | 'gzip_base64';
    binaryMode: boolean;
    id: string;// 前端自动生成，用于给每条记录添加id
}
