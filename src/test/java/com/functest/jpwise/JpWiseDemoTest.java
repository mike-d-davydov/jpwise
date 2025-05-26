package com.functest.jpwise;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.functest.jpwise.core.*;

/** Integration tests for JPWise demonstrating real-world web application testing scenarios. */
public class JpWiseDemoTest {
  private CyclingPartition chrome;
  private CyclingPartition firefox;
  private CyclingPartition safari;
  private CyclingPartition edge;
  private GenericPartition windows11;
  private GenericPartition windows10;
  private GenericPartition macOS;
  private GenericPartition ubuntu;
  private GenericPartition fedora;
  private SimpleValue hd;
  private SimpleValue qhd;
  private SimpleValue uhd;
  private SimpleValue hdReady;
  private SimpleValue network4g;
  private SimpleValue network5g;
  private SimpleValue networkFiber;
  private SimpleValue network3g;
  private SimpleValue admin;
  private SimpleValue manager;
  private SimpleValue user;
  private SimpleValue guest;

  @BeforeMethod
  public void setUp() {
    // Browser versions with cycling
    chrome =
        new CyclingPartition(
            "Chrome", Arrays.asList("116.0.5845.96", "116.0.5845.97", "116.0.5845.98"));
    firefox = new CyclingPartition("Firefox", Arrays.asList("118.0.2", "118.0.3", "118.1.0"));
    safari = new CyclingPartition("Safari", Arrays.asList("17.0", "17.0.1", "17.1"));
    edge = new CyclingPartition("Edge", Arrays.asList("117.0.2045.47", "117.0.2045.48", "117.1.0"));

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
    // Define browser-OS compatibility rules using direct method calls

    List<CompatibilityPredicate> browserOsRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with browser and OS parameters
              if (!(v1.getParentParameter().getName().equals("browser")
                  && v2.getParentParameter().getName().equals("operatingSystem"))) {
                return true;
              }

              // Safari only works with macOS
              if (v1.getName().equals("Safari")) {
                return v2.getName().equals("macOS");
              }

              // Edge only works with Windows
              if (v1.getName().equals("Edge")) {
                return v2.getName().equals("Windows 11") || v2.getName().equals("Windows 10");
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
      EquivalencePartition browserValue = combination.getValue(0);
      EquivalencePartition osValue = combination.getValue(1);

      if (browserValue.getName().equals("Safari")) {
        assertTrue(osValue.getName().equals("macOS"), "Safari should only work with macOS");
      }

      if (browserValue.getName().equals("Edge")) {
        assertTrue(
            osValue.getName().equals("Windows 11") || osValue.getName().equals("Windows 10"),
            "Edge should only work with Windows");
      }
    }
  }

