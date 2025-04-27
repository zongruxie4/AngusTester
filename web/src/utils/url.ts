
export const parseQuery = (url) => {
  try {
    const urlObj = new URL(url);
    const queryUrl = urlObj.search.replace('?', '');
    const result = {};
    const querries = queryUrl.split('&');
    querries.forEach(query => {
      const keyValue = query.split('=');
      result[keyValue[0]] = keyValue[1];
    });
    return result;
  } catch {
    return {};
  }
};
