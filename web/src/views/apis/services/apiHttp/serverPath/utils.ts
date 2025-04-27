import { HttpServer } from './PropsType';

export const getServerData = (dataSource: HttpServer): string => {
  const url = dataSource.url;
  const replaced = url.replace(/\{[a-zA-Z0-9_]+\}/g, match => {
    const key = match.replace('{', '').replace('}', '');
    return dataSource.variables?.[key]?.default || '';
  });

  return replaced;
};
