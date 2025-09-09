import { statusMap } from './httpSet';

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

export const getStatusText = (status:number):string => {
  let statusText = status + '';
  if (statusMap[status]) {
    statusText += ' ' + statusMap[status];
  }

  return statusText;
};
