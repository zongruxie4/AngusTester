import YAMl from 'yaml';

// ----------------------------------------------------
// Data format detection utilities
// ----------------------------------------------------

/**
 * Check if a string is valid JSON format.
 * @param inputString - String to validate
 * @returns True if string is valid JSON, false otherwise
 */
const isJSON = (inputString: string): boolean => {
  if (typeof inputString !== 'string') {
    return false;
  }

  try {
    const parsedObject = JSON.parse(inputString);
    return typeof parsedObject === 'object';
  } catch (error) {
    return false;
  }
};

/**
 * Check if a string contains valid HTML markup.
 * @param inputString - String to validate
 * @returns True if string contains HTML elements, false otherwise
 */
const isHtml = (inputString: string): boolean => {
  if (typeof inputString !== 'string') {
    return false;
  }

  const document = new DOMParser().parseFromString(inputString, 'text/html');
  return Array.from(document.body.childNodes).some(node => node.nodeType === 1);
};

/**
 * Check if a string is valid XML format.
 * @param inputString - String to validate
 * @returns True if string is valid XML, false otherwise
 */
const isXML = (inputString: string): boolean => {
  if (typeof inputString !== 'string') {
    return false;
  }

  const parser = new DOMParser();
  const document = parser.parseFromString(inputString, 'text/xml');
  const parseErrors = document.getElementsByTagName('parsererror');

  return parseErrors.length === 0;
};

/**
 * Check if a string is valid YAML format.
 * @param inputString - String to validate
 * @returns True if string is valid YAML, false otherwise
 */
const isYAML = (inputString: string): boolean => {
  if (typeof inputString !== 'string') {
    return false;
  }

  try {
    const parsedObject = YAMl.parse(inputString);
    return typeof parsedObject === 'object';
  } catch (error) {
    return false;
  }
};

export { isJSON, isHtml, isXML, isYAML };
