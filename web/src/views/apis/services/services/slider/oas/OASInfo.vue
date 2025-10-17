<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { TypographyParagraph } from 'ant-design-vue';
import { AsyncComponent, Grid, Icon, Input } from '@xcan-angus/vue-ui';
import { services } from '@/api/tester';
import { OpenAPIV3_1 } from '@/types/openapi-types';
import { ServiceSchemaDetail } from '@/views/apis/services/services/types';

const DescriptionModal = defineAsyncComponent(() => import('@/views/apis/services/components/MarkdownDescModal.vue'));

// Props and Setup
interface Props {
  id: string;
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  disabled: false
});

const { t } = useI18n();

// Data State
const schemaInfo = ref<ServiceSchemaDetail>();
const openapi = ref<string>();

// Title related state
const title = ref<string>();
const titleError = ref(false);
const isEditingTitle = ref(false);

// Summary related state
const summary = ref<string>();
const isEditingSummary = ref(false);

// Terms of service related state
const termsOfService = ref<string>();
const isEditingTermsOfService = ref(false);

// Contact related state
const contact = ref<OpenAPIV3_1.ContactObject>({ });
const isEditingContact = ref(false);

// License related state
const license = ref<OpenAPIV3_1.LicenseObject>({ });
const isEditingLicense = ref(false);

// Version related state
const version = ref<string>();
const isEditingVersion = ref(false);

// External docs related state
const externalDocs = ref<OpenAPIV3_1.ExternalDocumentationObject>({ });
const isEditingExternalDocs = ref(false);

// Description modal related state
const isModalVisible = ref(false);
const description = ref<string>();
const isEditingDescription = ref(false);

/**
 * Load schema information from API
 */
