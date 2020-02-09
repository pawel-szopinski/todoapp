Simple to-do web app (tasks & attachments) written in Java, using NanoHTTPD, PostgreSQL and Jackson.

App features:
1. Post task
2. Get single task
3. Get all tasks
4. Delete task
5. Set task completed flag to true (mark as done)
6. Attach (post) files to task
7. Get attachment from task

In order to run the app:
1. Create database.
1. Run SQL script located in the root dir in order to import db structure.
2. Rename "dbconfig_git_template.properties" to "dbconfig.properties" and update the details.