import { useI18n } from 'vue-i18n';
import beautify from 'js-beautify';

/**
 * Composable for managing match item popover logic
 * Provides data and formatting functions for match item examples
 */
export function useMatchItemPopover () {
  const { t } = useI18n();

  /**
   * Format XML data for better readability
   * @param data - XML string to format
   * @returns Formatted XML string
   */
  const prettyXml = (data: string) => {
    return beautify.html(data, {
      unformatted: ['code', 'pre', 'em', 'strong', 'span'],
      indent_inner_html: true,
      indent_char: ' ',
      indent_size: 2
    });
  };

  /**
   * Match item examples data
   * Contains examples for different extraction methods
   */
  const matchItemList = [
    {
      key: 'regexp',
      name: t('dataCommon.matchItemPopover.regexp.title'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('common.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataCommon.matchItemPopover.regexp.regexp')
        },
        {
          dataIndex: 'result',
          label: t('organization.group')
        },
        {
          dataIndex: 'item',
          label: t('common.item')
        }
      ]],
      data: {
        data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
        expression: '(1\\d{10})',
        result: '["18888888888","13999999999"]',
        item: t('dataCommon.matchItemPopover.regexp.item')
      }
    },
    {
      key: 'jsonpath',
      name: t('dataCommon.matchItemPopover.jsonpath.title'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('common.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataCommon.matchItemPopover.jsonpath.jsonpath')
        },
        {
          dataIndex: 'result',
          label: t('common.result')
        },
        {
          dataIndex: 'item',
          label: t('common.item')
        }
      ]],
      data: {
        data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
        expression: '$.store.book[*]',
        result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
        item: t('dataCommon.matchItemPopover.jsonpath.item')
      }
    },
    {
      key: 'xpath',
      name: t('dataCommon.matchItemPopover.xpath.title'),
      columns: [[
        {
          dataIndex: 'data',
          label: t('common.data')
        },
        {
          dataIndex: 'expression',
          label: t('dataCommon.matchItemPopover.xpath.xpath')
        },
        {
          dataIndex: 'result',
          label: t('common.result')
        },
        {
          dataIndex: 'item',
          label: t('common.item')
        }
      ]],
      data: {
        data: prettyXml('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
          '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
        expression: '/persons/person[age >= 30]',
        result: '["30codingbasketballAngus1", "32codingAngus2"]',
        item: t('dataCommon.matchItemPopover.xpath.item')
      }
    }
  ];

  return {
    matchItemList
  };
}
