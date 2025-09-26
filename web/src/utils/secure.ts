import { decode as dt, encode as et, toUint8Array as tua } from 'js-base64';

const toUint8Array = (str: string): Uint8Array => {
  return tua(str);
};

const decode = (value: string, native = false): { name: string; value: string; } | string => {
  if (native) {
    return dt(value);
  }

  const slices = dt(value).split(':');
  return {
    name: slices[0],
    value: slices[1]
  };
};

const encode = (name = '', value = ''): string => {
  if (!value) {
    return et(name);
  }

  if (!name && !value) {
    return '';
  }

  return et(name + ':' + value);
};

export { decode, encode, toUint8Array };
