# 🎭 Playwright Manager

The **PlaywrightManager** provides a convenient way to create, customize, and manage
[Playwright resources](playwright/src/main/java/org/playwright/common/PlaywrightResource.java). Focus more on writing your
test code and less on
managing the underlying Playwright resources.

## Table of Contents

- [Installation](#installation)
- [Usage Examples](#usage-examples)
    - [Creating resources with default options](#creating-resources-with-default-options)
    - [Creating resources with custom options](#creating-resources-with-custom-options)
    - [Simulating multi-browser scenario](#simulating-multi-browser-scenario)
    - [Running tests in parallel](#running-tests-in-parallel)

## Installation

To use this library in your project, add below Maven dependency to your pom.xml file. Make sure to use the
latest version available. This project is deployed to both
the [Maven Central Repository](https://central.sonatype.com/artifact/io.github.rohit-walia/playwright) and
the [GitHub Package Registry](https://github.com/rohit-walia?tab=packages&repo_name=playwright-manager)

```xml

<dependency>
    <groupId>io.github.rohit-walia</groupId>
    <artifactId>playwright</artifactId>
    <version>${playwright-manager.version}</version>
</dependency>
```

## Usage Examples

#### Creating resources with default options

Below example demonstrates how you can create Playwright resources by invoking the create() function, automate browser
actions like navigating to a URL, and finally closing those resources.

```Java
void test() {
  //create resources w/ default options
  Playwright playwright = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);
  Browser browser = PlaywrightManager.create(PlaywrightResource.BROWSER);
  BrowserContext browserContext = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT);

  //open browser page and execute automation
  Page page = browserContext.newPage();
  page.navigate("https://github.com/rohit-walia/playwright-manager");

  //remember to close the resources when you are finished with them!
  PlaywrightManager.close(browserContext);
  PlaywrightManager.close(browser);
  PlaywrightManager.close(playwright);
}
```

#### Creating resources with custom options

You can pass [Options](playwright/src/main/java/org/playwright/core/options) to the create() function to customize these
resources. This gives users more control over the behavior of Playwright resources.

```Java
void test() {
  //enable debug mode and verbose api logging
  PlaywrightOption playwrightOption = PlaywrightOption.builder()
      .enableDebugMode(true)
      .enableVerboseApiLogs(true)
      .build();

  Playwright playwright = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT, playwrightOption);

  //custom BrowserLaunchOption
  BrowserLaunchOption browserLaunchOption = BrowserLaunchOption.builder()
      .headless(false)
      .slowmo(300)
      .browser("chrome")
      .browserStartTimeout(30000)
      .build();

  //create Browser w/ custom BrowserLaunchOption
  Browser browser = PlaywrightManager.create(PlaywrightResource.BROWSER, browserLaunchOption);

  //create BrowserContext w/ custom BrowserContextOption
  BrowserContextOption browserContextOption = BrowserContextOption.builder().recordVideoDir("target/video").build();
  BrowserContext browserContext = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT, browserContextOption);

  //remember to close the resources when you are finished with them!
  PlaywrightManager.close(browserContext);
  PlaywrightManager.close(browser);
  PlaywrightManager.close(playwright);
}
```

#### Simulating multi-browser scenario

In some cases, you might need your automation to spawn multiple browsers. Since BrowserContext instances are isolated and
don’t share cookies/cache with each other, creating multiple BrowserContexts is one way to approach these types of scenarios.

Alternatively, you can also create multiple Browser instances and then create multiple BrowserContext per Browser instance.

```Java
void test() {
  Playwright playwright = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);
  Browser browser = PlaywrightManager.create(PlaywrightResource.BROWSER);

  //create multiple BrowserContext
  BrowserContext browserContext1 = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT);
  BrowserContext browserContext2 = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT);

  //browser1 will navigate to github
  Page page1 = browserContext1.newPage();
  page1.navigate("https://github.com/rohit-walia/playwright-manager");

  //browser2 will navigate to maven
  Page page2 = browserContext2.newPage();
  page2.navigate("https://central.sonatype.com/artifact/io.github.rohit-walia/playwright");

  //remember to close the resources when you are finished with them!
  PlaywrightManager.close(browserContext1);
  PlaywrightManager.close(browserContext2);
  PlaywrightManager.close(browser);
  PlaywrightManager.close(playwright);
}
```

In addition to passing Options, the create() function can also consume other resource instances.

```Java
void test() {
  //create two Playwright connections
  Playwright playwright1 = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);
  Playwright playwright2 = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);

  //create two Browsers
  Browser browser1 = PlaywrightManager.create(PlaywrightResource.BROWSER, playwright1);
  Browser browser2 = PlaywrightManager.create(PlaywrightResource.BROWSER, playwright2);

  //create BrowserContext for each Browser instance
  BrowserContext browserContext1 = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT, browser1);
  BrowserContext browserContext2 = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT, browser2);

  //remember to close the resources when you are finished with them!
  PlaywrightManager.close(playwright2);
  PlaywrightManager.close(browserContext2);
  PlaywrightManager.close(browser2);

  PlaywrightManager.close(browserContext1);
  PlaywrightManager.close(browser1);
  PlaywrightManager.close(playwright1);
}
```

#### Running tests in parallel

Playwright for Java, out of the box, is not thread safe.

All Playwright resources are expected to be called on the same thread where the Playwright object was created or proper
synchronization should be implemented to ensure only one thread calls Playwright resources at any given time More
details [here](https://playwright.dev/java/docs/multithreading)

This library also provides proper resource synchronication! With this library, go ahead and configure your test runner for
parallel execution :)


# Dependencies

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