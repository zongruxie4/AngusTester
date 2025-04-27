import { JDBCConfig, JDBCConfigInfo } from './JDBCConfigs/PropsType';
import { WaitingTimeConfig } from './WaitingTime/PropsType';
import { RendezvousConfig } from './Rendezvous/PropsType';
import { TransStartConfig } from './TransStart/PropsType';
import { TransEndConfig } from './TransEnd/PropsType';

export type TargetKey = 'JDBC' | 'TRANS_START' | 'TRANS_END' | 'RENDEZVOUS' | 'WAITING_TIME';

export type PipelineConfig = JDBCConfig | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig;
export type PipelineInfo = JDBCConfigInfo | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig;
