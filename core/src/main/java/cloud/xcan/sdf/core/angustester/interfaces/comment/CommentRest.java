package cloud.xcan.sdf.core.angustester.interfaces.comment;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.comment.facade.CommentFacade;
import cloud.xcan.sdf.core.angustester.interfaces.comment.facade.dto.AngusCommentAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.comment.facade.dto.AngusCommentFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.comment.facade.vo.AngusCommentDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.comment.facade.vo.AngusCommentTreeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Comment")
@Validated
@RestController
@RequestMapping("/api/v1/comment")
public class CommentRest {

  @Resource
  private CommentFacade commentFacade;

  @ApiOperation(value = "Add comment", nickname = "comment:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<AngusCommentDetailVo> add(@Valid @RequestBody AngusCommentAddDto dto) {
    return ApiLocaleResult.success(commentFacade.add(dto));
  }

  @ApiOperation(value = "Delete comments", nickname = "comment:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Valid @PathVariable("id") @Min(1) @ApiParam(name = "id", value = "Comment id", required = true) Long id) {
    commentFacade.delete(id);
  }

  @ApiOperation(value = "Query the tree of comment", nickname = "comment:tree")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<List<AngusCommentTreeVo>> tree(@Valid AngusCommentFindDto dto) {
    return ApiLocaleResult.success(commentFacade.tree(dto));
  }
}
