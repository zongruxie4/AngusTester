import { GM } from '@xcan-angus/infra';

import Auth from './Auth';
import App from './App';
import Ai from './Ai';
import Analysis from './Analysis';
import Dept from './Dept';
import User from './User';
import Group from './Group';
import Event from './Event';
import Setting from './Setting';

const baseUrl = GM;

export const app = new App(baseUrl);
export const auth = new Auth(baseUrl);
export const ai = new Ai(baseUrl);
export const analysis = new Analysis(baseUrl);
export const dept = new Dept(baseUrl);
export const user = new User(baseUrl);
export const group = new Group(baseUrl);
export const event = new Event(baseUrl);
export const setting = new Setting(baseUrl);
