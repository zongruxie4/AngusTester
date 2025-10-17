import { JDBCConfig, JDBCConfigInfo } from './JDBCConfigs/PropsType';
import { WaitingTimeConfig } from '@/plugins/test/components/UIConfigComp/WaitingTime/PropsType';
import { RendezvousConfig } from '@/plugins/test/components/UIConfigComp/Rendezvous/PropsType';
import { TransStartConfig } from '@/plugins/test/components/UIConfigComp/TransStart/PropsType';
import { TransEndConfig } from '@/plugins/test/components/UIConfigComp/TransEnd/PropsType';

export type TargetKey = 'JDBC' | 'TRANS_START' | 'TRANS_END' | 'RENDEZVOUS' | 'WAITING_TIME';

export type PipelineConfig = JDBCConfig | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig;
export type PipelineInfo = JDBCConfigInfo | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig;
