import { PUB_ESS, ESS } from '@xcan-angus/infra';

import PubStore from './PubStore';
import Edition from './Edition';
import Store from './Store';

const pubBaseUrl = PUB_ESS;

export const pubStore = new PubStore(pubBaseUrl);
export const edition = new Edition(ESS);
export const store = new Store(ESS);
