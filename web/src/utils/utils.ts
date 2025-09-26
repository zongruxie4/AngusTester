import { DATE_FORMAT, DATE_TIME_FORMAT } from '@/utils/constant';
import { TreeData } from '@/types/types';
import dayjs, { Dayjs } from 'dayjs';
import { SearchCriteria } from '@xcan-angus/infra';

// ----------------------------------------------------
// Size & bytes utilities
// ----------------------------------------------------

/**
 * Convert a numeric byte value to a human-readable string.
 * <p>
 * If a specific unit is provided, the value will be converted to that unit.
 * Otherwise, the function will auto-select the most appropriate unit based on the value.
 * </p>
 * @param value - Raw size in bytes
 * @param unit - Optional target unit to convert into
 * @returns Formatted size string with unit suffix (e.g., "10KB")
 */
export const sizeUnitFormat = (value: number, unit?: 'B' | 'KB' | 'MB' | 'GB' | 'TB' | 'PB' | 'EB' | 'ZB' | 'YB'): string => {
  const base = 1024;
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const magnitudeIndex = parseInt(`${Math.log(value) / Math.log(base)}`);
  if (unit) {
    const unitIndex = units.indexOf(unit);
    return Math.round((value / Math.pow(base, unitIndex))) + unit;
  } else {
    return Math.round((value / Math.pow(base, magnitudeIndex))) + units[magnitudeIndex];
  }
};

/**
 * Format bytes to a human-readable string using binary (1024) units.
 * @param size - Size in bytes
 * @param decimal - Number of decimals to keep (round half up)
 * @returns Formatted string like "10.24MB"
 */
export const formatBytes = (size = 0, decimal = 2): string => {
  if (size === 0) return '0B';
  const base = 1024;
  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  const magnitudeIndex = Math.floor(Math.log(size) / Math.log(base));
  return parseFloat((size / Math.pow(base, magnitudeIndex)).toFixed(decimal)) + units[magnitudeIndex];
};

type Fn = (ele: unknown, index: number) => boolean

// ----------------------------------------------------
// Date range utilities
// ----------------------------------------------------

/**
 * Format date string for time-based filters.
 * <p>
 * Converts a time key (e.g., lastDay, lastThreeDays, lastWeek) into
 * a pair of string-formatted datetimes using DATE_TIME_FORMAT.
 * </p>
 * @param key - Time key identifier
 * @returns Tuple of [startDateTime, endDateTime]
 */
export const formatDateString = (key: string): [string, string] => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  if (key === 'lastDay') {
    startDate = dayjs().startOf('date');
    endDate = dayjs();
  }

  if (key === 'lastThreeDays') {
    startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    endDate = dayjs();
  }

  if (key === 'lastWeek') {
    startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    endDate = dayjs();
  }

  return [
    startDate ? startDate.format(DATE_TIME_FORMAT) : '',
    endDate ? endDate.format(DATE_TIME_FORMAT) : ''
  ];
};

/**
 * Formats date range based on time key for quick search filters
 * @param key - Time key for date range calculation
 * @returns Array of date filter criteria
 */
/**
 * Build search criteria array for time-based quick filters.
 * <p>
 * Returns two criteria (start and end) when available, filtering by key "createdDate".
 * </p>
 * @param key - Time key identifier
 * @returns Array of SearchCriteria for start and end datetimes
 */
export const formatDateStringAsCriteria = (key: string): SearchCriteria[] => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  if (key === 'lastDay') {
    startDate = dayjs().startOf('date');
    endDate = dayjs();
  }

  if (key === 'lastThreeDays') {
    startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    endDate = dayjs();
  }

  if (key === 'lastWeek') {
    startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    endDate = dayjs();
  }

  return [
    startDate
      ? { value: startDate.format(DATE_TIME_FORMAT), op: SearchCriteria.OpEnum.GreaterThanEqual, key: 'createdDate' } as SearchCriteria
      : {} as SearchCriteria,
    endDate
      ? { value: endDate.format(DATE_TIME_FORMAT), op: SearchCriteria.OpEnum.LessThanEqual, key: 'createdDate' } as SearchCriteria
      : {} as SearchCriteria
  ].filter(Boolean);
};

