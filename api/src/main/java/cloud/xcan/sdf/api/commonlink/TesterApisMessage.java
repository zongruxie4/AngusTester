package cloud.xcan.sdf.api.commonlink;

/**
 * ATS001 ~ ATS999
 */
public interface TesterApisMessage {

  /*<******************Common(ATS001 ~ ATS029)******************>*/
  String FORBID_AUTH_CREATOR_CODE = "ATS001";
  String FORBID_AUTH_CREATOR = "xcm.altester.forbid.auth.creator";
  String NO_ADMIN_PERMISSION_CODE = "ATS002";
  String NO_ADMIN_PERMISSION = "xcm.altester.no.admin.permission";
  String NO_HANDLER_PERMISSION_CODE = "ATS003";
  String NO_HANDLER_PERMISSION = "xcm.altester.no.handler.permission";

  /*<******************Trash(ATS050 ~ ATS069)******************>*/
  String TRASH_NO_BACK_PERMISSION_CODE = "ATS050";
  String TRASH_NO_BACK_PERMISSION = "xcm.altester.trash.no.back.permission";
  String TRASH_NO_CLEAR_PERMISSION_CODE = "ATS051";
  String TRASH_NO_CLEAR_PERMISSION = "xcm.altester.trash.no.clear.permission";
  String TRASH_NO_VIEW_PERMISSION_CODE = "ATS052";
  String TRASH_NO_VIEW_PERMISSION = "xcm.altester.trash.no.view.permission";

  /*<******************Comment(ATS930 ~ ATS929)******************>*/
  String COMMENT_TARGET_NUM_OVER_LIMIT_CODE = "ATS001";
  String COMMENT_TARGET_NUM_OVER_LIMIT_T = "xcm.altester.comment.target.over.limit.t";
  String COMMENT_NO_DELETE_PERMISSION_CODE = "ATS002";
  String COMMENT_NO_DELETE_PERMISSION = "xcm.altester.comment.no.delete.permission";

  /*<******************Script#Auth(ATS520 ~ ATS524)******************>*/
  String SCRIPT_NO_AUTH_CODE = "ATS520";
  String SCRIPT_NO_AUTH_T = "xcm.altester.script.no.auth.t";
  String SCRIPT_NO_TARGET_AUTH_CODE = "ATS521";
  String SCRIPT_NO_TARGET_AUTH = "xcm.altester.script.no.target.auth.t";

  String EVENT_SUBJECT = "xcm.altester.event.subject";
}
