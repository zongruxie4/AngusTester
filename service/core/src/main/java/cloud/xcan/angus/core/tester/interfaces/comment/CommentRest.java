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

@Tag(name = "Comment Management", description = "Resource Comment Management - APIs for managing and querying comments and reviews on AngusTester resources including test cases, tasks, scripts, and scenarios with hierarchical comment tree support")
@Validated
@RestController
@RequestMapping("/api/v1/comment")
public class CommentRest {

  @Resource
  private CommentFacade commentFacade;

  @Operation(summary = "Add comment to resource", 
      description = "Create a new comment on a specific resource with hierarchical support for threaded discussions",
      operationId = "comment:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Comment created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<AngusCommentDetailVo> add(@Valid @RequestBody AngusCommentAddDto dto) {
    return ApiLocaleResult.success(commentFacade.add(dto));
  }

  @Operation(summary = "Delete comment", 
      description = "Remove a specific comment from the system with proper validation and cleanup",
      operationId = "comment:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Comment deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Valid @PathVariable("id") @Min(1) @Parameter(name = "id", description = "Comment identifier for deletion", required = true) Long id) {
    commentFacade.delete(id);
  }

  @Operation(summary = "Query comment tree", 
      description = "Retrieve hierarchical comment tree for a specific resource with threaded discussion support",
      operationId = "comment:tree")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Comment tree retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<List<AngusCommentTreeVo>> tree(@Valid @ParameterObject AngusCommentFindDto dto) {
    return ApiLocaleResult.success(commentFacade.tree(dto));
  }
}
