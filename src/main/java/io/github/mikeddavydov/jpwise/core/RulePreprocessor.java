package io.github.mikeddavydov.jpwise.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preprocesses test input by identifying and adding missing compatibility rules. Rules are
 * identified by analyzing existing rules and their interactions with other parameters. The
 * preprocessor returns a new TestInput instance with augmented rules.
 */
public class RulePreprocessor {
  private static final Logger logger = LoggerFactory.getLogger(RulePreprocessor.class);

  /**
   * Preprocesses the test input by identifying missing rules and returns a new TestInput instance
   * containing new TestParameter objects with augmented compatibility rules. The original TestInput
   * and its TestParameters are not modified.
   *
   * @param originalInput The test input to preprocess
   * @return A new TestInput instance with preprocessed rules.
   */
  public TestInput preprocess(TestInput originalInput) {
    logger.info("Preprocessing test input to identify missing rules and create new TestInput");

    List<RuleAddition> rulesToAdd = findMissingRules(originalInput);

    TestInput processedInput = new TestInput(); // Create a new TestInput instance

    if (rulesToAdd.isEmpty()) {
      logger.info("No missing rules identified. Constructing new TestInput identical to original.");
      for (TestParameter p : originalInput.getTestParameters()) {
        // Create new TestParameter with copies of partitions and dependencies
        processedInput.add(
            new TestParameter(
                p.getName(),
                new ArrayList<>(p.getPartitions()),
                new ArrayList<>(p.getDependencies())));
      }
      return processedInput;
    }

    Map<String, List<CompatibilityPredicate>> newRulesForTargetParameters = new HashMap<>();
    for (RuleAddition addition : rulesToAdd) {
      newRulesForTargetParameters
          .computeIfAbsent(addition.targetParam.getName(), k -> new ArrayList<>())
          .add(addition.rule);
      logger.debug(
          "Rule from {} identified to be added to {}",
          addition.sourceParam.getName(),
          addition.targetParam.getName());
    }

    for (TestParameter originalParam : originalInput.getTestParameters()) {
      List<CompatibilityPredicate> augmentedDependencies =
          new ArrayList<>(originalParam.getDependencies());
      List<CompatibilityPredicate> additionalRulesForThisParam =
          newRulesForTargetParameters.get(originalParam.getName());
      if (additionalRulesForThisParam != null) {
        for (CompatibilityPredicate newRule : additionalRulesForThisParam) {
          if (!augmentedDependencies.contains(newRule)) {
            augmentedDependencies.add(newRule);
            logger.debug(
                "Adding new rule to {}. Total rules now: {}",
                originalParam.getName(),
                augmentedDependencies.size());
          }
        }
      }

      TestParameter newParam =
          new TestParameter(
              originalParam.getName(),
              new ArrayList<>(originalParam.getPartitions()),
              augmentedDependencies);
      processedInput.add(newParam); // Add the new parameter to the new TestInput
    }

    logger.info("Created new TestInput with augmented rules.");
    return processedInput;
  }

  /** Represents a rule that needs to be added to a parameter. */
  private static class RuleAddition {
    final CompatibilityPredicate rule;
    final TestParameter targetParam;
    final TestParameter sourceParam;

    RuleAddition(
        CompatibilityPredicate rule, TestParameter sourceParam, TestParameter targetParam) {
      this.rule = rule;
      this.sourceParam = sourceParam;
      this.targetParam = targetParam;
    }
  }

  /** Finds all missing rules that need to be added to parameters. */
  private List<RuleAddition> findMissingRules(TestInput input) {
    List<RuleAddition> rulesToAdd = new ArrayList<>();
    List<TestParameter> parameters = input.getTestParameters();
    List<TestParameter> parametersWithRules = findParametersWithRules(parameters);

    for (TestParameter sourceParam : parametersWithRules) {
      rulesToAdd.addAll(findMissingRulesForParameter(sourceParam, parameters));
    }
    return rulesToAdd;
  }

  /** Finds parameters that have existing rules. */
  private List<TestParameter> findParametersWithRules(List<TestParameter> parameters) {
    return parameters.stream()
        .filter(param -> !param.getDependencies().isEmpty())
        .collect(Collectors.toList());
  }

  /** Finds missing rules for a specific parameter. */
  private List<RuleAddition> findMissingRulesForParameter(
      TestParameter sourceParam, List<TestParameter> allParameters) {
    List<RuleAddition> rulesToAdd = new ArrayList<>();
    for (CompatibilityPredicate rule : sourceParam.getDependencies()) {
      for (TestParameter targetParam : allParameters) {
        if (targetParam == sourceParam) {
          continue;
        }
        if (shouldAddRule(rule, sourceParam, targetParam)) {
          rulesToAdd.add(new RuleAddition(rule, sourceParam, targetParam));
        }
      }
    }
    return rulesToAdd;
  }

  /** Determines if a rule should be added to a target parameter. */
  private boolean shouldAddRule(
      CompatibilityPredicate rule, TestParameter sourceParam, TestParameter targetParam) {
    if (targetParam.getDependencies().contains(rule)) {
      return false;
    }
    return ruleInteractsWithParameter(rule, sourceParam, targetParam);
  }

  /**
   * Tests if a rule interacts with a target parameter by checking if any combination of values
   * between the source and target parameters would be incompatible.
   */
  private boolean ruleInteractsWithParameter(
      CompatibilityPredicate rule, TestParameter sourceParam, TestParameter targetParam) {
    for (EquivalencePartition sv : sourceParam.getPartitions()) {
      for (EquivalencePartition tv : targetParam.getPartitions()) {
        if (!rule.test(sv, tv)) {
          logger.trace(
              "Rule interaction found for rule on {} with {}: rule({}, {}) -> false",
              sourceParam.getName(),
              targetParam.getName(),
              sv.getName(),
              tv.getName());
          return true;
        }
        if (!rule.test(tv, sv)) {
          logger.trace(
              "Rule interaction found (symmetric check) for rule on {} with {}: rule({}, {}) -> false",
              sourceParam.getName(),
              targetParam.getName(),
              tv.getName(),
              sv.getName());
          return true;
        }
      }
    }
    logger.trace(
        "No rule interaction found for rule on {} with {}",
        sourceParam.getName(),
        targetParam.getName());
    return false;
  }
}
