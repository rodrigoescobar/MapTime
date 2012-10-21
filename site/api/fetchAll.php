<?php
mysql_connect('localhost', 'root', 'maptime') or die('Could not connect: ' .mysql_error());
mysql_select_db('db_maptime') or die('Could not select database' . mysql_error());

$qry = mysql_query('SELECT * FROM timepoint');

echo "<?xml version='1.0'?>\n";

while($row = mysql_fetch_array($qry)) {
	echo "<timepoint id='" . $row['TimePoint_ID'] . "'>\n";
	echo "	<name>" . $row['TimePoint_Name'] . "</name>\n";
	echo "	<description>" . $row['TimePoint_Description'] . "</description>\n";
	echo "	<sourceName>" . $row['TimePoint_SourceName'] . "</sourceName>\n";
	echo "	<sourceURL>" . $row['TimePoint_SourceURL'] . "</sourceURL>\n";
	echo "	<year>" . $row['TimePoint_Year'] . "</year>\n";
	echo "	<yearUnitID>" . $row['TimePoint_YearUnitID'] . "</yearUnitID>\n";
	echo "	<month>" . $row['TimePoint_Month'] . "</month>\n";
	echo "	<day>" . $row['TimePoint_Day'] . "</day>\n";
	echo "	<description>" . $row['TimePoint_Description'] . "</description>\n";
	echo "	<yearInBC>" . $row['TimePoint_YearInBC'] . "</yearInBC>\n";
	echo "</timepoint>\n";
}
?>;