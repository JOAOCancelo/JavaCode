package com.heima.schedule.feign;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class ScheduleClient implements IScheduleClient {

    @Autowired
    private TaskService taskService;

    //新增
    @Override
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task){
        return ResponseResult.okResult(taskService.addTask(task));
    };

    //取消
    @GetMapping("/api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable long taskId){
        return ResponseResult.okResult(taskService.cancelTask(taskId));
    }

    //按照类型和优先级拉取任务
    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type") int type, @PathVariable("priority") int priority) {
        return ResponseResult.okResult(taskService.poll(type,priority));
    }
}
