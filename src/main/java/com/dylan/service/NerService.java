package com.dylan.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dylan.util.HttpUtils;
import com.dylan.util.UrlUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NerService {

    //private static final String NER_URL = "http://139.224.114.96:8088/predict?sentence={{sentenceEncoded}}";
    private static final String NER_URL = "http://localhost:8090/predict?sentence={{sentenceEncoded}}";

    /**
     * 获取句子中的所有命名实体
     * @param sentence
     * @return
     */
    public List<String> getNameEntityList(String sentence) {
        String sentenceEncoded = UrlUtils.urlEncode(sentence);
        String url = NER_URL.replace("{{sentenceEncoded}}", sentenceEncoded);
        String nerJsonStr = HttpUtils.doGet(url);
        List<String> entityList = new ArrayList<>();
        if (StringUtils.isBlank(nerJsonStr)) {
            return null;
        }
        JSONObject nerJsonObject = JSON.parseObject(nerJsonStr);
        JSONArray entityArray = JSON.parseArray(nerJsonObject.getString("entities"));
        if (Objects.isNull(entityArray)) {
            return null;
        }
        for (Object obj : entityArray) {
            JSONObject entity = (JSONObject) obj;
            entityList.add(entity.getString("word"));
        }
        return entityList;
    }



    @Test
    public void testGetNameEntityList() {
        String sentence = "严徐迪就读于华理";
        String sentencenEncoded = UrlUtils.urlEncode(sentence);
        String url = "http://139.224.114.96:8088/predict?sentence=" + sentencenEncoded;
        System.out.println(url);
        String doGetResult = HttpUtils.doGet(url);
        System.out.println(doGetResult);
        List<String> entityList = getNameEntityList(doGetResult);
        System.out.println(entityList.toString());
    }
}
