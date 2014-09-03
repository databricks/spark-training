#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Simple python script for generating the spark USB.
#   Always run this script from the spark-training root directory.
# % ./build_usb.py origin/branch-1.1
#
# Running this script will create a usb directory, with the following contents:
#   - usb/usb.zip
#   - usb/[target or spark-training] - A folder with all the contents of usb.zip.
#     The folder is originally called target, and is then renamed to
#     spark-training before zipping.
import os
import sys

from optparse import OptionParser

def parse_args():
    parser = OptionParser(usage="build_usb [options] <spark-branch>"
                                + "\n\n<spark-branch> can be: origin/master, "
                                + "origin/branch-1.1, "
                                + " 70109da212343601d428252e9d298f6affa457f3",
                          add_help_option=False)
    (opts, args) = parser.parse_args()
    if len(args) != 1:
        parser.print_help()
        sys.exit(1)
    spark_branch = args[0]

    return (opts, spark_branch)

def clean(opts):
    print "Deleting usb directory"
    os.system("rm -rf usb")

def clone_spark(opts, spark_branch):
    print "Cloning Spark"
    os.system("mkdir -p usb/target")
    os.system("cd usb/target; git clone https://github.com/apache/spark.git")
    os.system("cd usb/target/spark; git reset --hard %s" % spark_branch)

def compile_and_configure_spark(opts):
    print "Building Spark"
    os.system("cp -r sbt usb/target")
    os.system("cd usb/target/spark; SPARK_HIVE=true ../sbt/sbt assembly/assembly")
    # Configures log4j to WARN.
    os.system("sed 's/log4j.rootCategory=INFO/log4j.rootCategory=WARN/1' usb/target/spark/conf/log4j.properties.template >  usb/target/spark/conf/log4j.properties")

def copy_and_build_projects(opts):
    print "Copying the data and examples over and build them."
    # Copy the data directory.
    os.system("cp -r data usb/target")
    # Copy the website.
    os.system("cp -r website usb/target")
    # Copy and build the simple app.
    os.system("cp -r simple-app usb/target")
    os.system("cd usb/target/simple-app; ../sbt/sbt package")
    # Copy and build streaming.
    os.system("cp -r streaming usb/target")
    os.system("cd usb/target/streaming/scala; ../../sbt/sbt assembly")
    # Copy and build machine learning.
    os.system("cp -r machine-learning usb/target")
    os.system("cd usb/target/machine-learning/scala; ../../sbt/sbt assembly")

def copy_readme(opts):
    git_hash = os.popen("cd usb/target/spark; git rev-parse HEAD").read().strip()
    print "Printing readme with hash %s" % git_hash
    os.system("sed \"s/GIT_HASH/%s/1\" usb.md > usb/target/README.md" % git_hash)

def zip(opts):
    print "Zipping it all up"
    os.system("mv usb/target usb/spark-training")
    os.system("cd usb; find spark-training | grep -v \"target\" > include; find spark-training -name \"spark-assembly*.jar\" >> include")
    os.system("cd usb; cat include | zip usb.zip -@ ")

def real_main():
    (opts, spark_branch) = parse_args()

    if not "JavaVirtualMachines/1.6" in os.popen("echo $JAVA_HOME").read():
        sys.exit("JAVA HOME must be set to 1.6")

    # Delete the old directories.
    clean(opts)

    # Clone a version of Spark.
    clone_spark(opts, spark_branch)

    # Run sbt to compile Spark.
    compile_and_configure_spark(opts)

    # Copy all the files over into the target directory
    copy_and_build_projects(opts)

    # Copy the readme over.
    copy_readme(opts)

    # Zip it all up
    zip(opts)

    print "Done creating the zip file.  To test run the following steps:"
    print "% cp usb/usb.zip /tmp"
    print "% cd /tmp"
    print "% unzip usb.zip"
    print "% cd usb/simple-app"
    print "Cut off your wifi connection, and test if you can still build."
    print ("../sbt/sbt package ")
    print "Then turn wifi back on to run."
    print ("../spark/bin/spark-submit --class \"SimpleApp\" "
           + "--master local[*] target/scala-2.10/simple-project_2.10-1.0.jar")
    print "No annoying logging lines should be output."


def main():
    try:
        real_main()
    except Exception, e:
        print >> sys.stderr, "\nError:\n", e
        sys.exit(1)

if __name__ == "__main__":
    main()
