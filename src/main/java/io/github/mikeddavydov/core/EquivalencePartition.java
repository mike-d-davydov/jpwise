package io.github.mikeddavydov.core;

/**
 * @author DavydovMD Date: 17.06.13 Time: 15:49
 */
public interface EquivalencePartition {
  TestParameter getParentParameter();

  void setParentParameter(TestParameter parameter);

  Object getValue();

  String getName();

  boolean isCompatibleWith(EquivalencePartition v2);
}
