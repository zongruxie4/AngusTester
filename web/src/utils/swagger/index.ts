import SwaggerUI from '@xcan-angus/swagger-ui';

import { IConfig } from './PropsType';

// ----------------------------------------------------
// OpenAPI schema processing utilities
// ----------------------------------------------------

/**
 * Generate XML example from OpenAPI v3.x schema.
 * @param schema - OpenAPI schema object
 * @param config - Optional SwaggerUI configuration
 * @returns XML example string
 */
const createXMLExample = (schema: Record<string, any>, config?: IConfig): string => {
  return SwaggerUI.plugins.SamplesFn.createXMLExample(schema, config);
};

/**
 * Generate JSON example from OpenAPI v3.x schema.
 * @param schema - OpenAPI schema object
 * @param config - Optional SwaggerUI configuration
 * @returns JSON example object or array
 */
const sampleFromSchema = (schema: Record<string, any>, config?: IConfig): Record<string, any> | Record<string, any>[] => {
  return SwaggerUI.plugins.SamplesFn.sampleFromSchema(schema, config);
};

// ----------------------------------------------------
// Schema deconstruction utilities
// ----------------------------------------------------

/**
 * Deconstruct schema by resolving $ref references.
 * <p>
 * Recursively resolves all $ref references in the schema
 * using the provided resolved reference models.
 * </p>
 * @param data - Data containing model and resolved reference models
 * @returns Deconstructed schema with resolved references
 */
const deconstruct = (data: Record<string, any>) => {
  const resolveReferences = (schema: Record<string, any>) => {
    if (Object.prototype.hasOwnProperty.call(schema, '$ref')) {
      const referencedModel = resolvedRefModels[schema.$ref];
      return resolveReferences(referencedModel);
    } else {
      for (const key in schema) {
        if (Object.prototype.toString.call(schema[key]) === '[object Object]') {
          schema[key] = resolveReferences(schema[key]);
        }
      }
    }

    return schema;
  };

  const model = JSON.parse(data.model || 'null');
  if (model?.$ref === data.ref) {
    return {};
  }
  let resolvedRefModels = data.resolvedRefModels;
  if (!resolvedRefModels) {
    return model;
  }

  resolvedRefModels = Object.entries(resolvedRefModels).reduce((accumulator, [key, value]) => {
    const stringValue = (value || 'null') as string;
    accumulator[key] = JSON.parse(stringValue);
    return accumulator;
  }, {});

  return resolveReferences(model);
};

export {
  deconstruct,
  createXMLExample,
  sampleFromSchema
};
