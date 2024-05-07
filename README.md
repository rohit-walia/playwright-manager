# 🎭 Playwright Resource Manager

The Playwright Resource Manager is a library dedicated to testing-oriented projects that utilize Microsoft's
**Playwright _for_ Java** automation tool. It provides a convenient way to manage Playwright resources, making it easier to
work with Playwright in your testing projects. The `PlaywrightManager` class is specifically designed to help you efficiently
manage the lifecycle of Playwright resources.

With PlaywrightManager, you can easily create, reuse, and close
[Playwright resources](playwright/src/main/java/org/playwright/common/PlaywrightResource.java). These resources can be
created with default options or customized to your specific configuration. Focus more on writing your test code and less on
managing the underlying Playwright resources.

## Installation

To use this library in your project, add below Maven dependency to your pom.xml file. Make sure to use the
latest version available. This project is deployed to both
the [Maven Central Repository](https://central.sonatype.com/artifact/io.github.rohit-walia/playwright) and
the[GitHub Package Registry](https://github.com/rohit-walia?tab=packages&repo_name=playwright-manager)

```xml

<dependency>
    <groupId>io.github.rohit-walia</groupId>
    <artifactId>playwright</artifactId>
    <version>${playwright-manager.version}</version>
</dependency>
```

## Usage Examples

#### Creating resources with default options

This code snippet shows how you can create Playwright resources using the **PlaywrightManager**. By default, resources will
be created with the default Option. See package [here](playwright/src/main/java/org/playwright/core/options) for available
options.

The BrowserContext resource is always created as a new instance. The Browser and Playwright instances are reused if there is
one already existing (default behavior). However, this can be overridden by passing ResourceOptionArg.NEW_BROWSER_INSTANCE or
ResourceOptionArg.NEW_PLAYWRIGHT_INSTANCE as arguments to the create() method.

```Java
void resourcesWithDefaultOptions() {
  // create resources w/ default options
  Playwright playwright = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);
  Browser browser = PlaywrightManager.create(PlaywrightResource.BROWSER);
  BrowserContext browserContext = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT);

  // open browser page and execute automation
  Page page = browserContext.newPage();
  page.navigate("https://github.com/rohit-walia/playwright-manager");

  // remember to close the resources when you are finished with them!
  PlaywrightManager.close(browserContext);
  PlaywrightManager.close(browser);
  PlaywrightManager.close(playwright);
}
```

#### Creating resources with custom options

You can pass arguments to the create() method to customize how you want to create these resources. Every Option is a Builder
Object.

```Java
void resourcesWithCustomOptions() {
  // enable debug mode and verbose api logging
  PlaywrightOption playwrightOption = PlaywrightOption.builder()
      .enableDebugMode(true)
      .enableVerboseApiLogs(true)
      .build();

  Playwright playwright = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT, playwrightOption);

  // custom BrowserLaunchOption
  BrowserLaunchOption browserLaunchOption = BrowserLaunchOption.builder()
      .headless(false)
      .slowmo(300)
      .browser("chrome")
      .browserStartTimeout(30000)
      .build();

  // create Browser w/ custom BrowserLaunchOption
  Browser browser = PlaywrightManager.create(PlaywrightResource.BROWSER, browserLaunchOption);

  // create BrowserContext w/ custom BrowserContextOption
  BrowserContextOption browserContextOption = BrowserContextOption.builder().recordVideoDir("target/video").build();
  BrowserContext browserContext = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT, browserContextOption);
}
```

# Tools, libraries, and technologies

### JUnit5

This project uses JUnit5 for testing. Tests can be found [here](playwright/src/test/java/org/playwright).
See [here](https://junit.org/junit5/docs/current/user-guide/) for more information on JUnit5.

### Lombok

This project uses lombok to decrease boilerplate code. If you are using Intellij please install the Lombok Plugin. If
you are using Eclipse STS follow the instructions [here](https://projectlombok.org/setup/eclipse).
If you are using another IDE you can see if it is supported on the Lombok website [here](https://projectlombok.org).

### Failsafe

This project is leveraging the [Failsafe Helper](https://github.com/rohit-walia/failsafe-helper) library for its error
handling, retry and fallback capabilities.

### Jackson

This project is leveraging the [Jackson Helper](https://github.com/rohit-walia/jackson-helper) library for its convenient
serialization and deserialization capabilities.

### Code Quality

As part of the build, there are several code quality checks running against the code base. All code quality files can be
found in the root of the project under the [codequality](.codequality) directory.

#### CheckStyle
