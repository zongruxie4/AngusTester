import { GM } from '@xcan-angus/tools';

import Auth from './Auth';
import App from './App';
import Setting from './Setting';
import Ai from './Ai';
import Analysis from './Analysis';
import Dept from './Dept';
import User from './User';
import Group from './Group';

const baseUrl = GM;

export const appInfo = new App(baseUrl);
export const auth = new Auth(baseUrl);
export const setting = new Setting(GM);
export const ai = new Ai(baseUrl);
export const analysis = new Analysis(baseUrl);
export const dept = new Dept(baseUrl);
export const user = new User(baseUrl);
export const group = new Group(baseUrl);
