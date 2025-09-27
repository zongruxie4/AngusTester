<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../PropsType';

const { t } = useI18n();

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const services = computed(() => {
  return props.dataSource?.content?.services;
});

const columns = computed(() => [
  [
    {
      dataIndex: 'name',
      name: t('common.name')
    },
    {
      dataIndex: 'status',
      name: t('common.status'),
      customRender: (text) => {
        return text?.message;
      }
    }
  ],
  [
    {
      dataIndex: 'projectName',
      name: t('reportPreview.services.info.fields.project')
    },
    {
      dataIndex: 'apisNum',
      name: t('reportPreview.services.info.fields.apiCount')
    }
  ],
  [
    {
      dataIndex: 'apisCaseNum',
      name: t('reportPreview.services.info.fields.apiCaseCount')
    },
    {
      dataIndex: 'createdByName',
      name: t('common.creator')
    }
  ],
  [
    {
      dataIndex: 'createdDate',
      name: t('common.createdDate')
    },
    {
      dataIndex: 'lastModifiedByName',
      name: t('common.modifier')
    }
  ],
  [
    {
      dataIndex: 'lastModifiedDate',
      name: t('common.lastModifiedDate')
    },
    {
      dataIndex: '',
      name: ''
    }
  ]
]);

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.services.info.title') }}</span>
    </h1>
    <div class="border-t border-l border-solid border-border-input">
      <div v-for="column in columns" class="flex border-b border-solid border-border-input">
        <template v-for="item in column">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ item.name }}
          </div>
          <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ item.customRender ? item.customRender(services[item.dataIndex]) : services[item.dataIndex] }}
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
