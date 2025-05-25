package com.functest.jpwise;

import com.functest.jpwise.algo.PairwiseAlgorithm;
import com.functest.jpwise.core.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.functest.jpwise.core.PartitionPredicates.*;
import static org.testng.Assert.*;

/**
 * Integration tests for JPWise demonstrating real-world web application testing scenarios
 * using equivalence classes and their concrete values.
 */
public class JpWiseDemoTest {
    private TestInput testInput;
    private TestParameter browser;
    private TestParameter operatingSystem;
    private TestParameter screenResolution;
    private TestParameter networkCondition;
    private TestParameter userRole;

    @BeforeMethod
    public void setUp() {
        // Browser versions with cycling
        browser = new TestParameter("browser", Arrays.asList(
            new CyclingPartition<>("Chrome", "116.0.5845.96", 
                Arrays.asList("116.0.5845.96", "116.0.5845.97", "116.0.5845.98")),
            new CyclingPartition<>("Firefox", "118.0.2",
                Arrays.asList("118.0.2", "118.0.3", "118.1.0")),
            new CyclingPartition<>("Safari", "17.0",
                Arrays.asList("17.0", "17.0.1", "17.1")),
            new CyclingPartition<>("Edge", "117.0.2045.47",
                Arrays.asList("117.0.2045.47", "117.0.2045.48", "117.1.0"))
        ));

        // OS equivalence classes with specific builds/versions
        operatingSystem = new TestParameter("operatingSystem", Arrays.<EquivalencePartition<?>>asList(
            GenericPartition.of("Windows 11", () -> "22H2 22621.2428"),
            GenericPartition.of("Windows 10", () -> "22H2 19045.3693"),
            GenericPartition.of("macOS", () -> "14.1.1"),
            GenericPartition.of("Ubuntu", () -> "22.04.3 LTS"),
            GenericPartition.of("Fedora", () -> "39.1.2")
        ));

        // Resolution equivalence classes with specific pixel dimensions
        screenResolution = new TestParameter("screenResolution", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("HD", "1920x1080"),
            SimpleValue.of("QHD", "2560x1440"),
            SimpleValue.of("4K", "3840x2160"),
            SimpleValue.of("HD Ready", "1366x768")
        ));

