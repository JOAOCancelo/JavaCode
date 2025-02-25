package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service

public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Autowired
    private IScheduleClient scheduleClient;
    /**
     * 添加任务队列到延迟队列中
     * @param id 文字id
     * @param publishTime 发布的时间 可以做为任务的执行时间
     */
    @Override
    @Async  //异步调用
    public void addNewsToTask(Integer id, Date publishTime) {


        //添加任务到延迟服务中
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));

        scheduleClient.addTask(task);
    }


    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    /**
     * 消费任务，审核文章
     */
    @Scheduled(fixedRate = 1000) //每一秒钟去拉取一次任务
    @Override
    public void scanNewsByTask() {
        ResponseResult responseResult = scheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if (responseResult.getCode() == 200 && responseResult.getData() != null) {
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()), Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }
    }
}
