package com.dylan.controller;


import com.dylan.service.NerService;
import com.dylan.service.QaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Controller
public class QAController {

    @Autowired
    private NerService nerService;

    @Autowired
    private QaService qaService;

    @RequestMapping(value = "/qa", method = RequestMethod.GET)
    @ResponseBody
    public String getAnswer(@RequestParam("question") String question) {
        List<String> entityList = nerService.getNameEntityList(question);
        if (Objects.isNull(entityList) || entityList.isEmpty()) {
            return "问句中没有实体！";
        }
        System.out.println(entityList.toString());
        if (entityList.size() > 1) {
            return "问句中的实体数不止一个，目前暂不支持多实体问题。";
        }
        String entity = entityList.get(0);
        List<String> ambiguousList = qaService.getAmbiguousList(entity);
        if (Objects.isNull(ambiguousList)) {
            return "抱歉！知识库中不存在该实体。";
        }
        System.out.println(ambiguousList.toString());
        for (String ambiguousEntity : ambiguousList) {
            Set<String> attributeSet = qaService.getEntityAttributeSet(ambiguousEntity);
            if (Objects.isNull(attributeSet)) {
                continue;
            }
            System.out.println(ambiguousEntity + ": " + attributeSet.toString());
            // 对于每个歧义实体的属性，如果与问题中的属性匹配，则直接返回属性值
            for (String attribute : attributeSet) {
                if (question.contains(attribute)) {
                    List<String> valueList = qaService.getEntityAttributeValueList(ambiguousEntity, attribute);
                    // 属性值太多了，就只显示前10个
                    if (valueList.size() > 10) {
                        valueList = valueList.subList(0, 9);
                    }
                    return ambiguousEntity + "的" + attribute + "是" + valueList.toString();
                }
            }
        }
        return "很抱歉！目前知识库中不存在该问题的答案。";
    }
}
