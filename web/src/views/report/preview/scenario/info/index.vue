<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../../PropsType';

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

const scenario = computed(() => {
  return props.dataSource?.content?.scenario;
});

const columns = computed(() => [
  [
    {
      dataIndex: 'name',
      name: t('reportPreview.scenario.info.fields.name')
    },
    {
      dataIndex: 'description',
      name: t('reportPreview.scenario.info.fields.description')
    }
  ],
  [
    {
      dataIndex: 'projectName',
      name: t('reportPreview.scenario.info.fields.project')
    },
    {
      dataIndex: 'plugin',
      name: t('reportPreview.scenario.info.fields.plugin')
    }
  ],
  [
    {
      dataIndex: 'scriptType',
      name: t('reportPreview.scenario.info.fields.scriptType'),
      customRender: (text) => {
        return text?.message;
      }
    },
    {
      dataIndex: 'scriptId',
      name: t('reportPreview.scenario.info.fields.scriptId')
    }
  ],
  [
    {
      dataIndex: 'scriptName',
      name: t('reportPreview.scenario.info.fields.scriptName')
    },
    {
      dataIndex: 'createdByName',
      name: t('reportPreview.scenario.info.fields.creator')
    }
  ],
  [
    {
      dataIndex: 'createdDate',
      name: t('reportPreview.scenario.info.fields.createTime')
    },
    {
      dataIndex: 'lastModifiedByName',
      name: t('reportPreview.scenario.info.fields.lastModifier')
    }
  ],
  [
    {
      dataIndex: 'lastModifiedDate',
      name: t('reportPreview.scenario.info.fields.lastModifyTime')
    }
  ]
]);

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.scenario.info.title') }}</span>
    </h1>
    <div class="border-t border-l border-solid border-border-input">
      <div v-for="column in columns" class="flex border-b border-solid border-border-input">
        <template v-for="item in column">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ item.name }}
          </div>
          <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ item.customRender ? item.customRender(scenario[item.dataIndex]) : scenario[item.dataIndex] }}
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
