package cloud.xcan.sdf.extension.angustester.api;

/**
 * ATS001 ~ ATS999
 */
public interface TesterExtensionMessage {

  /*<******************Services#Config(ATS395 ~ ATS399)******************>*/
  String SERVICE_DOC_FORMAT_ERROR = "xcm.altester.service.doc.format.error";

  String SERVICE_DOC_PARSE_ERROR_T = "xcm.altester.service.doc.parse.error.t";
  String SERVICE_DOC_VERSION_NOT_SUPPORTED_T = "xcm.altester.service.doc.version.not.supported.t";
  
}
