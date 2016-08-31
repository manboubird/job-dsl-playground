# Jenkins Job DSL Playground

http://job-dsl.herokuapp.com/

An app for debugging Groovy scripts using the [Jenkins Job DSL](https://github.com/jenkinsci/job-dsl-plugin). Allows the user to create jobs with the DSL and view the generated XML.

Powered by [Ratpack](https://github.com/ratpack/ratpack).

## Requirements 

- Java 8
- Python 3

## Usage

```
# module installation 
pip install -r scripts/python/requirements.txt 

# running in local 
./gradlew run --continuous

# update shell command param
DSL_PLAYGROUND_APP__SHELL_COMMAND="ls" ./gradlew run --continuous

# confirm config
curl http://127.0.0.1:5050/configprops

{
    "shellCommand": "ls",
    "inputDataBaseDir": "/tmp/"
}

# create standalone jar and run with shell command of DSL convertion python script
./gradlew clean shadowJar
DSL_PLAYGROUND_APP__SHELL_COMMAND="python3 scripts/python/convert.py" java -jar build/libs/job-dsl-playground-all.jar

```
