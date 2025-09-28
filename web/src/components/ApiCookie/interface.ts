
/**
 * Cookie parameter item types configuration
 */
export const cookieParameterItemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(itemType => ({ value: itemType, label: itemType }));

// Legacy export alias for backward compatibility
export const itemTypes = cookieParameterItemTypes;
