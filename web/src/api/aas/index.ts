import { GM } from '@xcan-angus/tools';

import AuthPolicy from './AuthPolicy';
import Auth from './Auth';
import App from './App';

const baseUrl = GM;

export const authPolicy = new AuthPolicy(baseUrl);
export const appInfo = new App(baseUrl);
export const auth = new Auth(baseUrl);
