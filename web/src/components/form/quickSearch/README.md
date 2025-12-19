# QuickSearchOptions Component

A configurable quick search options component that supports various types of predefined query conditions including audit information options, enum status options, and time options.

## Features

- **All Option**: Virtual option that clears all conditions when selected, including quick search options and external component conditions
- **Audit Information Options**: Configurable based on business needs, such as "Created by me", "Modified by me", etc.
- **Enum Status Options**: Configurable based on business needs, such as script type enums
- **Time Options**: Predefined time options with fixed values: last1Day, last3Day, last7Day, last30Day
- **Description Slot**: For receiving business description text
- **Mutual Exclusion Logic**: All option is mutually exclusive with other options, enum status options are mutually exclusive within the group, and time options are mutually exclusive within the group

## Basic Usage

```vue
<template>
  <QuickSearchOptions
    :config="searchConfig"
    :description-slot="descriptionText"
    @change="handleSearchChange"
  />
</template>

<script setup lang="ts">
import { QuickSearchOptions, createAuditOptions, createEnumOptions, createTimeOptions, createEnumTypeConfig } from '@/components/quickSearch';

const descriptionText = 'Quickly filter script data to <strong>improve search efficiency</strong>';

const searchConfig = {
  title: 'Quick Search',
  auditOptions: createAuditOptions([
    { key: 'createdBy', name: 'Created by me', fieldKey: 'createdBy' },
    { key: 'modifiedBy', name: 'Modified by me', fieldKey: 'modifiedBy' }
  ], currentUserId),
  
  // Option 1: Manual enum options
  enumOptions: createEnumOptions([
    { key: 'performance', name: 'Performance Test', fieldKey: 'type', fieldValue: 'PERFORMANCE' },
    { key: 'functional', name: 'Functional Test', fieldKey: 'type', fieldValue: 'FUNCTIONAL' }
  ]),
  // Option 2: Automatic enum options from enum type
  // enumType: createEnumTypeConfig(ScriptType, 'type', [ScriptType.MOCK_APIS]),

  timeOptions: createTimeOptions([
    { key: 'last1Day', name: 'Last 1 Day', timeRange: 'last1Day' },
    { key: 'last3Days', name: 'Last 3 Days', timeRange: 'last3Days' },
    { key: 'last7Days', name: 'Last 7 Days', timeRange: 'last7Days' },
    { key: 'last30Days', name: 'Last 30 Days', timeRange: 'last30Days' }
  ], 'createdDate'),
  externalClearFunction: () => {
    // Logic to clear external component conditions
    clearExternalFilters();
  }
};

const handleSearchChange = (selectedKeys: string[], searchCriteria: any[]) => {
  console.log('Selected options:', selectedKeys);
  console.log('Search criteria:', searchCriteria);
  // Execute search logic
};
</script>
```

## Advanced Usage

### Custom Time Range Calculation

```typescript
const customTimeOptions: TimeRangeOption[] = [
  {
    key: 'last1Day',
    name: 'Last 1 Day',
    type: 'time',
    fieldKey: 'createdDate',
    timeRange: 'last1Day',
    getDateRange: (timeRange) => {
      // Custom time range calculation logic
      const now = new Date();
      const endDate = now.toISOString().split('T')[0];
      const startDate = new Date(now.getTime() - 24 * 60 * 60 * 1000)
        .toISOString().split('T')[0];
      return [startDate, endDate];
    }
  }
];
```

### Using Composable

```vue
<script setup lang="ts">
import { useQuickSearch } from '@/components/quickSearch';

const { 
  menuItems, 
  selectedOptions, 
  handleOptionClick, 
  resetSelections,
  getSearchCriteria 
} = useQuickSearch(config, (selectedKeys, searchCriteria) => {
  // Handle option changes
});

// Manually reset selections
const handleReset = () => {
  resetSelections();
};

// Get current search criteria
const currentCriteria = getSearchCriteria();
</script>
```

## API Reference

### QuickSearchOptions Props

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| config | QuickSearchConfig | - | Quick search configuration |
| selectedOptions | string[] | [] | Currently selected options |
| descriptionSlot | string | '' | Description slot content (supports HTML) |

### QuickSearchConfig

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| title | string | 'Quick Search' | Title text |
| auditOptions | AuditInfoOption[] | - | Audit information options |
| enumOptions | EnumStatusOption[] | - | Enum status options |
| enumType | EnumTypeConfig | - | Enum type configuration for automatic generation |
| timeOptions | TimeRangeOption[] | - | Time options |
| externalClearFunction | () => void | - | External clear function |

### Events

| Event Name | Parameters | Description |
|------------|------------|-------------|
| change | (selectedKeys: string[], searchCriteria: SearchCriteria[]) | Triggered when options change |

### Methods

| Method Name | Parameters | Return Value | Description |
|-------------|------------|--------------|-------------|
| resetSelections | - | void | Reset all selections |
| getSearchCriteria | - | SearchCriteria[] | Get search criteria |
| clearExternalConditions | - | void | Clear external conditions |

## Type Definitions

### AuditInfoOption
```typescript
interface AuditInfoOption {
  key: string;
  name: string;
  type: 'audit';
  fieldKey: string;
  fieldValue: string;
}
```

### EnumStatusOption
```typescript
interface EnumStatusOption {
  key: string;
  name: string;
  type: 'enum';
  fieldKey: string;
  fieldValue: string;
}
```

### TimeRangeOption
```typescript
interface TimeRangeOption {
  key: string;
  name: string;
  type: 'time';
  fieldKey: string;
  timeRange: TimeRangeValue;
  getDateRange: (timeRange: TimeRangeValue) => string[];
}
```

### EnumTypeConfig
```typescript
interface EnumTypeConfig {
  enum: any;
  fieldKey: string;
  excludes?: any[];
}
```

## Notes

1. **All option** is required, other options are user-configurable, but at least one must be specified
2. **All option** is mutually exclusive with all other options
3. **Enum status options** are mutually exclusive within the group, only one can be selected
4. **Time options** are also mutually exclusive within the group, only one can be selected
5. The component automatically handles mutual exclusion logic between options
6. Supports injection of external component condition clearing methods
