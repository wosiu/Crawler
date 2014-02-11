Universal Crawler
=======

Multi-threaded Universal Crawler in JAVA for web or local file system crawlering. The module enables to extend the functionality of any location-related searches in really easy way - by overriding the appropriate default functions.
See Demo1, Demo2 or DisambiguationStatBuilder in icm branch as example.


Hadoop Statimporter
=======

Module of Universal Crawler (icm branch) for collecting logs from stdout logs of Apache Hadoop jobs' tasks.
Usage:

Build jar file (below statimporter.jar) for DisambiguationStatBuilder.java, and run:

java -jar statimporter.jar 'link from which you want to start crawlering, eg. main page of specific job' local_path_for_new_merged_logs

eg:
java -jar statimporter.jar 'http://hadoop-master.vls.icm.edu.pl:50030/jobdetails.jsp?jobid=job_201308201233_2253&refresh=30' stats/statistics.log
