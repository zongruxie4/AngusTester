<script lang="ts" setup>
import { Ref, defineAsyncComponent, inject, ref } from 'vue';
import { Button, Pagination } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, GridList, Icon, IconRefresh, Image, NoData, SearchPanel, Spin, Tooltip } from '@xcan-angus/vue-ui';
import { useDataSource } from './composables/useDataSource';

const { t } = useI18n();

// Async component imports for better performance
const AddModal = defineAsyncComponent(() => import('./Add.vue'));

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

/**
 * <p>Main composable for component logic</p>
 * <p>Integrates all business logic, validation, and API operations</p>
 */
const {
  // State
  loading,
  isFirstLoad,
  dataList,
  dataMap,
  total,
  params,
  isModalVisible,
  editData,

  // Search configuration
  searchOptions,
  generatePaginationTotalText,
  shouldShowPagination,

  // Methods
  handleSearchChange,
  handlePaginationChange,
  handleRefresh,
  handleAdd,
  handleEdit,
  handleDelete,
  handleTestConnection,
  handleModalClose,
  handleModalRefresh
} = useDataSource(projectId.value);
</script>

<template>
  <Spin
    :spinning="loading"
    class="pl-5 py-5 w-full h-full flex flex-col">
    <!-- Page Header -->
    <div class="text-3.5 font-semibold mb-2.5">{{ t('datasource.title') }}</div>
    <div class="mb-6 text-3.5">
      <div>{{ t('datasource.description') }}</div>
    </div>

    <!-- Added Data Sources Section -->
    <div class="text-3.5 font-semibold mb-2.5">{{ t('datasource.addedTitle') }}</div>

    <!-- Search and Action Bar -->
    <div class="flex pr-5">
      <SearchPanel
        :options="searchOptions"
        :width="284"
        class="flex-1"
        @change="handleSearchChange" />
      <div class="flex space-x-2.5">
        <Button
          type="primary"
          size="small"
          class="flex space-x-1"
          @click="handleAdd">
          <Icon icon="icon-jia" />
          {{ t('datasource.addDataSource') }}
        </Button>
        <Button
          class="flex items-center"
          size="small"
          type="default"
          @click="handleRefresh">
          <IconRefresh />
        </Button>
      </div>
    </div>

    <!-- Data Source List -->
    <template v-if="!isFirstLoad">
      <template v-if="dataList?.length">
        <GridList
          class="mt-3 overflow-y-auto flex flex-warp text-3 text-text-content"
          style="padding-right: 13px; scrollbar-gutter: stable;"
          :itemWidth="328"
          :dataSource="dataList">
          <template #default="record">
            <div class="px-3 pt-3 pb-2.5 border rounded border-border-divider flex flex-col justify-between w-full">
              <!-- Data Source Header -->
              <div class="w-full flex-1">
                <div class="flex items-center w-full mb-4">
                  <!-- Database Icon -->
                  <div style="background-color: #F7F8FB;" class="w-15 h-15 flex-none p-1.25 rounded mr-2.5">
                    <template v-if="record.database === 'H2'">
                      <img src="../../../assets/images/database/H2.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'HSQLDB'">
                      <img src="../../../assets/images/database/HSQLDB.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'SQLITE'">
                      <img src="../../../assets/images/database/SQLITE.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'POSTGRES'">
                      <img src="../../../assets/images/database/POSTGRES.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'MARIADB'">
                      <img src="../../../assets/images/database/MARIADB.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'MYSQL'">
                      <img src="../../../assets/images/database/MYSQL.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'ORACLE'">
                      <img src="../../../assets/images/database/ORACLE.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'SQLSERVER'">
                      <img src="../../../assets/images/database/SQLSERVER.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'DB2'">
                      <img src="../../../assets/images/database/DB2.png" class="w-full h-full" />
                    </template>
                  </div>

                  <!-- Data Source Info -->
                  <div class="flex items-center" style="width: calc(100% - 70px);">
                    <div class="mr-5 truncate flex-1 text-3.5 font-medium text-text-title" :title="record.name">
                      {{ record.name }}
                    </div>
                    <div class="px-2 py-0.25 whitespace-nowrap rounded -mt-10" style="background-color: #F7F8FB;color: #828894;">
                      {{ record.database }}
                    </div>
                  </div>
                </div>

                <!-- User Info -->
                <div class="flex items-center text-text-sub-content">
                  <div class="flex-none">
                    <Image
                      :src="record?.avatar"
                      class="w-5 h-5 rounded-full flex-none mr-2"
                      type="avatar" />
                  </div>
                  <div
                    style="max-width: 110px;"
                    class="truncate leading-5"
                    :title="record.lastModifiedByName">
                    {{ record.lastModifiedByName }}
                  </div>
                  <div class="flex-none leading-5">
                    &nbsp;&nbsp;{{ t('datasource.lastModifiedBy') }}&nbsp;&nbsp;{{ record.lastModifiedDate }}
                  </div>
                </div>
              </div>

              <!-- Action Bar -->
              <div class="border-t border-border-divider my-2.5"></div>
              <div class="flex justify-between items-center leading-3">
                <!-- Edit/Delete Actions -->
                <div class="flex space-x-2">
                  <Icon
                    icon="icon-shuxie"
                    class="text-3.5 text-text-sub-content cursor-pointer hover:text-text-link-hover"
                    @click="handleEdit(record)" />
                  <Icon
                    icon="icon-qingchu"
                    class="text-3.5 text-text-sub-content cursor-pointer hover:text-text-link-hover"
                    @click="handleDelete(record)" />
                </div>

                <!-- Connection Test Status -->
                <div class="flex items-center h-3">
                  <template v-if="dataMap[record.id]?.testLoading" class="text-text-sub-content">
                    {{ t('datasource.testConnection.testing') }}
                  </template>
                  <template v-else>
                    <template v-if="(typeof dataMap[record.id]?.connSuccess) === 'boolean'">
                      <div class="flex items-center">
                        <template v-if="dataMap[record.id]?.connSuccess">
                          <Icon icon="icon-duihao" class="text-status-success mr-1 text-3.25" />
                          <span>{{ t('datasource.testConnection.success') }}</span>
                        </template>
                        <template v-else>
                          <Tooltip
                            placement="topLeft"
                            arrowPointAtCenter
                            :title="dataMap[record.id]?.connFailureMessage"
                            :overlayStyle="{'max-width': '400px'}">
                            <div class="flex items-center cursor-pointer">
                              <Icon icon="icon-jinggao" class="text-status-error mr-1 text-3.25 -mt-0.25" />
                              <span>{{ t('datasource.testConnection.failed') }}</span>
                            </div>
                          </Tooltip>
                          <div>
                            <a class="ml-2" @click="handleTestConnection(record)">
                              {{ t('datasource.testConnection.retest') }}
                            </a>
                          </div>
                        </template>
                      </div>
                    </template>
                    <template v-else>
                      <div>
                        <a @click="handleTestConnection(record)">
                          {{ t('datasource.testConnection.testConnection') }}
                        </a>
                      </div>
                    </template>
                  </template>
                </div>
              </div>
            </div>
          </template>
        </GridList>

        <!-- Pagination -->
        <Pagination
          v-if="shouldShowPagination(total, params.pageSize)"
          :current="params.pageNo"
          :pageSize="params.pageSize"
          :total="total"
          :hideOnSinglePage="false"
          :showTotal="(total) => generatePaginationTotalText(total, params.pageNo, params.pageSize)"
          :showSizeChanger="false"
          size="middle"
          class="justify-end mr-5"
          @change="handlePaginationChange" />
      </template>

      <!-- No Data State -->
      <template v-if="!loading && !dataList?.length">
        <div class="pt-45">
          <NoData />
        </div>
      </template>
    </template>
  </Spin>

  <!-- Add/Edit Modal -->
  <AsyncComponent :visible="isModalVisible">
    <AddModal
      v-model:visible="isModalVisible"
      :editData="editData"
      :projectId="projectId"
      @refresh="handleModalRefresh" />
  </AsyncComponent>
</template>
