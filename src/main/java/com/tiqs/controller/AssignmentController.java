package com.tiqs.controller;

import com.tiqs.entity.Assignment;
import com.tiqs.service.AssignmentService;
import com.tiqs.common.ApiResponse; // updated import
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;
    public AssignmentController(AssignmentService assignmentService){this.assignmentService=assignmentService;}

    @PostMapping
    public ApiResponse<Assignment> create(@RequestBody Assignment a){log.info("请求创建作业 courseId={} title={}", a.getCourseId(),a.getTitle()); return ApiResponse.ok(assignmentService.create(a));}
    @GetMapping("/{id}")
    public ApiResponse<Assignment> get(@PathVariable Long id){log.debug("获取作业详情 id={}", id); return ApiResponse.ok(assignmentService.get(id));}
    @GetMapping
    public ApiResponse<List<Assignment>> listByCourse(@RequestParam Long courseId){log.debug("查询课程下作业列表 courseId={}", courseId); return ApiResponse.ok(assignmentService.listByCourse(courseId));}
    @PutMapping("/{id}")
    public ApiResponse<Assignment> update(@PathVariable Long id,@RequestBody Assignment a){a.setId(id); log.info("更新作业 id={}", id); return ApiResponse.ok(assignmentService.update(a));}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id){log.warn("删除作业 id={}", id); assignmentService.delete(id); return ApiResponse.ok(null);}
}
