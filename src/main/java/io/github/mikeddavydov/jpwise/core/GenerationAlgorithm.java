/**
 * Copyright (c) 2010 Ng Pan Wei, 2013 Mikhail Davydov
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.mikeddavydov.jpwise.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for test generation algorithms.
 *
 * <p>This class provides common functionality for generating test combinations and checking
 * compatibility between parameter values.
 */
public abstract class GenerationAlgorithm {
  private static final Logger logger = LoggerFactory.getLogger(GenerationAlgorithm.class);

  /**
   * Generates test combinations for the given input.
   *
   * <p>This method must be implemented by concrete algorithms to provide their specific combination
   * generation logic.
   *
   * @param input The test input to generate combinations for
   * @return A table containing the generated combinations
   */
  public abstract CombinationTable generate(TestInput input);

  /**
   * Checks if a combination is valid according to the compatibility rules.
   *
   * <p>A combination is valid if all pairs of values in it are compatible according to the rules
   * defined in their parameters.
   *
   * @param combination The combination to check
   * @return true if the combination is valid, false otherwise
   */
  public boolean isValidCombination(Combination combination) {
    if (combination == null) {
      throw new IllegalArgumentException("Combination cannot be null");
    }
    logger.trace("isValidCombination checking: {}", combination.getKey());

    List<TestParameter> parameters = combination.getParameters();
    for (int i = 0; i < parameters.size(); i++) {
      TestParameter param1 = parameters.get(i);
      EquivalencePartition value1 = combination.getValue(i);

      if (value1 == null) {
        continue; // Skip null values
      }

      // Check compatibility with all other parameters
      for (int j = i + 1; j < parameters.size(); j++) {
        TestParameter param2 = parameters.get(j);
        EquivalencePartition value2 = combination.getValue(j);

        if (value2 == null) {
          continue; // Skip null values
        }

        boolean p1Compatible = param1.areCompatible(value1, value2);
        // Arguments to p2.areCompatible are (value_of_p2, value_of_p1)
        boolean p2Compatible = param2.areCompatible(value2, value1);

        logger.trace(
            "  isValidCombination: Pair ('{}:{}', '{}:{}') -> "
                + "p1.areCompatible ({}) = {}, p2.areCompatible ({}) = {}",
            param1.getName(),
            value1.getName(),
            param2.getName(),
            value2.getName(),
            param1.getName(),
            p1Compatible,
            param2.getName(),
            p2Compatible);

        // Check if values are compatible according to both parameters' rules
        if (!p1Compatible || !p2Compatible) {
          logger.trace(
              "  Incompatible pair found by isValidCombination: ('{}:{}', '{}:{}') because p1Compatible={}, p2Compatible={}",
              param1.getName(),
              value1.getName(),
              param2.getName(),
              value2.getName(),
              p1Compatible,
              p2Compatible);
          return false;
        }
      }
    }
    logger.trace(
        "isValidCombination: All pairs in {} are compatible. Returning true.",
        combination.getKey());
    return true;
  }
}
