# 🎭 Playwright Resource Manager

This library is dedicated for testing projects utilizing Microsoft's Playwright for Java automation tool. It provides easy
way to manage Playwright resources in a thread safe manner.

## Installation

Add following repository in your settings.xml file

```xml
<repository>
  <id>github</id>
  <url>https://maven.pkg.github.com/rohit-walia/playwright-manager</url>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```

Paste Maven dependency in your pom.xml file and add `playwright.manager.version` in your properties.
This property should contain the latest version of this library. Lastly, run `mvn install`.

```xml
<dependency>
  <groupId>org.playwright</groupId>
  <artifactId>playwright</artifactId>
  <version>${playwright.manager.version}</version>
</dependency>
```

## Examples

#### Managing resources with default options

This code snippet shows how you can manage Playwright resources easily using the PlaywrightResourceManager.
By default, resources will be created with the default Option. See package [here](playwright/src/main/java/org/playwright/core/options)
for available options.

The BrowserContext resource is always created as a new instance.
The Browser and Playwright instances are reused if there is one already existing (default behavior). 
However this can be overridden by passing ResourceOptionArg.NEW_BROWSER_INSTANCE or ResourceOptionArg.NEW_PLAYWRIGHT_INSTANCE
as arguments to the create() method.

```Java
@Test
void resourcesWithDefaultOptions {
  // create resources
  Playwright playwright = PlaywrightResourceFactory.create(PlaywrightResource.PLAYWRIGHT);
  Browser browser = PlaywrightResourceFactory.create(PlaywrightResource.BROWSER);
  BrowserContext browserContext = PlaywrightResourceFactory.create(PlaywrightResource.BROWSER_CONTEXT);

  // open browser and do things
  Page page = browserContext.newPage();
  page.navigate("https://github.com/rohit-walia/playwright-manager");

  // close resources
  PlaywrightResourceFactory.close(browserContext);
  PlaywrightResourceFactory.close(browser);
  PlaywrightResourceFactory.close(playwright);
}
```

#### Managing resources with custom options

You can pass arguments to the create() method to customize how you want to create these resources. Every
Option is a Builder Object.

```Java
@Test
void resourcesWithCustomOptions {
  Playwright playwright = PlaywrightResourceFactory.create(PlaywrightResource.PLAYWRIGHT, PlaywrightOption.builder()
    .enableDebugMode(true)
    .build());

  Browser browser = PlaywrightResourceFactory.create(PlaywrightResource.BROWSER, BrowserLaunchOption.builder()
    .headless(false)
    .slowmo(300)
    .browser("chrome")
    .browserStartTimeout(30000)
    .build());

  BrowserContext browserContext = PlaywrightResourceFactory.create(PlaywrightResource.BROWSER_CONTEXT,
    BrowserContextOption.builder().recordVideoDir("target/video").build());
}
```

# Tools, libraries, and technologies

### Java17

This project is currently using Java v17. See [here](https://www.oracle.com/java/technologies/downloads/#java17) for Java v17
JDK binaries. JDK 17 binaries are free to use in production and free to redistribute, at no cost, under the 
Oracle No-Fee Terms and Conditions (NFTC).

### Junit5

Since Playwright for Java does not come bundled with a default test runner, this project is utilizing Junit5 as its test runner.
TestNG is a good alternative test runner as well. See [here](https://playwright.dev/java/docs/test-runners) for more information
on Playwright Test Runners.

### Lombok

This project uses lombok to decrease boilerplate code. If you are using Intellij please install the Lombok Plugin. If
you are using Eclipse STS follow the instructions [here](https://projectlombok.org/setup/eclipse).
If you are using another IDE you can see if it is supported on the Lombok website [here](https://projectlombok.org).

### Code Quality

As part of the build, there are several code quality checks running against the code base. All code quality files can be
found in the root of the project under the [codequality](.codequality) directory.

#### CheckStyle

The project runs checkstyle plugin to validate java code formatting and enforce best coding standards.

#### PMD

The project runs PMD code analysis to find common programming flaws like unused variables, empty catch blocks, unnecessary
object creation, and etc...
