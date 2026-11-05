---
name: webforj-handling-timers-and-async
description: "Schedules timers, periodic intervals, debounced actions, and background async work in a webforJ app, using the framework's thread-safe primitives (`Interval`, `Debouncer`, `Environment.runLater`, `PendingResult`) instead of raw `java.util.Timer` or new threads. Use whenever the user asks for periodic tasks, polling, scheduled updates, debouncing, search-as-you-type, throttling, background jobs, async work, long-running tasks, progress bars updated from a worker thread, Spring `@Async`, or any time the user reaches for `java.util.Timer`, `javax.swing.Timer`, plain `Thread`, `ExecutorService`, or `CompletableFuture` from inside a webforJ component. Use this skill BEFORE introducing any raw timer or thread, background threads cannot touch UI components and must hand updates to `Environment.runLater(env, ...)` with the `Environment` read on the UI thread."
---

# Handling Timers and Async in webforJ

webforJ runs each user session on its own UI thread, the `Environment` thread. Anything that touches a UI component (set text, set value, open a dialog, append a child) must run on that thread. Background work has no `Environment` of its own: a pool thread serves every user of the app, so it cannot tell which session to update. The view reads `Environment.getCurrent()` while it is still on the UI thread, hands that `Environment` to the work, and the work calls `Environment.runLater(env, ...)`. Use the four documented primitives below.

## Authoritative imports

```java
import com.webforj.Debouncer;
import com.webforj.Environment;
import com.webforj.Interval;
import com.webforj.PendingResult;
```

For Spring async work, also see [references/spring-async.md](references/spring-async.md).

## Banned mechanisms

These are NOT alternatives to the documented primitives:

| Banned | Why |
|---|---|
| `Environment.runLater(task)` and `Environment.runLater(supplier)` without an `Environment` argument | Deprecated since 26.10. They guess the session from the thread that created the calling thread. On a shared pool (Spring `@Async`, `ForkJoinPool.commonPool()`, any app-wide executor) every task lands in the session of the user whose call created that pool thread, silently, in another user's browser |
| `Environment.getCurrent()` inside background work | Returns `null` on any thread that is not a session's UI thread. The `Environment` is read on the UI thread and passed in |
| `java.util.Timer` / `TimerTask`, `javax.swing.Timer` | A timer thread per user does not scale and has no view lifecycle. Periodic work is `Interval`, delayed work is `Debouncer` |
| `new Thread(...)` per click or per user | A thread per user exhausts the server. Long work goes to a managed executor |

If the user's prompt or existing code uses any of the above to drive UI work, rewrite the work onto one of the four primitives in the next section. An external library callback (an HTTP client's `thenAccept`, a reactive subscriber, a Spring pool thread) is fine as long as it calls `Environment.runLater(env, ...)` with an `Environment` captured on the UI thread.

Projects below webforJ 26.02 have no `runLater(Environment, ...)` overload. There the one-argument form is only safe on a thread the view itself created and uses for one session, never on a shared pool. Recommend upgrading before adding background work to such a project.

## Pick your primitive

