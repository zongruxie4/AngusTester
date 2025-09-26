import { Ref } from 'vue';

// ----------------------------------------------------
// Form validation utilities
// ----------------------------------------------------

/**
 * Validate specific form fields and clear validation state on success.
 * @param formRef - Vue form reference
 * @param fields - Array of field names to validate
 */
export const validFields = (formRef: Ref, fields: string[]): void => {
  formRef.value.validateFields(fields).then(() => {
    formRef.value.clearValidate(fields);
  }, () => { /** */ });
};

// ----------------------------------------------------
// Size & bytes utilities
// ----------------------------------------------------

/**
 * Format bytes to human-readable string with space separator.
 * <p>
 * TODO: Replace with global utility method
 * </p>
 * @param size - Size in bytes
 * @param decimal - Number of decimal places (default 2)
 * @returns Formatted string like "10.24 MB"
 */
export const formatBytes = (size = 0, decimal = 2): string => {
  if (size === 0) return '0 B';
  const base = 1024;
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const magnitudeIndex = Math.floor(Math.log(size) / Math.log(base));
  return parseFloat((size / Math.pow(base, magnitudeIndex)).toFixed(decimal)) + ' ' + units[magnitudeIndex];
};

// ----------------------------------------------------
// HTTP headers utilities
// ----------------------------------------------------

type HeadersType = {
  paramsType?: boolean,
  headers: { 'XC-Opt-Tenant-Id'?: string }
}

/**
 * Create headers object with tenant ID for API requests.
 * @param tenantId - Tenant identifier
 * @param paramsType - Optional parameter type flag
 * @returns Headers configuration object
 */
export const handleHeaders = (tenantId: string, paramsType?: boolean): HeadersType => {
  const headers: HeadersType = {
    headers: {}
  };
  if (tenantId) {
    headers.headers['XC-Opt-Tenant-Id'] = tenantId;
  }
  if (paramsType) {
    headers.paramsType = paramsType;
  }
  return headers;
};

// ----------------------------------------------------
// Time utilities
// ----------------------------------------------------

/**
 * Extract hours, minutes, or seconds from total seconds.
 * @param totalSeconds - Total number of seconds
 * @param unit - Time unit to extract ('h', 'm', 's')
 * @param isShow - Whether to pad with leading zero for display
 * @returns Extracted time value as number or zero-padded string
 */
export const getUnitTimeNumber = (totalSeconds: number, unit: 'h' | 'm' | 's', isShow = false): number | string => {
  if (unit === 'h') {
    const hours = Math.floor(totalSeconds / 3600);
    return isShow && hours < 10 ? `0${hours}` : hours;
  } else if (unit === 'm') {
    const minutes = Math.floor(totalSeconds % 3600 / 60);
    return isShow && minutes < 10 ? `0${minutes}` : minutes;
  } else if (unit === 's') {
    const seconds = totalSeconds % 3600 % 60;
    return isShow && seconds < 10 ? `0${seconds}` : seconds;
  }
  return totalSeconds;
};

// ----------------------------------------------------
// Random utilities
// ----------------------------------------------------

/**
 * Generate a random integer within specified range.
 * @param min - Minimum value (inclusive)
 * @param max - Maximum value (inclusive)
 * @returns Random integer between min and max
 */
export const randomNum = (min: number, max: number): number => {
  if (min && !max) {
    return parseInt(`${Math.random() * min + 1}`, 10);
  }
  if (min && max) {
    return parseInt(`${Math.random() * (max - min + 1) + min}`, 10);
  }
  return 0;
};

// ----------------------------------------------------
// Duration parsing utilities
// ----------------------------------------------------

const unitMap = {
  ms: 'Millisecond',
  s: 'Second',
  min: 'Minute',
  h: 'Hour',
  d: 'Day'
};

/**
 * Split duration string into numeric value and unit.
 * <p>
 * Parses strings like '10s', '20min', '5h' and returns
 * the numeric part and corresponding English unit name.
 * </p>
 * @param durationString - Duration string to parse
 * @returns Tuple of [numericValue, unitName]
 */
export const splitTime = (durationString: string): [string, string] => {
  const numericValue = durationString.replace(/\D/g, '');
  const unitSuffix = durationString.replace(/\d/g, '');
  return [numericValue, unitMap[unitSuffix]];
};

/**
 * Convert current duration to match target duration unit.
 * <p>
 * Normalizes the current duration string to the same unit as the target duration.
 * The target duration unit takes precedence for the final result.
 * </p>
 * @param currentDurationStr - Current duration string (e.g., '2h', '30min')
 * @param targetDurationStr - Target duration string to match unit
 * @returns Converted duration value or '--' if current duration is empty
 * @remark If currentDurationStr has value, targetDurationStr must also have value
 */
export const getCurrentDuration = (currentDurationStr: string, targetDurationStr: string): string | number => {
  if (!currentDurationStr) {
    return '--';
  }

  const targetUnit = targetDurationStr.replace(/\d/g, '');
  const currentDurationValue = +splitTime(currentDurationStr)[0];

  // Convert to seconds
  if (targetUnit === 's') {
    // Current duration is in hours
    if (currentDurationStr.includes('h')) {
      return keepTwoDecimals(currentDurationValue * 60 * 60);
    }

    // Current duration is in minutes
    if (currentDurationStr.includes('min')) {
      return keepTwoDecimals(currentDurationValue * 60);
    }

    return currentDurationValue;
  }

  // Convert to minutes
  if (targetUnit === 'min') {
    if (currentDurationStr.includes('h')) {
      return keepTwoDecimals(currentDurationValue * 60);
    }

    if (currentDurationStr.includes('s')) {
      return keepTwoDecimals(currentDurationValue / 60);
    }

    return currentDurationValue;
  }

  // Convert to hours
  if (currentDurationStr.includes('s')) {
    return keepTwoDecimals(currentDurationValue / 60 / 60);
  }

  if (currentDurationStr.includes('min')) {
    return keepTwoDecimals(currentDurationValue / 60);
  }

  return currentDurationValue;
};

/**
 * Format number to keep at most two decimal places.
 * @param value - Number to format
 * @returns Formatted number with max 2 decimal places
 */
const keepTwoDecimals = (value: number): string | number => {
  if (value % 1 !== 0 && value.toString().split('.')[1].length > 2) {
    return value.toFixed(2);
  } else {
    return value;
  }
};
