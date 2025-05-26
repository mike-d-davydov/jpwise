package com.functest.jpwise;

import static com.functest.jpwise.core.PartitionPredicates.*;
import static org.testng.Assert.*;

import com.functest.jpwise.core.*;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Integration tests for JPWise demonstrating real-world web application testing scenarios. */
public class JpWiseDemoTest {
  private CyclingPartition<String> chrome;
  private CyclingPartition<String> firefox;
  private CyclingPartition<String> safari;
  private CyclingPartition<String> edge;
  private GenericPartition<String> windows11;
  private GenericPartition<String> windows10;
  private GenericPartition<String> macOS;
  private GenericPartition<String> ubuntu;
  private GenericPartition<String> fedora;
  private SimpleValue<String> hd;
  private SimpleValue<String> qhd;
  private SimpleValue<String> uhd;
  private SimpleValue<String> hdReady;
  private SimpleValue<String> network4g;
  private SimpleValue<String> network5g;
  private SimpleValue<String> networkFiber;
  private SimpleValue<String> network3g;
  private SimpleValue<String> admin;
  private SimpleValue<String> manager;
  private SimpleValue<String> user;
  private SimpleValue<String> guest;

  @BeforeMethod
  public void setUp() {
    // Browser versions with cycling
    chrome =
        new CyclingPartition<>(
            "Chrome",
            "116.0.5845.96",
            Arrays.asList("116.0.5845.96", "116.0.5845.97", "116.0.5845.98"));
    firefox =
        new CyclingPartition<>(
            "Firefox", "118.0.2", Arrays.asList("118.0.2", "118.0.3", "118.1.0"));
    safari = new CyclingPartition<>("Safari", "17.0", Arrays.asList("17.0", "17.0.1", "17.1"));
    edge =
        new CyclingPartition<>(
            "Edge", "117.0.2045.47", Arrays.asList("117.0.2045.47", "117.0.2045.48", "117.1.0"));

    // OS with dynamic versions
    windows11 = GenericPartition.of("Windows 11", () -> "22H2 22621.2428");
    windows10 = GenericPartition.of("Windows 10", () -> "22H2 19045.3693");
    macOS = GenericPartition.of("macOS", () -> "14.1.1");
    ubuntu = GenericPartition.of("Ubuntu", () -> "22.04.3 LTS");
    fedora = GenericPartition.of("Fedora", () -> "39.1.2");

    // Screen resolutions
    hd = SimpleValue.of("HD", "1920x1080");
    qhd = SimpleValue.of("QHD", "2560x1440");
    uhd = SimpleValue.of("4K", "3840x2160");
    hdReady = SimpleValue.of("HD Ready", "1366x768");

    // Network conditions
    network4g = SimpleValue.of("4G", "100Mbps");
    network5g = SimpleValue.of("5G", "1Gbps");
    networkFiber = SimpleValue.of("Fiber", "10Gbps");
    network3g = SimpleValue.of("3G", "10Mbps");

    // User roles
    admin = SimpleValue.of("Admin", "FULL_ACCESS");
    manager = SimpleValue.of("Manager", "DEPARTMENT_ACCESS");
    user = SimpleValue.of("User", "BASIC_ACCESS");
    guest = SimpleValue.of("Guest", "READ_ONLY");
  }

