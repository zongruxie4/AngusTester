import { PUB_STORAGE, STORAGE } from '@xcan-angus/infra';
import File from './File';
import Space from './Space';
import PubSpace from './PubSpace';

const baseUrl = STORAGE;
const pubBaseUrl = PUB_STORAGE;

export const fileApi = new File(baseUrl);
export const space = new Space(baseUrl);
export const pubSpace = new PubSpace(pubBaseUrl);
