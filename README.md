# sso

## Run:
` ./gradlew bootrun --args='--spring.profiles.active=dev --jasypt.encryptor.password=${JASYPT_SECRET_KEY}' `

###  Another way is by adding environment variables:
#### `SPRING_PROFILE = local or dev `
#### `JASYPT_SECRET_KEY = key`

` ./gradlew bootrun `

## Setup local docker mongo
`docker run -d --name mongo-local -p 27017:27017 --restart unless-stopped -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=toor -e MONGO_INITDB_DATABASE=sso mongo:4.4`

## CodeStyle
https://github.com/google/styleguide