---
layout: global
prev: index.html
next: logging-into-the-cluster.html
title: Launching a Spark/Shark Cluster on EC2
---

This section will walk you through the process of launching a small cluster using your own Amazon EC2 account and our scripts and AMI (New to AMIs? See this [intro to AMIs](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html)).

<p class="alert alert-warn" style="overflow:hidden;" markdown="1">
<i class="icon-info-sign">    </i>
If you are attending the training in person, we have already launched a cluster
for you and emailed you its hostname and SSH keys, so you can proceed directly
to [Logging Into Your Cluster](logging-into-the-cluster.html).
</p>

<p class="alert alert-error" style="overflow:hidden;" markdown="1">
<i class="icon-info-sign">    </i>
<strong>If you launch your own cluster using these instructions, don't forget to turn off your cluster when you are finished with the exercises.</strong> If you leave them on, it could cost you <strong>hundreds or thousands of dollars in Amazon EC2 fees!</strong> Also, use the AWS web console to double check that the machines are shut down when finished.
</p>


## Pre-requisites

The cluster setup script we'll use below requires Python 2.x and has been tested to work on Linux or OS X.
We will use the [Bash shell](http://www.gnu.org/software/bash/manual/bashref.html) in our examples below.
If you are using Windows, consider installing [Cygwin](http://www.cygwin.com/) (note that we have not tested this, hence providing debug support would be hard).

## Setting up EC2 keys

Make sure you have an Amazon EC2 account.
Set the environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` to your Amazon EC2 access key ID and secret access key.
These can be obtained from the [AWS homepage](http://aws.amazon.com/) by clicking `Account > Security Credentials > Access Credentials`:

![Downloading AWS access keys](img/aws-accesskey.png)

    export AWS_ACCESS_KEY_ID=<ACCESS_KEY_ID>
    export AWS_SECRET_ACCESS_KEY=<SECRET_ACCESS_KEY>

Create an Amazon EC2 key pair for yourself.
This can be done by logging into your Amazon Web Services account through the [AWS console](http://aws.amazon.com/console/), selecting `EC2` from the `Services` menu,  selecting `Key Pairs` on the left sidebar, and creating and downloading a key:

![Downloading an EC2 Keypair](img/aws-keypair.png)



Make sure that you set the permissions for the private key file to `600` (i.e. only you can read and write it) so that `ssh` will work (commands to do this are provided farther below).

<div class="alert alert-info">
<i class="icon-info-sign"> 	</i>
The AMI we are using for this exercise is only available in the `us-east` region.
So make sure you create a key-pair in that region!
</div>

## Getting the scripts to launch EC2 cluster

Check out the launch scripts by cloning the github repository.

    git clone git://github.com/amplab/training-scripts.git -b ampcamp4

## Launching the cluster
Launch the cluster by running the following command.
This script will launch a cluster, create a HDFS cluster and configure Mesos, Spark, and Shark.
Finally, it will copy the datasets used in the exercises from EBS to the HDFS cluster.
_This can take around 20-30 mins._

    cd training-scripts
    ./spark-ec2 -i <key_file> -k <name_of_key_pair> --copy launch amplab-training

Where `<name_of_key_pair>` is the name of your EC2 key pair (that you gave it when you created it), `<key_file>` is the private key file for your key pair.

For example, if you created a key pair named `ampcamp-key` and the private key (`<key_file>`) is in your home directory and is called `ampcamp.pem`, then the command would be

    ./spark-ec2 -i ~/ampcamp.pem -k ampcamp-key --copy launch amplab-training

This command may take a 30-40 minutes or longer and should produce a bunch of output as it first spins up the nodes for your cluster, sets up <a href="http://amplab.cs.berkeley.edu/bdas">BDAS</a> on them, and performs a large distributed file copy of the wikipedia files we'll use in these training documents from EBS to your instance of HDFS.

The following are some errors that you may encounter, and other frequently asked questions:


<div class="accordion" id="q-accordion">
<div class="accordion-group">
<div class="accordion-heading">
  <a class="accordion-toggle" data-toggle="collapse" href="#collapse-q1" data-parent="#q-accordion">
        I get the following error when running this command: <code>UNPROTECTED KEY FILE...</code>
  </a>
</div><!--accordion-heading-->

<div id="collapse-q1" class="accordion-body collapse">
<div class="accordion-inner" markdown="1">

__Question: I got the following permission error when I ran the above command. Help!__

<pre class="nocode">
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@ WARNING: UNPROTECTED PRIVATE KEY FILE! @
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
Permissions 0644 for ‘../ampcamp.pem' are too open.
It is recommended that your private key files are NOT accessible by others.
This private key will be ignored.
bad permissions: ignore key: ../ampcamp.pem
Permission denied (publickey).
</pre>

__Answer:__ Run the next two commands.

    chmod 600 ../ampcamp.pem
    ./spark-ec2 -i <key_file> -k <name_of_key_pair> --copy --resume launch amplab-training

</div><!--accordion-inner-->
</div><!--accordion-body-->
</div><!--accordion-group-->

<div class="accordion-group">
<div class="accordion-heading">
  <a class="accordion-toggle" data-toggle="collapse" href="#collapse-q2" data-parent="#q-accordion">
    I get the following error when running this command: <code>Could not read http://s3.amazonaws.com/ampcamp-amis/latest-ampcamp3</code>
  </a>
</div><!--accordion-heading-->

<div id="collapse-q2" class="accordion-body collapse">
<div class="accordion-inner" markdown="1">

__Question: I got the following error when I ran the above command. Help!__

<pre class="nocode">
Searching for existing cluster amplab-training...
Could not read http://s3.amazonaws.com/ampcamp-amis/latest-ampcamp3
</pre>

__Answer:__ The lookup for the AMP Camp AMI failed. You can manually specificy the AMI to use by adding the '-a' flag to the script.
For example to use the AMP Camp 3 AMI, you can try the following command

    ./spark-ec2 -i <key_file> -k <name_of_key_pair> -a ami-452f622c --copy launch amplab-training

</div><!--accordion-inner-->
</div><!--accordion-body-->
</div><!--accordion-group-->

<div class="accordion-group">
<div class="accordion-heading">
  <a class="accordion-toggle" data-toggle="collapse" href="#collapse-q3" data-parent="#q-accordion">
    I get the following error when running this command: <code>Your requested instance type (m1.xlarge) is not supported...</code>
  </a>
</div><!--accordion-heading-->

<div id="collapse-q3" class="accordion-body collapse">
<div class="accordion-inner" markdown="1">

__Question: I got the following permission error when I ran the above command. Help!__

<pre class="nocode">
"Your requested instance type (m1.xlarge) is not supported in your requested Availability Zone (us-east-1b).  Please retry your request by not specifying an Availability Zone or choosing us-east-1d, us-east-1c, us-east-1a, us-east-1e."
</pre>

__Answer:__ Add the `-z` flag to your command line arguments to use an availability zone other than `us-east-1b`.
You can set the value of that flag to "none", as in the following example command, which tells the script to pick a random availability zone.
It may randomly pick an availability zone that doesn't support this instance size (such as `us-east-1b`), so you may need to try this command a few times to get it to work.

    ./spark-ec2 -i <key_file> -k <name_of_key_pair> -z none --copy launch amplab-training

</div><!--accordion-inner-->
</div><!--accordion-body-->
</div><!--accordion-group-->

<div class="accordion-group">
<div class="accordion-heading">
  <a class="accordion-toggle" data-toggle="collapse" href="#collapse-q4" data-parent="#q-accordion">
    The commands hangs at: <code>Copying AMP Camp Wikipedia pagecount data...</code>
  </a>
</div><!--accordion-heading-->

<div id="collapse-q4" class="accordion-body collapse">
<div class="accordion-inner" markdown="1">

__Question: The above command is stuck at the following line. Help!__

<pre class="nocode">
Copying AMP Camp Wikipedia pagecount data...
</pre>

__Answer:__ The data copy from EBS to your HDFS cluster is running and can take up to 30-40 minutes. If it has been longer and if you want to retry the data copy, use the following steps:

1. Stop the current process using `Ctrl + C`

2. Login to the master node by running

   ~~~
   ./spark-ec2 -i <key_file> -k <key_pair> login amplab-training
   ~~~

3. Delete the directory the data was supposed to be copied to

   ~~~
   /root/ephemeral-hdfs/bin/hadoop fs -rmr /wiki
   ~~~

4. Logout and run the following command to retry copying data from EBS

   ~~~
   ./spark-ec2 -i <key_file> -k <key_pair> copy-data amplab-training
   ~~~

</div><!--accordion-inner-->
</div><!--accordion-body-->
</div><!--accordion-group-->

<div class="accordion-group">
<div class="accordion-heading">
  <a class="accordion-toggle" data-toggle="collapse" href="#collapse-q5" data-parent="#q-accordion">
        Can I specify the instances types while creating the cluster?
  </a>
</div><!--accordion-heading-->

<div id="collapse-q5" class="accordion-body collapse">
<div class="accordion-inner" markdown="1">

__Question: Can I specify the instances types while creating the cluster?__

__Answer:__ These exercises have been designed to work with at least 5 slave
machines using instances of type __m1.xlarge__.
You can also launch the cluster with different [instance types](http://aws.amazon.com/ec2/instance-types/).
However, you should ensure two things:

1. __Correct number of slaves:__ Make sure that the total memory in the slaves is about 54GB as the exercises are designed accordingly.
   So if you are using `m1.large` instances (which have 7.5 GB memory), then you should launch a cluster with at least 8 slaves.

   You can specify the instance type in the above command by setting the flag `-t <instance_type>` .
   Similarly, you can specify the number of slaves by setting the flag `-s <number of slaves>`.
   For example, to launching a cluster with 8 `m1.large` slaves, use

   ~~~
   ./spark-ec2 -i <key_file> -k <name_of_key_pair> -t m1.large -s 8 --copy launch amplab-training
   ~~~

2. __Correct java heap setting for Spark:__ Make sure to change the `SPARK_MEM` variable in
   `/root/spark/conf/spark-env.sh` and `/root/shark/conf/shark-env.sh` on all of the instances to match the amount of memory available in the instance type you use.
   This is typically set it to the total amount of memory of the instance minus 1 GB for the OS (that is, for `m1.large` with 7.5GB memory, set `SPARK_MEM=6g`).
   There is a easy way to change this configuration on all the instances.
   First, change this file in the master.
   Then run

   ~~~
   /root/spark-ec2/copy-dir /root/spark/conf/ .
   ~~~

   to copy the configuration directory to all slaves.

__Information:__ Sometimes the EC2 instances don't initialize within the standard waiting time of 120 seconds.
If that happens you, will ssh errors (or check in the Amazon web console).
In this case, try increasing the waiting to 4 minutes using the `-w 240` option.

</div><!--accordion-inner-->
</div><!--accordion-body-->
</div><!--accordion-group-->

<div class="accordion-group">
<div class="accordion-heading">
  <a class="accordion-toggle" data-toggle="collapse" href="#collapse-q6" data-parent="#q-accordion">
        Can I use another EC2 region to launch the cluster?
  </a>
</div><!--accordion-heading-->

<div id="collapse-q6" class="accordion-body collapse">
<div class="accordion-inner" markdown="1">

__Question: Can I use a EC2 region other than us-east-1 while creating the cluster?__

__Answer:__ These exercises have been created and tested on the us-east-1 region. However we have also copied the AMI to the us-west-1 region as well.
To use the us-west-1 region, you can run the following command:

    ./spark-ec2 -i <key_file> -k <name_of_key_pair> -r us-west-1 -a ami-6ac4ee2f --copy launch amplab-training

We do not support running the AMI in any other EC2 regions. In case you need to do so, feel free to contact us for more help.

</div><!--accordion-inner-->
</div><!--accordion-body-->
</div><!--accordion-group-->

</div><!--accordion-->

If you launched the cluster with the default script above (no custom instance type and/or number of slaves), your cluster should contain 6 m1.xlarge Amazon EC2 nodes.

![Running EC2 instances in AWS Management Console](img/aws-runninginstances-m1.xlarge.png)

## Post-launch steps
Your cluster should be ready to use.
You can find the master hostname (`<master_node_hostname>` in the instructions below) by running

    ./spark-ec2 -i <key_file> -k <key_pair> get-master amplab-training

At this point, it would be helpful to open a text file and copy `<master_node_hostname>` there.
In a later exercise, you will want to have `<master_node_hostname>` ready at hand without having to scroll through your terminal history.

<h2>Terminating the cluster <span>(Not yet, only after you do the rest of the exercises!)</span></h2>
__After you are done with your exercises (and only then)__, you can terminate the cluster by running

    ./spark-ec2 -i <key_file> -k <key_pair> destroy amplab-training

<p class="alert alert-error" style="overflow:hidden;" markdown="1">
<i class="icon-info-sign">    </i>
<strong>Don't forget to shut down your machines and double check that they successfully terminated (via the AWS web console).</strong> If you leave your machines running, it could cost you a lot of money!
</p>


## Log into your cluster
Move onto the next section for instructions that walk you through logging into your shiney new BDAS cluster.
