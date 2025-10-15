import { codeUtils } from '@xcan-angus/infra';
import SwaggerUI from '@xcan-angus/swagger-ui';

import apiUtils from '@/utils/apis';

const { ungzip } = codeUtils;
const loadAsync = async (base64Text, name) => {
  const buf = await ungzip(base64Text);
  return await new File([buf], name);
};

const { valueKey, fileNameKey, enabledKey } = apiUtils.API_EXTENSION_KEY;

export const getRequestBodyData = async (apiBody = {} as {[key: string]: any}, resolveModels = {}, contentType: string) => {
  if (!contentType) {
    return undefined;
  }
  const apiBodyData = apiUtils.analysisBody(apiBody, resolveModels);
  if (!apiBodyData?.content?.[contentType]) {
    return undefined;
  }
  const bodyContent = apiBodyData.content[contentType] || { shema: {} };
  if (contentType === 'application/x-www-form-urlencoded') {
    const list = Object.keys(bodyContent?.schema?.properties || {}).map(key => {
      const itemSchema = bodyContent.schema?.properties?.[key];
      if (!itemSchema.type && itemSchema.properties) {
        itemSchema.type = 'object';
      }
      if (!itemSchema.type && itemSchema.items) {
        itemSchema.type = 'array';
      }
      if (['object', 'array', 'number', 'integer', 'boolean'].includes(itemSchema.type) && typeof itemSchema[valueKey] === 'string') {
        try {
          itemSchema.value = JSON.parse(itemSchema[valueKey]);
        } catch {}
      }
      return {
        name: key,
        ...itemSchema
      };
    });
    return list;
  }
  if (contentType === 'multipart/form-data') {
    const keys = Object.keys(bodyContent?.schema?.properties || {});
    const list = [];
    for (const key of keys) {
      const itemSchema = bodyContent.schema?.properties?.[key];
      if (!itemSchema.type && itemSchema.properties) {
        itemSchema.type = 'object';
      }
      if (!itemSchema.type && itemSchema.items) {
        itemSchema.type = 'array';
      }
      if ((['object', 'array', 'number', 'integer', 'boolean'].includes(itemSchema.type) || itemSchema.format === 'binary') && typeof itemSchema[valueKey] === 'string') {
        try {
          itemSchema[valueKey] = JSON.parse(itemSchema[valueKey]);
        } catch {}
      }
      if (itemSchema.format === 'binary') {
        if (itemSchema.type === 'string') {
          if (itemSchema[valueKey][valueKey]) {
            const fileObj = itemSchema[valueKey][valueKey];
            const originFileObj = fileObj.originFileObj ? fileObj.originFileObj : await loadAsync(fileObj[valueKey], fileObj[fileNameKey]);
            fileObj.originFileObj = originFileObj;
          }
        } else if (itemSchema.type === 'array') {
          const fileObjArr = itemSchema?.[valueKey];
          if (Array.isArray(fileObjArr)) {
            for (const idx in fileObjArr) {
              const fileObj = fileObjArr[idx];
              const originFileObj = fileObj.originFileObj ? fileObj.originFileObj : await loadAsync(fileObj[valueKey], fileObj[fileNameKey]);
              fileObjArr[idx].originFileObj = originFileObj;
            }
          }
        }
      }
      list.push({
        name: key,
        [enabledKey]: true,
        ...itemSchema
      });
    }
    return list;
  }
  if (bodyContent.schema?.format === 'binary') {
    if (bodyContent.schema?.[valueKey]) {
      const buf = await ungzip(bodyContent.schema?.[valueKey]);
      const file = await new File([buf], bodyContent.schema?.[fileNameKey]);
      return file;
    }
    return undefined;
  } else {
    if (bodyContent[valueKey] !== undefined) {
      return bodyContent[valueKey];
    } else {
      if (contentType === 'application/xml') {
        return SwaggerUI.extension.sampleFromSchemaGeneric(bodyContent?.schema, { useValue: true }, (bodyContent?.[valueKey] !== undefined ? bodyContent?.[valueKey] : undefined), true);
      } else {
        return SwaggerUI.extension.sampleFromSchemaGeneric(bodyContent?.schema, { useValue: true }, (bodyContent?.[valueKey] !== undefined ? bodyContent?.[valueKey] : undefined));
      }
    }
  }
};
