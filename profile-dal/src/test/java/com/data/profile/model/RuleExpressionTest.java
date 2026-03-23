package com.data.profile.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Collections;

/**
 * 功能：示例
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/22 20:17
 */
public class RuleExpressionTest {

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static void main(String[] args) {
        // 规则组1 ((性别标签=女) AND (VIP用户群组=是) AND (用户启动总次数>=1) AND (事件属性操作系统=iOS)) OR
        // 规则组2 ((性别标签=男) AND (VIP用户群组=是) AND (用户启动总次数>=2) AND (事件属性操作系统=Android)) OR
        // 规则组3 依次启动：(应用启动事件 AND 事件属性操作系统=Android),(应用启动事件 AND 事件属性操作系统=iOS)


        // 时间周期：昨天
        RuleTimePeriod timePeriod = RuleTimePeriod.builder().type(2).unit("day").amount(2).beginTimestamp(1774022400000L).endTimestamp(1774022400000L).build();

        // 1. 规则组1
        // 1.1 规则1 性别=女
        RuleFilter ruleFilter11 = RuleFilter.builder().type(1).id("L1").name("性别").op("=").values(Collections.singletonList("女")).build();
        RuleFilterGroup ruleFilterGroup11 = RuleFilterGroup.builder().ruleFilters(Collections.singletonList(ruleFilter11)).build();
        RuleFilterExpression ruleFilterExpression11 = RuleFilterExpression.builder().logic("AND").ruleFilterGroups(Arrays.asList(ruleFilterGroup11)).build();
        Rule rule11 = Rule.builder().type("1").ruleFilterExpression(ruleFilterExpression11).build();
        System.out.println(gson.toJson(rule11));

        // 1.2 规则2 VIP用户群组=是
        RuleFilter ruleFilter12 = RuleFilter.builder().type(2).id("G1").name("VIP用户群组").op("=").values(Collections.singletonList("是")).build();
        RuleFilterGroup ruleFilterGroup12 = RuleFilterGroup.builder().ruleFilters(Collections.singletonList(ruleFilter12)).build();
        RuleFilterExpression ruleFilterExpression12 = RuleFilterExpression.builder().logic("AND").ruleFilterGroups(Arrays.asList(ruleFilterGroup12)).build();
        Rule rule12 = Rule.builder().type("2").ruleFilterExpression(ruleFilterExpression12).build();
        System.out.println(gson.toJson(rule12));

        // 1.3 规则3 (用户启动总次数>=1) AND (事件属性操作系统=iOS)
        // 1.3.1 过滤条件
        RuleFilter ruleFilter13 = RuleFilter.builder().type(3).id("A1").name("操作系统").op("=").values(Collections.singletonList("iOS")).build();
        RuleFilterGroup ruleFilterGroup13 = RuleFilterGroup.builder().ruleFilters(Collections.singletonList(ruleFilter13)).build();
        RuleFilterExpression ruleFilterExpression13 = RuleFilterExpression.builder().logic("AND").ruleFilterGroups(Arrays.asList(ruleFilterGroup13)).build();
        // 1.3.2 指标
        RuleMeasure ruleMeasure13 = RuleMeasure.builder().op(">=").values(Arrays.asList("1")).type("count").name("总次数").build();
        Event event13 = Event.builder().eventId("E121212121").eventName("用户启动").build();
        RuleEvent ruleEvent13 = RuleEvent.builder().event(event13).measure(ruleMeasure13).period(timePeriod).build();
        Rule rule13 = Rule.builder().type("3").ruleFilterExpression(ruleFilterExpression13).ruleEvent(ruleEvent13).build();
        System.out.println(gson.toJson(rule13));

        RuleGroup ruleGroup1 = RuleGroup.builder().logic("AND").rules(Arrays.asList(rule11, rule12, rule13)).build();

        // 2. 规则组2
        // 2.1 规则1 性别=男
        RuleFilter ruleFilter21 = RuleFilter.builder().type(1).id("L1").name("性别").op("=").values(Collections.singletonList("男")).build();
        RuleFilterGroup ruleFilterGroup21 = RuleFilterGroup.builder().ruleFilters(Collections.singletonList(ruleFilter21)).build();
        RuleFilterExpression ruleFilterExpression21 = RuleFilterExpression.builder().logic("AND").ruleFilterGroups(Arrays.asList(ruleFilterGroup21)).build();
        Rule rule21 = Rule.builder().type("1").ruleFilterExpression(ruleFilterExpression21).build();
        System.out.println(gson.toJson(rule21));

        // 2.2 规则2 VIP用户群组=是
        RuleFilter ruleFilter22 = RuleFilter.builder().type(2).id("G1").name("VIP用户群组").op("=").values(Collections.singletonList("是")).build();
        RuleFilterGroup ruleFilterGroup22 = RuleFilterGroup.builder().ruleFilters(Collections.singletonList(ruleFilter22)).build();
        RuleFilterExpression ruleFilterExpression22 = RuleFilterExpression.builder().logic("AND").ruleFilterGroups(Arrays.asList(ruleFilterGroup22)).build();
        Rule rule22 = Rule.builder().type("2").ruleFilterExpression(ruleFilterExpression22).build();
        System.out.println(gson.toJson(rule22));

        // 2.3 规则3 (用户启动总次数>=2) AND (事件属性操作系统=Android)
        // 2.3.1 过滤条件
        RuleFilter ruleFilter23 = RuleFilter.builder().type(3).id("A1").name("操作系统").op("=").values(Collections.singletonList("Android")).build();
        RuleFilterGroup ruleFilterGroup23 = RuleFilterGroup.builder().ruleFilters(Collections.singletonList(ruleFilter23)).build();
        RuleFilterExpression ruleFilterExpression23 = RuleFilterExpression.builder().logic("AND").ruleFilterGroups(Arrays.asList(ruleFilterGroup23)).build();
        // 2.3.2 指标
        RuleMeasure ruleMeasure23 = RuleMeasure.builder().op(">=").values(Arrays.asList("2")).type("count").name("总次数").build();
        Event event23 = Event.builder().eventId("E121212121").eventName("用户启动").build();
        RuleEvent ruleEvent23 = RuleEvent.builder().event(event23).measure(ruleMeasure23).period(timePeriod).build();
        Rule rule23 = Rule.builder().type("3").ruleFilterExpression(ruleFilterExpression23).ruleEvent(ruleEvent23).build();
        System.out.println(gson.toJson(rule23));

        RuleGroup ruleGroup2 = RuleGroup.builder().rules(Arrays.asList(rule21, rule22, rule23)).build();

        // 3. 规则组3
        // 依次启动：(应用启动事件 AND 事件属性操作系统=Android),(应用启动事件 AND 事件属性操作系统=iOS)
        // 规则1-事件1
        // 3.1.1.1 过滤条件
        RuleFilter ruleFilter31 = RuleFilter.builder().type(3).id("A1").name("操作系统").op("=").values(Collections.singletonList("Android")).build();
        RuleFilterGroup ruleFilterGroup31 = RuleFilterGroup.builder().ruleFilters(Collections.singletonList(ruleFilter31)).build();
        RuleFilterExpression ruleFilterExpression31 = RuleFilterExpression.builder().logic("AND").ruleFilterGroups(Arrays.asList(ruleFilterGroup31)).build();
        // 3.1.1.2 事件
        Event event31 = Event.builder().eventId("E1212121232").eventName("应用启动").build();
        RuleEvent ruleEvent31 = RuleEvent.builder().event(event31).period(timePeriod).build();
        RuleSequence ruleSequence31 = RuleSequence.builder().ruleEvent(ruleEvent31).ruleFilterExpression(ruleFilterExpression31).build();

        // 规则1-事件2
        // 3.1.2.1 过滤条件
        RuleFilter ruleFilter32 = RuleFilter.builder().type(3).id("A1").name("操作系统").op("=").values(Collections.singletonList("iOS")).build();
        RuleFilterGroup ruleFilterGroup32 = RuleFilterGroup.builder().ruleFilters(Collections.singletonList(ruleFilter32)).build();
        RuleFilterExpression ruleFilterExpression32 = RuleFilterExpression.builder().logic("AND").ruleFilterGroups(Arrays.asList(ruleFilterGroup32)).build();
        // 3.1.2.2 事件
        Event event32 = Event.builder().eventId("E1212121232").eventName("应用启动").build();
        RuleEvent ruleEvent32 = RuleEvent.builder().event(event32).period(timePeriod).build();
        RuleSequence ruleSequence32 = RuleSequence.builder().ruleEvent(ruleEvent32).ruleFilterExpression(ruleFilterExpression32).build();

        Rule rule31 = Rule.builder().type("4").ruleSequences(Arrays.asList(ruleSequence31, ruleSequence32)).build();
        System.out.println(gson.toJson(rule31));

        RuleGroup ruleGroup3 = RuleGroup.builder().rules(Arrays.asList(rule31)).build();

        // 规则表达式
        RuleExpression ruleExpression = RuleExpression.builder().logic("OR").ruleGroups(Arrays.asList(ruleGroup1, ruleGroup2, ruleGroup3)).build();
        System.out.println(gson.toJson(ruleExpression));



        String json = "{\"logic\":\"AND\",\"rule_groups\":[{\"logic\":\"AND\",\"rules\":[{\"type\":\"1\",\"filter_expression\":{\"logic\":\"AND\",\"filter_groups\":[{\"logic\":\"AND\",\"filters\":[{\"type\":1,\"id\":\"0821067752317952\",\"name\":\"性别\",\"op\":\"=\",\"values\":[\"vv\"]}]}]}}]}]}";
        RuleExpression ruleExpression1 = gson.fromJson(json, RuleExpression.class);
        System.out.println("结果：" + gson.toJson(ruleExpression1));
    }
}
