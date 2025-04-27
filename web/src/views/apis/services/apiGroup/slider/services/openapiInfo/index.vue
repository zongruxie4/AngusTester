<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { TypographyParagraph } from 'ant-design-vue';
import { AsyncComponent, Grid, Icon, Input } from '@xcan-angus/vue-ui';

import { services } from '@/api/altester';
import { ISchema } from './PropsType';

interface Props {
  id: string;
  type: 'PROJECT' | 'SERVICE';
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  type: 'PROJECT',
  disabled: false
});

const DescriptionModal = defineAsyncComponent(() => import('@/views/apis/services/components/markdownDescModal/index.vue'));

const columns = [[
  { label: '名称', dataIndex: 'title' },
  { label: '摘要', dataIndex: 'summary' },
  { label: 'OpenAPI', dataIndex: 'openapi' },
  { label: '服务条款', dataIndex: 'termsOfService' },
  { label: '联系人', dataIndex: 'contact' },
  { label: '许可协议', dataIndex: 'license' },
  { label: '文档版本', dataIndex: 'version' },
  { label: '外部文档', dataIndex: 'externalDocs' },
  { label: '描述', dataIndex: 'description' }
]];

const title = ref<string>();
const titleError = ref(false);
const editTitleFlag = ref(false);
const editTitle = () => {
  editTitleFlag.value = true;
};
const confirmEditTitle = async () => {
  if (!title.value) {
    titleError.value = true;
    return;
  }

  const [error] = await toSave();
  if (error) {
    return;
  }
  schemaInfo.value!.info.title = title.value;
  editTitleFlag.value = false;
};
const cancelEditTitle = () => {
  editTitleFlag.value = false;
  titleError.value = false;
  title.value = schemaInfo.value?.info.title;
};
const titleChange = () => {
  titleError.value = false;
};

const summary = ref<string>();
const editSummaryFlag = ref(false);
const editSummary = () => {
  editSummaryFlag.value = true;
};
const confirmEditSummary = async () => {
  const [error] = await toSave();
  if (error) {
    return;
  }
  schemaInfo.value!.info.summary = summary.value;
  editSummaryFlag.value = false;
};
const cancelEditSummary = () => {
  editSummaryFlag.value = false;
  summary.value = schemaInfo.value?.info.summary;
};

const openapi = ref<string>();

const termsOfService = ref<string>();
const editTermsOfServiceFlag = ref(false);
const editTermsOfService = () => {
  editTermsOfServiceFlag.value = true;
};
const confirmEditTermsOfService = async () => {
  const [error] = await toSave();
  if (error) {
    return;
  }
  schemaInfo.value!.info.termsOfService = termsOfService.value;
  editTermsOfServiceFlag.value = false;
};
const cancelEditTermsOfService = () => {
  editTermsOfServiceFlag.value = false;
  termsOfService.value = schemaInfo.value?.info.termsOfService;
};

const contact = ref<{ name: string; email: string; url: string; }>({ name: '', email: '', url: '' });
const editContactFlag = ref(false);
const editContact = () => {
  editContactFlag.value = true;
};
const confirmEditContact = async () => {
  const [error] = await toSave();
  if (error) {
    return;
  }
  schemaInfo.value!.info.contact = contact.value;
  editContactFlag.value = false;
};
const cancelEditContact = () => {
  editContactFlag.value = false;
  contact.value = schemaInfo.value?.info.contact || { name: '', email: '', url: '' };
};

const license = ref<{ name: string; url: string; }>({ name: '', url: '' });
const editLicenseFlag = ref(false);
const editLicense = () => {
  editLicenseFlag.value = true;
};
const confirmEditLicense = async () => {
  const [error] = await toSave();
  if (error) {
    return;
  }
  schemaInfo.value!.info.license = license.value;
  editLicenseFlag.value = false;
};
const cancelEditLicense = () => {
  editLicenseFlag.value = false;
  license.value = schemaInfo.value?.info.license || { name: '', url: '' };
};

const version = ref<string>();
const editVersionFlag = ref(false);
const editVersion = () => {
  editVersionFlag.value = true;
};
const confirmEditVersion = async () => {
  const [error] = await toSave();
  if (error) {
    return;
  }
  schemaInfo.value!.info.version = version.value;
  editVersionFlag.value = false;
};
const cancelEditVersion = () => {
  editVersionFlag.value = false;
  version.value = schemaInfo.value?.info.version;
};

