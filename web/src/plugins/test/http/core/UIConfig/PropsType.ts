import { HTTPConfig, HTTPInfo } from './HTTPConfigs/PropsType';
import { WaitingTimeConfig } from '@/plugins/test/components/UIConfigComp/WaitingTime/PropsType';
import { RendezvousConfig } from '@/plugins/test/components/UIConfigComp/Rendezvous/PropsType';
import { TransStartConfig } from '@/plugins/test/components/UIConfigComp/TransStart/PropsType';
import { ThroughputConfig } from './Throughput/PropsType';
import { TransEndConfig } from '@/plugins/test/components/UIConfigComp/TransEnd/PropsType';

export type TargetKey = 'HTTP' | 'TRANS_START' | 'TRANS_END' | 'RENDEZVOUS' | 'WAITING_TIME' | 'THROUGHPUT' | 'TRANS_END';
export type PipelineConfig = HTTPConfig | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig | ThroughputConfig;
export type PipelineInfo = HTTPInfo | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig;
