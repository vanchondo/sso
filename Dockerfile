FROM openjdk:17

ARG secret_key
ENV JASYPT_SECRET_KEY=$secret_key
COPY build/libs/*.jar .
RUN rm *-plain.jar
RUN mv *.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-Djasypt.encryptor.password=${JASYPT_SECRET_KEY}","-jar","/app.jar"]