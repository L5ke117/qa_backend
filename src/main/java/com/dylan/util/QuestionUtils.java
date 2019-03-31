package com.dylan.util;

import com.dylan.enums.QuestionCategory;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class QuestionUtils {

    /**
     * 是非型问句的特征词语
     */
    private static final List<String> YES_NO_LIST = new ArrayList<>(Arrays.asList("是不是", "是吗", "对吗", "对不对", "不是吗",
            "不对吗", "有错吗", "有问题吗", "是这样吗", "有没有", "包不包括", "存不存在"));

    /**
     * 获取问句分类
     * @param question
     * @return
     */
    public static String getQuestionCategory(String question) {
        // 判断是否是非型问题
        for (String yesNoStr : YES_NO_LIST) {
            if (question.contains(yesNoStr)) {
                return QuestionCategory.YES_NO.value();
            }
        }
        String segResult = NlpAnalysis.parse(question).toString();
        // 判断是否数量型
        if (segResult.contains("/m") || segResult.contains("/q") || question.contains("有多少")) {
            return QuestionCategory.QUANTITY.value();
        }
        // 判断是否简单实体型
        Integer nounCount = QuestionUtils.getNounCount(segResult);
        if (nounCount.equals(1)) {
            return QuestionCategory.ENTITY.value();
        }
        // 判断是否简单事实型
        if (nounCount >= 2) {
            return QuestionCategory.FACT.value();
        }
        return QuestionCategory.RAW.value();
    }

    /**
     * 将“演员，歌手，填词人，制片人”这样的复合值拆分出来
     * @param valueList
     * @return
     */
    public static List<String> splitCombinedValue(List<String> valueList) {
        if (Objects.isNull(valueList) || valueList.isEmpty()) {
            return valueList;
        }
        List<String> resultList = new ArrayList<>(valueList);
        for (String valueItem : valueList) {
            if (valueItem.contains("等")) {
                resultList.remove(valueItem);
                valueItem = StringUtils.remove(valueItem, "等");
                resultList.add(valueItem);
            }
            if (valueItem.contains("，")) {
                String[] valueArray = StringUtils.split(valueItem, "，");
                resultList.addAll(Arrays.asList(valueArray));
                resultList.remove(valueItem);
            } else if (valueItem.contains("、")) {
                String[] valueArray = StringUtils.split(valueItem, "、");
                resultList.addAll(Arrays.asList(valueArray));
                resultList.remove(valueItem);
            }
        }
        return resultList;
    }

    /**
     * 获取句子中的名词个数
     * @param questionSeg
     * @return
     */
    public static Integer getNounCount(String questionSeg) {
        Integer count = 0;
        count += StringUtils.countMatches(questionSeg, "/n");
        count += StringUtils.countMatches(questionSeg, "/an");
        return count;
    }
}
