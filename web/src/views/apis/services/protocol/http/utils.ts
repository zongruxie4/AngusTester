import Ajv from 'ajv';
import addFormats from 'ajv-formats';
import { API_EXTENSION_KEY, deepDelAttrFromObj, getModelDataByRef } from '@/utils/apis';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { deconstruct } from '@/utils/swagger';

const { valueKey, enabledKey } = API_EXTENSION_KEY;

/**
 * AJV instance for JSON schema validation
 */
const ajvValidator = new Ajv();

addFormats(ajvValidator);

/**
 * Creates default parameter configuration
 * <p>
 * Generates a default parameter item with standard configuration
 * </p>
 * @param config - Additional configuration to merge
 * @returns Default parameter item
 */
export const getDefaultParams = (config = {}): ParamsItem => {
  return {
    name: '',
    in: 'query',
    description: '',
    [valueKey]: '',
    [enabledKey]: true,
    schema: { type: 'string' },
    ...config
  };
};

/**
 * Creates default body item configuration
 * <p>
 * Generates a default body item for form data
 * </p>
 * @param config - Additional configuration to merge
 * @returns Default body item
 */
export const getBodyDefaultItem = (config = {}) => {
  return {
    name: '',
    in: undefined,
    description: '',
    [valueKey]: '',
    [enabledKey]: true,
    type: 'string',
    ...config
  };
};

/**
 * Gets new item from metadata
 * <p>
 * Finds empty item in metadata or creates new default item
 * </p>
 * @param metaData - Array of parameter items
 * @param config - Optional configuration for new item
 * @returns New item or undefined
 */
const getNewItem = (metaData: ParamsItem[], config?: ParamsItem): ParamsItem | undefined => {
  const emptyItem = metaData?.find((item) => !item.name);
  if (!emptyItem) {
    return getBodyDefaultItem(config);
  }
  return undefined;
};

/**
 * Validates data against schema
 * <p>
 * Uses AJV to validate data against JSON schema
 * </p>
 * @param data - Data to validate
 * @param schema - JSON schema to validate against
 * @returns Array of validation errors or empty array
 */
export const validateType = (data: any, schema: any) => {
  delete schema.exampleSetFlag;
  const validator = ajvValidator.compile(schema);
  const isValid = validator(data);
  if (!isValid) {
    // Always return an array to satisfy spread operations
    return (validator.errors || []) as any[];
  }
  return [];
};

/**
 * Validates parameter data
 * <p>
 * Validates parameter data against its schema
 * </p>
 * @param data - Data to validate
 * @param schema - Schema to validate against
 * @returns Array of validation errors
 */
const validateParameterData = (data: any, schema: any) => {
  return validateType(data, schema);
};

/**
 * Validates query parameters
 * <p>
 * Validates all query parameters in the data array
 * </p>
 * @param data - Array of parameter data
 * @returns True if all parameters are valid
 */
const validateQueryParameters = (data: any[]) => {
  if (!data.length) {
    return true;
  }
  const validationErrors: any[] = [];
  data.forEach(item => {
    validationErrors.push(...validateParameterData(item[valueKey], deepDelAttrFromObj(item.schema, [])));
  });
  return !validationErrors.length;
};

/**
 * Validates body form data
 * <p>
 * Validates all form data parameters
 * </p>
 * @param data - Array of form data
 * @returns True if all form data is valid
 */
const validateBodyFormData = (data: any[]) => {
  if (!data.length) {
    return true;
  }
  const validationErrors: any[] = [];
  data.forEach(item => {
    validationErrors.push(...validateParameterData(item[valueKey],
      deepDelAttrFromObj(item, [valueKey, 'types', 'exampleSetFlag', 'name', enabledKey, 'key']))
    );
  });
  return !validationErrors.length;
};

export {
  getNewItem,
  validateQueryParameters as validateQueryParameter,
  validateBodyFormData as validateBodyForm
};

/**
 * Gets reference data by reference ID
 * <p>
 * Fetches and deconstructs model data by reference
 * </p>
 * @param reference - Reference ID
 * @param serviceId - Service ID
 * @returns Deconstructed reference data
 */
export const getReferenceData = async (reference: string, serviceId: string) => {
  const [error, response] = await getModelDataByRef(serviceId, reference);
  if (error) {
    return '';
  }
  return deconstruct(response.data || {});
};

/**
 * Transforms reference JSON to data JSON
 * <p>
 * Recursively resolves all $ref references in schema
 * </p>
 * @param schema - Schema object to transform
 * @param serviceId - Service ID for reference resolution
 * @returns Transformed schema with resolved references
 */
export const transformRefJsonToDataJson = async (schema: any = {}, serviceId: string) => {
  for (const key in schema) {
    if (key === '$ref') {
      const referenceData = await getReferenceData(schema.$ref, serviceId);
      schema = { ...schema, ...referenceData };
      delete schema.$ref;
    }
    if (Object.prototype.toString.call(schema[key]) === '[object Object]') {
      schema[key] = await transformRefJsonToDataJson(schema[key], serviceId);
    }
  }
  return schema;
};

/**
 * Deep parses JSON string
 * <p>
 * Recursively parses JSON strings until a non-string result is obtained
 * </p>
 * @param jsonString - JSON string to parse
 * @returns Parsed JSON object or original string if parsing fails
 */
export const deepParseJson = (jsonString: string) => {
  try {
    const result = JSON.parse(jsonString);
    if (typeof result === 'string') {
      return deepParseJson(result);
    }
    return result;
  } catch {
    return jsonString;
  }
};
