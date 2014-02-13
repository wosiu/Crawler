if [ -z "$1" ]
then
	echo "No arguments given. Give start URL (e.g. URL of main page of job: http://hadoop-master.vls.icm.edu.pl:50030/jobdetails.jsp?jobid=...)"
	exit
fi

if [ ! -f target/crawler-*-SNAPSHOT-jar-with-dependencies.jar ]
then
	mvn clean install -P full
fi

java -jar target/crawler-*-SNAPSHOT-jar-with-dependencies.jar $1 $2
