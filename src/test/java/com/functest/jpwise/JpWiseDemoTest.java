package com.functest.jpwise;

import com.functest.jpwise.algo.CombinatorialAlgorithm;
import com.functest.jpwise.algo.PairwiseAlgorithm;
import com.functest.jpwise.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Integration tests for JPWise demonstrating real-world web application testing scenarios
 * using equivalence classes and their concrete values.
 */
public class JpWiseDemoTest {
    private final static Logger logger = LoggerFactory.getLogger(JpWiseDemoTest.class);
    
    private TestInput testInput;
    private TestParameter browser;
    private TestParameter operatingSystem;
    private TestParameter screenResolution;
    private TestParameter networkCondition;
    private TestParameter userRole;

    @BeforeMethod
    public void setUp() {
        // Browser equivalence classes with specific versions
        browser = new TestParameter("browser", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Chrome", "116.0.5845.96"),    // Chrome family with specific version
            SimpleValue.of("Firefox", "118.0.2"),         // Firefox family with specific version
            SimpleValue.of("Safari", "17.0"),             // Safari family with specific version
            SimpleValue.of("Edge", "117.0.2045.47")       // Edge family with specific version
        ));

        // OS equivalence classes with specific builds/versions
        operatingSystem = new TestParameter("operatingSystem", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Windows 11", "22H2 22621.2428"),   // Windows 11 family with specific build
            SimpleValue.of("Windows 10", "22H2 19045.3693"),   // Windows 10 family with specific build
            SimpleValue.of("macOS", "14.1.1"),                 // macOS family with specific version
            SimpleValue.of("Ubuntu", "22.04.3 LTS"),           // Ubuntu family with specific version
            SimpleValue.of("Fedora", "39.1.2")                 // Fedora family with specific version
        ));

        // Resolution equivalence classes with specific pixel dimensions
        screenResolution = new TestParameter("screenResolution", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("HD", "1920x1080"),           // HD class with specific resolution
            SimpleValue.of("QHD", "2560x1440"),          // QHD class with specific resolution
            SimpleValue.of("4K", "3840x2160"),           // 4K class with specific resolution
            SimpleValue.of("HD Ready", "1366x768")       // HD Ready class with specific resolution
        ));

        // Network condition classes with specific speeds
        networkCondition = new TestParameter("networkCondition", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("4G", "100Mbps"),             // 4G class with typical speed
            SimpleValue.of("5G", "1Gbps"),               // 5G class with typical speed
            SimpleValue.of("Fiber", "10Gbps"),           // Fiber class with typical speed
            SimpleValue.of("3G", "10Mbps")               // 3G class with typical speed
        ));

        // User role classes with specific permissions
        userRole = new TestParameter("userRole", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Admin", "FULL_ACCESS"),       // Admin class with permission level
            SimpleValue.of("Manager", "DEPARTMENT_ACCESS"), // Manager class with permission level
            SimpleValue.of("User", "BASIC_ACCESS"),       // User class with permission level
            SimpleValue.of("Guest", "READ_ONLY")          // Guest class with permission level
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
                if (!v1.getParentParameter().getName().equals("browser") ||
                    !v2.getParentParameter().getName().equals("operatingSystem")) {
                    return true;
                }

                String browser = v1.getName();
                String os = v2.getName();

                // Safari only works with macOS
                if (browser.equals("Safari")) {
                    return os.equals("macOS");
                }

                // Edge only works with Windows
                if (browser.equals("Edge")) {
                    return os.startsWith("Windows");
                }

                // Chrome and Firefox work with all OS
                return true;
            }
        );

        // Create test input with browser-OS compatibility rules
        TestInput compatibilityInput = new TestInput();
        compatibilityInput.add(new TestParameter("browser", browser.getValues(), browserOsRules));
        compatibilityInput.add(operatingSystem);

        TestGenerator generator = new TestGenerator(compatibilityInput);
        generator.generate(new PairwiseAlgorithm());

        CombinationTable results = generator.result();
        
        // Verify browser-OS compatibility rules
        for (Combination combination : results.combinations()) {
            String browserName = combination.getValue(0).getName();
            String osName = combination.getValue(1).getName();
            
            if (browserName.equals("Safari")) {
                assertEquals(osName, "macOS", 
                    "Safari should only be paired with macOS");
            }
            if (browserName.equals("Edge")) {
                assertTrue(osName.startsWith("Windows"), 
                    "Edge should only be paired with Windows");
            }
        }
    }

    @Test
    public void testEnterpriseAccessControl() {
        // Enterprise department classes with specific functions
        TestParameter department = new TestParameter("department", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Sales", "REVENUE_CRITICAL"),
            SimpleValue.of("Engineering", "TECHNICAL"),
            SimpleValue.of("Marketing", "CONTENT_FOCUSED"),
            SimpleValue.of("Support", "CUSTOMER_FACING")
        ));

        // Access level classes with specific capabilities
        TestParameter accessLevel = new TestParameter("accessLevel", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Full", "ALL_OPERATIONS"),
            SimpleValue.of("ReadWrite", "MODIFY_CONTENT"),
            SimpleValue.of("ReadOnly", "VIEW_CONTENT"),
            SimpleValue.of("Restricted", "LIMITED_VIEW")
        ));

        // Define access control rules
        List<CompatibilityPredicate> accessRules = Arrays.asList(
            (v1, v2) -> {
                String role = v1.getName();
                String access = v2.getName();
                
                // Only admins get full access
                if (access.equals("Full")) {
                    return role.equals("Admin");
                }
                // Engineering gets ReadWrite access
                if (role.equals("Engineering") && access.equals("ReadWrite")) {
                    return true;
                }
                // Guests are always restricted
                if (role.equals("Guest")) {
                    return access.equals("Restricted");
                }
                // Other roles get ReadOnly or Restricted
                return access.equals("ReadOnly") || access.equals("Restricted");
            }
        );

        TestInput enterpriseInput = new TestInput();
        enterpriseInput.add(userRole);
        enterpriseInput.add(department);
        enterpriseInput.add(new TestParameter("accessLevel", accessLevel.getValues(), accessRules));

        TestGenerator generator = new TestGenerator(enterpriseInput);
        generator.generate(new CombinatorialAlgorithm());

        CombinationTable results = generator.result();
        
        // Verify access control rules
        for (Combination combination : results.combinations()) {
            String role = combination.getValue(0).getName();
            String access = combination.getValue(2).getName();
            
            if (access.equals("Full")) {
                assertEquals(role, "Admin", 
                    "Only Admin should have Full access");
            }
            if (role.equals("Guest")) {
                assertEquals(access, "Restricted", 
                    "Guests should have Restricted access");
            }
        }
    }

    @Test
    public void testResponsiveDesign() {
        // Device type classes with specific characteristics
        TestParameter deviceType = new TestParameter("deviceType", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Desktop", "LARGE_SCREEN"),
            SimpleValue.of("Tablet", "MEDIUM_SCREEN"),
            SimpleValue.of("Mobile", "SMALL_SCREEN")
        ));

        // Orientation classes with specific dimensions
        TestParameter orientation = new TestParameter("orientation", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Landscape", "WIDTH > HEIGHT"),
            SimpleValue.of("Portrait", "HEIGHT > WIDTH")
        ));

        // Define device-resolution compatibility rules
        List<CompatibilityPredicate> resolutionRules = Arrays.asList(
            (v1, v2) -> {
                String device = v1.getName();
                String resolution = v2.getName();
                
                // Mobile devices don't support 4K
                if (device.equals("Mobile") && resolution.equals("4K")) {
                    return false;
                }
                // Tablets don't use HD Ready
                if (device.equals("Tablet") && resolution.equals("HD Ready")) {
                    return false;
                }
                return true;
            }
        );

        TestInput responsiveInput = new TestInput();
        responsiveInput.add(new TestParameter("deviceType", deviceType.getValues(), resolutionRules));
        responsiveInput.add(screenResolution);
        responsiveInput.add(orientation);

        TestGenerator generator = new TestGenerator(responsiveInput);
        generator.generate(new PairwiseAlgorithm());

        CombinationTable results = generator.result();
        
        // Verify responsive design rules
        for (Combination combination : results.combinations()) {
            String device = combination.getValue(0).getName();
            String resolution = combination.getValue(1).getName();
            
            if (device.equals("Mobile")) {
                assertNotEquals(resolution, "4K", 
                    "Mobile devices should not use 4K resolution");
            }
            if (device.equals("Tablet")) {
                assertNotEquals(resolution, "HD Ready", 
                    "Tablets should not use HD Ready resolution");
            }
        }
    }

    @Test
    public void testPerformanceScenarios() {
        // Load condition classes with specific user counts
        TestParameter loadCondition = new TestParameter("loadCondition", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Light", "100_USERS"),
            SimpleValue.of("Medium", "1000_USERS"),
            SimpleValue.of("Heavy", "10000_USERS"),
            SimpleValue.of("Peak", "100000_USERS")
        ));

        // Cache status classes with specific states
        TestParameter cacheStatus = new TestParameter("cacheStatus", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Warm", "FULLY_POPULATED"),
            SimpleValue.of("Cold", "EMPTY"),
            SimpleValue.of("Partial", "PARTIALLY_POPULATED")
        ));

        // Concurrent users classes with specific thresholds
        TestParameter concurrentUsers = new TestParameter("concurrentUsers", Arrays.<ParameterValue<?>>asList(
            SimpleValue.of("Low", "100"),
            SimpleValue.of("Medium", "1000"),
            SimpleValue.of("High", "10000")
        ));

        // Define performance compatibility rules
        List<CompatibilityPredicate> performanceRules = Arrays.asList(
            (v1, v2) -> {
                // Only apply rules if we're dealing with load and network conditions
                if (!v1.getParentParameter().getName().equals("loadCondition") ||
                    !v2.getParentParameter().getName().equals("networkCondition")) {
                    return true;
                }

                String load = v1.getName();
                String network = v2.getName();
                
                // Peak load requires high-speed networks
                if (load.equals("Peak")) {
                    return network.equals("5G") || network.equals("Fiber");
                }
                // Heavy load doesn't work well with 3G
                if (load.equals("Heavy")) {
                    return !network.equals("3G");
                }
                return true;
            }
        );

        TestInput performanceInput = new TestInput();
        performanceInput.add(new TestParameter("loadCondition", loadCondition.getValues(), performanceRules));
        performanceInput.add(networkCondition);
        performanceInput.add(cacheStatus);
        performanceInput.add(concurrentUsers);

        TestGenerator generator = new TestGenerator(performanceInput);
        generator.generate(new PairwiseAlgorithm());

        CombinationTable results = generator.result();
        
        // Verify performance rules
        for (Combination combination : results.combinations()) {
            String load = combination.getValue(0).getName();
            String network = combination.getValue(1).getName();
            
            if (load.equals("Peak")) {
                assertTrue(network.equals("5G") || network.equals("Fiber"),
                    "Peak load should only use high-speed networks");
            }
            if (load.equals("Heavy")) {
                assertNotEquals(network, "3G",
                    "Heavy load should not use 3G network");
            }
        }
    }
}
