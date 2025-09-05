import { computed, ref, watch } from 'vue';
import { mock } from '@/api/tester';

import { GridColumn, HeaderItem, RequestRecordDetail } from '@/views/apis/mock/detail/types';

/**
 * Composable for managing detailed request/response information.
 * <p>
 * Handles fetching and formatting of request/response headers and body data
 * for the selected record.
 */
export function useRecordDetail () {
  const detail = ref<RequestRecordDetail>();
  const requestColumns = ref<GridColumn[][]>([[]]);
  const requestInfo = ref<Record<string, string>>({});
  const responseColumns = ref<GridColumn[][]>([[]]);
  const responseInfo = ref<Record<string, string>>({});

  /**
   * Transform header array into grid column format and info object.
   */
  const transformHeaders = (headers: HeaderItem[] = []) => {
    const columns: GridColumn[] = [];
    const info: Record<string, string> = {};

    headers.forEach(header => {
      columns.push({
        label: header.name,
        dataIndex: header.name
      });
      info[header.name] = header.value;
    });

    return { columns, info };
  };

  /**
   * Fetch detailed information for a specific record.
   */
  const loadRecordDetail = async (recordId: string) => {
    if (!recordId) return;

    // Reset state
    requestColumns.value = [[]];
    requestInfo.value = {};
    responseColumns.value = [[]];
    responseInfo.value = {};

    const [error, { data }] = await mock.getMockApiLogDetail(recordId);
    if (error) return;

    detail.value = data;

    // Transform request headers
    if (data.requestHeaders?.length) {
      const { columns, info } = transformHeaders(data.requestHeaders);
      requestColumns.value = [columns];
      requestInfo.value = info;
    }

    // Transform response headers
    if (data.responseHeaders?.length) {
      const { columns, info } = transformHeaders(data.responseHeaders);
      responseColumns.value = [columns];
      responseInfo.value = info;
    }
  };

  /**
   * Computed content type for response body formatting.
   * <p>
   * Currently defaults to 'json' but could be enhanced to detect
   * actual content type from response headers.
   */
  const contentType = computed(() => 'json');

  return {
    detail,
    requestColumns,
    requestInfo,
    responseColumns,
    responseInfo,
    contentType,
    loadRecordDetail
  };
}
