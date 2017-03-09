### Assignment

# Features
Uses an in memory database for portability and easy testing.
Docker image available in case durability is the priority.

# Usage
* Run: `make run`
* navigate to localhost:8080
* username & password = root

### If you dont have make installed
Run: `mvn install && java -jar target/assignment-1.0.0.jar`

# You may edit the business application config
`vim src/main/resources/app.config.properties`
##### Defaults
- Max vido duration = 10 (minutes)
- Video upload directory = uploads
- Required format =mp4
- Username = root
- Password = root

# You may edit the spring application config
`vim src/main/resources/application.properties`

# Docker
If you have docker installed and want to persist the data:
- Provide your db settings: `vim src/main/resources/application.properties`

# Assumptions
Assuming the system has maven 3 and java 8 installed
Assuming the system has ffprobe installed at /usr/local/bin/ffprobe
Assuming the file upload will not exceed 10GB
Assuming that chef templates will remove the hardcoded and sensitive values in the config at deployment

