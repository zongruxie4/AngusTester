import { useI18n } from 'vue-i18n';
import { HttpMethod } from '@xcan-angus/infra';

/**
 * Composable for grid column definitions and HTTP method styling.
 * <p>
 * Provides consistent configuration for data grid display and
 * HTTP method color coding across the request record interface.
 */
export function useGridColumns () {
  const { t } = useI18n();

  /**
   * Grid column definitions for basic record information.
   */
  const gridColumns = [
    [
      {
        label: t('mock.detail.requestRecord.apiName'),
        dataIndex: 'summary'
      },
      {
        label: t('mock.detail.requestRecord.requestId'),
        dataIndex: 'requestId'
      },
      {
        label: t('mock.detail.requestRecord.requestTime'),
        dataIndex: 'requestDate'
      },
      {
        label: t('mock.detail.requestRecord.requestUrl'),
        dataIndex: 'endpoint'
      },
      {
        label: t('protocol.requestMethod'),
        dataIndex: 'method'
      },
      {
        label: t('protocol.statusCode'),
        dataIndex: 'responseStatus'
      }
    ]
  ];

  /**
   * Format options for response body display.
   */
  const formatTabs = [
    {
      name: t('mock.detail.requestRecord.formatOptions.beautify'),
      value: 'pretty'
    },
    {
      name: t('mock.detail.requestRecord.formatOptions.raw'),
      value: 'raw'
    },
    {
      name: t('actions.preview'),
      value: 'preview'
    }
  ];

  /**
   * Color mapping for HTTP method labels.
   */
  const httpMethodColors: Record<HttpMethod, string> = {
    GET: 'text-http-get',
    POST: 'text-http-post',
    DELETE: 'text-http-delete',
    PATCH: 'text-http-patch',
    PUT: 'text-http-put',
    OPTIONS: 'text-http-options',
    HEAD: 'text-http-head',
    TRACE: 'text-http-trace'
  };

  return {
    gridColumns,
    formatTabs,
    httpMethodColors
  };
}
