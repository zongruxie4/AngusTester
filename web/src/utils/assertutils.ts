import uitlAssert from '@/components/apis/ApiAssert/utils/assert';

import uitlExpression from '@/components/apis/ApiAssert/utils/expression';
import extract from '@/components/apis/ApiAssert/utils/extract';
import uitlJsonpath from '@/components/apis/ApiAssert/utils/jsonpath';
import uitlProxy from '@/components/apis/ApiAssert/utils/proxy';
import uitlRegexp from '@/components/apis/ApiAssert/utils/regexp';
import uitlXpath from '@/components/apis/ApiAssert/utils/xpath';

export const assertUtil = {
  assert: uitlAssert,
  uitlExpression,
  extract,
  jsonpath: uitlJsonpath,
  proxy: uitlProxy,
  regexp: uitlRegexp,
  xpath: uitlXpath
};

export default {
  assert: uitlAssert,
  uitlExpression,
  extract,
  jsonpath: uitlJsonpath,
  proxy: uitlProxy,
  regexp: uitlRegexp,
  xpath: uitlXpath
};
