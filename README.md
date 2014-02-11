Hadoop Statimporter
=======

Module of Universal Crawler (look at master branch) for collecting logs from stdout logs of Apache Hadoop jobs' tasks.
Usage:

Build jar file (below statimporter.jar), and run:

java -jar statimporter.jar 'link from which you want to start crawlering, eg. main page of specific job' local_path_for_new_merged_logs

eg:
java -jar statimporter.jar 'http://hadoop-master.vls.icm.edu.pl:50030/jobdetails.jsp?jobid=job_201308201233_2253&refresh=30' stats/statistics.log
