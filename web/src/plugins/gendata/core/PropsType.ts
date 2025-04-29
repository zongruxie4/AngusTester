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
    sysAdminFlag: boolean;
    deptHeadFlag: boolean;
    enabled: boolean;
    source: {
      value: string;
      message: string;
    };
    lockedFlag: boolean;
    tenantId: string;
    tenantName: string;
    createdBy: string;
    createdDate: string;
    lastModifiedBy: string;
    lastModifiedDate: string;
    passwordStrength: {
      value: string;
      message: string;
    };
    passwordExpiredFlag: boolean;
    tenantRealNameStatus: {
      value: string;
      message: string;
    };
    onlineFlag: boolean;
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
