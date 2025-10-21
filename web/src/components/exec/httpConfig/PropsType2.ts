
export type RequestBodyFormItem = {
  id:string;
  name: string;
  type: 'string' | 'number' | 'integer' | 'boolean' | 'array'|'file'|'file(array)';
  description?: string;
  enabled?: boolean;
  format?: 'binary'|'string';
  contentType?: string;
  contentEncoding?: 'gzip_base64';
  fileName?: string;
  value?: string;
}

export type FileContent = {
  size: number;
  type: 'string' | 'array';
  format: 'binary'|'string';
  contentType: string;
  contentEncoding: 'gzip_base64';
  value: string;
  fileName: string;
}

export type ContentType = null| 'application/x-www-form-urlencoded' |
'multipart/form-data' |
'application/json' |
'text/html' |
'application/xml' |
'application/javascript' |
'text/plain' |
'application/octet-stream'|
'*/*'|
'*/*'

export type RequestBody = {
  forms: RequestBodyFormItem[];
  rawContent: string;
  type:'string'|'number'|'integer'|'boolean';
  format:'string'|'binary';
  contentEncoding:'gzip_base64';
  fileName:string;
}

export const deepParseJson = (jsonStr: string) => {
  try {
    const result = JSON.parse(jsonStr);
    if (typeof result === 'string') {
      return deepParseJson(result);
    }
    return result;
  } catch {
    return jsonStr;
  }
};