const externalDocs = ref<{ description: string; url: string }>({ description: '', url: '' });
const editExternalDocsFlag = ref(false);
const editExternalDocs = () => {
  editExternalDocsFlag.value = true;
};
const confirmEditExternalDocs = async () => {
  const [error] = await toSave();
  if (error) {
    return;
  }
  schemaInfo.value!.externalDocs = externalDocs.value;
  editExternalDocsFlag.value = false;
};
const cancelEditExternalDocs = () => {
  editExternalDocsFlag.value = false;
  externalDocs.value = schemaInfo.value?.externalDocs || { description: '', url: '' };
};

const modalVisible = ref(false);
const description = ref<string>();
const editDescriptionFlag = ref(false);
const editDescription = () => {
  modalVisible.value = true;
  editDescriptionFlag.value = true;
};
const previewDescription = () => {
  modalVisible.value = true;
  editDescriptionFlag.value = false;
};
const cancelEditDescription = () => {
  modalVisible.value = false;
  editDescriptionFlag.value = false;
  description.value = schemaInfo.value?.info.description;
};
const saveDescription = async (value) => {
  if (!editDescriptionFlag.value) {
    return;
  }
  description.value = value;

  const [error] = await toSave();
  if (error) {
    return;
  }
  schemaInfo.value!.info.description = description.value;
  modalVisible.value = false;
  editDescriptionFlag.value = false;
};

const toSave = async () => {
  const params = {
    contact: contact.value,
    description: description.value,
    license: license.value,
    summary: summary.value,
    termsOfService: termsOfService.value,
    title: title.value,
    version: version.value
  };
  return await services.putSchemaInfo(props.id, params);
};

const schemaInfo = ref<ISchema>();
const loadInfo = async () => {
  const [error, res] = await services.loadSchema(props.id);
  if (error) {
    return;
  }

  const data: ISchema = res.data;
  schemaInfo.value = JSON.parse(JSON.stringify(data));
  title.value = data.info.title;
  summary.value = data.info.summary;
  termsOfService.value = data.info.termsOfService;
  contact.value = data.info.contact || { name: '', email: '', url: '' };
  license.value = data.info.license || { name: '', url: '' };
  version.value = data.info.version;
  openapi.value = data.openapi;
  description.value = data.info.description;
  externalDocs.value = schemaInfo.value?.externalDocs || { description: '', url: '' };
};

