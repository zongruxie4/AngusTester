/**
 * Quick Search Components
 *
 * A configurable quick search options component that supports:
 * - Audit information options (e.g., "我创建的", "我修改的")
 * - Enum status options (e.g., script types, status values)
 * - Time range options (e.g., "最近1天", "最近3天", "最近7天", "最近30天")
 * - Custom external clear functions
 * - Description slots for business explanations
 */

export { default as QuickSearchOptions } from './QuickSearchOptions.vue';
export {
  useQuickSearch,
  createAuditOptions,
  createEnumOptions,
  createTimeOptions,
  createEnumTypeConfig
} from './composables/useQuickSearch';
export type {
  BaseQuickSearchOption,
  AuditInfoOption,
  EnumStatusOption,
  TimeRangeOption,
  QuickSearchOption,
  QuickSearchConfig,
  EnumTypeConfig,
  QuickSearchOptionsProps,
  UseQuickSearchReturn,
  TimeRangeValue
} from './types';
