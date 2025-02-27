package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import org.springframework.web.bind.annotation.RequestBody;

public interface WmNewsService extends IService<WmNews> {


    /**
     * 查询文章列表
     * @param dto
     * @return
     */
    public ResponseResult findList(WmNewsPageReqDto dto);

    /**
     * 文章发布
     * @param dto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto dto);


    /**
     * 文章上下架
     * @param dto
     * @return
     */
    public ResponseResult downOrUp(WmNewsDto dto);
}
