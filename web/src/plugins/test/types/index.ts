import { JDBCConfig, JDBCConfigInfo } from '@/plugins/test/jdbc/core/UIConfig/JDBCConfigs/PropsType';
import { HTTPConfig, HTTPInfo } from '@/plugins/test/http/core/UIConfig/HTTPConfigs/PropsType';
import { FtpPipelineInfo } from '@/plugins/test/ftp/core/PropsType';
import { JmsPipelineInfo } from '@/plugins/test/jms/core/PropsType';
import { LdapPipelineInfo } from '@/plugins/test/ldap/core/PropsType';
import { MailPipelineInfo } from '@/plugins/test/mail/core/PropsType';
import { SmtpPipelineInfo } from '@/plugins/test/smtp/core/PropsType';
import { TcpPipelineInfo } from '@/plugins/test/tcp/core/PropsType';
import { WebSocketPipelineInfo } from '@/plugins/test/websocket/core/PropsType';

import { ScriptType as ScriptTypeInfra } from '@xcan-angus/infra';
import { ScenarioType } from '@/enums/enums';

export type ScriptType = keyof typeof ScriptTypeInfra;

export type PluginType = 'Ftp' | 'Jms' | 'Ldap' | 'Mail' | 'Smtp' | 'Tcp' | 'WebSocket';


export type WaitingTimeConfig = {
    id: string; // 前端自动生成，用于给每条记录添加id
    beforeName:string;
    transactionName:string;
    target: 'WAITING_TIME';
    name: string;
    description: string;
    enabled: boolean;
    maxWaitTimeInMs: string;
    minWaitTimeInMs?: string;// 固定等待时间不用传
}

export type RendezvousConfig = {
    id: string; // 前端自动生成，用于给每条记录添加id
    target: 'RENDEZVOUS';
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
    target: 'TRANS_START';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
}

export type TransEndConfig = {
    id: string;
    target: 'TRANS_END';
    name: string;
    description: string;
    beforeName: string;
    transactionName: string;
    enabled: boolean;
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
    script: {
        plugin: ScenarioType;
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineInfo[];
        };
        type: {value:ScriptType;message:string;};
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