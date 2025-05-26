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
package com.functest.jpwise.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * The problem domain which we want to cover. It has many testParameters, each having several
 * possible values.
 *
 * @author panwei, davydovmd
 */
@SuppressWarnings("rawtypes")
public class TestInput {
  /** List of testParameters. */
  private List<TestParameter> testParameters;

  /** Constructor. */
  public TestInput() {
    super();
    testParameters = new ArrayList<>();
  }

  public List<TestParameter> getTestParameters() {
    return ImmutableList.copyOf(testParameters);
  }

  /**
   * add a dimension.
   *
   * @param newTestParameter parameter to add
   */
  public void add(TestParameter newTestParameter) {
    testParameters.add(newTestParameter);
  }

  /**
   * return the number of dimension of the domain.
   *
   * @return
   */
  public int size() {
    return testParameters.size();
  }

  /**
   * Get a dimension via its index.
   *
   * @param index
   * @return
   */
  public TestParameter get(int index) {
    return testParameters.get(index);
  }

  @Override
  public String toString() {
    return String.format("TestInput{testParameters=%s}", testParameters);
  }
}
