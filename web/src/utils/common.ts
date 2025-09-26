// ----------------------------------------------------
// Parameter utilities
// ----------------------------------------------------

/**
 * Filter out empty or falsy values from an object.
 * @param params - Object to filter
 * @returns Object with only non-empty values
 */
const pureParams = (params: Record<string, any>): Record<string, any> => {
  const filteredParams = {};
  Object.keys(params).forEach(key => {
    if (params[key] && (params[key] !== '')) {
      filteredParams[key] = params[key];
    }
  });

  return filteredParams;
};

// ----------------------------------------------------
// Blob and data conversion utilities
// ----------------------------------------------------

/**
 * Convert a Blob to a data URL (base64 encoded string).
 * @param blob - Blob object to convert
 * @returns Promise that resolves to base64 data URL
 */
const blobToDataURL = (blob: Blob): Promise<unknown> => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = function (event) {
      const base64Data = event.target?.result;
      resolve(base64Data);
    };
    reader.readAsDataURL(blob);
  });
};

// ----------------------------------------------------
// Size and bytes utilities
// ----------------------------------------------------

/**
 * Format bytes to human-readable string with space separator.
 * @param size - Size in bytes
 * @param decimal - Number of decimal places (default 2)
 * @returns Formatted string like "10.24 MB"
 */
const formatBytes = (size = 0, decimal = 2): string => {
  if (size === 0) return '0 B';
  const base = 1024;
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const magnitudeIndex = Math.floor(Math.log(size) / Math.log(base));
  return parseFloat((size / Math.pow(base, magnitudeIndex)).toFixed(decimal)) + ' ' + units[magnitudeIndex];
};

/**
 * Convert bytes to a specific unit with custom decimal precision.
 * @param size - Size in bytes
 * @param unit - Target unit (B, KB, MB, GB, TB, PB)
 * @param decimal - Number of decimal places (default 5)
 * @returns Formatted string with specified unit
 */
const formatBytesToUnit = (size = 0, unit = 'GB', decimal = 5): string => {
  if (size === 0) return '0';
  const unitMultipliers = {
    B: 1, KB: 1024, MB: 1048576, GB: 1073741824, TB: 1099511627776, PB: 1125899906842624
  };
  const multiplier = unitMultipliers[unit] || 1073741824;
  return (size / multiplier).toFixed(decimal);
};

// ----------------------------------------------------
// Data manipulation utilities
// ----------------------------------------------------

/**
 * Group array of objects by a specific key.
 * @param data - Array of objects to group
 * @param key - Property name to group by
 * @returns Object with grouped arrays
 */
const group = (data: Array<Record<string, any>>, key: string): Record<string, Array<any>> => {
  const groupedData: Record<string, Array<any>> = {};
  data.forEach((item) => {
    const keyValue = item[key];
    if (!groupedData[keyValue]) {
      groupedData[keyValue] = [item];
    } else {
      groupedData[keyValue].push(item);
    }
  });
  return groupedData;
};

// ----------------------------------------------------
// File download utilities
// ----------------------------------------------------

/**
 * Trigger download of data as a file.
 * @param data - Data to download (BlobPart)
 * @param filename - Name of the file to download
 */
const download = (data: BlobPart, filename: string): void => {
  const blob = new Blob([data], { type: 'application/octet-stream' });
  const downloadUrl = window.URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = downloadUrl;
  anchor.download = filename;
  anchor.click();
  window.URL.revokeObjectURL(downloadUrl);
};

// ----------------------------------------------------
// HTTP method styling utilities
// ----------------------------------------------------

/**
 * Background color classes for HTTP methods.
 */
const bgColor = {
  GET: 'bg-http-get',
  POST: 'bg-http-post',
  DELETE: 'bg-http-delete',
  PATCH: 'bg-http-patch',
  PUT: 'bg-http-put',
  OPTIONS: 'bg-http-options',
  HEAD: 'bg-http-head',
  TRACE: 'bg-http-trace'
};

/**
 * Text color classes for HTTP methods.
 */
const textColor = {
  GET: 'text-http-get',
  POST: 'text-http-post',
  DELETE: 'text-http-delete',
  PATCH: 'text-http-patch',
  PUT: 'text-http-put',
  OPTIONS: 'text-http-options',
  HEAD: 'text-http-head',
  TRACE: 'text-http-trace'
};

export {
  blobToDataURL,
  download,
  formatBytes,
  pureParams,
  formatBytesToUnit,
  group,
  bgColor,
  textColor
};
