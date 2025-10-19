
// ----------------------------------------------------
// Validation utilities
// ----------------------------------------------------

/**
 * Validate URL correctness.
 * @param url - URL string to validate
 * @returns True if URL is valid, false otherwise
 */
export const isValidUrl = (url: string): boolean => {
  try {
    // eslint-disable-next-line no-new
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

/**
 * Validate maximum value constraint.
 * @param val - Value to validate
 * @param max - Maximum allowed value
 * @returns Error message if validation fails, undefined if valid
 */
export const validateMaximum = (val: number, max: number): string | undefined => {
  if (val > max) {
    return `Value must be less than ${max}`;
  }
};

/**
 * Validate minimum value constraint.
 * @param val - Value to validate
 * @param min - Minimum allowed value
 * @returns Error message if validation fails, undefined if valid
 */
export const validateMinimum = (val: number, min: number): string | undefined => {
  if (val < min) {
    return `Value must be greater than ${min}`;
  }
};

/**
 * Validate number format.
 * @param val - Value to validate
 * @returns Error message if validation fails, undefined if valid
 */
export const validateNumber = (val: string): string | undefined => {
  if (!/^-?\d+(\.?\d+)?$/.test(val)) {
    return 'Value must be a number';
  }
};

/**
 * Validate integer format.
 * @param val - Value to validate
 * @returns Error message if validation fails, undefined if valid
 */
export const validateInteger = (val: string): string | undefined => {
  if (!/^-?\d+$/.test(val)) {
    return 'Value must be an integer';
  }
};

/**
 * Validate boolean value.
 * @param val - Value to validate
 * @returns Error message if validation fails, undefined if valid
 */
export const validateBoolean = (val: any): string | undefined => {
  if (!(val === 'true' || val === 'false' || val === true || val === false)) {
    return 'Value must be a boolean';
  }
};

/**
 * Validate string value.
 * @param val - Value to validate
 * @returns Error message if validation fails, undefined if valid
 */
export const validateString = (val: any): string | undefined => {
  if (val && typeof val !== 'string') {
    return 'Value must be a string';
  }
};

/**
 * Validate DateTime value.
 * @param val - Value to validate
 * @returns Error message if validation fails, undefined if valid
 */
export const validateDateTime = (val: string): string | undefined => {
  if (isNaN(Date.parse(val))) {
    return 'Value must be a DateTime';
  }
};

/**
 * Validate GUID value.
 * @param val - Value to validate
 * @returns Error message if validation fails, undefined if valid
 */
export const validateGuid = (val: any): string | undefined => {
  const guidString = val.toString().toLowerCase();
  if (!/^[{(]?[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}[)}]?$/.test(guidString)) {
    return 'Value must be a Guid';
  }
};

/**
 * Validate maximum length constraint.
 * @param val - Value to validate
 * @param max - Maximum allowed length
 * @returns Error message if validation fails, undefined if valid
 */
export const validateMaxLength = (val: string, max: number): string | undefined => {
  if (val.length > max) {
    return `Value must be no longer than ${max} character${max !== 1 ? 's' : ''}`;
  }
};

/**
 * Validate minimum items constraint.
 * @param val - Array to validate
 * @param min - Minimum required items
 * @returns Error message if validation fails, undefined if valid
 */
export const validateMinItems = (val: any[], min: number): string | undefined => {
  if ((!val && min >= 1) || (val && val.length < min)) {
    return `Array must contain at least ${min} item${min === 1 ? '' : 's'}`;
  }
};

/**
 * Validate maximum items constraint.
 * @param val - Array to validate
 * @param max - Maximum allowed items
 * @returns Error message if validation fails, undefined if valid
 */
export const validateMaxItems = (val: any[], max: number): string | undefined => {
  if (val && val.length > max) {
    return `Array must not contain more then ${max} item${max === 1 ? '' : 's'}`;
  }
};

/**
 * Validate minimum length constraint.
 * @param val - Value to validate
 * @param min - Minimum required length
 * @returns Error message if validation fails, undefined if valid
 */
export const validateMinLength = (val: string, min: number): string | undefined => {
  if (val.length < min) {
    return `Value must be at least ${min} character${min !== 1 ? 's' : ''}`;
  }
};

/**
 * Validate pattern constraint using regular expression.
 * @param val - Value to validate
 * @param rxPattern - Regular expression pattern
 * @returns Error message if validation fails, undefined if valid
 */
export const validatePattern = (val: string, rxPattern: string): string | undefined => {
  const pattern = new RegExp(rxPattern);
  if (!pattern.test(val)) {
    return 'Value must follow pattern ' + rxPattern;
  }
};
