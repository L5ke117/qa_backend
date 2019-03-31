package com.dylan.controller;


import com.dylan.enums.QuestionCategory;
import com.dylan.service.NerService;
import com.dylan.service.QaService;
import com.dylan.util.JsonUtils;
import com.dylan.util.QuestionUtils;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringUtils;
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
            return JsonUtils.buildJsonStr(1, "问句中没有实体！");
        }
        System.out.println(entityList.toString());
        if (entityList.size() > 2) {
            return JsonUtils.buildJsonStr(1, "目前暂不支持多实体问答。");
        }
        String entity = entityList.get(0);
        if (entityList.size() == 2) {
            for (String entityItem : entityList) {
                // 判断实体的位置是不是在“的”前面
                int index = StringUtils.indexOf(question, entityItem) + entityItem.length();
                if (index == StringUtils.indexOf(question, "的")) {
                    entity = entityItem;
                    break;
                }
            }
        }
        String questionCategory = QuestionUtils.getQuestionCategory(question);
        if (questionCategory.equals(QuestionCategory.ENTITY.value())) {
            String desc = qaService.getEntityDesc(entity);
            if (StringUtils.isNotBlank(desc)) {
                return JsonUtils.buildJsonStr(0, desc);
            } else {
                return JsonUtils.buildJsonStr(1, "抱歉！知识库中不存在该实体。");
            }
        }
        List<String> ambiguousList = qaService.getAmbiguousList(entity);
        if (Objects.isNull(ambiguousList)) {
            return JsonUtils.buildJsonStr(1, "抱歉！知识库中不存在该实体。");
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
                    if (Objects.isNull(valueList) || valueList.isEmpty()) {
                        continue;
                    }
                    valueList = QuestionUtils.splitCombinedValue(valueList);
                    if (questionCategory.equals(QuestionCategory.FACT.value())) {
                        // 属性值太多了，就只显示前10个
                        if (valueList.size() > 10) {
                            valueList = valueList.subList(0, 9);
                        }
                        return JsonUtils.buildJsonStr(0, ambiguousEntity + "的" + attribute + "是" + valueList.toString() + "。");
                    }
                    if (questionCategory.equals(QuestionCategory.YES_NO.value())) {
                        for (String value : valueList) {
                            if (question.contains(value)) {
                                if (valueList.size() == 1) {
                                    return JsonUtils.buildJsonStr(0, "是的，" + ambiguousEntity + "的" + attribute + "是" + value + "。");
                                } else {
                                    return JsonUtils.buildJsonStr(0, "是的，" + ambiguousEntity + "的" + attribute + "有" + valueList.toString() + "，包括" + value + "。");
                                }
                            }
                        }
                        if (valueList.size() == 1) {
                            return JsonUtils.buildJsonStr(0, "不是的，" + ambiguousEntity + "的" +attribute + "是" + valueList.get(0) + "。");
                        } else {
                            return JsonUtils.buildJsonStr(0, "不是的，" + ambiguousEntity + "的" +attribute + "有" + valueList.toString() + "。");
                        }
                    }
                    if (questionCategory.equals(QuestionCategory.QUANTITY.value())) {
                        int count = valueList.size();
                        String quantifier = "个";
                        Result segResult = NlpAnalysis.parse(question);
                        List<Term> termList = segResult.getTerms();
                        for (Term term : termList) {
                            if (term.getNatureStr().equals("m")) {
                                quantifier = StringUtils.substring(term.getName(), term.getName().length() - 1);
                                break;
                            } else if (term.getNatureStr().equals("q")) {
                                quantifier = term.getName();
                                break;
                            }
                        }
                        return JsonUtils.buildJsonStr(0, ambiguousEntity + "有" + count + quantifier + attribute + "。");
                    }
                }
            }
        }
        return JsonUtils.buildJsonStr(1, "很抱歉！目前知识库中不存在该问题的答案。");
    }

    @RequestMapping(value = "/seg", method = RequestMethod.GET)
    @ResponseBody
    public String getSeg(@RequestParam("question") String question) {
        Result segResult = NlpAnalysis.parse(question);
        List<Term> termList = segResult.getTerms();
        System.out.println(segResult.toString());
        for (Term term : termList) {
            System.out.println(term.getName());
            System.out.println(term.getRealName());
            System.out.println(term.getNatureStr());
        }
        String questionCategory = QuestionUtils.getQuestionCategory(question);
        return JsonUtils.buildJsonStr(0, questionCategory);
    }
}
