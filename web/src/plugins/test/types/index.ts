import { JDBCConfig, JDBCConfigInfo } from '@/plugins/test/jdbc/core/UIConfig/JDBCConfigs/PropsType';
import { HTTPConfig, HTTPInfo } from '@/plugins/test/http/core/UIConfig/HTTPConfigs/PropsType';
import { FtpPipelineInfo } from '@/plugins/test/ftp/core/PropsType';
import { JmsPipelineInfo } from '@/plugins/test/jms/core/PropsType';
import { LdapPipelineInfo } from '@/plugins/test/ldap/core/PropsType';
import { MailPipelineInfo } from '@/plugins/test/mail/core/PropsType';
import { SmtpPipelineInfo } from '@/plugins/test/smtp/core/PropsType';
import { TcpPipelineInfo } from '@/plugins/test/tcp/core/PropsType';
import { WebSocketPipelineInfo } from '@/plugins/test/websocket/core/PropsType';

import { ScriptType as ScriptTypeInfra, AssertionCondition, EnumMessage, ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';
import { ScenarioType, PiplineTarget } from '@/enums/enums';

export type ScriptType = keyof typeof ScriptTypeInfra;
export type AssertCondition = keyof typeof AssertionCondition;

export type PluginType = 'Ftp' | 'Jms' | 'Ldap' | 'Mail' | 'Smtp' | 'Tcp' | 'WebSocket' | 'Jdbc' | 'Http';

 /**
* Pipline target types supported within a transaction
*/
export type PiplineTargetType = keyof typeof PiplineTarget;


export type WaitingTimeConfig = {
    id: string; // 前端自动生成，用于给每条记录添加id
    beforeName:string;
    transactionName:string;
    target: PiplineTarget.WAITING_TIME;
    name: string;
    description: string;
    enabled: boolean;
    maxWaitTimeInMs: string;
    minWaitTimeInMs?: string;// 固定等待时间不用传
}


export type RendezvousConfig = {
    id: string; // 前端自动生成，用于给每条记录添加id
    target: PiplineTarget.RENDEZVOUS;
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName:string;
    timeoutInMs: string;
    threads: string;
}

export type TransStartConfig = {
    id: string; // 前端自动生成，用于给每条记录添加id
    target: PiplineTarget.TRANS_START;
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
}

export type TransEndConfig = {
    id: string;
    target: PiplineTarget.TRANS_END;
    name: string;
    description: string;
    beforeName: string;
    transactionName: string;
    enabled: boolean;
}

/**
 * Timeline data interface
 * Contains timing information for all phases of an HTTP request
 * All values are in milliseconds
 */
export interface TimelineConfig {
    fetchStart: string;              // Start of fetch operation
    domainLookupStart: string;       // Start of DNS lookup
    domainLookupEnd: string;         // End of DNS lookup
    connectStart: string;            // Start of TCP connection
    connectEnd: string;              // End of TCP connection
    secureConnectionStart: string;   // Start of SSL/TLS handshake
    secureConnectionEnd: string;     // End of SSL/TLS handshake
    requestStart: string;            // Start of request
    responseStart: string;           // Start of response (TTFB)
    responseEnd: string;             // End of response (download complete)
    total: string;                   // Total duration
}

/**
 * Throughput controller configuration interface
 */
export interface ThroughputConfig {
    target: PiplineTarget.THROUGHPUT;       // Target type identifier
    name: string;               // Throughput controller name
    description: string;        // Optional description
    enabled: boolean;           // Whether this controller is enabled
    beforeName: string;         // Name of step before this controller
    transactionName: string;    // Associated transaction name
    permitsPerSecond: string;   // Maximum requests per second (rate limit)
    timeoutInMs: string;        // Max wait time in milliseconds before timeout
}

export type PipelineConfig = WebSocketPipelineInfo | TcpPipelineInfo | SmtpPipelineInfo | MailPipelineInfo | LdapPipelineInfo | JmsPipelineInfo | FtpPipelineInfo | HTTPConfig | JDBCConfig | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig;
export type PipelineInfo = WebSocketPipelineInfo | TcpPipelineInfo | SmtpPipelineInfo | MailPipelineInfo | LdapPipelineInfo | JmsPipelineInfo | FtpPipelineInfo | HTTPInfo | JDBCConfigInfo | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig;

export type ScenarioInfo = {
    description: string;
    favourite: boolean;
    follow: boolean;
    id: string;
    name: string;
    plugin: ScenarioType;
    moduleId?: string;
    script: {
        plugin: ScenarioType;
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineInfo[];
        };
        type: EnumMessage<ScriptTypeInfra>;
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
    plugin: ScenarioType;
    script: {
        plugin: ScenarioType;
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
    plugin: PluginType;
    moduleId?: string;
    script: {
        plugin: PluginType;
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineConfig[];
        };
        type: ScriptType;
        configuration: { [key: string]: any };
    };
    scriptId: string;
}

export type ExecContent = {
    finish: boolean;
    nodeId: string;
    timestamp0: string;
    name: string;
    extField: string;
    iteration: string;
    key: string;
    content: {
        name: string;
        success: boolean;
        failMessage: string;
        request0: {
            method: string;
            url: string;
            headers: {[key:string]:string};
            forms: string;
            rawContent: string;
            size: string;
            headerArray: string[];
            queryString:string;
        };
        response: {
            status: string;
            size: string;
            bodySize: string;
            headers: {[key:string]:string};
            rawContent: string;
            timeline: {
                fetchStart: string;
                domainLookupStart: string;
                domainLookupEnd: string;
                connectStart: string;
                connectEnd: string;
                secureConnectionStart: string;
                secureConnectionEnd: string;
                requestStart: string;
                responseStart: string;
                responseEnd: string;
                total: string;
            };
            headerArray: string[];
        };
        assertions: {
            name: string;
            enabled: boolean;
            type: {
                value: 'STATUS' | 'HEADER' | 'BODY' | 'BODY_SIZE' | 'SIZE' | 'DURATION';
                message: string;
            };
            assertionCondition: {
                value: AssertCondition;
                message: string;
            };
            expected: string;
            success: boolean;
            ignore: boolean
            actualExpected: string;
            condition?: string;
            actualCondition?: string;
            parameterName: string;
            result?: {
                failure: boolean;
                message: string;
            };
            extraction?:{
                name: string;
                method: {
                    value: string;
                    message: string;
                },
                expression: string;
                matchItem: string;
                defaultValue:string;
                location: string;
                parameterName: string;
                value: string;
                finalValue: string;
            }
        }[];
    };
    timestamp: string;
}

export type DatasetItem = {
    createdBy: string;
    createdByName: string;
    createdDate: string;
    description: string;
    extracted: boolean;
    extraction: {
        defaultValue: string;
        expression: string;
        failureMessage: string;
        finalValue: string;
        matchItem: string;
        method: EnumMessage<ExtractionMethod>;
        name: string;
        source: ExtractionSource;
        value: string;
        fileType: EnumMessage<ExtractionFileType>;
        path: string;
        encoding: Encoding;
        quoteChar: string;
        escapeChar: string;
        separatorChar: string;
        rowIndex: string;
        columnIndex: string;
        select: string;
        parameterName: string;
        datasource: {
            type: { value: string; message: string; };
            username: string;
            password: string;
            jdbcUrl: string;
        };
    };
    id: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    name: string;
    parameters: {
        name: string;
        value: string;
    }[];
    projectId: string;
    source?: string;// 前端自动添加
    previewFlag?: boolean;// 前端自动添加
};