  @Test
  public void testEnterpriseAccessControl() {
    // Enterprise department classes
    SimpleValue sales = SimpleValue.of("Sales", "REVENUE_CRITICAL");
    SimpleValue engineering = SimpleValue.of("Engineering", "TECHNICAL");
    SimpleValue marketing = SimpleValue.of("Marketing", "CONTENT_FOCUSED");
    SimpleValue support = SimpleValue.of("Support", "CUSTOMER_FACING");

    // Access level classes
    SimpleValue fullAccess = SimpleValue.of("Full", "ALL_OPERATIONS");
    SimpleValue readWrite = SimpleValue.of("ReadWrite", "MODIFY_CONTENT");
    SimpleValue readOnly = SimpleValue.of("ReadOnly", "VIEW_CONTENT");
    SimpleValue restricted = SimpleValue.of("Restricted", "LIMITED_VIEW");

    // Define access control rules using direct method calls
    List<CompatibilityPredicate> accessRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with role and access level
              if (!(v1.getParentParameter().getName().equals("userRole")
                  && v2.getParentParameter().getName().equals("accessLevel"))) {
                return true;
              }

              // Only admins get full access
              if (v2.getName().equals("Full")) {
                return v1.getName().equals("Admin");
              }

              // ReadWrite access is only for Admin and Manager
              if (v2.getName().equals("ReadWrite")) {
                return v1.getName().equals("Admin") || v1.getName().equals("Manager");
              }

              // Guests are always restricted
              if (v1.getName().equals("Guest")) {
                return v2.getName().equals("Restricted");
              }

              // Other roles get ReadOnly or Restricted
              return v2.getName().equals("ReadOnly") || v2.getName().equals("Restricted");
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
      EquivalencePartition roleValue = combination.getValue(0);
      EquivalencePartition accessValue = combination.getValue(2);

      if (roleValue.getName().equals("Admin")) {
        // Admin can have any access level
        assertTrue(true);
      } else if (roleValue.getName().equals("Guest")) {
        assertTrue(
            accessValue.getName().equals("Restricted"),
            "Guests should only have Restricted access");
      } else if (roleValue.getName().equals("Manager")) {
        assertTrue(
            accessValue.getName().equals("ReadWrite")
                || accessValue.getName().equals("ReadOnly")
                || accessValue.getName().equals("Restricted"),
            "Managers should have appropriate access level");
      } else {
        assertTrue(
            accessValue.getName().equals("ReadOnly") || accessValue.getName().equals("Restricted"),
            "Other roles should have ReadOnly or Restricted access");
      }
    }
  }

  @Test
  public void testDeviceCompatibility() {
    // Device types
    SimpleValue desktop = SimpleValue.of("Desktop", "WORKSTATION");
    SimpleValue laptop = SimpleValue.of("Laptop", "PORTABLE");
    SimpleValue tablet = SimpleValue.of("Tablet", "TOUCH_ENABLED");
    SimpleValue mobile = SimpleValue.of("Mobile", "HANDHELD");

    // Define device-resolution compatibility rules
    List<CompatibilityPredicate> deviceRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with device and resolution
              if (!(v1.getParentParameter().getName().equals("deviceType")
                  && v2.getParentParameter().getName().equals("screenResolution"))) {
                return true;
              }

              // Mobile devices don't support 4K
              if (v1.getName().equals("Mobile")) {
                return !v2.getName().equals("4K");
              }

              // Tablets don't use HD Ready
              if (v1.getName().equals("Tablet")) {
                return !v2.getName().equals("HD Ready");
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
      EquivalencePartition deviceValue = combination.getValue(0);
      EquivalencePartition resolutionValue = combination.getValue(1);

      if (deviceValue.getName().equals("Mobile")) {
        assertFalse(
            resolutionValue.getName().equals("4K"), "Mobile devices should not use 4K resolution");
      }
      if (deviceValue.getName().equals("Tablet")) {
        assertFalse(
            resolutionValue.getName().equals("HD Ready"),
            "Tablets should not use HD Ready resolution");
      }
    }
  }

  @Test
  public void testPerformanceScenarios() {
    // Load conditions
    SimpleValue lightLoad = SimpleValue.of("Light", "100_USERS");
    SimpleValue mediumLoad = SimpleValue.of("Medium", "1000_USERS");
    SimpleValue heavyLoad = SimpleValue.of("Heavy", "10000_USERS");
    SimpleValue peakLoad = SimpleValue.of("Peak", "100000_USERS");

    // Cache status
    SimpleValue warmCache = SimpleValue.of("Warm", "FULLY_POPULATED");
    SimpleValue coldCache = SimpleValue.of("Cold", "EMPTY");
    SimpleValue partialCache = SimpleValue.of("Partial", "PARTIALLY_POPULATED");

    // Define performance compatibility rules
    List<CompatibilityPredicate> performanceRules =
        Arrays.asList(
            (v1, v2) -> {
              // Only apply rules if we're dealing with load and network conditions
              if (!(v1.getParentParameter().getName().equals("loadCondition")
                  && v2.getParentParameter().getName().equals("networkCondition"))) {
                return true;
              }

              // Peak load requires high-speed networks
              if (v1.getName().equals("Peak")) {
                return v2.getName().equals("5G") || v2.getName().equals("Fiber");
              }

              // Heavy load doesn't work well with 3G
              if (v1.getName().equals("Heavy")) {
                return !v2.getName().equals("3G");
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
      EquivalencePartition loadValue = combination.getValue(0);
      EquivalencePartition networkValue = combination.getValue(1);

      if (loadValue.getName().equals("Peak")) {
        assertTrue(
            networkValue.getName().equals("5G") || networkValue.getName().equals("Fiber"),
            "Peak load should only use high-speed networks");
      }

      if (loadValue.getName().equals("Heavy")) {
        assertFalse(networkValue.getName().equals("3G"), "Heavy load should not use 3G network");
      }
    }
  }
}
