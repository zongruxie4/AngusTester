<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import FileIcon from '@/views/data/file/components/Icon.vue';
import { useSearch } from './composables/useSearch';

const emit = defineEmits<{(e: 'search', id: string):void}>();

const { t } = useI18n();

// Use the search composable
const {
  isSearchVisible,
  searchParams,
  showSearch,
  handleSearchChange,
  handleDropdownVisibility,
  init
} = useSearch();

// Initialize the composable
init();

/**
 * Handle search execution
 *
 * @param value - Selected value
 * @param valueObj - Selected value object
 */
const handleChange = (value: string, valueObj: any): void => {
  handleSearchChange(value, valueObj);
  if (value) {
    emit('search', valueObj.parentDirectoryId);
  }
};

/**
 * Handle dropdown visibility change
 *
 * @param visible - Dropdown visibility state
 */
const handleVisible = (visible: boolean): void => {
  handleDropdownVisibility(visible);
};
</script>
