# R2dbc and jdbc JMeter samplers

This class library contains two classes which extend from JavaSamplerClient: JdbcSampler and R2dbcSampler.
Maven is required to build the class library.

## Running the .jmx files

Insert test plan contents:
* r2dbc insert test with interleaved queries
* r2dbc insert test with sequential queries
* jdbc insert test with sequential queries

Select test plan contents:
* r2dbc selects for 1, 1000 and 3000 values
* jdbc selects for 1, 1000, 3000 and maximum values

R2dbc select of all values will spike your CPU to 100% for ages, you have been warned.
Open the file in the JMeter ui to change the exact config of every sampler.

Assuming that JMeter bin directory is on the path, use this command to run the tests in non-gui mode.
This command will run the testplan against a local database and append the results to results.csv.
This file does not have to exist. Log output is in a file jmeter.log, usefull when the tests fail.

```
 jmeter -n -t jdbc_r2dbc_sampler.jmx -l results.csv -Jloops=10 -Jinserts=
10 -Juser=postgres -Jpassword=postgres -Jhost=localhost -Jport=5432 -Jdatabase=postgres
```

Loops configures how many times every individual test runs, default 1.
Inserts configures how many insert query to perform, default 10.
All database defaults are the same as the values in the command.
Defaults for loops and inserts are small as to not accidentally overload a server.

See [non-gui mode](https://jmeter.apache.org/usermanual/get-started.html#non_gui) and  [variables](https://jmeter.apache.org/usermanual/test_plan.html#using_variables) 
for an explanation on the paramters.

## Build and include in JMeter

Steps to use these samplers as Java Request samplers in JMeter:

1. Run `mvn clean package` in the root of the project. This generates a 'fat jar' with all dependencies 
(primarily a lot of JMeter code).
2. copy `\target\r2dbc-jmeter-sampler.jar` to `$JMETER_HOME\lib\ext` where $JMETER_HOME is the root directory if JMeter.
All JAR files of dependencies must also be added to this directory if you change the build to not create a fat jar.
3. Restart JMeter if it's running (external samplers are registered on startup).
4. Create a new sampler within a thread group of type `Java Request` and select your class from the dropdown.

## JMeter sampler configuration (JMeter GUI only)

When you create on of the samplers, you are greeted with a dozen or so configuration options. 
Here's what they are and when they are applicable.

### Postgres connection configuration.

| Parameter  | Default  |
|---|---|
| username  | postgres  |
| password | no value |  
| host | localhost  | 
| port | 5432  | 
| database | postgres | 

### Sampler configuration

| Parameter | Values | Note |
|---|---|---|
| driverType | pooled, unpooled | R2dbc uses r2dbc-pool, JDBC uses HikariCP |
| queryType | select, insert, insert, interleaved | insert interleaved is only recognized by r2dbc |
| selectCount | number | defaults to Integer.MAX_VALUE, limits select results | 
| insertCount | number | only on insert queryTypes | 
| retryCount | number | only used by R2DBC, default 3 |
| retryDelay | number | only used by R2DBC, default 100 ms|

R2dbc's insert is equivalent to using JDBC, meaning that queries run sequential, without 
interleaving because of the non-blocking IO, which defeats the point of using R2DBC.
R2dbc's insert interleaved uses the flatMap operator to take advantage of the non-blocking driver.

R2dbc uses `retryCount` and `retryDelay` to provide exponenential backoff when there are 
too many open connections already. The r2dbc repository will go to a delay of maximum 
1 second when retrying, so setting retryDelay to more than 1 second does not make sense.
