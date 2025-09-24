<script setup lang="ts">
import { Ref, inject, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { HttpMethodText, Icon, Input } from '@xcan-angus/vue-ui';
import { Checkbox, CheckboxGroup, Divider, FormItemRest } from 'ant-design-vue';
import { useApiList } from './composables/useApiList';

interface ApiListProps {
  serviceId: string;
  apiIds: string[];
}

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const props = withDefaults(defineProps<ApiListProps>(), {
  serviceId: '',
  apiIds: () => []
});

const emit = defineEmits<{(e: 'update:apiIds', value: string[]): void}>();

const { t } = useI18n();

// Use the API list composable
const {
  name,
  dataList,
  checkedList,
  handleCheckAllChange
} = useApiList(projectId.value, props.serviceId, props.apiIds);

// Watch for changes in checked list and emit update
watch(checkedList, (newValue) => {
  emit('update:apiIds', newValue);
});
</script>

<template>
  <div class="border p-3.5 rounded">
    <FormItemRest>
      <Input
        v-model:value="name"
        :placeholder="t('mock.addMock.apiList.searchApiName')"
        :disabled="!props.serviceId"
        class="mb-2"
        allowClear>
        <template #suffix>
          <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
        </template>
      </Input>
      <div class="flex py-0.5 bg-bg-table-head text-text-title text-3 font-normal mb-1 pl-7.5">
        <div class="w-20 mr-2">
          {{ t('mock.addMock.apiList.requestMethod') }}
        </div>
        <div class="w-40 mr-2">
          URL
        </div>
        <div class="flex-1">
          {{ t('mock.addMock.apiList.apiName') }}
        </div>
      </div>
      <CheckboxGroup
        v-model:value="checkedList"
        style="width: 100%;"
        class="space-y-2 h-70 overflow-y-auto">
        <div
          v-for="item in dataList"
          :key="item.id"
          class="flex flex-1 items-center text-3 text-text-content">
          <Checkbox
            :value="item.id"
            class="mr-3.5 -mb-0.5">
          </Checkbox>
          <HttpMethodText class="w-20 mr-2 h-5 pt-0.75" :value="item?.method" />
          <div
            class="truncate mr-2 pt-1 w-40"
            :title="item.endpoint">
            {{ item.endpoint }}
          </div>
          <div
            class="truncate pt-1 flex-1"
            :title="item.summary">
            {{ item.summary }}
          </div>
        </div>
      </CheckboxGroup>
      <Divider class="my-2" />
      <Checkbox
        :checked="(checkedList.length > 0 && checkedList.length === dataList.length)"
        :indeterminate="(checkedList.length > 0 && checkedList.length !== dataList.length)"
        @change="e => handleCheckAllChange(e.target.checked)">
        {{ t('actions.selectAll') }}
      </Checkbox>
    </FormItemRest>
  </div>
</template>
