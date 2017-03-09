
.DEFAULT: help
all: help

# TARGET:build              compile program
build:
	mvn clean compile

# TARGET:run          		Run app locally
run:
	mvn clean install && java -jar target/assignment-1.0.0.jar

# TARGET:help               Help
help:
	# Usage:
	#   make <target> [OPTION=value]
	#
	# Targets:
	@egrep "^# TARGET:" [Mm]akefile | sed 's/^# TARGET:/#   /'
