package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.articles.IArticleClient;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.baidu.MyBaiduCensor;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private IArticleClient iArticleClient;

    /**
     * 自媒体文章审核
     * @param id
     */
    @Override
    @Async  //表面当前方法是异步方法
    public void autoScanWmNews(Integer id) {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //1.查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }

        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            //从内容中提取纯文本内容和图片
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);

            //自管理的敏感词过滤
            boolean isSensitive = handleSensitive((String) textAndImages.get("content"),wmNews);
            if(isSensitive) return;
            //2.审核文章内容 百度云接口
            boolean isTextScan = handleTextScan((String) textAndImages.get("content"),wmNews);
            if(isTextScan)return;
            //3.审核图片 百度云接口
            boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"),wmNews);
            if(isImageScan)return;
            //4.审核成功 保存app端的相关文章数据
            ResponseResult responseResult = saveAppArticle(wmNews);
            if(responseResult.getCode() !=200) {
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }
            //回填articleid
            wmNews.setArticleId((long)responseResult.getData());
            updateWmNews(wmNews,(short)9,"审核成功");

        }
    }

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;
    /**
     * 自管理的敏感词审核
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitive(String content, WmNews wmNews) {
        boolean flag = true;
        //获取所有的敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);

        //查看文章中是否包含敏感词
        Map<String,Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size()>0) {
            updateWmNews(wmNews,(short) 2,"当前文章中存在违规内容"+map);
            flag = false;
        }
        return flag;
    }

    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private IArticleClient articleClient;
    @Autowired
    private Tess4jClient tess4jClient;
    /**
     * 保存app端相关的文章数据
     * @param wmNews
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        //属性拷贝
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,dto);
        //文章布局
        dto.setLayout(wmNews.getType());
        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmNews != null){
            dto.setChannelId(wmNews.getChannelId());
        }
        //作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser != null){
            dto.setAuthorName(wmUser.getName());
        }

        //设置文章id
        if(wmNews.getArticleId() != null){
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());

        ResponseResult responseResult = articleClient.saveArticle(dto);
        return responseResult;
    }


    /**
     * 审核图片
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImageScan(List<String> images, WmNews wmNews) {

        boolean flag = true;
        if (images == null || images.size() == 0) {
            return flag;
        }
        //下周图片minio
        //图片去重
        images = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> imageList = new ArrayList<>();

        try {
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);

                //byte[] 转化为bufferedImage
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(in);
                //图片识别
                String result = tess4jClient.doOCR(bufferedImage);
                //过滤文字
                boolean isSensitive = handleSensitive(result, wmNews);
                if(!isSensitive) {
                    return isSensitive;
                }
                imageList.add(bytes);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        //审核图片

        String res = myBaiduCensor.scanImageList(imageList);
        try {


        if (res != null) {
            //审核失败
            if (res.equals("不合规")) {
                flag = false;
                updateWmNews(wmNews, (short) 2, "当前文章中存在违规内容");
            }
            //不确定 需要人工审核
            if (res.equals("疑似")) {
                flag = false;
                updateWmNews(wmNews, (short) 3, "当前图片不确定");
            }
        }
    }catch (Exception e){
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    @Autowired
    private MyBaiduCensor myBaiduCensor;

    @Autowired
    private WmChannelMapper wmChannelMapper;
    /**
     * 审核纯文本内容
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {

        boolean flag = true;

        if((wmNews.getTitle()+""+content).length() == 0){
            return flag;
        }
        try{
        String res = myBaiduCensor.scanText(wmNews.getTitle() + "-" + content);
        if (res != null) {
            //审核失败
            if(res.equals("不合规")){
            flag = false;
                updateWmNews(wmNews, (short) 2,"当前文章中存在违规内容");
        }
            //不确定信息 需要人工审核
            if(res.equals("疑似")){
                flag = false;
                updateWmNews(wmNews, (short) 3,"当前文章存在不确定内容");
            }

            }}catch (Exception e){
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改文章内容
     * @param wmNews
     * @param status
     * @param reason
     */
        private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
        }

    /**
     * 1.从自媒体文章的内容中提取文本和图片
     * 2.提取文章的封面图片
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {

        //存储纯文本内容
    StringBuilder stringBuilder = new StringBuilder();
    //存储图片内容
        List<String> images = new ArrayList<>();

    //1.从自媒体文章的内容中提取文本和图片
        if(StringUtils.isBlank(wmNews.getContent())){
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if(map.get("type").equals("text")){
                stringBuilder.append(map.get("value"));
                }

                if(map.get("type").equals("image")){
                images.add((String) map.get("value"));
                }
            }
        }
        //2.提取文章的封面图片
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        Map<String ,Object> map = new HashMap<>();
        map.put("content",stringBuilder.toString());
        map.put("images",images);
        return map;
    }

}


