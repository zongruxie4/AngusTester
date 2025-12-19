import { encode as et } from 'js-base64';

export type UserInfo = {
    id: string;
    username: string;
    fullName: string;
    firstName: string;
    lastName: string;
    itc: string;
    country: string;
    mobile: string;
    email: string;
    landline: string;
    avatar: string;
    title: string;
    gender: {
      value: string;
      message: string;
    };
    address: string;
    sysAdmin: boolean;
    deptHead: boolean;
    enabled: boolean;
    source: {
      value: string;
      message: string;
    };
    locked: boolean;
    tenantId: string;
    tenantName: string;
    createdBy: string;
    createdDate: string;
    modifiedBy: string;
    modifiedDate: string;
    passwordStrength: {
      value: string;
      message: string;
    };
    passwordExpired: boolean;
    tenantRealNameStatus: {
      value: string;
      message: string;
    };
    online: boolean;
    onlineDate: string;
    preference: {
      themeCode: string;
      language: {
        value: string;
        message: string;
      };
      defaultTimeZone: string;
    };
  };

// 加密
const encode = (name = '', value = ''): string => {
  if (!value) {
    return et(name);
  }

  if (!name && !value) {
    return '';
  }

  return et(name + ':' + value);
};

export {
  encode
};