| Need | Primitive | Section |
|---|---|---|
| Repeating action at a fixed cadence (poll, refresh, rotate, inactivity check) | `Interval` | [Periodic tasks](#periodic-tasks-interval) |
| Single action after a quiet period (search-as-you-type, validation) | `Debouncer` | [Debouncing](#debouncing-debouncer) |
| Long CPU or I/O work that updates the UI as it runs | `Environment.runLater(env, ...)` from a managed `ExecutorService` | [Worker thread updates](#worker-thread-updates-environmentrunlater) |
| Long task hosted by a Spring `@Service` | Spring `@Async` + `Environment.runLater(env, ...)` in callbacks | [references/spring-async.md](references/spring-async.md) |
| Pure client-side debounce on a DOM event (no server round-trip) | `ElementEventOptions.setDebounce(ms)` | inside [Debouncing](#debouncing-debouncer) |

## Periodic tasks: `Interval`

Use for: polling, live data refresh, content rotation, inactivity dialogs. `Interval` runs on the UI thread, so listener bodies can touch components directly.

Steps:

```
- [ ] 1. Construct with the delay in seconds (float) and the elapsed listener
- [ ] 2. Call interval.start() to activate
- [ ] 3. (Optional) add more listeners or change the delay
- [ ] 4. Stop in onDestroy / onDidDestroy
```

### 1. Construct

```java
float dealy = 2f;

EventListener<Interval.ElapsedEvent> firstListener = (e -> {
// Executable code
});

Interval interval = new Interval(delay, firstListener);
```

The delay is in seconds and accepts fractional values. Very small delays produce a flood of events faster than the program can handle, keep the delay above a few hundred milliseconds unless you have a measured reason.

### 2. Start

`Interval` does NOT auto-start. Call `start()` once you are ready for it to fire:

```java
interval.start();
```

Stop with `stop()`, restart with `restart()`.

### 3. Optional: extra listeners and dynamic delay

Attach more listeners with `addElapsedListener(...)`:

```java
EventListener<Interval.ElapsedEvent> secondListener = (e -> {
// Executable code
});

interval.addElapsedListener(secondListener);
```

Change the delay with `setDelay(float)`, the new value applies after a stop or restart:

```java
//Changing the Delay
Interval.setDelay(2f);
Interval.restart();
```

### 4. Stop on destroy

Call `interval.stop()` from the view's destroy hook (`onDestroy` for routes, `onDidDestroy` for `Composite`). The same `stop()` API documented for stopping the timer at any time applies here.

`Interval` is the documented choice for recurring work in a webforJ app. A `java.util.Timer`, `javax.swing.Timer`, or a new `Thread` per user does not scale across many concurrent sessions and cannot update the UI safely.

## Debouncing: `Debouncer`

Use for: search-as-you-type, save-after-typing, validation, any action that should fire only after a burst of input settles. `Debouncer` runs on the UI thread via `Interval`, so the action body can touch components directly without `Environment.runLater(...)`.

Steps:

```
- [ ] 1. Construct with the delay in seconds (float)
- [ ] 2. Call debouncer.run(() -> action) from the event handler
- [ ] 3. (Optional) flush(), cancel(), isPending()
- [ ] 4. Cancel pending action in onDidDestroy
```

### 1. Construct

```java
Debouncer debounce = new Debouncer(0.3f);
```

The delay is in SECONDS as a float. `0.3f` for 300 ms, `1.5f` for 1.5 s. Not milliseconds, not int.

### 2. Trigger from the event handler

Each `run(...)` cancels the previous pending action and restarts the timer, so rapid input does not produce repeated work:

```java
textField.onModify(e -> {
  debounce.run(() -> search(textField.getText()));
});
```

### 3. Optional control

Force the pending action to run immediately:

```java
submitButton.onClick(e -> {
  debounce.flush();
  if (isValid()) {
    submitForm();
  }
});
```

Drop a pending action without running it:

```java
debounce.cancel();
```

Check whether an action is queued:

```java
if (debounce.isPending()) {
  statusLabel.setText("Processing...");
}
```

### 4. Cancel on destroy

```java
public class SearchPanel extends Composite<Div> {
  private final Debouncer debounce = new Debouncer(0.3f);

  @Override
  protected void onDidDestroy() {
    debounce.cancel();
  }
}
```

### Client-side alternative: `ElementEventOptions.setDebounce`

For a simple debounce on a single DOM event with no server round-trip, debounce client-side instead. The unit is milliseconds (int) and the handler is throttled in the browser:

```java
// Using ElementEventOptions for client-side debouncing
ElementEventOptions options = new ElementEventOptions();
options.setDebounce(300);

element.addEventListener("input", e -> {
  // This handler is debounced on the client
}, options);
```

Use `Debouncer` (server-side, programmatic, full control) when you need to cancel or flush from elsewhere. Use `ElementEventOptions.setDebounce` when you just want to throttle a DOM event.

## Worker thread updates: `Environment.runLater`

Use for: long CPU/IO work inside a view that needs to update the UI as it runs. `Environment.runLater(env, ...)` is the only safe way to update the UI from a background thread.

Two overloads, both since 26.02, both taking the target `Environment` first:

```java
// Schedule a task with no return value
public static PendingResult<Void> runLater(Environment target, Runnable task)

// Schedule a task that returns a value
public static <T> PendingResult<T> runLater(Environment target, Supplier<T> supplier)
```

Both return a `PendingResult` for tracking and cancellation. Called on the target's own UI thread the task runs at once. Called from any other thread it is queued and runs on the target's UI thread in FIFO order.

Steps:

```
- [ ] 1. Declare a per-view single-thread daemon ExecutorService
- [ ] 2. Track pending UI updates and a volatile cancellation flag
- [ ] 3. Read Environment.getCurrent() on the UI thread, then submit the work
- [ ] 4. Wrap every UI mutation inside the worker in Environment.runLater(env, ...)
- [ ] 5. Cooperatively check the cancellation flag and handle InterruptedException
- [ ] 6. Cancel and shut down on view destroy
- [ ] 7. Guard component access on the cancellation path with isDestroyed()
```

### 1. Per-view executor

Use a single-thread daemon executor. Daemon threads do not prevent JVM shutdown. A single-thread executor avoids resource exhaustion when many sessions run the same view.

```java
private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
  Thread t = new Thread(r, "LongTaskView-Worker");
  t.setDaemon(true);
  return t;
});
```

### 2. Track pending state

```java
private CompletableFuture<Void> currentTask = null;
private final List<PendingResult<?>> pendingUIUpdates = new ArrayList<>();
private volatile boolean isCancelled = false;
```

### 3. Read the target, then submit work

The method that starts the work runs on the UI thread (a click handler, a constructor, `onDidCreate`). That is the only place `Environment.getCurrent()` returns this user's `Environment`. Read it into a local before the work starts and let the worker capture it. The runAsync wrapper from the documented case study (and the matching sample):

```java
Environment env = Environment.getCurrent();

currentTask = CompletableFuture.runAsync(() -> {
  // ... cancellation check, blocking call with InterruptedException, runLater(env, ...) UI updates ...
}, executor);
```

The body is shown verbatim in steps 4 and 5.

The one mistake to avoid is reading the target inside the worker. On a background thread `Environment.getCurrent()` returns `null`, and `runLater(null, ...)` throws `NullPointerException`:

```java
// Wrong: getCurrent() is read on the worker thread
executor.submit(() -> {
  String data = fetchRemoteData();
  Environment.runLater(Environment.getCurrent(), () -> dataLabel.setText(data));
});

// Right: getCurrent() is read on the UI thread, the worker captures it
Environment env = Environment.getCurrent();
executor.submit(() -> {
  String data = fetchRemoteData();
  Environment.runLater(env, () -> dataLabel.setText(data));
});
```

With the target in hand the call works from any thread: the view's executor, a Spring `@Async` thread, `ForkJoinPool.commonPool()`, an HTTP client's callback thread.

### 4. Wrap every UI mutation

Inside the worker, never touch components directly. Every UI op goes through `Environment.runLater(env, ...)`. Capture the returned `PendingResult` and append to `pendingUIUpdates`:

```java
final int progress = i;
PendingResult<Void> updateResult = Environment.runLater(env, () -> {
  progressBar.setValue(progress);
  statusField.setValue("Processing... " + progress + "%");
});
pendingUIUpdates.add(updateResult);
```

When `runLater(env, ...)` is called on the target's own UI thread (an event handler), it executes synchronously and immediately. When called from a background thread, it queues for asynchronous execution. Multiple background submissions execute in strict FIFO order.

### 5. Cooperative cancellation

Each iteration checks the flag and emits a final UI update that flips the buttons back, then returns:

```java
if (isCancelled) {
  PendingResult<Void> cancelUpdate = Environment.runLater(env, () -> {
    statusField.setValue("Task cancelled!");
    progressBar.setValue(0);
    resultField.setValue("");
    startButton.setEnabled(true);
    cancelButton.setEnabled(false);
    showToast("Task was cancelled", Theme.GRAY);
  });
  pendingUIUpdates.add(cancelUpdate);
  return;
}
```

Any blocking call handles `InterruptedException` by restoring the interrupt flag and returning:

```java
try {
  Thread.sleep(100); // 10 seconds total
} catch (InterruptedException e) {
  // Thread was interrupted - exit immediately
  Thread.currentThread().interrupt(); // Restore interrupted status
  return;
}
```

### 6. Cancel and shut down on destroy

`onDestroy` cancels the task, the queued UI updates, and the executor:

```java
@Override
protected void onDestroy() {
  super.onDestroy();

  // Cancel any running task and pending UI updates
  cancelTask();

  // Clear task reference
  currentTask = null;

  // Shutdown the instance executor gracefully
  executor.shutdown();
}
```

The cancel helper:

```java
private void cancelTask() {
  if (currentTask != null && !currentTask.isDone()) {
    // Set the cancelled flag
    isCancelled = true;

    // Cancel the main task (interrupts the thread)
    currentTask.cancel(true);

    // Cancel all pending UI updates
    for (PendingResult<?> pending : pendingUIUpdates) {
      if (!pending.isDone()) {
        pending.cancel();
      }
    }

    if (!statusField.isDestroyed() && !cancelButton.isDestroyed()) {
      statusField.setValue("Cancelling task...");
      cancelButton.setEnabled(false);

      showToast("Cancellation requested", Theme.GRAY);
    }
  }
}
```

### 7. Guard component access on the cancellation path

A late-firing cancel handler can still try to write to components after the view tears down. Guard the access with `isDestroyed()`:

```java
private void showToast(String message, Theme theme) {
  if (!globalToast.isDestroyed()) {
    globalToast.setText(message);
    globalToast.setTheme(theme);
    globalToast.open();
  }
}
```

Same rule for any helper that can be reached from a teardown path: `if (!component.isDestroyed()) { ... }` around the body.

## Spring `@Async` services

Spring Boot apps often move long work into a `@Service` annotated `@Async`. The service runs on Spring's thread pool, which is shared by every user of the app. The view reads `Environment.getCurrent()` in the handler that starts the task, and every UI update inside the service callbacks wraps in `Environment.runLater(env, ...)`.

See [references/spring-async.md](references/spring-async.md) for the full pattern: `@EnableAsync` activation, `@Async` service signature returning `CompletableFuture<T>`, view constructor injection, completion handling via `whenComplete`, and lifecycle cancellation with `cancel(true)`.

## Verify

### Agent checks

Run these. They catch the regressions this skill exists to prevent:

```bash
mvn clean compile
```

Then grep the modified files for banned mechanisms. Any match is broken and must be rewritten onto the documented primitives:

```bash
grep -nE "java\.util\.Timer|javax\.swing\.Timer|new Thread\(|new TimerTask\(" src
```

```bash
grep -nE "runLater\(\s*\(|runLater\(\s*[A-Za-z_][A-Za-z0-9_]*\s*\)|runLater\(\s*[A-Za-z_][A-Za-z0-9_.]*::" src
```

Every hit of the second grep is a `runLater` call without an `Environment` argument, the deprecated form. Rewrite it to `runLater(env, ...)` with `env` read on the UI thread.

```bash
grep -nE "thenAccept|thenApply|thenRun|whenComplete|subscribe\(" src
```

For every hit, read the body. If it touches UI components without wrapping the access in `Environment.runLater(env, ...)`, it is broken. If it calls `Environment.getCurrent()` inside the callback, it is broken too, because that returns `null` there.

For every `Interval`, `Debouncer`, `PendingResult`, `CompletableFuture`, and per-view `ExecutorService` declared on a view, confirm by reading the source that `onDestroy` (or `onDidDestroy` for `Composite`) calls the matching teardown (`stop()`, `cancel()`, `pendingResult.cancel()`, `cancel(true)`, `executor.shutdown()`).

### Manual checks (ask the user to run these, do NOT claim them)

- Run the app, exercise the feature, confirm the UI updates without freezing and no `IllegalStateException` appears in the console.
- Trigger the cancellation path (navigate away mid-task, press cancel). Confirm no exceptions fire after the view is gone.
- Confirm the worker thread (if any) exits when the task completes or is cancelled.

## Quick reference

| Need | API |
|---|---|
| Periodic events at a fixed delay | `new Interval(seconds, listener)`, then `interval.start()` |
| Stop / restart / change delay | `interval.stop()`, `interval.restart()`, `interval.setDelay(s)` then `restart()` |
| Add another listener | `interval.addElapsedListener(listener)` |
| Debounce a server-side action | `new Debouncer(seconds)`, then `debouncer.run(action)` |
| Cancel / force / inspect a debounce | `debouncer.cancel()`, `debouncer.flush()`, `debouncer.isPending()` |
| Client-side debounce on a DOM event | `ElementEventOptions.setDebounce(ms)` |
| Read the target before background work starts | `Environment env = Environment.getCurrent();` on the UI thread |
| Update UI from a worker thread | `Environment.runLater(env, () -> { /* UI work */ })` |
| Get a value back from a UI-thread task | `Environment.runLater(env, supplier)` -> `PendingResult<T>` |
| Cancel a queued UI update | `pendingResult.cancel()` |
| Spring async setup | see [references/spring-async.md](references/spring-async.md) |
| Cancel a Spring async task | `currentTask.cancel(true)` from `onDestroy` |
| Guard a component access on a teardown path | wrap the call in `if (!component.isDestroyed()) { ... }` |
| Shut down a per-view executor | `executor.shutdown()` inside `onDestroy` after cancelling the task |
| Banned: do not use any of these from a webforJ component | `Environment.runLater(task)` without an `Environment` argument, `Environment.getCurrent()` on a background thread, `java.util.Timer`, `javax.swing.Timer`, a `new Thread(...)` per user |
