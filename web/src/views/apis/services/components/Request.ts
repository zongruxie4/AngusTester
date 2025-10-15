export const baseSource = [
  {
    name: 'Request URL',
    key: 'url',
    value: ''
  },
  {
    name: 'Request Method',
    key: 'method',
    value: ''
  },
  {
    name: 'Status Code',
    key: 'statusText',
    value: ''
  }
];

export const StatusMap = {
  100: 'Continue',
  101: 'Switching Protocols',
  200: 'OK',
  201: 'Created',
  202: 'Accepted',
  203: 'Non-Authoritative info',
  204: 'No content',
  205: 'Reset content',
  206: 'Partial content',
  300: 'Multiple Choices',
  301: 'Moved Permanently',
  302: 'Found',
  303: 'See Other',
  304: 'Not Modified',
  305: 'Use Proxy',
  306: 'Unused',
  307: 'Temporary Redirect',
  400: 'Bad Request',
  401: 'Unauthorized',
  402: 'Payment Required',
  403: 'Forbidden',
  404: 'Not Found',
  405: 'Method Not Allowed',
  406: 'Not Acceptable',
  407: 'Proxy Authentication Required',
  408: 'Request Time-out',
  409: 'Conflict',
  410: 'Gone',
  411: 'Length Required',
  412: 'precondition Failed',
  413: 'Request Entity Too Large',
  414: 'Request-URI Too Large',
  415: 'Unsupported Media Type',
  416: 'Requested range not satisfiable',
  417: 'Expectation Failed',
  500: 'Internal Server error',
  501: 'Not Implemented',
  502: 'Bad Gateway',
  503: 'services Unavailable',
  504: 'Gateway Time-out',
  505: 'http version not supported'
};
export const getStatusText = (status:number):string => {
  let statusText = status + '';
  if (StatusMap[status]) {
    statusText += ' ' + StatusMap[status];
  }

  return statusText;
};
