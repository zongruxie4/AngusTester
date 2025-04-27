import { CTRL } from '@xcan-angus/tools';

import Exec from './Exec';
import Node from './Node';
import NodeInfo from './NodeInfo';
import Mock from './Mock';

const baseUrl = CTRL;

export const exec = new Exec(baseUrl);
export const nodeCtrl = new Node(baseUrl);
export const nodeInfo = new NodeInfo(baseUrl);
export const mock = new Mock(baseUrl);
