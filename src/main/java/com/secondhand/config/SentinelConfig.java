package com.secondhand.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 流量防卫兵规则：对商品检索、核心支付接口设置限流与熔断降级。
 * - 商品列表/详情：流控 QPS 限流，保护 MySQL 与下游缓存
 * - 下单/支付：异常比例熔断，依赖（Redis/DB）抖动时自动降级，避免雪崩
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        log.info("Sentinel 流控/熔断规则初始化完成");
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        // 商品检索限流：单机 QPS 200
        rules.add(flowRule("goodsResource:list", 200));
        rules.add(flowRule("goodsResource:detail", 300));
        // 核心支付/下单限流：单机 QPS 100
        rules.add(flowRule("orderResource:create", 100));
        rules.add(flowRule("orderResource:pay", 100));
        FlowRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        // 商品详情：异常比例 > 50% 且 1 分钟内触发，熔断 10s
        DegradeRule detailRule = new DegradeRule();
        detailRule.setResource("goodsResource:detail");
        detailRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        detailRule.setCount(0.5);
        detailRule.setTimeWindow(10);
        detailRule.setMinRequestAmount(20);
        detailRule.setStatIntervalMs(60_000);
        rules.add(detailRule);

        // 支付接口：慢调用比例熔断（RT > 500ms 占比超 50% 熔断 10s）
        DegradeRule payRule = new DegradeRule();
        payRule.setResource("orderResource:pay");
        payRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        payRule.setCount(500);
        payRule.setTimeWindow(10);
        payRule.setMinRequestAmount(10);
        payRule.setStatIntervalMs(60_000);
        rules.add(payRule);

        DegradeRuleManager.loadRules(rules);
    }

    private FlowRule flowRule(String resource, int count) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(count);
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        return rule;
    }
}
