package com.dylan.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dylan.util.HttpUtils;
import com.dylan.util.UrlUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QAService {
    private static final String AMBIGUOUS_URL = "https://api.ownthink.com/kg/ambiguous?mention={{entityEncoded}}";
    private static final String ENTITY_KNOWLEDGE_URL = "https://api.ownthink.com/kg/knowledge?entity={{entityEncoded}}";
    public static final String  ENTITY_ATTRIBUTE_URL = "https://api.ownthink.com/kg/eav?entity={{entityEncoded}}&attribute={{attributeEncoded}}";
    /**
     * 获取实体的每个歧义
     * @param entity
     * @return
     */
    public List<String> getAmbiguousList(String entity) {
        if (StringUtils.isBlank(entity)) {
            return null;
        }
        String entityEncoded = UrlUtils.urlEncode(entity);
        String url = AMBIGUOUS_URL.replace("{{entityEncoded}}", entityEncoded);
        String result = HttpUtils.doGet(url);
        JSONObject ambiguousObject = JSON.parseObject(result);
        if (!"success".equals(ambiguousObject.getString("message"))) {
            return null;
        }
        JSONArray ambiguousArray = JSON.parseArray(ambiguousObject.getString("data"));
        List<String> ambiguousList = new ArrayList<>();
        for (Object obj : ambiguousArray) {
            JSONArray ambiguous = (JSONArray) obj;
            ambiguousList.add(ambiguous.getString(0));
        }
        return ambiguousList;
    }

    /**
     * 获取每个歧义实体的所有属性
     * @param ambiguousEntity
     * @return
     */
    public Set<String> getEntityAttributeSet(String ambiguousEntity) {
        if (StringUtils.isBlank(ambiguousEntity)) {
            return null;
        }
        Set<String> attributeSet = new HashSet<>();
        String entityEncoded = UrlUtils.urlEncode(ambiguousEntity);
        String url = ENTITY_KNOWLEDGE_URL.replace("{{entityEncoded}}", entityEncoded);
        String result = HttpUtils.doGet(url);
        JSONObject entityKnowledgeObject = JSON.parseObject(result);
        if (!"success".equals(entityKnowledgeObject.getString("message"))) {
            return null;
        }
        JSONObject dataObject = entityKnowledgeObject.getJSONObject("data");
        if (Objects.isNull(dataObject)) {
            return null;
        }
        JSONArray attributeArray = dataObject.getJSONArray("avp");
        if (Objects.isNull(attributeArray)) {
            return null;
        }
        for (Object obj : attributeArray) {
            JSONArray attribute = (JSONArray) obj;
            attributeSet.add(attribute.getString(0));
        }
        return attributeSet;
    }

    public List<String> getEntityAttributeValueList(String entity, String attribute) {
        if (StringUtils.isBlank(entity) || StringUtils.isBlank(attribute)) {
            return null;
        }
        List<String> valueList = new ArrayList<>();
        String entityEncoded = UrlUtils.urlEncode(entity);
        String attributeEncoded = UrlUtils.urlEncode(attribute);
        String url = ENTITY_ATTRIBUTE_URL.replace("{{entityEncoded}}", entityEncoded)
            .replace("{{attributeEncoded}}", attributeEncoded);
        String result = HttpUtils.doGet(url);
        JSONObject entityAttributeValueObject = JSON.parseObject(result);
        if (!"success".equals(entityAttributeValueObject.getString("message"))) {
            return null;
        }
        JSONObject dataObject = entityAttributeValueObject.getJSONObject("data");
        if (Objects.isNull(dataObject)) {
            return null;
        }
        JSONArray valueArray = dataObject.getJSONArray("value");
        if (Objects.isNull(valueArray)) {
            return null;
        }
        valueList = JSONObject.parseArray(valueArray.toJSONString(), String.class);
        return valueList;
    }

    @Test
    public void testGetAmbiguousList() {
        String entity = "草莓";
        List<String> ambiguousList = getAmbiguousList(entity);
        System.out.println(ambiguousList.toString());
    }

    @Test
    public void testGetEntityAttributeSet() {
        String entity = "刘德华";
        Set<String> attributeSet = getEntityAttributeSet(entity);
        System.out.println(attributeSet.toString());
    }

    @Test
    public void testGetEntityAttributeValueList() {
        String entity = "刘德华";
        String attribute = "英文名";
        List<String> valueList = getEntityAttributeValueList(entity, attribute);
        System.out.println(valueList.toString());
    }
}
