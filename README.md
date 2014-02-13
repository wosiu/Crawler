Universal Crawler
=======

Multi-threaded Universal Crawler in JAVA for web or local file system crawlering. The module enables to extend the functionality of any location-related searches in really easy way - by overriding the appropriate default functions.
See Demo1, Demo2 or DisambiguationStatBuilder in icm branch as example.


Hadoop Statimporter
=======

Module of Universal Crawler (icm branch) for collecting logs from stdout logs of Apache Hadoop jobs' tasks.
Usage:

1. Make a tunnel with hadoop at port 23456, eg:
ssh -D 23456 your_login@hadoop.vls.icm.edu.pl
2. Run run.sh as follows:

./run.sh 'link from which you want to start crawlering, eg. main page of specific job' [local_path_for_new_merged_logs]

eg:
./run.sh 'http://hadoop-master.vls.icm.edu.pl:50030/jobtasks.jsp?jobid=job_201401301427_2519&type=map&pagenum=1&state=completed' statistics.log

Path for logs is optional - there is default one configured.
Note that you need Maven Plugin installed.
