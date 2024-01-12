package org.playwright.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import lombok.SneakyThrows;
import org.playwright.common.OptionCtx;
import org.playwright.common.PlaywrightResource;
import org.playwright.common.ResourceOptionArg;
import org.playwright.core.options.BrowserContextOption;
import org.playwright.core.options.BrowserLaunchOption;
import org.playwright.core.options.PlaywrightOption;
import org.playwright.core.options.TracingStartOption;
import org.playwright.core.options.TracingStopOption;
import org.playwright.failsafe.FailsafeRetry;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract factory interface for managing Playwright resources.
 */
public interface PlaywrightResourceFactory {

  /**
   * Create Playwright resources which includes: Playwright, Browser, BrowserContext. <br><br>
   *
   * <p>
   * By default, resources will be created with the default Option. See package {@link org.playwright.core.options} for
   * available options. <br>
   *
   * The BrowserContext resource is always created as a new instance. <br>
   * The Browser and Playwright instances are reused if there is one already existing (default behavior). However this
   * can be overridden by passing ResourceOptionArg.NEW_BROWSER_INSTANCE or ResourceOptionArg.NEW_PLAYWRIGHT_INSTANCE</p><br>
   *
   * <p>The arguments passed to create() are optional and can be in any order. In addition to passing
   * ResourceOptionArg constants, any of the resource Option classes that implements IOption can be passed as arguments.
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
   * Close Playwright resource.
   *
   * @param object resource
   * @param args   Optional arguments
   */
  @SneakyThrows
  static <T extends AutoCloseable> void close(T object, Object... args) {
    if (object instanceof BrowserContext) {
      TracingStopOption tracingStopOption = Arrays.stream(args)
          .filter(arg -> arg instanceof TracingStopOption)
          .map(arg -> (TracingStopOption) arg)
          .findFirst()
          .orElse(OptionCtx.exists(OptionCtx.Key.TRACE_STOP_OPTION)
              ? (TracingStopOption) OptionCtx.getContext().get(OptionCtx.Key.TRACE_STOP_OPTION)
              : TracingStopOption.builder().build());

      ((BrowserContext) object).tracing().stop(tracingStopOption.forPlaywright());
    }
    object.close();
  }

  private static Playwright createPlaywright(Object[] args) {
    List<ResourceOptionArg> argsList = Arrays.stream(args)
        .filter(arg -> arg instanceof ResourceOptionArg)
        .map(arg -> (ResourceOptionArg) arg)
        .toList();

    if (PlaywrightSingleton.getInstance() != null && !argsList.contains(ResourceOptionArg.NEW_PLAYWRIGHT_INSTANCE)) {
      return PlaywrightSingleton.getInstance();
    }

    PlaywrightOption options = Arrays.stream(args)
        .filter(arg -> arg instanceof PlaywrightOption)
        .map(arg -> (PlaywrightOption) arg)
        .findFirst()
        .orElse(OptionCtx.exists(OptionCtx.Key.PLAYWRIGHT_OPTION)
            ? (PlaywrightOption) OptionCtx.getContext().get(OptionCtx.Key.PLAYWRIGHT_OPTION)
            : PlaywrightOption.builder().build());

    // failsafe retry put in place to avoid rare occurrence of playwright driver failing to initialize at Runtime.
    FailsafeRetry.tryAgain(() -> PlaywrightSingleton.setInstance(Playwright.create(options.forPlaywright())), 5, 1);

    OptionCtx.add(OptionCtx.Key.PLAYWRIGHT_OPTION, options);

    return PlaywrightSingleton.getInstance();
  }

  private static Browser createBrowser(Object[] args) {
    List<ResourceOptionArg> argsList = Arrays.stream(args)
        .filter(arg -> arg instanceof ResourceOptionArg)
        .map(arg -> (ResourceOptionArg) arg)
        .toList();

    if (BrowserSingleton.getInstance() != null && !argsList.contains(ResourceOptionArg.NEW_BROWSER_INSTANCE)) {
      return BrowserSingleton.getInstance();
    }

    if (PlaywrightSingleton.getInstance() == null) {
      throw new PlaywrightException("Playwright instance is not initialized. Please initialize Playwright before "
          + "creating a Browser.");
    }
    Playwright playwright = PlaywrightSingleton.getInstance();

    BrowserLaunchOption options = Arrays.stream(args)
        .filter(arg -> arg instanceof BrowserLaunchOption)
        .map(arg -> (BrowserLaunchOption) arg)
        .findFirst()
        .orElse(OptionCtx.exists(OptionCtx.Key.BROWSER_LAUNCH_OPTION)
            ? (BrowserLaunchOption) OptionCtx.getContext().get(OptionCtx.Key.BROWSER_LAUNCH_OPTION)
            : BrowserLaunchOption.builder().build());

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
    BrowserContextOption browserContextOption = Arrays.stream(args)
        .filter(arg -> arg instanceof BrowserContextOption)
        .map(arg -> (BrowserContextOption) arg)
        .findFirst()
        .orElse(OptionCtx.exists(OptionCtx.Key.BROWSER_CONTEXT_OPTION)
            ? (BrowserContextOption) OptionCtx.getContext().get(OptionCtx.Key.BROWSER_CONTEXT_OPTION)
            : BrowserContextOption.builder().build());

    TracingStartOption tracingStartOption = Arrays.stream(args)
        .filter(arg -> arg instanceof TracingStartOption)
        .map(arg -> (TracingStartOption) arg)
        .findFirst()
        .orElse(OptionCtx.exists(OptionCtx.Key.TRACE_START_OPTION)
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
