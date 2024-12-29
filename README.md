This is reproducer for issue when using verx-web-openapi and metrics\
I noticed that the issue happens when using Operaito#failureHandler to set error handler, but it's not happening when using Router#errorHandler.
I worte test that makes a call which will fail on validation, and subsequent to metrics show the reportrd path is doubled path>path.
![image](https://github.com/user-attachments/assets/46e1f51a-00f4-4436-a12e-b8a4617ab03d)
