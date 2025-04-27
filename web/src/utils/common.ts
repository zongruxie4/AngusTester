const pureParams = (params: Record<string, any>): Record<string, any> => {
  const res = {};
  Object.keys(params).forEach(i => {
    if (params[i] ?? (params[i] !== '')) {
      res[i] = params[i];
    }
  });

  return res;
};

/**
 * file转base64
*/
const blobToDataURL = (blob: Blob):Promise<unknown> => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = function (evt) {
      const base64 = evt.target?.result;
      resolve(base64);
    };
    reader.readAsDataURL(blob);
  });
};

const formatBytes = (size = 0, decimal = 2):string => {
  if (size === 0) return '0 B';
  const c = 1024;
  const e = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const f = Math.floor(Math.log(size) / Math.log(c));
  return parseFloat((size / Math.pow(c, f)).toFixed(decimal)) + ' ' + e[f];
};

const formatBytesToUnit = (size = 0, unit = 'GB', decimal = 5):string => {
  if (size === 0) return '0';
  const unitMap = {
    B: 1, KB: 1024, MB: 1048576, GB: 1073741824, TB: 1099511627776, PB: 1125899906842624
  };
  const c = unitMap[unit] || 1073741824;
  return (size / c).toFixed(decimal);
};

// 根据关键词分组
const group = (data:Array<Record<string, any>>, key: string) => {
  const res:Record<string, Array<any>> = {};
  data.forEach((item) => {
    const keyValue = item[key];
    if (!res[keyValue]) {
      res[keyValue] = [item];
    } else {
      res[keyValue].push(item);
    }
  });
  return res;
};

const download = (data: BlobPart, filename: string): void => {
  const blob = new Blob([data], { type: 'application/octet-stream' });
  const downloadUrl = window.URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = downloadUrl;
  anchor.download = filename;
  anchor.click();
  window.URL.revokeObjectURL(downloadUrl);
};

const bgColor = {
  GET: 'bg-http-get',
  POST: 'bg-http-post',
  DELETE: 'bg-http-delete',
  PATCH: 'bg-http-patch',
  PUT: 'bg-http-put',
  OPTIONS: 'bg-http-options',
  HEAD: 'bg-http-head',
  TRACE: 'bg-http-trace'
};

const textColor = {
  GET: 'text-http-get',
  POST: 'text-http-post',
  DELETE: 'text-http-delete',
  PATCH: 'text-http-patch',
  PUT: 'text-http-put',
  OPTIONS: 'text-http-options',
  HEAD: 'text-http-head',
  TRACE: 'text-http-trace'
};

export {
  blobToDataURL,
  download,
  formatBytes,
  pureParams,
  formatBytesToUnit,
  group,
  bgColor,
  textColor
};
