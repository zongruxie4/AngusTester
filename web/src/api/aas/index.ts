import { GM, PUB_ESS } from '@xcan-angus/tools';

import AuthPolicy from './AuthPolicy';
import Auth from './Auth';
import App from './App';
import Setting from './setting';

const baseUrl = GM;

export const authPolicy = new AuthPolicy(baseUrl);
export const appInfo = new App(baseUrl);
export const auth = new Auth(baseUrl);
export const setting = new Setting(GM);
