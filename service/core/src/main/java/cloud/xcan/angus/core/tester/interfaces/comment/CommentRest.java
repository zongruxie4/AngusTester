package cloud.xcan.angus.core.tester.interfaces.comment;


import cloud.xcan.angus.core.tester.interfaces.comment.facade.CommentFacade;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentAddDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.dto.AngusCommentFindDto;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentDetailVo;
import cloud.xcan.angus.core.tester.interfaces.comment.facade.vo.AngusCommentTreeVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "Comment", description = "AngusTester Resource Comments - Unified entry point for managing and querying reviews/comments on AngusTester resources (e.g., test cases, tasks).")
@Validated
@RestController
@RequestMapping("/api/v1/comment")
public class CommentRest {

  @Resource
  private CommentFacade commentFacade;

  @Operation(description = "Add comment", operationId = "comment:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<AngusCommentDetailVo> add(@Valid @RequestBody AngusCommentAddDto dto) {
    return ApiLocaleResult.success(commentFacade.add(dto));
  }

  @Operation(description = "Delete comments", operationId = "comment:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Valid @PathVariable("id") @Min(1) @Parameter(name = "id", description = "Comment id", required = true) Long id) {
    commentFacade.delete(id);
  }

  @Operation(description = "Query the tree of comment", operationId = "comment:tree")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<List<AngusCommentTreeVo>> tree(@Valid @ParameterObject AngusCommentFindDto dto) {
    return ApiLocaleResult.success(commentFacade.tree(dto));
  }
}
