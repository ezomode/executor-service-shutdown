#### Shutting down an ExecutorService after uncaught exception thrown from supplied Runnable

##### doStuffAndScheduleShutdown()
Notice the ScheduledExecutorService has stopped as soon as condition `counter >= 4` was satisfied and an exception thrown. 
The proper shutdown is issued at ~9000 millis.
```
main() from thread=main
End of doStuffAndScheduleShutdown() at millis: 52

do stuff in thread: named-thread-0
do stuff in thread: named-thread-0
do stuff in thread: named-thread-0
do stuff in thread: named-thread-0
Shutting down from thread: named-thread-0 at millis: 9053
```

##### shutdownThroughFuture()
Notice future.get() is blocking -> `End of shutdownThroughFuture()` appears last. If the Runnable would proceed without 
an issue, we would block the outer thread indefinitely.
```
main() from thread=main
do stuff in thread=named-thread-0
do stuff in thread=named-thread-0
do stuff in thread=named-thread-0
do stuff in thread=named-thread-0
Caught exception: java.lang.NullPointerException
Shutting down due to EXCEPTION thread(Thread-0) description: shutting down singleThreadScheduledExecutor at millis: 3156
End of shutdownThroughFuture()
```

##### shutdownThroughFutureAsync()
Here we run a recurring checking (`future.get()`) logic inside of a separate Thread managed by the ScheduledExecutorService.
```
main() from thread=main
End of shutdownThroughFutureAsync()

do stuff in thread=named-thread-0
Before future.get() in shutdownThroughFutureAsync()
do stuff in thread=named-thread-0
do stuff in thread=named-thread-0
do stuff in thread=named-thread-0
Caught exception: java.lang.NullPointerException
Shutting down due to EXCEPTION thread(named-thread-1) description: shutting down singleThreadScheduledExecutor at millis: 3159
Shutting down due to EXCEPTION thread(named-thread-1) description: shutting down scheduledThreadPool at millis: 3159
```

##### shutdownThroughRunnable()
This version passes an instance of ExecutorService to the Runnable object. Runnable handles the shutdown.
```
main() from thread=main
End of shutdownThroughRunnable()

do stuff in thread=named-thread-0
do stuff in thread=named-thread-0
do stuff in thread=named-thread-0
do stuff in thread=named-thread-0
Shutting down due to EXCEPTION thread(named-thread-0) description: counter>=4 at millis: 3159
```