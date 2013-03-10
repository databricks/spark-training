---
layout: global
title: Logging into the Cluster
prev: launching-a-spark-shark-cluster-on-ec2.html
next: overview-of-the-exercises.html
skip-chapter-toc: true
---
<ul class="nav nav-tabs" data-tabs="tabs">
  <li class="active"><a data-toggle="tab" href="#login_linux">Linux, Cygwin, or OS X</a></li>
  <li><a data-toggle="tab" href="#login_windows">Windows</a></li>
</ul>

<div class="tab-content">
<div class="tab-pane active" id="login_linux" markdown="1">
Log into your cluster via

    ssh -i <key_file> root@<master_node_hostname>

where `key_file` here is the private keyfile that was given to you by the tutorial instructors (or your AWS EC2 keyfile if you spun up your own cluster).

__Question: I got the following permission error when I ran the above command. Help!__

<pre class="nocode">
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@             WARNING: UNPROTECTED PRIVATE KEY FILE!              @
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
Permissions 0644 for '../ampcamp.pem' are too open.
It is recommended that your private key files are NOT accessible by others.
This private key will be ignored.
bad permissions: ignore key: ../ampcamp.pem
Permission denied (publickey).
</pre>

__Answer:__ Run this command, then try to log in again:

    chmod 600 ../ampcamp.pem
</div>
<div class="tab-pane" id="login_windows" markdown="1">
You can use [PuTTY](http://www.putty.org/) to log into the cluster from Windows.

1. Download PuTTY from [here](http://the.earth.li/~sgtatham/putty/latest/x86/putty.exe).

2. Start PuTTY and enter the hostname that was mailed to you, as shown in the screenshot below.

   ![Enter username in PuTTY](img/putty-host.png)

3. Click on Connection > Data in the Category area and enter `root` as the username

   ![Enter login in PuTTY](img/putty-login.png)

4. Click on Connection > SSH > Auth in the Category area and enter the path to the private key file (`ampcamp-all.ppk`) that was sent to you by mail.

   ![Enter login in PuTTY](img/putty-private-key.png)

5. Click on Open
</div>
</div>
