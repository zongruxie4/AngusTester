import { cookie } from '@xcan-angus/tools';

import enMessages from './en';
import zhMessages from './zh_CN';

// TODO 存在多个重复类
class UseI18n {
  private localeCookie:string;
  private messages:{[key:string]:any};
  constructor () {
    const localeCookie = cookie.get('localeCookie');
    if (!['en', 'zh_CN'].includes(localeCookie)) {
      this.localeCookie = 'zh_CN';
    }

    if (this.localeCookie === 'en') {
      this.messages = enMessages;
    } else {
      this.messages = zhMessages;
    }
  }

  get = (key:string):string => {
    const keys = key.split('.');
    const len = keys.length - 1;
    const temp = keys.reduce((prev, curv, index) => {
      if (index < len) {
        prev = prev[curv];
      }
      return prev;
    }, this.messages);

    return temp ? temp[keys[len]] : key;
  };
}

const i18n = new UseI18n();

const t = (key:string, payload?:{[key:string]:any}): string => {
  const message = i18n.get(key);
  if (!message) {
    return key;
  }

  if (!payload) {
    return message;
  }

  return Object.entries(payload).reduce((prev, [key, value]) => {
    const regexp = new RegExp('{' + key + '}', 'gi');
    return prev.replace(regexp, value);
  }, message);
};

export { t };
