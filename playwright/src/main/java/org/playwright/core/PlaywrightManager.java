package org.playwright.core;

import static org.playwright.utils.ObjUtils.getFromArray;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import lombok.SneakyThrows;
import org.failsafe.failsafe.retry.RetryAgain;
import org.playwright.common.OptionCtx;
import org.playwright.common.PlaywrightResource;
import org.playwright.common.ResourceOptionArg;
import org.playwright.common.Timeout;
import org.playwright.core.options.BrowserContextOption;
import org.playwright.core.options.BrowserLaunchOption;
import org.playwright.core.options.PlaywrightOption;
import org.playwright.core.options.TracingStartOption;
import org.playwright.core.options.TracingStopOption;
import org.playwright.utils.ObjUtils;

import java.util.List;
import java.util.Optional;

/**
 * Abstract factory interface for managing Playwright resources.
 */
public interface PlaywrightManager {

  org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlaywrightManager.class);

  /**
   * Create Playwright resources which includes: Playwright, Browser, & BrowserContext. <br><br>
   *
   * <p>By default, resources will be created with default Options. See package {@link org.playwright.core.options} for
   * available options.</p><br>
   *
   * <p>
   * The BrowserContext resource is always created as a new instance. <br>
   * The Browser and Playwright instances are reused if there is one already existing (default behavior). However this
   * can be overridden by passing ResourceOptionArg.NEW_BROWSER_INSTANCE or ResourceOptionArg.NEW_PLAYWRIGHT_INSTANCE</p><br>
   *
   * <p>The arguments passed to create() are optional and can be in any order. In addition to passing
   * ResourceOptionArg, any of the resource Option classes that implements IOption can be passed as arguments.
   * This will override the default options.</p>
   *
   * @param resource Playwright resource enum
   * @param args     Optional arguments for resource creation
   * @return Playwright resource
   */
  @SuppressWarnings("unchecked")
  static <T extends AutoCloseable> T create(PlaywrightResource resource, Object... args) {
    return switch (resource) {
      case PLAYWRIGHT -> (T) createPlaywright(args);
      case BROWSER -> (T) createBrowser(args);
      case BROWSER_CONTEXT -> (T) createBrowserContext(args);
    };
  }

  /**
   * Get Playwright resources. If multiple resources of the same type are created, return the last created resource.
   *
   * @param resource Playwright resource enum
   * @return Playwright resource
   */
  @SuppressWarnings("unchecked")
  static <T extends AutoCloseable> Optional<T> get(PlaywrightResource resource) {
    return switch (resource) {
      case PLAYWRIGHT -> Optional.ofNullable((T) PlaywrightSingleton.getInstance());
      case BROWSER -> Optional.ofNullable((T) BrowserSingleton.getInstance());
      case BROWSER_CONTEXT -> {
        if (BrowserSingleton.getInstance() == null || BrowserSingleton.getInstance().contexts().isEmpty()) {
          yield Optional.empty();
        }
        List<BrowserContext> contexts = BrowserSingleton.getInstance().contexts();
        yield Optional.of((T) contexts.get(contexts.size() - 1));
      }
    };
  }

  /**
   * Close Playwright resource and relinquishing any underlying resources. This also removes the resource instance and
   * OptionContext so invoking PlaywrightManager#get or OptionCtx#getContext after closing resource will not return anything.
   *
   * @param object resource
   * @param args   Optional arguments
   */
  @SneakyThrows
  static <T extends AutoCloseable> void close(T object, Object... args) {
    if (object instanceof BrowserContext) {
      TracingStopOption tracingStopOption = getFromArray(args, TracingStopOption.class).orElse(
          OptionCtx.exists(OptionCtx.Key.TRACE_STOP_OPTION)
              ? (TracingStopOption) OptionCtx.getContext().get(OptionCtx.Key.TRACE_STOP_OPTION)
              : TracingStopOption.builder().build());

      ((BrowserContext) object).tracing().stop(tracingStopOption.forPlaywright());
      OptionCtx.remove(OptionCtx.Key.BROWSER_CONTEXT_OPTION);
    }
    if (object instanceof Browser) {
      OptionCtx.remove(OptionCtx.Key.BROWSER_LAUNCH_OPTION);
      BrowserSingleton.removeInstance();
    }
    if (object instanceof Playwright) {
      OptionCtx.remove(OptionCtx.Key.PLAYWRIGHT_OPTION);
      PlaywrightSingleton.removeInstance();
    }
    object.close();
  }

  private static Playwright createPlaywright(Object[] args) {
    List<ResourceOptionArg> argsList = ObjUtils.filterFromArray(args, ResourceOptionArg.class);

    // handle creating Playwright instance when one already active
    if (PlaywrightSingleton.getInstance() != null) {
      if (argsList.isEmpty()) {
        log.info("Existing Playwright connection already active. Returning existing instance.");
        return PlaywrightSingleton.getInstance();
      }
      if (!argsList.contains(ResourceOptionArg.NEW_PLAYWRIGHT_INSTANCE)) {
        throw new IllegalArgumentException("Invalid argument passed " + argsList);
      }
      log.warn("You are creating more than one Playwright instance.");
    }

    log.info("Creating Playwright resource...");

    PlaywrightOption options = getFromArray(args, PlaywrightOption.class).orElseGet(() -> {
      if (OptionCtx.exists(OptionCtx.Key.PLAYWRIGHT_OPTION)) {
        log.info("Reusing existing PlaywrightOptions.");
        return (PlaywrightOption) OptionCtx.getContext().get(OptionCtx.Key.PLAYWRIGHT_OPTION);
      }
      log.info("Using default PlaywrightOptions.");
      return PlaywrightOption.builder().build();
    });

    // failsafe retry put in place to avoid rare occurrence of playwright driver failing to initialize at Runtime.
    RetryAgain.onceWithDelay(() -> PlaywrightSingleton.setInstance(Playwright.create(options.forPlaywright())),
        Timeout.FIVE.getSecond());

    OptionCtx.add(OptionCtx.Key.PLAYWRIGHT_OPTION, options);

    return PlaywrightSingleton.getInstance();
  }

  private static Browser createBrowser(Object[] args) {
    List<ResourceOptionArg> argsList = ObjUtils.filterFromArray(args, ResourceOptionArg.class);

    // handle creating Browser instance when one already active
    if (BrowserSingleton.getInstance() != null) {
      if (argsList.isEmpty()) {
        log.info("Existing Browser connection already active. Returning existing instance.");
        return BrowserSingleton.getInstance();
      }
      if (!argsList.contains(ResourceOptionArg.NEW_BROWSER_INSTANCE)) {
        throw new IllegalArgumentException("Invalid argument passed " + argsList);
      }
      log.info("You are creating more than one Browser instance.");
    }

    if (PlaywrightSingleton.getInstance() == null) {
      throw new PlaywrightException("Playwright instance is not initialized. Please initialize Playwright before "
          + "creating a Browser.");
    }
    Playwright playwright = PlaywrightSingleton.getInstance();

    BrowserLaunchOption options = getFromArray(args, BrowserLaunchOption.class).orElseGet(() -> {
      if (OptionCtx.exists(OptionCtx.Key.BROWSER_LAUNCH_OPTION)) {
        log.info("Reusing existing BrowserLaunchOptions.");
        return (BrowserLaunchOption) OptionCtx.getContext().get(OptionCtx.Key.BROWSER_LAUNCH_OPTION);
      }
      log.info("Using default BrowserLaunchOptions.");
      return BrowserLaunchOption.builder().build();
    });

    Browser browser = switch (options.getBrowser()) {
      case "chromium", "chrome", "msedge" -> playwright.chromium().launch(options.forPlaywright());
      case "firefox" -> playwright.firefox().launch(options.forPlaywright());
      case "webkit" -> playwright.webkit().launch(options.forPlaywright());
      default -> throw new PlaywrightException("Unsupported browser: " + options.getBrowser());
    };

    BrowserSingleton.setInstance(browser);
    OptionCtx.add(OptionCtx.Key.BROWSER_LAUNCH_OPTION, options);
    return BrowserSingleton.getInstance();
  }

  private static BrowserContext createBrowserContext(Object[] args) {
    BrowserContextOption browserContextOption = getFromArray(args, BrowserContextOption.class).orElse(
        OptionCtx.exists(OptionCtx.Key.BROWSER_CONTEXT_OPTION)
            ? (BrowserContextOption) OptionCtx.getContext().get(OptionCtx.Key.BROWSER_CONTEXT_OPTION)
            : BrowserContextOption.builder().build());

    TracingStartOption tracingStartOption = getFromArray(args, TracingStartOption.class).orElse(
        OptionCtx.exists(OptionCtx.Key.TRACE_START_OPTION)
            ? (TracingStartOption) OptionCtx.getContext().get(OptionCtx.Key.TRACE_START_OPTION)
            : TracingStartOption.builder().build());

    if (BrowserSingleton.getInstance() == null) {
      throw new PlaywrightException("Browser instance is not initialized. Please initialize Browser before "
          + "creating a BrowserContext.");
    }

    BrowserContext browserCtx = BrowserSingleton.getInstance().newContext(browserContextOption.forPlaywright());
    browserCtx.tracing().start(tracingStartOption.forPlaywright());

    OptionCtx.add(OptionCtx.Key.BROWSER_CONTEXT_OPTION, browserContextOption);
    OptionCtx.add(OptionCtx.Key.TRACE_START_OPTION, tracingStartOption);

    return browserCtx;
  }
}
