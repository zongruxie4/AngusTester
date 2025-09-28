// Local imports
import { ParamsItem } from '../../interface';

/**
 * Component state interface for flat form data management
 */
export interface ComponentState {
  formData: ParamsItem[];
}

// Legacy export alias for backward compatibility
export type State = ComponentState;

/**
 * Basic parameter item types configuration for flat forms
 */
export const basicFlatParameterItemTypes = [
  'string',
  'boolean',
  'integer',
  'number'
].map(type => ({ value: type, label: type }));

// Legacy export alias for backward compatibility
export const itemTypes = basicFlatParameterItemTypes;

/**
 * Form data parameter types configuration with file support for flat forms
 */
export const flatFormDataParameterTypes = [
  'string',
  'boolean',
  'integer',
  'number',
  'file',
  'file(array)'
].map(type => ({ value: type, label: type }));

// Legacy export alias for backward compatibility
export const formDataTypes = flatFormDataParameterTypes;