onMounted(() => {
  watch(() => props.id, (newValue) => {
    if (newValue && ['PROJECT', 'SERVICE'].includes(props.type)) {
      loadInfo();
    }
  }, { immediate: true });
});
</script>
<template>
  <Grid
    :columns="columns"
    marginBottom="18px">
    <template #title>
      <template v-if="!editTitleFlag">
        <div class="flex items-start">
          <TypographyParagraph
            class="flex-1"
            :content="title"
            :ellipsis="{ rows: 3, expandable: true }" />
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editTitle" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <Input
            v-model:value="title"
            :maxlength="100"
            :error="titleError"
            :autoSize="{ minRows: 3, maxRows: 8 }"
            trim
            type="textarea"
            showCount
            class="w-full"
            placeholder="100字符以内"
            @pressEnter="confirmEditTitle"
            @change="titleChange" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditTitle" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditTitle" />
        </div>
      </template>
    </template>
    <template #summary>
      <template v-if="!editSummaryFlag">
        <div class="flex items-start">
          <TypographyParagraph
            class="flex-1"
            :content="summary"
            :ellipsis="{ rows: 3, expandable: true }" />
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editSummary" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <Input
            v-model:value="summary"
            :maxlength="400"
            :autoSize="{ minRows: 3, maxRows: 8 }"
            trim
            type="textarea"
            showCount
            class="w-full"
            placeholder="400字符以内"
            @pressEnter="confirmEditSummary" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditSummary" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditSummary" />
        </div>
      </template>
    </template>
    <template #openapi>{{ openapi }}</template>
    <template #termsOfService>
      <template v-if="!editTermsOfServiceFlag">
        <div class="flex items-start">
          <a
            :href="termsOfService"
            class="flex-1 block"
            target="_blank"
            rel="noreferrer">
            <TypographyParagraph :content="termsOfService" :ellipsis="{ rows: 3, expandable: true }" />
          </a>
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editTermsOfService" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <Input
            v-model:value="termsOfService"
            :maxlength="800"
            :autoSize="{ minRows: 3, maxRows: 8 }"
            trim
            type="textarea"
            showCount
            class="w-full"
            placeholder="800字符以内"
            @pressEnter="confirmEditTermsOfService" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditTermsOfService" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditTermsOfService" />
        </div>
      </template>
    </template>
    <template #contact>
      <template v-if="!editContactFlag">
        <div class="flex items-start">
          <div class="flex-1 space-y-2">
            <TypographyParagraph
              v-if="contact.name"
              :content="contact.name"
              :ellipsis="{ rows: 3, expandable: true }" />
            <TypographyParagraph
              v-if="contact.email"
              :content="contact.email"
              :ellipsis="{ rows: 3, expandable: true }" />
            <a
              v-if="contact.url"
              :href="contact.url"
              class="block"
              target="_blank"
              rel="noreferrer">
              <TypographyParagraph :content="contact.url" :ellipsis="{ rows: 3, expandable: true }" />
            </a>
          </div>
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editContact" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <div class="space-y-2">
            <Input
              v-model:value="contact.name"
              :maxlength="100"
              placeholder="名称(100字符以内)"
              class="w-full"
              @pressEnter="confirmEditContact" />
            <Input
              v-model:value="contact.email"
              :maxlength="400"
              placeholder="邮箱"
              class="w-full"
              @pressEnter="confirmEditContact" />
            <Input
              v-model:value="contact.url"
              :maxlength="800"
              placeholder="url"
              class="w-full"
              @pressEnter="confirmEditContact" />
          </div>
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditContact" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditContact" />
        </div>
      </template>
    </template>
    <template #license>
      <template v-if="!editLicenseFlag">
        <div class="flex items-start">
          <div class="flex-1 space-y-2">
            <TypographyParagraph
              v-if="license.name"
              :content="license.name"
              :ellipsis="{ rows: 3, expandable: true }" />
            <a
              v-if="license.url"
              :href="license.url"
              class="block"
              target="_blank"
              rel="noreferrer">
              <TypographyParagraph :content="license.url" :ellipsis="{ rows: 3, expandable: true }" />
            </a>
          </div>
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editLicense" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <div class="space-y-2">
            <Input
              v-model:value="license.name"
              :maxlength="100"
              placeholder="名称(100字符以内)"
              class="w-full"
              @pressEnter="confirmEditLicense" />
            <Input
              v-model:value="license.url"
              :maxlength="800"
              placeholder="url"
              class="w-full"
              @pressEnter="confirmEditLicense" />
          </div>
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditLicense" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditLicense" />
        </div>
      </template>
    </template>
    <template #version>
      <template v-if="!editVersionFlag">
        <div class="flex items-start">
          <div class="flex-1">{{ version }}</div>
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editVersion" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <Input
            v-model:value="version"
            :maxlength="100"
            class="w-full"
            placeholder="100字符以内"
            @pressEnter="confirmEditVersion" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditVersion" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditVersion" />
        </div>
      </template>
    </template>
    <template #externalDocs>
      <template v-if="!editExternalDocsFlag">
        <div class="flex items-start">
          <div class="flex-1 space-y-2">
            <a
              v-if="externalDocs.url"
              class="block"
              :href="externalDocs.url"
              target="_blank"
              rel="noreferrer">
              <TypographyParagraph :content="externalDocs.url" :ellipsis="{ rows: 3, expandable: true }" />
            </a>
            <TypographyParagraph
              v-if="externalDocs.description"
              :content="externalDocs.description"
              :ellipsis="{ rows: 3, expandable: true }" />
          </div>
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editExternalDocs" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <div class="space-y-2">
            <Input
              v-model:value="externalDocs.url"
              :maxlength="100"
              placeholder="url"
              class="w-full"
              @pressEnter="confirmEditExternalDocs" />
            <Input
              v-model:value="externalDocs.description"
              :maxlength="800"
              :autoSize="{ minRows: 3, maxRows: 8 }"
              trim
              type="textarea"
              showCount
              class="w-full"
              placeholder="描述（800字符以内）"
              @pressEnter="confirmEditExternalDocs" />
          </div>
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditExternalDocs" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditExternalDocs" />
        </div>
      </template>
    </template>
    <template #description>
      <div class="flex items-start">
        <TypographyParagraph
          class="flex-1"
          :content="description"
          :ellipsis="{ rows: 3, expandable: false }" />
        <div title="查看" class="block leading-3 h-3 w-3 flex-shrink-0 ml-2 mt-0.25 cursor-pointer text-text-link">
          <Icon icon="icon-spread" @click="previewDescription" />
        </div>
        <Icon
          v-if="!props.disabled"
          icon="icon-shuxie"
          class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
          @click="editDescription" />
      </div>
    </template>
  </Grid>
  <AsyncComponent :visible="modalVisible">
    <DescriptionModal
      v-model:visible="modalVisible"
      :value="description"
      :isEdit="editDescriptionFlag"
      @ok="saveDescription"
      @cancel="cancelEditDescription" />
  </AsyncComponent>
</template>
