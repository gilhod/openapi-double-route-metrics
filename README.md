This is reproducer for issue when using verx-web-openapi and metrics\
I noticed that the issue happens when using Operaito#failureHandler to set error handler, but it's not happening when using Router#errorHandler.
I worte test that makes a call which will fail on validation, and subsequent to metrics show the reportrd path is doubled path>path.
