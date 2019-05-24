# R2dbc and jdbc JMeter samplers

This class library contains two classes which extend from JavaSamplerClient: JdbcSampler and R2dbcSampler.
Maven is required to build the class library. 

## Usage

Steps to use these samplers as Java Request samplers in JMeter:

1. Run `mvn clean package` in the root of the project. This generates a 'fat jar' with all dependencies 
(primarily a lot of JMeter code).
2. copy `\target\r2dbc-jmeter-sampler.jar` to `$JMETER_HOME\lib\ext` where $JMETER_HOME is the root directory if JMeter.
All JAR files of dependencies must also be added to this directory if you change the build to not create a fat jar.
3. Restart JMeter if it's running (external samplers are registered on startup).
4. Create a new sampler within a thread group of type `Java Request` and select your class from the dropdown.

## Configuration

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
| insertCount | number | only on insert queryTypes | 
| retryCount | number | only used by R2DBC, default 3 |
| retryDelay | number | only used by R2DBC, default 100 ms|

R2dbc uses `retryCount` and `retryDelay` to provide exponenential backoff when there are 
too many open connections already. The r2dbc repository will go to a delay of maximum 
1 second when retrying, so setting retryDelay to more than 1 second does not make sense.