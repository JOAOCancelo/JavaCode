package com.heima.wemedia.baidu;

import com.baidu.aip.contentcensor.AipContentCensor;
import com.baidu.aip.contentcensor.EImgType;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 自定义的百度审核
 */
@Component
public class MyBaiduCensor {

    @Autowired
    private AipContentCensor aipContentCensor;

    /**
     * 自定义审核纯文本内容
     * @param text
     * @return
     */
    public String scanText(String text){

        JSONObject json = aipContentCensor.textCensorUserDefined(text);

        return (String) json.get("conclusion");
    }

    /**
     * 审核图片
     * 根据图片的在线url
     */
    public String scanImage(String url){

        JSONObject result = aipContentCensor.imageCensorUserDefined(url, EImgType.URL, null);

        return (String) result.get("conclusion");

    }

    /**
     * 字节数组审核图片
     * @param image
     * @return
     */
    public String scanImage(byte[] image){
        JSONObject jsonObject = aipContentCensor.imageCensorUserDefined(image, null);
        return (String) jsonObject.get("conclusion");
    }

    /**
     * 审核本地文件
     * @param imagePath 传入文件路径
     * @return
     */
    public String scanImageFile(String imagePath){

        JSONObject jsonObject = aipContentCensor.imageCensorUserDefined(imagePath, EImgType.FILE, null);
        return (String) jsonObject.get("conclusion");
    }

    /**
     * 批量审核图片数组
     * @param imgList
     * @return
     */
    public String scanImageList(List<byte[]> imgList){
        //做一个结果集
        List<String> resultList = new ArrayList<>();

        for (byte[] bytes : imgList) {
            String res = scanImage(bytes);
            if(res.equals("不合规")){
                //只要有一张是不合规的，就返回不合规
                return "不合规";
            }
            //就是疑似，合规，失败
            //添加到结果集中
            resultList.add(res);
        }
        //判断结果集合
        //如果有 疑似，则返回
        if(resultList.contains("疑似")||resultList.contains("审核失败")){
            return "疑似";
        }
        //都没有，则
        return "合规";
    }

}