/**
 * Find the last index in an array that satisfies the callback condition.
 * @param arr - Target array
 * @param callback - Predicate to test elements
 * @returns Index of the last matching element, or -1 if none match
 */
export const findLastIndex = (arr: unknown[], callback: Fn): number => {
  if (!arr?.length) {
    return -1;
  }

  for (let i = arr.length; i--;) {
    if (typeof callback === 'function') {
      if (callback(arr[i], i)) {
        return i;
      }
    }
  }

  return -1;
};

export const getCurrentPage = (pageNo:number, pageSize:number, total:number):number => {
  if (pageNo === 1 || pageSize >= total) {
    return 1;
  }

  const remainder = total % pageSize;
  if (remainder === 1) {
    const totalPage = Math.ceil(total / pageSize);
    if (totalPage === pageNo) {
      return pageNo - 1;
    }
  }

  return pageNo;
};

export const durationUnitOpt = [
  { label: 'Hour', value: 'h' }, { label: 'Minute', value: 'min' }, { label: 'Second', value: 's' }, { label: 'Millisecond', value: 'ms' }
];

// ----------------------------------------------------
// Duration utilities
// ----------------------------------------------------

/**
 * Map duration unit shorthand to English label.
 * @param unit - One of 's' | 'ms' | 'min' | 'h' | 'd'
 * @returns English label string
 */
export const getDurationUnit = (unit: 's'|'ms'|'min'|'h'|'d') => {
  switch (unit) {
    case 's':
      return 'Second';
    case 'ms':
      return 'Millisecond';
    case 'min':
      return 'Minute';
    case 'h':
      return 'Hour';
    case 'd':
      return 'Day';
    default: return 'Second';
  }
};

/**
 * Split a duration literal into numeric part and unit.
 * <p>
 * Supports suffixes: h, ms, min, s, d. Returns [value, unit].
 * </p>
 * @param duration - Duration string like "10ms" or "5h"
 * @returns Two-element array [value, unit] or [] if not matched
 */
export const splitDuration = (duration: string): string[] => {
  if (duration.includes('h')) {
    duration = duration.replace('h', '');
    return [duration, 'h'];
  } else if (duration.includes('ms')) {
    duration = duration.replace('ms', '');
    return [duration, 'ms'];
  } else if (duration.includes('min')) {
    duration = duration.replace('min', '');
    return [duration, 'min'];
  } else if (duration.includes('s')) {
    duration = duration.replace('s', '');
    return [duration, 's'];
  } else if (duration.includes('d')) {
    duration = duration.replace('d', '');
    return [duration, 'd'];
  }
  return [];
};

const getRandomIntInclusive = (min:number, max:number):number => {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min + 1)) + min;
};

// ----------------------------------------------------
// Random & naming utilities
// ----------------------------------------------------

/**
 * Generate a randomized alphanumeric string composed of roughly equal
 * proportions of digits, lowercase letters, and uppercase letters.
 * @param len - Target length (default 9)
 * @returns Random string
 */
export const randomString = (len = 9): string => {
  const digits = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];
  const lowercaseLetters = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'];
  const uppercaseLetters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
  const pools = [digits, lowercaseLetters, uppercaseLetters];
  let taken = 0;
  let averagePerPool = Math.floor(len / 3);
  const buffer = pools.reduce((accumulator, pool, poolIndex) => {
    if (poolIndex === 2) {
      // Ensure residual characters are included in the last pool
      averagePerPool = len - taken;
    }

    for (let i = 0; i < averagePerPool; i++) {
      accumulator.push(pool[getRandomIntInclusive(0, pool.length - 1)]);
      taken++;
    }

    // Shuffle progressively to avoid patterns
    accumulator.sort(() => { return 0.5 - Math.random(); });
    return accumulator;
  }, [] as (number|string)[]);

  return buffer.join('');
};

