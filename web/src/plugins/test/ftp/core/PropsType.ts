export type ScriptType = 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';

export type PipelineConfig = {
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

export type FtpPipelineInfo = PipelineConfig;

export type ScenarioInfo = {
    description: string;
    favourite: boolean;
    follow: boolean;
    id: string;
    name: string;
    plugin: 'Ftp';
    script: {
        plugin: 'Ftp';
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineConfig[];
        };
        type: { value: ScriptType; message: string; };
        configuration?: {
            duration: {
                unit: string;
                value: string
            };
            iterations: string;
            nodeSelectors: {
                appNodeIds: [];
                availableNodeIds: [];
                num: string;
                strategy: {
                    cpuIdleRate: string;
                    cpuSpec: string;
                    diskIdleRate: string;
                    diskSpec: string;
                    enabled: boolean;
                    idleRateEnabled: boolean;
                    lastExecuted: boolean;
                    maxTaskNum: string;
                    memoryIdleRate: string;
                    memorySpec: string;
                    specEnabled: boolean
                }
            };
            onError: {
                action: string;
                sampleError: boolean;
                sampleErrorNum: string
            };
            priority: string;
            reportInterval: {
                unit: string;
                value: string
            };
            shutdownTimeout: {
                unit: string;
                value: string
            };
            startAtDate: string;
            startMode: string;
            thread: {
                rampDownInterval: {
                    unit: string;
                    value: string
                };
                rampDownThreads: string;
                rampUpInterval: {
                    unit: string;
                    value: string
                };
                rampUpThreads: string;
                threads: string
            };
            variables: {
                description: string;
                enabled: boolean;
                name: string;
                value: string
            }[];
        };
    };
    scriptId: string;
    scriptName: string;
}

export type ScenarioConfig = {
    description: string;
    favourite: boolean;
    follow: boolean;
    id: string;
    name: string;
    plugin: 'Ftp';
    script: {
        plugin: 'Ftp';
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineConfig[];
        };
        type: ScriptType;
        configuration: {
            duration: string;
            iterations: string;
            nodeSelectors: {
                appNodeIds: [];
                availableNodeIds: [];
                num: string;
                strategy: {
                    cpuIdleRate: string;
                    cpuSpec: string;
                    diskIdleRate: string;
                    diskSpec: string;
                    enabled: boolean;
                    idleRateEnabled: boolean;
                    lastExecuted: boolean;
                    maxTaskNum: string;
                    memoryIdleRate: string;
                    memorySpec: string;
                    specEnabled: boolean
                }
            };
            onError: {
                action: string;
                sampleError: boolean;
                sampleErrorNum: string
            };
            priority: string;
            reportInterval: {
                unit: string;
                value: string
            };
            shutdownTimeout: {
                unit: string;
                value: string
            };
            startAtDate: string;
            startMode: string;
            thread: {
                rampDownInterval: string;
                rampDownThreads: string;
                rampUpInterval: string;
                rampUpThreads: string;
                threads: string;
                resetAfterRamp: boolean;
            };
            variables: {
                description: string;
                enabled: boolean;
                name: string;
                value: string
            }[];
        };
    };
    scriptId: string;
    scriptName: string;
}

export type SaveFormData = {
    description: string;
    projectId: string;
    id: string;
    name: string;
    plugin: 'Ftp';
    script: {
        plugin: 'Ftp';
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineConfig[];
        };
        type: ScriptType;
        configuration: { [key: string]: any };
    };
    scriptId: string;
}
