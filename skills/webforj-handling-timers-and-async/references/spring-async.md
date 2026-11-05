# Spring `@Async` services

When a webforJ Spring app moves a long task to a Spring service, the service runs on Spring's thread pool. That pool is shared by every user of the app, so a pool thread cannot know which user's UI to update. The view reads the user's `Environment` with `Environment.getCurrent()` in the handler that starts the task, and every UI update from inside the service callbacks wraps in `Environment.runLater(env, ...)` with it. Direct component access from `@Async` methods always fails, and `Environment.getCurrent()` returns `null` on a pool thread.

## Imports

```java
import com.webforj.Environment;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
```

## Activate Spring async

Activate Spring's async infrastructure on the app class:

```java
@SpringBootApplication
@EnableAsync
@Routify(packages = { "com.example.views" })
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```

## Async service

A service method marked `@Async` returns `CompletableFuture` and accepts a progress callback that is invoked from the background thread:

```java
@Service
public class BackgroundService {

  @Async
  public CompletableFuture<String> performLongRunningTask(Consumer<Integer> progressCallback) {
    try {
      for (int i = 0; i <= 10; i++) {
          // Report progress
          int progress = i * 10;
          if (progressCallback != null) {
              progressCallback.accept(progress);
          }

          // Simulate work
          Thread.sleep(500);
      }

      return CompletableFuture.completedFuture(
        "Task completed successfully from background service!");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return CompletableFuture.failedFuture(e);
    }
  }
}
```

## View wiring

In the view, the click handler reads the `Environment` first. Every UI update inside callbacks (the progress callback AND `whenComplete`) wraps in `Environment.runLater(env, ...)`:

```java
@Route("/")
public class HelloWorldView extends Composite<FlexLayout> {
  private Button asyncBtn = new Button("Start Background Task");
  private ProgressBar progressBar = new ProgressBar();
  private CompletableFuture<String> currentTask;

  public HelloWorldView(BackgroundService backgroundService) {
    // Service is injected by Spring
    asyncBtn.addClickListener(e -> {
      // The click handler runs on this user's UI thread
      Environment env = Environment.getCurrent();

      currentTask = backgroundService.performLongRunningTask(progress -> {
        Environment.runLater(env, () -> {
          progressBar.setValue(progress);
        });
      });
    });
  }
}
```

## Completion handling

The `whenComplete` callback also runs on a background thread, so the same rule applies, with the same `env`:

```java
currentTask.whenComplete((result, error) -> {
  Environment.runLater(env, () -> {
    asyncBtn.setEnabled(true);
    progressBar.setVisible(false);
    if (error != null) {
      Toast.show("Task failed: " + error.getMessage(), Theme.DANGER);
    } else {
      Toast.show(result, Theme.SUCCESS);
    }
  });
});
```

Without `Environment.runLater`, webforJ throws because background threads can't access UI components. With the one-argument `Environment.runLater(task)` (deprecated since 26.10) the update lands in the session of whichever user first used that pool thread, which is the wrong browser once the app has two users.

## Cancellation on destroy

Cancel any running task when the view is destroyed. `cancel(true)` interrupts the worker thread, which causes blocking calls like `Thread.sleep` to throw `InterruptedException` so the service can exit cleanly:

```java
@Override
protected void onDestroy() {
  // Cancel the task if view is destroyed
  if (currentTask != null && !currentTask.isDone()) {
    currentTask.cancel(true);
  }
}
```
