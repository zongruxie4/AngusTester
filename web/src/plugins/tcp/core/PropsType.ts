export type ScriptType = 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';

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

export type SceneInfo = {
    description: string;
    favouriteFlag: boolean;
    followFlag: boolean;
    id: string;
    name: string;
    plugin: 'Tcp';
    script: {
        plugin: 'Tcp';
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

export type SceneConfig = {
    description: string;
    favouriteFlag: boolean;
    followFlag: boolean;
    id: string;
    name: string;
    plugin: 'Tcp';
    script: {
        plugin: 'Tcp';
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
    plugin: 'Tcp';
    script: {
        plugin: 'Tcp';
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineConfig[];
        };
        type: ScriptType;
        configuration: { [key: string]: any };
    };
    scriptId: string;
}