/**
 * Generate a unique copy name by appending "copy" or "copyN" suffix.
 * <p>
 * If the base name already exists in the array, it will append "copy" or
 * increment the copy number to ensure uniqueness.
 * </p>
 * @param strings - Array of existing strings to check against
 * @param str - Base string to generate copy name for
 * @returns Unique copy name string
 */
export const generateCopyName = (strings:string[], str:string) => {
  // Check if the base string already exists in the array
  if (!strings.includes(str)) {
    return str;
  }

  let maxCopyNumber = 0;

  // Find the highest copy number among existing copy names
  // Look for strings that start with the base name and end with "copy" + number
  for (let i = 0; i < strings.length; i++) {
    const s = strings[i];
    if (s.startsWith(str) && s.match(/copy\d*/)) {
      let copyNumber = parseInt(s.replace(`${str} copy`, ''));
      if (isNaN(copyNumber)) {
        copyNumber = 1;
      }
      if (copyNumber > maxCopyNumber) {
        maxCopyNumber = copyNumber;
      }
    }
  }

  // If copy names exist, return base name with incremented copy number
  if (maxCopyNumber > 0) {
    return `${str} copy${maxCopyNumber + 1}`;
  }

  // Return base name with "copy" suffix
  return `${str} copy`;
};

// ----------------------------------------------------
// Date sequence utilities
// ----------------------------------------------------

/**
 * Get a list of dates (formatted by DATE_FORMAT) counting back from today.
 * @param size - Number of days to include (default 7)
 * @returns Array of date strings from today backwards
 */
export const getDateArr = (size = 7) => {
  const dates: string[] = [];
  for (let i = 0; i < size; i++) {
    // Get current date minus i days
    const date = dayjs().subtract(i, 'day').format(DATE_FORMAT);
    dates.push(date);
  }
  return dates;
};

export const getDateArrWithTime = (start, end) => {
  const dates: string[] = [];
  while (!dates[0] || (dayjs(dates[0]).isAfter(dayjs(start)))) {
    if (!dates[0]) {
      const date = dayjs(end).format(DATE_FORMAT);
      dates.unshift(date);
    } else {
      const date = dayjs(dates[0]).subtract(1, 'day').format(DATE_FORMAT);
      dates.unshift(date);
    }
  }
  return dates;
};

// ----------------------------------------------------
// Tree traversal utilities
// ----------------------------------------------------

/**
 * Recursively traverse tree data and apply a callback to each node.
 * <p>
 * This mutates nodes by enriching structural metadata such as level, index,
 * ancestral ids, and child-level depth. The callback can optionally return a
 * new object whose enumerable properties will be merged into the node.
 * </p>
 * @param treeData - Array of tree nodes to traverse
 * @param callback - Function to apply to each node; defaults to identity
 * @returns Processed tree data (same reference as input)
 */
export const travelTreeData = (treeData: TreeData[], callback = (item: TreeData) => item): TreeData[] => {
  function travel (treeData: TreeData[], level = 0, ids: string[] = []) {
    treeData.forEach((item, idx) => {
      // Set node properties for tree structure
      item.level = level;
      item.index = idx;
      item.ids = [...ids, item.id];
      item.isLast = idx === (treeData.length - 1);

      // Recursively process children
      travel(item.children || [], level + 1, item.ids);

      // Calculate child levels for depth validation
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(i => i.childLevels || 0)) : 0) + 1;

      // Apply callback function to current item
      const processedItem = callback(item);
      if (processedItem !== item) {
        Object.assign(item, processedItem);
      }
    });
  }

  travel(treeData);
  return treeData;
};