        // Network condition classes with specific speeds
        networkCondition = new TestParameter("networkCondition", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("4G", "100Mbps"),
            SimpleValue.of("5G", "1Gbps"),
            SimpleValue.of("Fiber", "10Gbps"),
            SimpleValue.of("3G", "10Mbps")
        ));

        // User role classes with specific permissions
        userRole = new TestParameter("userRole", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("Admin", "FULL_ACCESS"),
            SimpleValue.of("Manager", "DEPARTMENT_ACCESS"),
            SimpleValue.of("User", "BASIC_ACCESS"),
            SimpleValue.of("Guest", "READ_ONLY")
        ));

        testInput = new TestInput();
        testInput.add(browser);
        testInput.add(operatingSystem);
        testInput.add(screenResolution);
        testInput.add(networkCondition);
        testInput.add(userRole);
    }

    @Test
    public void testBrowserCompatibilityRules() {
        // Define browser-OS compatibility rules based on real-world constraints
        List<CompatibilityPredicate> browserOsRules = Arrays.asList(
            (v1, v2) -> {
                // Only apply rules if we're dealing with browser and OS parameters
                if (!(parameterNameIs("browser").test(v1) && parameterNameIs("operatingSystem").test(v2)) &&
                    !(parameterNameIs("operatingSystem").test(v1) && parameterNameIs("browser").test(v2))) {
                    return true;
                }

                // Ensure v1 is the browser value
                if (parameterNameIs("operatingSystem").test(v1)) {
                    EquivalencePartition<?> temp = v1;
                    v1 = v2;
                    v2 = temp;
                }

                // Safari only works with macOS
                if (nameIs("Safari").test(v1)) {
                    return nameIs("macOS").test(v2);
                }

                // Edge only works with Windows
                if (nameIs("Edge").test(v1)) {
                    return or(
                        nameIs("Windows 11"),
                        nameIs("Windows 10")
                    ).test(v2);
                }

                // Chrome and Firefox work with all OS
                return true;
            }
        );

        // Create test input with browser-OS compatibility rules
        TestInput compatibilityInput = new TestInput();
        compatibilityInput.add(new TestParameter("browser", browser.getPartitions(), browserOsRules));
        compatibilityInput.add(operatingSystem);

        TestGenerator generator = new TestGenerator(compatibilityInput);
        generator.generate(new PairwiseAlgorithm());

        CombinationTable results = generator.result();
        
        // Verify browser-OS compatibility rules
        for (Combination combination : results.combinations()) {
            EquivalencePartition<?> browserValue = combination.getValue(0);
            EquivalencePartition<?> osValue = combination.getValue(1);
            
            if (nameIs("Safari").test(browserValue)) {
                assertTrue(nameIs("macOS").test(osValue), 
                    "Safari should only work with macOS");
            }
            
            if (nameIs("Edge").test(browserValue)) {
                assertTrue(
                    or(nameIs("Windows 11"), nameIs("Windows 10")).test(osValue),
                    "Edge should only work with Windows"
                );
            }
        }
    }

    @Test
    public void testEnterpriseAccessControl() {
        // Enterprise department classes with specific functions
        TestParameter department = new TestParameter("department", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("Sales", "REVENUE_CRITICAL"),
            SimpleValue.of("Engineering", "TECHNICAL"),
            SimpleValue.of("Marketing", "CONTENT_FOCUSED"),
            SimpleValue.of("Support", "CUSTOMER_FACING")
        ));

        // Access level classes with specific capabilities
        TestParameter accessLevel = new TestParameter("accessLevel", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("Full", "ALL_OPERATIONS"),
            SimpleValue.of("ReadWrite", "MODIFY_CONTENT"),
            SimpleValue.of("ReadOnly", "VIEW_CONTENT"),
            SimpleValue.of("Restricted", "LIMITED_VIEW")
        ));

        // Define access control rules using predicates
        List<CompatibilityPredicate> accessRules = Arrays.asList(
            (v1, v2) -> {
                // Only apply rules if we're dealing with role and access level
                if (!(parameterNameIs("userRole").test(v1) && parameterNameIs("accessLevel").test(v2)) &&
                    !(parameterNameIs("accessLevel").test(v1) && parameterNameIs("userRole").test(v2))) {
                    return true;
                }

                // Ensure v1 is the role value
                if (parameterNameIs("accessLevel").test(v1)) {
                    EquivalencePartition<?> temp = v1;
                    v1 = v2;
                    v2 = temp;
                }

                // Only admins get full access
                if (nameIs("Full").test(v2)) {
                    return nameIs("Admin").test(v1);
                }

                // ReadWrite access is only for Admin and Manager
                if (nameIs("ReadWrite").test(v2)) {
                    return or(nameIs("Admin"), nameIs("Manager")).test(v1);
                }

                // Guests are always restricted
                if (nameIs("Guest").test(v1)) {
                    return nameIs("Restricted").test(v2);
                }

                // Other roles get ReadOnly or Restricted
                return or(nameIs("ReadOnly"), nameIs("Restricted")).test(v2);
            }
        );

        TestInput enterpriseInput = new TestInput();
        enterpriseInput.add(new TestParameter("userRole", userRole.getPartitions(), accessRules));
        enterpriseInput.add(department);
        enterpriseInput.add(accessLevel);

        TestGenerator generator = new TestGenerator(enterpriseInput);
        generator.generate(new PairwiseAlgorithm());

        CombinationTable results = generator.result();
        
        // Verify access control rules
        for (Combination combination : results.combinations()) {
            EquivalencePartition<?> roleValue = combination.getValue(0);
            EquivalencePartition<?> departmentValue = combination.getValue(1);
            EquivalencePartition<?> accessValue = combination.getValue(2);

            if (nameIs("Admin").test(roleValue)) {
                // Admin can have any access level
                assertTrue(true);
            } else if (nameIs("Guest").test(roleValue)) {
                assertTrue(nameIs("Restricted").test(accessValue),
                    "Guests should only have Restricted access");
            } else if (nameIs("Manager").test(roleValue)) {
                assertTrue(
                    or(
                        nameIs("ReadWrite"),
                        nameIs("ReadOnly"),
                        nameIs("Restricted")
                    ).test(accessValue),
                    "Managers should have appropriate access level"
                );
            } else {
                assertTrue(
                    or(nameIs("ReadOnly"), nameIs("Restricted")).test(accessValue),
                    "Other roles should have ReadOnly or Restricted access"
                );
            }
        }
    }

    @Test
    public void testDeviceCompatibility() {
        // Device type classes
        TestParameter deviceType = new TestParameter("deviceType", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("Desktop", "WORKSTATION"),
            SimpleValue.of("Laptop", "PORTABLE"),
            SimpleValue.of("Tablet", "TOUCH_ENABLED"),
            SimpleValue.of("Mobile", "HANDHELD")
        ));

        // Define device-resolution compatibility rules
        List<CompatibilityPredicate> deviceRules = Arrays.asList(
            (v1, v2) -> {
                // Only apply rules if we're dealing with device and resolution
                if (!(parameterNameIs("deviceType").test(v1) && parameterNameIs("screenResolution").test(v2)) &&
                    !(parameterNameIs("screenResolution").test(v1) && parameterNameIs("deviceType").test(v2))) {
                    return true;
                }

                // Ensure v1 is the device value
                if (parameterNameIs("screenResolution").test(v1)) {
                    EquivalencePartition<?> temp = v1;
                    v1 = v2;
                    v2 = temp;
                }

                // Mobile devices don't support 4K
                if (nameIs("Mobile").test(v1)) {
                    return !nameIs("4K").test(v2);
                }

                // Tablets don't use HD Ready
                if (nameIs("Tablet").test(v1)) {
                    return !nameIs("HD Ready").test(v2);
                }

                return true;
            }
        );

        TestInput deviceInput = new TestInput();
        deviceInput.add(new TestParameter("deviceType", deviceType.getPartitions(), deviceRules));
        deviceInput.add(screenResolution);

        TestGenerator generator = new TestGenerator(deviceInput);
        generator.generate(new PairwiseAlgorithm());

        CombinationTable results = generator.result();
        
        // Verify device compatibility rules
        for (Combination combination : results.combinations()) {
            EquivalencePartition<?> deviceValue = combination.getValue(0);
            EquivalencePartition<?> resolutionValue = combination.getValue(1);
            
            if (nameIs("Mobile").test(deviceValue)) {
                assertFalse(nameIs("4K").test(resolutionValue), 
                    "Mobile devices should not use 4K resolution");
            }
            if (nameIs("Tablet").test(deviceValue)) {
                assertFalse(nameIs("HD Ready").test(resolutionValue), 
                    "Tablets should not use HD Ready resolution");
            }
        }
    }

    @Test
    public void testPerformanceScenarios() {
        // Load condition classes with specific user counts
        TestParameter loadCondition = new TestParameter("loadCondition", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("Light", "100_USERS"),
            SimpleValue.of("Medium", "1000_USERS"),
            SimpleValue.of("Heavy", "10000_USERS"),
            SimpleValue.of("Peak", "100000_USERS")
        ));

        // Cache status classes with specific states
        TestParameter cacheStatus = new TestParameter("cacheStatus", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("Warm", "FULLY_POPULATED"),
            SimpleValue.of("Cold", "EMPTY"),
            SimpleValue.of("Partial", "PARTIALLY_POPULATED")
        ));

        // Concurrent users classes with specific thresholds
        TestParameter concurrentUsers = new TestParameter("concurrentUsers", Arrays.<EquivalencePartition<?>>asList(
            SimpleValue.of("Low", "100"),
            SimpleValue.of("Medium", "1000"),
            SimpleValue.of("High", "10000")
        ));

        // Define performance compatibility rules using predicates
        List<CompatibilityPredicate> performanceRules = Arrays.asList(
            (v1, v2) -> {
                // Only apply rules if we're dealing with load and network conditions
                if (!(parameterNameIs("loadCondition").test(v1) && parameterNameIs("networkCondition").test(v2)) &&
                    !(parameterNameIs("networkCondition").test(v1) && parameterNameIs("loadCondition").test(v2))) {
                    return true;
                }

                // Ensure v1 is the load value
                if (parameterNameIs("networkCondition").test(v1)) {
                    EquivalencePartition<?> temp = v1;
                    v1 = v2;
                    v2 = temp;
                }

                // Peak load requires high-speed networks
                if (nameIs("Peak").test(v1)) {
                    return or(nameIs("5G"), nameIs("Fiber")).test(v2);
                }

                // Heavy load doesn't work well with 3G
                if (nameIs("Heavy").test(v1)) {
                    return not(nameIs("3G")).test(v2);
                }

                // All other combinations are compatible
                return true;
            }
        );

        TestInput performanceInput = new TestInput();
        performanceInput.add(new TestParameter("loadCondition", loadCondition.getPartitions(), performanceRules));
        performanceInput.add(networkCondition);
        performanceInput.add(cacheStatus);
        performanceInput.add(concurrentUsers);

        TestGenerator generator = new TestGenerator(performanceInput);
        generator.generate(new PairwiseAlgorithm());

        CombinationTable results = generator.result();
        
        // Verify performance rules
        for (Combination combination : results.combinations()) {
            EquivalencePartition<?> loadValue = combination.getValue(0);
            EquivalencePartition<?> networkValue = combination.getValue(1);
            
            if (nameIs("Peak").test(loadValue)) {
                assertTrue(
                    or(nameIs("5G"), nameIs("Fiber")).test(networkValue),
                    "Peak load should only use high-speed networks"
                );
            }
            
            if (nameIs("Heavy").test(loadValue)) {
                assertFalse(nameIs("3G").test(networkValue),
                    "Heavy load should not use 3G network");
            }
        }
    }
}
