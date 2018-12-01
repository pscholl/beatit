 Skeleton project for the 'beatit' project, i.e. inertial smoking detection from
the wrist via Smartwatches.

 The segfeat module is a basic implementation for segmenting a time-series of
inertial sensor data, and extracting a simple statistics feature vector. It can
be used both directly from java, and via the command-line to read CSV files.
The CSV fields need to be separated by SPACES, and the first field can be an
optional String label to designate whether this sample was recorded while
smoking. If not a 'NULL' label can be added, i.e. this line:

 smoking 9.14 0 0.0
 NULL 9.14 0 1

designates a sample that recorded three sensor values (9.14, 0, 0) while
smoking. While the second line designates that the sensor samples where
not recorded while smoking.

 To segment and extract features from a CSV file via the command line, the
following command can be used:

 java -jar segfeat/build/libs/segfeat.jar file.csv

use 'java -jar segfeat/build/libs/segfeat.jar -h' to get infos about further
usage possibilities.
