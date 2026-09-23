package com.secondhand.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelConfig {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @PostConstruct
    public void init() {
        if (System.getProperty("csp.sentinel.dashboard.server") == null) {
            System.setProperty("csp.sentinel.dashboard.server", "localhost:8858");
        }
        if (System.getProperty("csp.sentinel.api.port") == null) {
            System.setProperty("csp.sentinel.api.port", "8719");
        }
        InitExecutor.doInit();
        initFlowRules();
        initDegradeRules();
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule goodsListRule = new FlowRule("goodsList");
        goodsListRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        goodsListRule.setCount(50);
        goodsListRule.setLimitApp("default");
        rules.add(goodsListRule);

        FlowRule goodsDetailRule = new FlowRule("goodsDetail");
        goodsDetailRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        goodsDetailRule.setCount(100);
        goodsDetailRule.setLimitApp("default");
        rules.add(goodsDetailRule);

        FlowRule createOrderRule = new FlowRule("createOrder");
        createOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        createOrderRule.setCount(20);
        createOrderRule.setLimitApp("default");
        rules.add(createOrderRule);

        FlowRule payOrderRule = new FlowRule("payOrder");
        payOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        payOrderRule.setCount(20);
        payOrderRule.setLimitApp("default");
        rules.add(payOrderRule);

        FlowRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        DegradeRule createOrderDegrade = new DegradeRule("createOrder");
        createOrderDegrade.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        createOrderDegrade.setCount(0.5);
        createOrderDegrade.setTimeWindow(10);
        createOrderDegrade.setMinRequestAmount(5);
        createOrderDegrade.setStatIntervalMs(60000);
        rules.add(createOrderDegrade);

        DegradeRule payOrderDegrade = new DegradeRule("payOrder");
        payOrderDegrade.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        payOrderDegrade.setCount(0.5);
        payOrderDegrade.setTimeWindow(10);
        payOrderDegrade.setMinRequestAmount(5);
        payOrderDegrade.setStatIntervalMs(60000);
        rules.add(payOrderDegrade);

        DegradeRuleManager.loadRules(rules);
    }
}