// Local imports
import { ParamsItem } from '../../interface';

/**
 * Component state interface for form data management
 */
export interface ComponentState {
  formData: ParamsItem[];
}

// Legacy export alias for backward compatibility
export type State = ComponentState;

/**
 * Basic parameter item types configuration
 */
export const basicParameterItemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(type => ({ value: type, label: type }));

// Legacy export alias for backward compatibility
export const itemTypes = basicParameterItemTypes;

/**
 * Form data parameter types configuration with file support
 */
export const formDataParameterTypes = [
  'string',
  'array(json)',
  'array(xml)',
  'boolean',
  'integer',
  'object(json)',
  'object(xml)',
  'number',
  'file',
  'file(array)'
].map(type => ({ value: type, label: type }));

// Legacy export alias for backward compatibility
export const formDataTypes = formDataParameterTypes;