  @Test
  public void testBrowserCompatibilityRules() {
    // Define browser-OS compatibility rules using fluent API
    List<CompatibilityPredicate> browserOsRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with browser and OS parameters
              if (!(parameterNameIs("browser").test(v1)
                      && parameterNameIs("operatingSystem").test(v2))
                  && !(parameterNameIs("operatingSystem").test(v1)
                      && parameterNameIs("browser").test(v2))) {
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
                return or(nameIs("Windows 11"), nameIs("Windows 10")).test(v2);
              }

              // Chrome and Firefox work with all OS
              return true;
            });

    // Create test parameters with rules
    TestParameter browser =
        new TestParameter("browser", Arrays.asList(chrome, firefox, safari, edge), browserOsRules);
    TestParameter os =
        new TestParameter(
            "operatingSystem", Arrays.asList(windows11, windows10, macOS, ubuntu, fedora));

    // Generate and verify combinations
    CombinationTable results = JPWise.generatePairwise(browser, os);

    // Verify browser-OS compatibility rules
    for (Combination combination : results.combinations()) {
      EquivalencePartition<?> browserValue = combination.getValue(0);
      EquivalencePartition<?> osValue = combination.getValue(1);

      if (nameIs("Safari").test(browserValue)) {
        assertTrue(nameIs("macOS").test(osValue), "Safari should only work with macOS");
      }

      if (nameIs("Edge").test(browserValue)) {
        assertTrue(
            or(nameIs("Windows 11"), nameIs("Windows 10")).test(osValue),
            "Edge should only work with Windows");
      }
    }
  }

  @Test
  public void testEnterpriseAccessControl() {
    // Enterprise department classes
    SimpleValue<String> sales = SimpleValue.of("Sales", "REVENUE_CRITICAL");
    SimpleValue<String> engineering = SimpleValue.of("Engineering", "TECHNICAL");
    SimpleValue<String> marketing = SimpleValue.of("Marketing", "CONTENT_FOCUSED");
    SimpleValue<String> support = SimpleValue.of("Support", "CUSTOMER_FACING");

    // Access level classes
    SimpleValue<String> fullAccess = SimpleValue.of("Full", "ALL_OPERATIONS");
    SimpleValue<String> readWrite = SimpleValue.of("ReadWrite", "MODIFY_CONTENT");
    SimpleValue<String> readOnly = SimpleValue.of("ReadOnly", "VIEW_CONTENT");
    SimpleValue<String> restricted = SimpleValue.of("Restricted", "LIMITED_VIEW");

    // Define access control rules using predicates
    List<CompatibilityPredicate> accessRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with role and access level
              if (!(parameterNameIs("userRole").test(v1) && parameterNameIs("accessLevel").test(v2))
                  && !(parameterNameIs("accessLevel").test(v1)
                      && parameterNameIs("userRole").test(v2))) {
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
            });

    // Create parameters with rules and generate combinations
    CombinationTable results =
        JPWise.generatePairwise(
            new TestParameter("userRole", Arrays.asList(admin, manager, user, guest), accessRules),
            new TestParameter("department", Arrays.asList(sales, engineering, marketing, support)),
            new TestParameter(
                "accessLevel", Arrays.asList(fullAccess, readWrite, readOnly, restricted)));

    // Verify access control rules
    for (Combination combination : results.combinations()) {
      EquivalencePartition<?> roleValue = combination.getValue(0);
      EquivalencePartition<?> accessValue = combination.getValue(2);

      if (nameIs("Admin").test(roleValue)) {
        // Admin can have any access level
        assertTrue(true);
      } else if (nameIs("Guest").test(roleValue)) {
        assertTrue(
            nameIs("Restricted").test(accessValue), "Guests should only have Restricted access");
      } else if (nameIs("Manager").test(roleValue)) {
        assertTrue(
            or(nameIs("ReadWrite"), nameIs("ReadOnly"), nameIs("Restricted")).test(accessValue),
            "Managers should have appropriate access level");
      } else {
        assertTrue(
            or(nameIs("ReadOnly"), nameIs("Restricted")).test(accessValue),
            "Other roles should have ReadOnly or Restricted access");
      }
    }
  }

  @Test
  public void testDeviceCompatibility() {
    // Device types
    SimpleValue<String> desktop = SimpleValue.of("Desktop", "WORKSTATION");
    SimpleValue<String> laptop = SimpleValue.of("Laptop", "PORTABLE");
    SimpleValue<String> tablet = SimpleValue.of("Tablet", "TOUCH_ENABLED");
    SimpleValue<String> mobile = SimpleValue.of("Mobile", "HANDHELD");

    // Define device-resolution compatibility rules
    List<CompatibilityPredicate> deviceRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with device and resolution
              if (!(parameterNameIs("deviceType").test(v1)
                      && parameterNameIs("screenResolution").test(v2))
                  && !(parameterNameIs("screenResolution").test(v1)
                      && parameterNameIs("deviceType").test(v2))) {
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
            });

    // Create parameters with rules and generate combinations
    CombinationTable results =
        JPWise.generatePairwise(
            new TestParameter(
                "deviceType", Arrays.asList(desktop, laptop, tablet, mobile), deviceRules),
            new TestParameter("screenResolution", Arrays.asList(hd, qhd, uhd, hdReady)));

    // Verify device compatibility rules
    for (Combination combination : results.combinations()) {
      EquivalencePartition<?> deviceValue = combination.getValue(0);
      EquivalencePartition<?> resolutionValue = combination.getValue(1);

      if (nameIs("Mobile").test(deviceValue)) {
        assertFalse(
            nameIs("4K").test(resolutionValue), "Mobile devices should not use 4K resolution");
      }
      if (nameIs("Tablet").test(deviceValue)) {
        assertFalse(
            nameIs("HD Ready").test(resolutionValue), "Tablets should not use HD Ready resolution");
      }
    }
  }

  @Test
  public void testPerformanceScenarios() {
    // Load conditions
    SimpleValue<String> lightLoad = SimpleValue.of("Light", "100_USERS");
    SimpleValue<String> mediumLoad = SimpleValue.of("Medium", "1000_USERS");
    SimpleValue<String> heavyLoad = SimpleValue.of("Heavy", "10000_USERS");
    SimpleValue<String> peakLoad = SimpleValue.of("Peak", "100000_USERS");

    // Cache status
    SimpleValue<String> warmCache = SimpleValue.of("Warm", "FULLY_POPULATED");
    SimpleValue<String> coldCache = SimpleValue.of("Cold", "EMPTY");
    SimpleValue<String> partialCache = SimpleValue.of("Partial", "PARTIALLY_POPULATED");

    // Define performance compatibility rules
    List<CompatibilityPredicate> performanceRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with load and network conditions
              if (!(parameterNameIs("loadCondition").test(v1)
                      && parameterNameIs("networkCondition").test(v2))
                  && !(parameterNameIs("networkCondition").test(v1)
                      && parameterNameIs("loadCondition").test(v2))) {
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

              return true;
            });

    // Create parameters with rules and generate combinations
    CombinationTable results =
        JPWise.generatePairwise(
            new TestParameter(
                "loadCondition",
                Arrays.asList(lightLoad, mediumLoad, heavyLoad, peakLoad),
                performanceRules),
            new TestParameter(
                "networkCondition", Arrays.asList(network4g, network5g, networkFiber, network3g)),
            new TestParameter("cacheStatus", Arrays.asList(warmCache, coldCache, partialCache)));

    // Verify performance rules
    for (Combination combination : results.combinations()) {
      EquivalencePartition<?> loadValue = combination.getValue(0);
      EquivalencePartition<?> networkValue = combination.getValue(1);

      if (nameIs("Peak").test(loadValue)) {
        assertTrue(
            or(nameIs("5G"), nameIs("Fiber")).test(networkValue),
            "Peak load should only use high-speed networks");
      }

      if (nameIs("Heavy").test(loadValue)) {
        assertFalse(nameIs("3G").test(networkValue), "Heavy load should not use 3G network");
      }
    }
  }
}