const loadSchemaInfo = async () => {
  const [error, res] = await services.loadSchema(props.id);
  if (error) {
    return;
  }

  const data: ServiceSchemaDetail = res.data;
  schemaInfo.value = JSON.parse(JSON.stringify(data));

  // Initialize form data
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

/**
 * Save schema information to API
 */
const saveSchemaInfo = async () => {
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

// Title Management
const startEditingTitle = () => {
  isEditingTitle.value = true;
};

/**
 * Confirm title edit and save to API
 */
const confirmTitleEdit = async () => {
  if (!title.value) {
    titleError.value = true;
    return;
  }

  const [error] = await saveSchemaInfo();
  if (error) {
    return;
  }
  schemaInfo.value!.info.title = title.value;
  isEditingTitle.value = false;
};

const cancelTitleEdit = () => {
  isEditingTitle.value = false;
  titleError.value = false;
  title.value = schemaInfo.value?.info.title;
};

const handleTitleChange = () => {
  titleError.value = false;
};

// Summary Management
const startEditingSummary = () => {
  isEditingSummary.value = true;
};

/**
 * Confirm summary edit and save to API
 */
const confirmSummaryEdit = async () => {
  const [error] = await saveSchemaInfo();
  if (error) {
    return;
  }
  schemaInfo.value?.info.summary = summary.value;
  isEditingSummary.value = false;
};

const cancelSummaryEdit = () => {
  isEditingSummary.value = false;
  summary.value = schemaInfo.value?.info.summary;
};

// Terms of Service Management
const startEditingTermsOfService = () => {
  isEditingTermsOfService.value = true;
};

/**
 * Confirm terms of service edit and save to API
 */
const confirmTermsOfServiceEdit = async () => {
  const [error] = await saveSchemaInfo();
  if (error) {
    return;
  }
  schemaInfo.value?.info.termsOfService = termsOfService.value;
  isEditingTermsOfService.value = false;
};

const cancelTermsOfServiceEdit = () => {
  isEditingTermsOfService.value = false;
  termsOfService.value = schemaInfo.value?.info.termsOfService;
};

// Contact Management
const startEditingContact = () => {
  isEditingContact.value = true;
};

/**
 * Confirm contact edit and save to API
 */
const confirmContactEdit = async () => {
  const [error] = await saveSchemaInfo();
  if (error) {
    return;
  }
  schemaInfo.value?.info.contact = contact.value;
  isEditingContact.value = false;
};

const cancelContactEdit = () => {
  isEditingContact.value = false;
  contact.value = schemaInfo.value?.info.contact || { name: '', email: '', url: '' };
};

// License Management
const startEditingLicense = () => {
  isEditingLicense.value = true;
};

/**
 * Confirm license edit and save to API
 */
const confirmLicenseEdit = async () => {
  const [error] = await saveSchemaInfo();
  if (error) {
    return;
  }
  schemaInfo.value?.info.license = license.value;
  isEditingLicense.value = false;
};

const cancelLicenseEdit = () => {
  isEditingLicense.value = false;
  license.value = schemaInfo.value?.info.license || { name: '', url: '' };
};

// Version Management
const startEditingVersion = () => {
  isEditingVersion.value = true;
};

/**
 * Confirm version edit and save to API
 */
const confirmVersionEdit = async () => {
  const [error] = await saveSchemaInfo();
  if (error) {
    return;
  }
  schemaInfo.value?.info.version = version.value;
  isEditingVersion.value = false;
};

const cancelVersionEdit = () => {
  isEditingVersion.value = false;
  version.value = schemaInfo.value?.info.version;
};

// External Docs Management
const startEditingExternalDocs = () => {
  isEditingExternalDocs.value = true;
};

/**
 * Confirm external docs edit and save to API
 */
const confirmExternalDocsEdit = async () => {
  const [error] = await saveSchemaInfo();
  if (error) {
    return;
  }
  schemaInfo.value.externalDocs = externalDocs.value;
  isEditingExternalDocs.value = false;
};

const cancelExternalDocsEdit = () => {
  isEditingExternalDocs.value = false;
  externalDocs.value = schemaInfo.value.externalDocs || { description: '', url: '' };
};

// Description Management
const startEditingDescription = () => {
  isModalVisible.value = true;
  isEditingDescription.value = true;
};

const previewDescription = () => {
  isModalVisible.value = true;
  isEditingDescription.value = false;
};

const cancelDescriptionEdit = () => {
  isModalVisible.value = false;
  isEditingDescription.value = false;
  description.value = schemaInfo.value?.info.description;
};

/**
 * Save description changes to API
 */
const saveDescription = async (value) => {
  if (!isEditingDescription.value) {
    return;
  }
  description.value = value;

  const [error] = await saveSchemaInfo();
  if (error) {
    return;
  }
  schemaInfo.value?.info.description = description.value;
  isModalVisible.value = false;
  isEditingDescription.value = false;
};

// Lifecycle
onMounted(() => {
  watch(() => props.id, (newValue) => {
    if (newValue) {
      loadSchemaInfo();
    }
  }, { immediate: true });
});

// Table Configuration
const columns = [[
  { label: t('service.serviceOpenApi.columns.title'), dataIndex: 'title' },
  { label: t('service.serviceOpenApi.columns.summary'), dataIndex: 'summary' },
  { label: t('service.serviceOpenApi.columns.openapi'), dataIndex: 'openapi' },
  { label: t('service.serviceOpenApi.columns.termsOfService'), dataIndex: 'termsOfService' },
  { label: t('service.serviceOpenApi.columns.contact'), dataIndex: 'contact' },
  { label: t('service.serviceOpenApi.columns.license'), dataIndex: 'license' },
  { label: t('common.version'), dataIndex: 'version' },
  { label: t('service.serviceOpenApi.columns.externalDocs'), dataIndex: 'externalDocs' },
  { label: t('common.description'), dataIndex: 'description' }
]];
</script>
<template>
  <Grid
    :columns="columns"
    marginBottom="18px">
    <template #title>
      <template v-if="!isEditingTitle">
        <div class="flex items-start">
          <TypographyParagraph
            class="flex-1"
            :content="title"
            :ellipsis="{ rows: 3, expandable: true }" />
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="startEditingTitle" />
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
            :placeholder="t('service.serviceOpenApi.placeholder.title')"
            @pressEnter="confirmTitleEdit"
            @change="handleTitleChange" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmTitleEdit" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelTitleEdit" />
        </div>
      </template>
    </template>
    <template #summary>
      <template v-if="!isEditingSummary">
        <div class="flex items-start">
          <TypographyParagraph
            class="flex-1"
            :content="summary"
            :ellipsis="{ rows: 3, expandable: true }" />
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="startEditingSummary" />
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
            :placeholder="t('service.serviceOpenApi.placeholder.summary')"
            @pressEnter="confirmSummaryEdit" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmSummaryEdit" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelSummaryEdit" />
        </div>
      </template>
    </template>
    <template #openapi>{{ openapi }}</template>
    <template #termsOfService>
      <template v-if="!isEditingTermsOfService">
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
            @click="startEditingTermsOfService" />
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
            :placeholder="t('service.serviceOpenApi.placeholder.termsOfService')"
            @pressEnter="confirmTermsOfServiceEdit" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmTermsOfServiceEdit" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelTermsOfServiceEdit" />
        </div>
      </template>
    </template>
    <template #contact>
      <template v-if="!isEditingContact">
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
            @click="startEditingContact" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <div class="space-y-2">
            <Input
              v-model:value="contact.name"
              :maxlength="100"
              :placeholder="t('service.serviceOpenApi.placeholder.contactName')"
              class="w-full"
              @pressEnter="confirmContactEdit" />
            <Input
              v-model:value="contact.email"
              :maxlength="400"
              :placeholder="t('service.serviceOpenApi.placeholder.contactEmail')"
              class="w-full"
              @pressEnter="confirmContactEdit" />
            <Input
              v-model:value="contact.url"
              :maxlength="800"
              :placeholder="t('service.serviceOpenApi.placeholder.contactUrl')"
              class="w-full"
              @pressEnter="confirmContactEdit" />
          </div>
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmContactEdit" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelContactEdit" />
        </div>
      </template>
    </template>
    <template #license>
      <template v-if="!isEditingLicense">
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
            @click="startEditingLicense" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <div class="space-y-2">
            <Input
              v-model:value="license.name"
              :maxlength="100"
              :placeholder="t('service.serviceOpenApi.placeholder.licenseName')"
              class="w-full"
              @pressEnter="confirmLicenseEdit" />
            <Input
              v-model:value="license.url"
              :maxlength="800"
              :placeholder="t('service.serviceOpenApi.placeholder.licenseUrl')"
              class="w-full"
              @pressEnter="confirmLicenseEdit" />
          </div>
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmLicenseEdit" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelLicenseEdit" />
        </div>
      </template>
    </template>
    <template #version>
      <template v-if="!isEditingVersion">
        <div class="flex items-start">
          <div class="flex-1">{{ version }}</div>
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="startEditingVersion" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <Input
            v-model:value="version"
            :maxlength="100"
            class="w-full"
            :placeholder="t('common.version')"
            @pressEnter="confirmVersionEdit" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmVersionEdit" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelVersionEdit" />
        </div>
      </template>
    </template>
    <template #externalDocs>
      <template v-if="!isEditingExternalDocs">
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
            @click="startEditingExternalDocs" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <div class="space-y-2">
            <Input
              v-model:value="externalDocs.url"
              :maxlength="100"
              :placeholder="t('service.serviceOpenApi.placeholder.externalDocsUrl')"
              class="w-full"
              @pressEnter="confirmExternalDocsEdit" />
            <Input
              v-model:value="externalDocs.description"
              :maxlength="800"
              :autoSize="{ minRows: 3, maxRows: 8 }"
              trim
              type="textarea"
              showCount
              class="w-full"
              :placeholder="t('service.serviceOpenApi.placeholder.externalDocsDescription')"
              @pressEnter="confirmExternalDocsEdit" />
          </div>
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmExternalDocsEdit" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelExternalDocsEdit" />
        </div>
      </template>
    </template>
    <template #description>
      <div class="flex items-start">
        <TypographyParagraph
          class="flex-1"
          :content="description"
          :ellipsis="{ rows: 3, expandable: false }" />
        <div :title="t('actions.view')" class="block leading-3 h-3 w-3 flex-shrink-0 ml-2 mt-0.25 cursor-pointer text-text-link">
          <Icon icon="icon-spread" @click="previewDescription" />
        </div>
        <Icon
          v-if="!props.disabled"
          icon="icon-shuxie"
          class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
          @click="startEditingDescription" />
      </div>
    </template>
  </Grid>
  <AsyncComponent :visible="isModalVisible">
    <DescriptionModal
      v-model:visible="isModalVisible"
      :value="description"
      :isEdit="isEditingDescription"
      @ok="saveDescription"
      @cancel="cancelDescriptionEdit" />
  </AsyncComponent>
</template>
