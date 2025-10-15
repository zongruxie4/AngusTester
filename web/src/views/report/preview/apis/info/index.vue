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

const apis = computed(() => {
  return props.dataSource?.content?.apis;
});

const columns = computed(() => [
  [
    {
      dataIndex: 'endpoint',
      name: t('reportPreview.apis.info.fields.apiUrl')
    },
    {
      dataIndex: 'method',
      name: t('protocol.requestMethod'),
      customRender: (text) => {
        return text?.message;
      }
    }
  ],
  [
    {
      dataIndex: 'operationId',
      name: t('reportPreview.apis.info.fields.operationId')
    },
    {
      dataIndex: 'ownerName',
      name: t('common.owner')
    }
  ],
  [
    {
      dataIndex: 'description',
      name: t('common.description')
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
      dataIndex: 'createdByName',
      name: t('common.creator')
    },
    {
      dataIndex: 'createdDate',
      name: t('common.createdDate')
    }
  ],
  [
    {
      dataIndex: 'lastModifiedByName',
      name: t('common.modifier')
    },
    {
      dataIndex: 'lastModifiedDate',
      name: t('common.lastModifiedDate')
    }
  ]
]);

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.apis.info.title') }}</span>
    </h1>
    <div class="border-t border-l border-solid border-border-input">
      <div v-for="column in columns" class="flex border-b border-solid border-border-input">
        <template v-for="item in column">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ item.name }}
          </div>
          <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ item.customRender ? item.customRender(apis[item.dataIndex]) : apis[item.dataIndex] }}
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
