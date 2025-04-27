import { PUB_STORE, STORE } from '@xcan-angus/tools';

import PubStore from './PubStore';
import Edition from './Edition';
import Store from './Store';

const pubBaseUrl = PUB_STORE;

export const pubStore = new PubStore(pubBaseUrl);
export const edition = new Edition(STORE);
export const store = new Store(STORE);
